package kr.hhplus.be.server.infrastructure.persistence.seat;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
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
    @Column(name = "status", length = 10, nullable = false)
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

    // record 영속성 문제로 인해 엔티티 상태를 변경하는 메서드 추가
    public void changeStatus(SeatStatus newStatus) {
        this.status = newStatus;
    }
}
