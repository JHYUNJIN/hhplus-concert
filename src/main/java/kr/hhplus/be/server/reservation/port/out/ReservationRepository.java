package kr.hhplus.be.server.reservation.port.out;

import kr.hhplus.be.server.reservation.domain.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ReservationRepository {
    Reservation save(Reservation reservation);
    Optional<Reservation> findById(UUID reservationId);
    List<Reservation> findAll();

    /**
     * 지정된 시간 이전에 만료되었고, PENDING 상태인 예약 ID 목록을
     * 지정된 개수(limit)만큼 조회합니다.
     * @param now   현재 시간
     * @param limit 조회할 최대 개수
     * @return 만료된 예약 ID 목록
     */
    List<UUID> findExpiredPendingReservationIds(LocalDateTime now, int limit);

    void deleteAll();

}
