// src/main/java/kr/hhplus/be/server/domain/Seat.java
package kr.hhplus.be.server.domain.concert;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.reservation.Reservation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import kr.hhplus.be.server.domain.enums.SeatGrade;
import kr.hhplus.be.server.domain.enums.SeatStatus;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SEAT")
@Getter
@Setter
@NoArgsConstructor
public class Seat {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id; // 좌석 UUID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_date_id", nullable = false)
    private ConcertDate concertDate; // 콘서트 날짜 ID

    @Column(name = "seat_no", nullable = false)
    private Integer seatNo; // 좌석 번호 (1-50)

    @Column(name = "price", nullable = false, precision = 8, scale = 0)
    private BigDecimal price; // 좌석 가격

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_grade", nullable = false) // 컬럼명 변경
    private SeatGrade seatGrade; // 필드명 및 타입 변경

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SeatStatus status; // 좌석 상태

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 양방향 관계 설정 (이 부분은 동일)
    @OneToMany(mappedBy = "seat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();

    // 편의를 위한 생성자 (생성자 파라미터도 변경)
    public Seat(String id, ConcertDate concertDate, Integer seatNo, BigDecimal price, SeatGrade seatGrade, SeatStatus status) {
        this.id = id;
        this.concertDate = concertDate;
        this.seatNo = seatNo;
        this.price = price;
        this.seatGrade = seatGrade; // 변경
        this.status = status;
    }
}