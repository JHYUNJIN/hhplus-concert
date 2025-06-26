package kr.hhplus.be.server.domain.reservation;

import kr.hhplus.be.server.domain.enums.ReservationStatus;


import java.util.List;
import java.util.Optional;


public interface ReservationRepository {
    // 예약 저장 및 업데이트
    Reservation save(Reservation reservation);

    // ID로 예약 조회
    Optional<Reservation> findById(String reservationId);

    // 특정 사용자 ID로 예약 목록 조회
    List<Reservation> findByUserId(String userId);

    // 특정 좌석 ID로 예약 목록 조회
    List<Reservation> findBySeatId(String seatId);

    // 예약 상태로 예약 목록 조회
    List<Reservation> findByStatus(ReservationStatus status);

    // ID로 예약 조회 (업데이트를 위한 락 포함)
    Optional<Reservation> findByIdForUpdate(String reservationId);

    // 모든 예약 삭제 (테스트 용도)
    void deleteAll();
}