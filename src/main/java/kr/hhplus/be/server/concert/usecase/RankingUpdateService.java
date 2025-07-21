// 랭킹 업데이트를 비동기적으로 처리하는 책임을 가집니다.
package kr.hhplus.be.server.concert.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingUpdateService {

    private final ConcertSoldOutManager concertSoldOutManager;

    /**
     * @Async 어노테이션을 통해 이 메소드는 별도의 스레드에서 비동기적으로 실행됩니다.
     * MSA 전환을 위해 PaymentSuccessEvent에 대한 의존성을 제거하고,
     * 콘서트 도메인에 필요한 데이터만 파라미터로 받도록 수정했습니다.
     *
     * @param concertId     콘서트 ID
     * @param seatSize      전체 좌석 수
     * @param soldOutTime   최종 매진 시간 (결제 성공 시간)
     */
    @Async
    public void updateRankingAsync(UUID concertId, int seatSize, LocalDateTime soldOutTime) {
        log.info("비동기 랭킹 업데이트 시작. ConcertId: {}", concertId);
        try {
            concertSoldOutManager.processUpdateRanking(concertId, seatSize, soldOutTime);
        } catch (Exception e) {
            // 비동기 작업에서 발생한 예외 별도 처리, 로그를 기록하여 어드민에서 관리할 수 있는 정보를 남김
            log.error("비동기 랭킹 업데이트 중 오류 발생. ConcertId: {}", concertId, e);
        }
    }
}