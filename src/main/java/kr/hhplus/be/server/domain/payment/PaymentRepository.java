package kr.hhplus.be.server.domain.payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findByReservationId(UUID reservationId);

    void deleteAll();

    Optional<Payment> findByReservationIdForUpdate(UUID reservationId);
}
