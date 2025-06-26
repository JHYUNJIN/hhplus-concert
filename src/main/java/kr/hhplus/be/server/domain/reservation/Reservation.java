// src/main/java/kr/hhplus/be/server/domain/Reservation.java
package kr.hhplus.be.server.domain.reservation;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "RESERVATION")
@Getter
@Setter
@NoArgsConstructor
public class Reservation {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id; // 예약 UUID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // user_id 컬럼이 User 엔티티의 id를 참조
    private User user; // 사용자 ID (User 엔티티 참조)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat; // 좌석 ID (Seat 엔티티 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private kr.hhplus.be.server.domain.enums.ReservationStatus status; // 예약 상태

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 생성일시

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 수정일시

    // 양방향 관계 설정
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    // 편의를 위한 생성자
    public Reservation(String id, User user, Seat seat, kr.hhplus.be.server.domain.enums.ReservationStatus status) {
        this.id = id;
        this.user = user;
        this.seat = seat;
        this.status = status;
    }
}