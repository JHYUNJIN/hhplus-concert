package kr.hhplus.be.server.domain.seat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeatRepository {

    Seat save(Seat seat);
    Optional<Seat> findBySeatIdAndConcertDateId(UUID seatId, UUID concertDateId);
    Integer countRemainingSeat(UUID concertDateId);
    List<Seat> findAvailableSeats(UUID concertId, UUID concertDateId);

    Optional<Seat> findById(UUID seatId);

    void deleteAll();

    List<Seat> findByConcertDateId(UUID id);
}
