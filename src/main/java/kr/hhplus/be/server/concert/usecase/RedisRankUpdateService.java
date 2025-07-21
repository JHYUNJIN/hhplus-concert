package kr.hhplus.be.server.concert.usecase;

import kr.hhplus.be.server.concert.adapter.out.persistence.soldOutRank.ConcertSoldOutRankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisRankUpdateService {

    private final ConcertSoldOutRankRepository concertSoldOutRankRepository;

    /**
     * Redis 랭킹 업데이트를 재시도 로직과 함께 실행합니다.
     * Redis 연결 실패 등 일시적인 오류 발생 시, 1초 간격으로 최대 3번까지 재시도합니다.
     * @param concertId 콘서트 ID
     * @param score     랭킹 점수
     */
    @Retryable(
            value = { Exception.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public void updateRankWithRetry(UUID concertId, long score) {
        log.info("Redis 랭킹 업데이트 시도. ConcertId: {}", concertId);
        Long rank = concertSoldOutRankRepository.updateRank(concertId, score);
        log.info("Redis 랭킹 업데이트 성공 - CONCERT_ID: {}, RANKING: {}", concertId, rank);
    }

    /**
     * 모든 재시도가 실패했을 때 실행되는 복구 메소드입니다.
     * 실패를 명확히 로깅하여, 모니터링 시스템이 감지하고 운영자가 조치할 수 있도록 합니다.
     * @param e         마지막으로 발생한 예외
     * @param concertId 실패한 콘서트 ID
     * @param score     실패한 랭킹 점수
     */
    @Recover
    public void recoverUpdateRank(Exception e, UUID concertId, long score) {
        // 이 시점에서 실패한 작업을 별도의 DB 테이블이나 DLQ에 저장하여 나중에 수동으로 처리할 수 있음
        log.error("최종 재시도 실패! Redis 랭킹 업데이트 누락. ConcertId: {}, Score: {}", concertId, score, e);
    }
}