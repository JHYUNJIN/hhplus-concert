package kr.hhplus.be.server.reservation.usecase;

import kr.hhplus.be.server.common.event.EventPublisher;
import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.util.DistributedLockKeyGenerator;
import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.queue.domain.QueueTokenUtil;
import kr.hhplus.be.server.queue.port.out.QueueTokenRepository;
import kr.hhplus.be.server.reservation.domain.ReservationCreatedEvent;
import kr.hhplus.be.server.reservation.port.in.ReservationCreateInput;
import kr.hhplus.be.server.reservation.port.in.ReserveSeatResult;
import kr.hhplus.be.server.reservation.port.in.dto.CreateReservationResult;
import kr.hhplus.be.server.reservation.port.in.dto.ReserveSeatCommand;
import kr.hhplus.be.server.reservation.port.out.SeatHoldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ReservationInteractor implements ReservationCreateInput {

    private final QueueTokenRepository queueTokenRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final EventPublisher eventPublisher;
    private final DistributedLockManager distributedLockManager;
    private final CreateReservationManager createReservationManager;

    @Override
    @Transactional
    public ReserveSeatResult reserveSeat(ReserveSeatCommand command) {
        // 1. 분산 락 획득 전 Redis 에서 좌석 잠금 상태 확인
        QueueToken queueToken = getQueueTokenAndValid(command);
        if(seatHoldRepository.isHoldSeat(command.seatId(),queueToken.userId())){
            throw new CustomException(ErrorCode.ALREADY_RESERVED_SEAT, "이미 예약된 좌석입니다.");
        }

        String lockKey = DistributedLockKeyGenerator.getReserveSeatLockKey(command.seatId());
        System.out.println("🚀[로그:정현진] lockKey : " + lockKey);
        try {
            // 2. 검증을 통과한 요청만 분산 락을 획득하고 핵심 로직을 실행
            CreateReservationResult result = distributedLockManager.executeWithLockHasReturn(
                    lockKey,
                    () -> createReservationManager.processCreateReservation(command, queueToken)
            );

            // 3. 예약생성 이벤트 발행
            eventPublisher.publish(ReservationCreatedEvent.from(result));
            return ReserveSeatResult.from(result);
        } catch (Exception e) {
            log.error("좌석 예약 처리 중 예외 발생. command: {}", command, e);
            if (e instanceof CustomException) {
                throw (CustomException) e;
            }
            throw new RuntimeException("좌석 예약에 실패했습니다.", e);
        }
    }

    private QueueToken getQueueTokenAndValid(ReserveSeatCommand command) throws CustomException {
        QueueToken queueToken = queueTokenRepository.findQueueTokenByTokenId(command.queueTokenId());
        QueueTokenUtil.validateActiveQueueToken(queueToken);
        return queueToken;
    }

}

