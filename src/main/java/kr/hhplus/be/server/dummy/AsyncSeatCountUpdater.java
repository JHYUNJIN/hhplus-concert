package kr.hhplus.be.server.dummy;

import kr.hhplus.be.server.common.util.DistributedLockKeyGenerator;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.port.out.ConcertDateRepository;
import kr.hhplus.be.server.concert.port.out.SeatRepository;
import kr.hhplus.be.server.reservation.usecase.DistributedLockManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AsyncSeatCountUpdater {

    private final ConcertDateRepository concertDateRepository;
    private final SeatRepository seatRepository;
    private final DistributedLockManager distributedLockManager;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDataGeneration(DummyDataGeneratedEvent event) {
        updateAvailableSeatCounts(event.concertDateIds());
    }

    @Transactional
    public void updateAvailableSeatCounts(List<UUID> concertDateIds) {
        try {
            distributedLockManager.executeWithLock(DistributedLockKeyGenerator.getDummyUpdateSeatsLockKey(), () -> {
                log.info("분산락 획득, 생성된 좌석 수에 맞춰 availableSeatCount 업데이트 시작...");

                List<Seat> allSeats = seatRepository.findByConcertDateIds(concertDateIds);
                Map<UUID, Long> seatCountMap = allSeats.stream()
                        .filter(Seat::isAvailable)
                        .collect(Collectors.groupingBy(Seat::concertDateId, Collectors.counting()));

                concertDateIds.forEach(dateId -> {
                    Long count = seatCountMap.getOrDefault(dateId, 0L);
                    concertDateRepository.updateAvailableSeatCount(dateId, count);
                });

                log.info("availableSeatCount 업데이트 완료.");
            });
        } catch (Exception e) {
            log.error("좌석 수 업데이트 중 오류 발생", e);
        }
    }
}