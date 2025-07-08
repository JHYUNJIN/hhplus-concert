package kr.hhplus.be.server.infrastructure.persistence.reservation;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface JpaReservationRepository extends JpaRepository<ReservationEntity, String> {
//    // 특정 seatId와 상태(status)를 가진 예약 조회 시 비관적 락 적용
//    // LockModeType.PESSIMISTIC_WRITE로 레코드 쓰기 락 설정, 다른 트랜잭션의 수정 방지
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("SELECT r FROM Reservation r WHERE r.seatId = :seatId AND r.status = :status")
//    Optional<Reservation> findBySeatIdAndStatusWithPessimisticLock(@Param("seatId") UUID seatId, @Param("status") ReservationStatus status);
//
//    // (선택 사항) PENDING 상태의 예약이 있는지 확인하는 메소드 (락 없이)
//    Optional<Reservation> findBySeatIdAndStatus(UUID seatId, ReservationStatus status);
}
