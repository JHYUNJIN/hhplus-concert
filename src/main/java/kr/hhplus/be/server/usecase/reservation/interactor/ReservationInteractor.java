package kr.hhplus.be.server.usecase.reservation.interactor;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.event.reservation.ReservationCreatedEvent;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.queue.QueueTokenRepository;
import kr.hhplus.be.server.domain.queue.QueueTokenUtil;
import kr.hhplus.be.server.domain.seat.SeatHoldRepository;
import kr.hhplus.be.server.infrastructure.persistence.lock.DistributedLockManager;
import kr.hhplus.be.server.infrastructure.persistence.reservation.CreateReservationManager;
import kr.hhplus.be.server.usecase.event.EventPublisher;
import kr.hhplus.be.server.usecase.reservation.input.ReservationCreateInput;
import kr.hhplus.be.server.usecase.reservation.input.ReserveSeatCommand;
import kr.hhplus.be.server.usecase.reservation.output.CreateReservationResult;
import kr.hhplus.be.server.usecase.reservation.output.ReservationOutput;
import kr.hhplus.be.server.usecase.reservation.output.ReserveSeatResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ReservationInteractor implements ReservationCreateInput {

    private final static String LOCK_KEY_PREFIX = "reserve:seat:";

    private final QueueTokenRepository queueTokenRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final EventPublisher eventPublisher;
    private final ReservationOutput reservationOutput;
    private final DistributedLockManager distributedLockManager;
    private final CreateReservationManager createReservationManager;

    @Override
    @Transactional
    public void reserveSeat(ReserveSeatCommand command) {
        // 1. 분산 락 획득 전 Redis 에서 좌석 잠금 상태 확인
        QueueToken queueToken = getQueueTokenAndValid(command);
        if(seatHoldRepository.isHoldSeat(command.seatId(),queueToken.userId())){
            throw new CustomException(ErrorCode.ALREADY_RESERVED_SEAT, "이미 예약된 좌석입니다.");
        }

        String lockKey = LOCK_KEY_PREFIX + command.seatId();
        try {
            // 2. 검증을 통과한 요청만 분산 락을 획득하고 핵심 로직을 실행
            CreateReservationResult result = distributedLockManager.executeWithLockHasReturn(
                    lockKey,
                    () -> createReservationManager.processCreateReservation(command, queueToken)
            );

            // 3. 예약생성 이벤트 발행
            eventPublisher.publish(ReservationCreatedEvent.from(result));
            reservationOutput.ok(ReserveSeatResult.from(result));
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

