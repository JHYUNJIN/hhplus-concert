package kr.hhplus.be.server.infrastructure.web.reservation.dto;

import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.enums.ReservationStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationWebResponse {
    private String reservationId;
    private String userId;
    private String seatId;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReservationWebResponse from(Reservation reservation) {
        return new ReservationWebResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getSeat().getId(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }
}