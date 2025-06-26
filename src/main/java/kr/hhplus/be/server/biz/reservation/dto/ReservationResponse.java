package kr.hhplus.be.server.biz.reservation.dto;

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
public class ReservationResponse {
    private String reservationId;
    private String userId;
    private String seatId;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getSeat().getId(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }
}