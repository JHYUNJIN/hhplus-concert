package kr.hhplus.be.server.infrastructure.persistence.payment;

import kr.hhplus.be.server.domain.enums.PaymentStatus;
import kr.hhplus.be.server.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, String> {
    List<Payment> findByUserId(String userId);
    Optional<Payment> findByReservationId(String reservationId);
    List<Payment> findByStatus(PaymentStatus status);
}