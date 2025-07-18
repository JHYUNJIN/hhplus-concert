package kr.hhplus.be.server.reservation.adapter.out.persistence.reservation;

import jakarta.persistence.*;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.domain.enums.ReservationStatus;
import kr.hhplus.be.server.common.persistence.BaseTimeEntity;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "RESERVATION")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class ReservationEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", length = 36)
    private String id;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "seat_id", length = 36, nullable = false)
    private String seatId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10, nullable = false)
    @ColumnDefault("'PENDING'")
    private ReservationStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public static ReservationEntity from(Reservation reservation) {
        return ReservationEntity.builder()
                .id(reservation.id() != null ? reservation.id().toString() : null)
                .userId(reservation.userId().toString())
                .seatId(reservation.seatId().toString())
                .status(reservation.status())
                .expiresAt(reservation.expiresAt())
                .build();
    }

    public Reservation toDomain() {
        return Reservation.builder()
                .id(UUID.fromString(id))
                .userId(UUID.fromString(userId))
                .seatId(UUID.fromString(seatId))
                .status(status)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .expiresAt(expiresAt)
                .build();
    }
}
