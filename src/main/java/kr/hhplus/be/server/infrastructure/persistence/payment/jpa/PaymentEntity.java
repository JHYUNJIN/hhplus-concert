package kr.hhplus.be.server.infrastructure.persistence.payment.jpa;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
import kr.hhplus.be.server.infrastructure.persistence.BaseTimeEntity;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "PAYMENT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class PaymentEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", length = 36)
    private String id;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "reservation_id", length = 36, nullable = false)
    private String reservationId;

    @Column(name = "amount", precision = 10, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @ColumnDefault("'PENDING'")
    private PaymentStatus status;

    @Column(name = "failure_reason")
    private String failureReason;

    public static PaymentEntity from(Payment payment) {
        return PaymentEntity.builder()
                .id(payment.id() != null ? payment.id().toString() : null)
                .userId(payment.userId().toString())
                .reservationId(payment.reservationId().toString())
                .amount(payment.amount())
                .status(payment.status())
                .build();
    }

    public Payment toDomain() {
        return Payment.builder()
                .id(UUID.fromString(id))
                .userId(UUID.fromString(userId))
                .reservationId(UUID.fromString(reservationId))
                .status(status)
                .amount(amount)
                .failureReason(failureReason)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }
}
