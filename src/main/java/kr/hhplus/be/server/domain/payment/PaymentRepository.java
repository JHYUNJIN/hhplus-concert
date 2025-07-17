package kr.hhplus.be.server.domain.payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Optional<Payment> findById(UUID paymentId);

    Payment save(Payment payment);

    Optional<Payment> findByReservationId(UUID reservationId);

    void deleteAll();

    int updateStatusIfExpected(UUID reservationId,
                                      PaymentStatus newStatus,
                                      PaymentStatus expectedStatus);

}
