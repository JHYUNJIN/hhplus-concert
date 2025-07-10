package kr.hhplus.be.server.infrastructure.persistence.payment;

import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentJpaGateway implements PaymentRepository {

    private final JpaPaymentRepository jpaPaymentRepository;

    @Override
    public Payment save(Payment payment) {
        PaymentEntity paymentEntity = PaymentEntity.from(payment);
        return jpaPaymentRepository.save(paymentEntity).toDomain();
    }

    @Override
    public Optional<Payment> findByReservationId(UUID reservationId) {
        return jpaPaymentRepository.findByReservationId(reservationId.toString())
                .map(PaymentEntity::toDomain);
    }

    @Override
    public void deleteAll() {
        jpaPaymentRepository.deleteAll();
    }

}
