package kr.hhplus.be.server.reservation.adapter.out.persistence.reservation;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaReservationRepository extends JpaRepository<ReservationEntity, String> {

    // 만료 시간 경과 & 상태가 PENDING 인 예약의 ID 목록을 조회하는 쿼리
    @Query("SELECT r.id FROM ReservationEntity r WHERE r.status = 'PENDING' AND r.expiresAt < :now")
    List<String> findExpiredPendingReservationIds(LocalDateTime now, Pageable pageable);

}