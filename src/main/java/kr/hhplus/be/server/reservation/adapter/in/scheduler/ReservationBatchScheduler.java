package kr.hhplus.be.server.reservation.adapter.in.scheduler;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.util.DistributedLockKeyGenerator;
import kr.hhplus.be.server.reservation.port.in.ReservationCancellationUseCase;
import kr.hhplus.be.server.reservation.port.out.ReservationRepository;
import kr.hhplus.be.server.reservation.usecase.DistributedLockManager;
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

    private static final int BATCH_SIZE = 1000; // ⭐️ 한 번에 처리할 최대 만료 예약 수

    private final ReservationRepository reservationRepository;
    private final ReservationCancellationUseCase reservationCancellationUseCase;
    private final DistributedLockManager distributedLockManager;

    // 매 1분마다 실행 (cron 표현식: "초 분 시 일 월 요일")
    @Scheduled(cron = "0 * * * * *")
    public void expirePendingReservations() {
        log.info("예약 만료 배치 작업 시작...");
        try {
            // 분산 락을 사용하여 여러 서버 인스턴스에서 동시에 실행되는 것을 방지, 단일 서버 환경이라면 필요 없지만, 다중 서버 환경에서는 필수임
            distributedLockManager.executeWithLock(DistributedLockKeyGenerator.getReservationExpireBatchLockKey(), () -> {
                List<UUID> expiredIds = reservationRepository.findExpiredPendingReservationIds(LocalDateTime.now(), BATCH_SIZE);
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