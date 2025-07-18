package kr.hhplus.be.server.reservation.port.out;

import kr.hhplus.be.server.reservation.domain.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ReservationRepository {
    Reservation save(Reservation reservation);
    Optional<Reservation> findById(UUID reservationId);
    List<Reservation> findAll();
    List<UUID> findExpiredPendingReservationIds(LocalDateTime now);

    void deleteAll();

}
