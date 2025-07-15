package kr.hhplus.be.server.domain.reservation;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder(toBuilder = true)
public record Reservation(
        UUID id,
        UUID userId,
        UUID seatId,
        ReservationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime expiresAt
) {
    public static Reservation of(UUID userId, UUID seatId) {
        LocalDateTime now = LocalDateTime.now();
        return Reservation.builder()
                .userId(userId)
                .seatId(seatId)
                .status(ReservationStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .expiresAt(now.plusMinutes(5)) // 예약 유효 시간 5분
                .build();
    }

    public Reservation payment() {
        return this.toBuilder()
                .status(ReservationStatus.SUCCESS)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public Reservation fail() {
        return this.toBuilder()
                .status(ReservationStatus.FAILED)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public Reservation expire() {
        return this.toBuilder()
                .status(ReservationStatus.EXPIRED)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}