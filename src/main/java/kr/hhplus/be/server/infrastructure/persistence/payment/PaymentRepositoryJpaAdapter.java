package kr.hhplus.be.server.infrastructure.persistence.payment;

import kr.hhplus.be.server.domain.enums.PaymentStatus;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PaymentRepositoryJpaAdapter implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    public PaymentRepositoryJpaAdapter(PaymentJpaRepository paymentJpaRepository) {
        this.paymentJpaRepository = paymentJpaRepository;
    }

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public Optional<Payment> findById(String paymentId) {
        return paymentJpaRepository.findById(paymentId);
    }

    @Override
    public List<Payment> findByUserId(String userId) {
        return paymentJpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<Payment> findByReservationId(String reservationId) {
        return paymentJpaRepository.findByReservationId(reservationId);
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        return paymentJpaRepository.findByStatus(status);
    }

    @Override
    public void deleteAll() {
        paymentJpaRepository.deleteAll();
    }
}