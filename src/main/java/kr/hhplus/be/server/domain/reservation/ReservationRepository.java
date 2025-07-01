package kr.hhplus.be.server.domain.reservation;

import kr.hhplus.be.server.common.exception.CustomException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ReservationRepository {
    Reservation save(Reservation reservation);
    Optional<Reservation> findById(UUID reservationId) throws CustomException;
    List<Reservation> findAll();

    void deleteAll();
}
