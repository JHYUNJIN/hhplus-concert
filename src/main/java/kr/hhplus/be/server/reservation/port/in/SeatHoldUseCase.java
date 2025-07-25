package kr.hhplus.be.server.reservation.port.in;

import java.util.UUID;

public interface SeatHoldUseCase {
    /**
     * 지정된 좌석을 특정 사용자가 임시 점유합니다.
     * @param seatId 좌석 ID
     * @param userId 사용자 ID
     */
    void hold(UUID seatId, UUID userId);

}