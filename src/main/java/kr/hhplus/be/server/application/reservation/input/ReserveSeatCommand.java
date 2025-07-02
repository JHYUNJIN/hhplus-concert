package kr.hhplus.be.server.application.reservation.input;

import kr.hhplus.be.server.infrastructure.api.reservation.dto.request.ReservationRequest;

import java.util.UUID;



public record ReserveSeatCommand(
        UUID concertId,
        UUID concertDateId,
        UUID seatId,
        String queueTokenId
){
    public static ReserveSeatCommand of(ReservationRequest request, UUID seatId, String queueToken) {
        return new ReserveSeatCommand(request.concertId(), request.concertDateId(), seatId, queueToken);
    }
}
