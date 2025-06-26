package kr.hhplus.be.server.infrastructure.persistence.concert;

import kr.hhplus.be.server.domain.concert.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface SeatJpaRepository extends JpaRepository<Seat, String> {
    List<Seat> findByConcertDateId(String concertDateId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.id = :id")
    Optional<Seat> findByIdWithPessimisticLock(String id);
}