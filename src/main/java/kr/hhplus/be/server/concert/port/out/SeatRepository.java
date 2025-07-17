package kr.hhplus.be.server.concert.port.out;

import kr.hhplus.be.server.concert.domain.Seat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeatRepository {

    Seat save(Seat seat);
    Optional<Seat> findBySeatIdAndConcertDateId(UUID seatId, UUID concertDateId);
    Optional<Seat> findById(UUID seatId);
    List<Seat> findAvailableSeats(UUID concertId, UUID concertDateId);
    List<Seat> findByConcertDateId(UUID concertDateId);

    void deleteAll();


    // 더미데이터 전용 메소드
    List<Seat> findByConcertDateIds(List<UUID> concertDateIds);

}
