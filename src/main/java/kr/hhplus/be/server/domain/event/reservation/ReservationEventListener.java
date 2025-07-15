package kr.hhplus.be.server.domain.event.reservation;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.usecase.reservation.ReservationCancellationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationEventListener {

    private final TaskScheduler taskScheduler;
    private final ReservationCancellationUseCase reservationCancellationUseCase;

    /**
     * 예약 생성 이벤트 수신 후, 예약 만료를 처리할 스케줄링 작업을 등록합니다.
     * 예약 트랜잭션이 성공적으로 커밋된 이후에만 실행됩니다.
     * @param event 예약 생성 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReservationCreated(ReservationCreatedEvent event) {
        log.info("ReservationCreatedEvent 수신. 예약 ID: {}, 만료 시간: {}", event.reservationId(), event.expiresAt());

        Instant expirationInstant = event.expiresAt().atZone(ZoneId.systemDefault()).toInstant();

        taskScheduler.schedule(() -> {
            log.info("스케줄된 예약 만료 작업 실행. 예약 ID: {}", event.reservationId());
            try {
                // 결제 대기 상태인 경우 예약을 만료 처리
                reservationCancellationUseCase.cancelIfUnpaid(event.reservationId());
            } catch (CustomException e) {
                log.error("예약 만료 처리 중 오류 발생. 예약 ID: {}", event.reservationId(), e);
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "예약 만료 처리 중 오류가 발생했습니다.");
            }
        }, expirationInstant);
    }
}