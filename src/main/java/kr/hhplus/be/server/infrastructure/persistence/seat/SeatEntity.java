package kr.hhplus.be.server.infrastructure.persistence.seat;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatGrade;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import kr.hhplus.be.server.infrastructure.persistence.BaseTimeEntity;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "SEAT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class SeatEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", length = 36)
    private String id;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "concert_date_id", length = 36, nullable = false)
    private String concertDateId;

    @Column(name = "seat_no", nullable = false)
    private Integer seatNo;

    @Column(name = "price", precision = 8, nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false)
    private SeatGrade seatGrade;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @ColumnDefault("'AVAILABLE'")
    private SeatStatus status;

    @Version
    @Column(name = "version")
    private Long version; // 낙관적 락(Optimistic Locking)을 위한 버전 필드

    public static SeatEntity from(Seat seat) {
        return SeatEntity.builder()
                .concertDateId(seat.concertDateId().toString())
                .seatNo(seat.seatNo())
                .price(seat.price())
                .seatGrade(seat.seatGrade())
                .status(seat.status())
                .build();
    }

    public Seat toDomain() {
        return Seat.builder()
                .id(UUID.fromString(id))
                .concertDateId(UUID.fromString(concertDateId))
                .seatNo(seatNo)
                .price(price)
                .seatGrade(seatGrade)
                .status(status)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }

    /**
     * Seat 도메인 객체의 변경된 상태를 현재 SeatEntity 인스턴스에 반영합니다.
     * 이 메서드는 DB에서 조회된 영속 상태의 엔티티를 업데이트할 때 사용됩니다.
     *
     * @param seat 업데이트할 Seat 도메인 객체
     */
    public void update(Seat seat) {
        // ID는 변경하지 않습니다. (엔티티의 식별자)
        // this.id = domainSeat.id().toString(); // ID는 변경하지 않아야 합니다.

        // 변경될 수 있는 필드들을 도메인 객체의 값으로 업데이트합니다.
        this.concertDateId = seat.concertDateId().toString();
        this.seatNo = seat.seatNo();
        this.price = seat.price();
        this.seatGrade = seat.seatGrade();
        this.status = seat.status(); // <-- 상태 변경이 핵심

        // createdAt은 @CreationTimestamp에 의해 최초 생성 시 설정되고 updatable=false이므로 변경하지 않습니다.
        // updatedAt은 @UpdateTimestamp에 의해 자동으로 갱신됩니다.
        // 따라서 여기서는 직접 this.updatedAt = LocalDateTime.now(); 할 필요가 없습니다.
    }
}
