package kr.hhplus.be.server.domain.reservation;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;

@Builder
public record Reservation(
        UUID id,
        UUID userId,
        UUID seatId,
        ReservationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static Reservation of(UUID userId, UUID seatId) {
        return Reservation.builder()
                .userId(userId)
                .seatId(seatId)
                .status(ReservationStatus.PENDING)
                .build();
    }

    public Reservation payment() {
        return Reservation.builder()
                .id(id)
                .userId(userId)
                .seatId(seatId)
                .status(ReservationStatus.SUCCESS)
                .createdAt(createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public Reservation fail() {
        return Reservation.builder()
                .id(id)
                .userId(userId)
                .seatId(seatId)
                .status(ReservationStatus.FAILED)
                .createdAt(createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
