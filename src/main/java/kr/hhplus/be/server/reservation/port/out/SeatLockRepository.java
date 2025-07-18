package kr.hhplus.be.server.reservation.port.out;

import java.util.UUID;

public interface SeatLockRepository {
    boolean acquisitionLock(UUID seatId); // 좌석 잠금 획득
    void releaseLock(UUID seatId); // 좌석 잠금 해제
}
