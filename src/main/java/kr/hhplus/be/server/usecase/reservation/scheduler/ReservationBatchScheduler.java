package kr.hhplus.be.server.usecase.reservation.scheduler;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import kr.hhplus.be.server.infrastructure.persistence.lock.DistributedLockManager;
import kr.hhplus.be.server.usecase.reservation.ReservationCancellationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationBatchScheduler {

    private final ReservationRepository reservationRepository;
    private final ReservationCancellationUseCase reservationCancellationUseCase;
    private final DistributedLockManager distributedLockManager;

    private static final String EXPIRE_LOCK_KEY = "reservation:expire-batch";

    // 매 1분마다 실행 (cron 표현식: "초 분 시 일 월 요일")
    @Scheduled(cron = "0 * * * * *")
    public void expirePendingReservations() {
        log.info("예약 만료 배치 작업 시작...");
        try {
            // 분산 락을 사용하여 여러 서버 인스턴스에서 동시에 실행되는 것을 방지
            distributedLockManager.executeWithLock(EXPIRE_LOCK_KEY, () -> {
                List<UUID> expiredIds = reservationRepository.findExpiredPendingReservationIds(LocalDateTime.now());
                if (expiredIds.isEmpty()) {
                    log.info("만료된 예약이 없습니다.");
                    return;
                }
                for (UUID reservationId : expiredIds) {
                    try {
                        // 예약 만료 처리
                        reservationCancellationUseCase.cancelIfUnpaid(reservationId);
                    } catch (CustomException e) {
                        // 개별 예약 처리 실패가 전체 배치 작업에 영향을 주지 않도록 예외 처리, throw 하지 않음
                        log.error("예약 만료 처리 중 오류 발생. 예약 ID: {}", reservationId, e);
                    }
                }
                log.info("만료된 예약 일괄 처리 작업 완료.");
            });
        } catch (Exception e) {
            log.error("예약 만료 배치 작업 실행 중 락 획득 실패 또는 예외 발생", e);
        }
    }
}