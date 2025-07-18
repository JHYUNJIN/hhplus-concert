package kr.hhplus.be.server.concert.usecase;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import kr.hhplus.be.server.concert.adapter.out.persistence.soldOutRank.ConcertSoldOutRankRepository;
import kr.hhplus.be.server.queue.port.out.QueueTokenRepository;
import kr.hhplus.be.server.reservation.port.out.SeatHoldRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.port.out.ConcertRepository;
import kr.hhplus.be.server.concert.domain.SoldOutRank;
import kr.hhplus.be.server.concert.port.out.SoldOutRankRepository;
import kr.hhplus.be.server.payment.domain.PaymentSuccessEvent;
import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConcertSoldOutManager {

    private static final int MAX_SEAT_COUNT = 50;

    private final ConcertRepository concertRepository;
    private final ConcertSoldOutRankRepository concertSoldOutRankRepository;
    private final SoldOutRankRepository soldOutRankRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final QueueTokenRepository queueTokenRepository;

    @Transactional
    public void processUpdateRanking(PaymentSuccessEvent event, UUID concertId, int seatSize) throws CustomException {
        try {
            Concert concert = getConcert(concertId);

            long soldOutTime = Duration.between(concert.openTime(), event.occurredAt()).getSeconds();

            long score = calcScore(soldOutTime, concert.openTime(), seatSize);

            Long rank = concertSoldOutRankRepository.updateRank(concertId, score);
            concertRepository.save(concert.soldOut(event.occurredAt()));
            soldOutRankRepository.save(SoldOutRank.of(concertId, score, soldOutTime));

            // --- Redis 데이터 정리 로직 ---
            seatHoldRepository.deleteHold(event.seat().id(), event.user().id());
            log.info("좌석 점유(hold) 해제 완료. SeatId: {}", event.seat().id());

            queueTokenRepository.expiresQueueToken(event.queueToken().tokenId().toString());
            log.info("대기열 토큰 만료 처리 완료. TokenId: {}", event.queueToken().tokenId());

            log.info("콘서트 매진 랭킹 업데이트 - CONCERT_ID: {}, RANKING: {}", concertId, rank);
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
