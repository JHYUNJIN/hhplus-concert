package kr.hhplus.be.server.usecase.reservation;

import java.util.UUID;

public interface ReservationCancellationUseCase {
    /**
     * 결제되지 않은 예약을 만료 처리합니다.
     * @param reservationId 만료시킬 예약 ID
     */
    void cancelIfUnpaid(UUID reservationId);
}