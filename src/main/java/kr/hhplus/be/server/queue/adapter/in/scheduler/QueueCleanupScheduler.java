package kr.hhplus.be.server.queue.adapter.in.scheduler;

import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.port.out.ConcertRepository;
import kr.hhplus.be.server.queue.port.out.QueueTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueueCleanupScheduler {

    private final QueueTokenRepository queueTokenRepository;
    private final ConcertRepository concertRepository; // 콘서트 목록을 가져오기 위해 의존성 주입

    /**
     * 매 30초마다 실행되어 만료된 대기열 토큰을 정리합니다.
     */
    @Scheduled(cron = "*/30 * * * * *")
    public void cleanupExpiredWaitingTokens() {
        // 오픈된 콘서트 목록을 가져와서 각 콘서트에 대해 만료된 대기 토큰을 정리
        List<Concert> allConcerts = concertRepository.findByOpenConcerts(LocalDateTime.now());
        for (Concert concert : allConcerts) {
            try {
                long removedCount = queueTokenRepository.removeExpiredWaitingTokens(concert.id());
                if (removedCount > 0) {
                    log.info("✅ 콘서트 ID {}: 만료된 대기 토큰 {}개 삭제 완료", concert.id(), removedCount);
                }
            } catch (Exception e) {
                log.error("❌ 콘서트 ID {}의 만료 토큰 정리 중 오류 발생", concert.id(), e);
            }
        }
        log.info("⏰ 만료된 대기열 토큰 정리 스케줄러 종료");
    }

    /**
     * 매 300초마다 실행되어 만료된 활성 토큰을 정리합니다.
     */
    @Scheduled(cron = "*/300 * * * * *")
    public void cleanupStaleActiveTokens() {
        List<Concert> allConcerts = concertRepository.findAll();
        for (Concert concert : allConcerts) {
            try {
                long removedCount = queueTokenRepository.removeStaleActiveTokens(concert.id());
                if (removedCount > 0) {
                    log.info("✅ 콘서트 ID {}: 만료된 활성 토큰 {}개 삭제 완료", concert.id(), removedCount);
                }
            } catch (Exception e) {
                log.error("❌ 콘서트 ID {}의 만료 활성 토큰 정리 중 오류 발생", concert.id(), e);
            }
        }
    }
}
