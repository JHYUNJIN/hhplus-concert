package kr.hhplus.be.server.reservation.event;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.reservation.port.out.SeatHoldRepository;
import kr.hhplus.be.server.reservation.domain.ReservationCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationCreatedSeatLockListener {

    private final SeatHoldRepository seatHoldRepository;

    /**
     * 예약 생성 이벤트 리스너
     * 예약 트랜잭션이 성공적으로 커밋된 이후에만 실행됩니다.
     * @param event 예약 생성 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReservationCreated(ReservationCreatedEvent event) {
        // 레디스에 좌석 잠금
        try {
            log.info("예약 생성 이벤트 수신: {}", event);
            seatHoldRepository.hold(event.seatId(), event.userId());
        } catch (CustomException e) {
            log.error("좌석 잠금 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.SEAT_LOCK_FAILED, "좌석 잠금에 실패했습니다.");
        }

    }
}