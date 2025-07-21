package kr.hhplus.be.server.concert.usecase;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.concert.adapter.out.persistence.soldOutRank.ConcertSoldOutRankRepository;
import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.SoldOutRank;
import kr.hhplus.be.server.concert.port.out.ConcertRepository;
import kr.hhplus.be.server.concert.port.out.SoldOutRankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConcertSoldOutManager {

    private static final int MAX_SEAT_COUNT = 50;

    private final ConcertRepository concertRepository;
    private final SoldOutRankRepository soldOutRankRepository;
    private final RedisRankUpdateService redisRankUpdateService;

    /**
     * MSA 전환을 위해 PaymentSuccessEvent에 대한 의존성을 제거하고,
     * 콘서트 도메인에 필요한 데이터만 파라미터로 받도록 수정했습니다.
     * @param concertId     콘서트 ID
     * @param seatSize      전체 좌석 수
     * @param soldOutTime   최종 매진 시간
     */
    @Transactional
    public void processUpdateRanking(UUID concertId, int seatSize, LocalDateTime soldOutTime) throws CustomException {
        try {
            Concert concert = getConcert(concertId); // 콘서트 정보 조회

            // ⭐️ 방어 코드 추가: 매진 시간이 티켓 오픈 시간보다 빠를 수 없는 논리적 오류 방지
            if (soldOutTime.isBefore(concert.openTime())) {
                log.warn("매진 시간({})이 콘서트 오픈 시간({})보다 빠릅니다. 랭킹을 업데이트하지 않습니다. ConcertId: {}",
                        soldOutTime, concert.openTime(), concertId);
                return; // 랭킹 업데이트 중단
            }

            long soldOutDuration = Duration.between(concert.openTime(), soldOutTime).getSeconds();
            long score = calcScore(soldOutDuration, concert.openTime(), seatSize);

            // 1. 데이터베이스 관련 작업을 먼저 수행
            concertRepository.save(concert.soldOut(soldOutTime));
            soldOutRankRepository.save(SoldOutRank.of(concertId, score, soldOutDuration));

            // 2. ⭐️ DB 트랜잭션이 성공적으로 커밋된 '이후' 실행될 작업
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.info("DB 트랜잭션 커밋 성공. Redis 랭킹 업데이트를 시작합니다. ConcertId: {}", concertId);
                    // 재시도 로직이 포함된 서비스를 호출
                    redisRankUpdateService.updateRankWithRetry(concertId, score);
                }
            });
        } catch (Exception e) {
            log.error("랭킹 정보 갱신 실패 - CONCERT_ID: {}, ERROR: {}", concertId, e.getMessage(), e);
            throw new CustomException(ErrorCode.RANKING_UPDATE_FAILED, "랭킹 정보 갱신에 실패했습니다.");
        }
    }

    private Concert getConcert(UUID concertId) throws CustomException {
        return concertRepository.findById(concertId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND));
    }

    /**
     * 점수 계산
     * @param soldOutTime 매진 소요 시간
     * @param openTime 티켓팅 오픈 시간
     * @param seatSize 좌석 총 개수
     * @return 점수
     */
    private long calcScore(long soldOutTime, LocalDateTime openTime, int seatSize) {
        int concertDateScore = 100 - (seatSize / MAX_SEAT_COUNT);
        long openTimeStamp = openTime.toEpochSecond(ZoneOffset.UTC);
        String score = String.format("%d%d%d", soldOutTime, concertDateScore, openTimeStamp);
        return Long.parseLong(score);
    }
}
