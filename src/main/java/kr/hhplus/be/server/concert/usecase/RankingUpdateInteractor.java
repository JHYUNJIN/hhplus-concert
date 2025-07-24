package kr.hhplus.be.server.concert.usecase;

import kr.hhplus.be.server.concert.domain.ConcertDate;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.domain.enums.SeatStatus;
import kr.hhplus.be.server.concert.port.in.GetConcertDateUseCase;
import kr.hhplus.be.server.concert.port.in.RankingUpdateUseCase;
import kr.hhplus.be.server.concert.port.out.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingUpdateInteractor implements RankingUpdateUseCase {

    private final ConcertSoldOutManager concertSoldOutManager;
    private final SeatRepository seatRepository;
    private final GetConcertDateUseCase getConcertDateUseCase;

    /**
     * @Async 어노테이션을 통해 이 메소드는 별도의 스레드에서 비동기적으로 실행됩니다.
     * 매진 여부를 확인하고, 필요할 경우에만 랭킹 업데이트를 담당하는 Manager를 호출합니다.
     * @param concertDateId 매진 여부를 확인할 콘서트 날짜 ID
     * @param occurredAt    결제가 발생한 시간 (최종 매진 시간으로 사용될 수 있음)
     */
    @Override
    @Async
    public void updateRankingIfNeeded(UUID concertDateId, LocalDateTime occurredAt) {
        try {
            // 좌석이 모두 매진되었는지 확인
            ConcertDate concertDate = getConcertDateUseCase.findById(concertDateId);
            List<Seat> allSeats = seatRepository.findByConcertDateId(concertDate.id());
            boolean isAllSeatsAssigned = allSeats.stream()
                    .allMatch(seatItem -> seatItem.status() == SeatStatus.ASSIGNED);

            if (!isAllSeatsAssigned) return;
            concertSoldOutManager.processUpdateRanking(
                    concertDate.concertId(),
                    allSeats.size(),
                    occurredAt
            );
        } catch (Exception e) {
            log.error("비동기 랭킹 업데이트 중 오류 발생. ConcertDateId: {}", concertDateId, e);
        }
    }

}