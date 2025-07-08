package kr.hhplus.be.server.infrastructure.persistence.payment;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.Optional;

public interface JpaPaymentRepository extends JpaRepository<PaymentEntity, String> {
    Optional<PaymentEntity> findByReservationId(String reservationId);

    @Query("select p from PaymentEntity p where p.reservationId = :reservationId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "javax.persistence.lock.timeout", value = "5000")
    })
    Optional<PaymentEntity> findByReservationIdForUpdate(String reservationId);
}
