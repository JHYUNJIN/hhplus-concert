package kr.hhplus.be.server.reservation.port.out;

import java.util.UUID;

public interface SeatHoldRepository {
    void hold(UUID seatId, UUID userId);
    boolean isHoldSeat(UUID seatId, UUID userId);
    void deleteHold(UUID seatId, UUID userId);
}
