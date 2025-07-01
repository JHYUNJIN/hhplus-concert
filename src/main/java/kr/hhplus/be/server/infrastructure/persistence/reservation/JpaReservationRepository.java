package kr.hhplus.be.server.infrastructure.persistence.reservation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaReservationRepository extends JpaRepository<ReservationEntity, String> {
}
