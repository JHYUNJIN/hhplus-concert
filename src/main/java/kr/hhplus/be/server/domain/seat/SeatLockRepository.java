package kr.hhplus.be.server.domain.seat;

import java.util.UUID;

public interface SeatLockRepository {
    boolean acquisitionLock(UUID seatId);
    void releaseLock(UUID seatId);
}
