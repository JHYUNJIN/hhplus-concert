package kr.hhplus.be.server.infrastructure.persistence.reservation;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.enums.ReservationStatus;
import kr.hhplus.be.server.domain.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReservationJpaRepository extends JpaRepository<Reservation, String> {
    List<Reservation> findByUserId(String userId);
    List<Reservation> findBySeatId(String seatId);
    List<Reservation> findByStatus(ReservationStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.id = :id")
    Optional<Reservation> findByIdWithPessimisticLock(String id);
}