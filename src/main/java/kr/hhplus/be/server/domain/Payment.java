// src/main/java/kr/hhplus/be/server/domain/Payment.java
package kr.hhplus.be.server.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import kr.hhplus.be.server.domain.enums.PaymentStatus;

@Entity
@Table(name = "PAYMENT")
@Getter
@Setter
@NoArgsConstructor
public class Payment {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id; // 결제 UUID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // user_id 컬럼이 User 엔티티의 id를 참조
    private User user; // 사용자 ID (User 엔티티 참조)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private kr.hhplus.be.server.domain.Reservation reservation; // 예약 ID (Reservation 엔티티 참조)

    @Column(name = "amount", nullable = false, precision = 10, scale = 0)
    private BigDecimal amount; // 결제 금액

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status; // 결제 상태

    @Column(name = "failure_reason", columnDefinition = "TEXT") // TEXT 타입 매핑
    private String failureReason; // 결제 실패 사유

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 편의를 위한 생성자
    public Payment(String id, User user, kr.hhplus.be.server.domain.Reservation reservation, BigDecimal amount, PaymentStatus status, String failureReason) {
        this.id = id;
        this.user = user;
        this.reservation = reservation;
        this.amount = amount;
        this.status = status;
        this.failureReason = failureReason;
    }
}