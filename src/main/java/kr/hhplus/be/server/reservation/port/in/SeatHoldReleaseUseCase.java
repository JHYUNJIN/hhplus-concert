package kr.hhplus.be.server.reservation.port.in;

import java.util.UUID;

public interface SeatHoldReleaseUseCase {
    void releaseHold(UUID seatId, UUID userId);
}