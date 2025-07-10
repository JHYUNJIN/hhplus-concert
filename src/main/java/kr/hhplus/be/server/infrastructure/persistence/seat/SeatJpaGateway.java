package kr.hhplus.be.server.infrastructure.persistence.seat;

import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SeatJpaGateway implements SeatRepository {

    private final JpaSeatRepository jpaSeatRepository;

    @Override
    public Seat save(Seat seat) {
        SeatEntity seatEntity = jpaSeatRepository.save(SeatEntity.from(seat));
        return seatEntity.toDomain();
    }

    @Override
    public Optional<Seat> findBySeatIdAndConcertDateId(UUID seatId, UUID concertDateId) {
        return jpaSeatRepository.findBySeatIdAndConcertDateId(seatId.toString(), concertDateId.toString())
                .map(SeatEntity::toDomain);
    }

    @Override
    public Integer countRemainingSeat(UUID concertDateId) {
        return jpaSeatRepository.countRemainingSeat(concertDateId.toString());
    }

    @Override
    public List<Seat> findAvailableSeats(UUID concertId, UUID concertDateId) {
        return jpaSeatRepository.findAvailableSeats(concertId.toString(), concertDateId.toString()).stream()
                .map(SeatEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Seat> findById(UUID seatId) {
        return jpaSeatRepository.findById(seatId.toString())
                .map(SeatEntity::toDomain);
    }

    @Override
    public void deleteAll() {
        jpaSeatRepository.deleteAll();
    }
}

