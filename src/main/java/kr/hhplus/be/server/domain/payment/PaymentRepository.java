package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.infrastructure.persistence.payment.PaymentEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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
