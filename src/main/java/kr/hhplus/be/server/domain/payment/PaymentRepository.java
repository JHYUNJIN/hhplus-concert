package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.domain.enums.PaymentStatus;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    // 결제 저장 및 업데이트
    Payment save(Payment payment);

    // ID로 결제 조회
    Optional<Payment> findById(String paymentId);

    // 특정 사용자 ID로 결제 목록 조회
    List<Payment> findByUserId(String userId);

    // 특정 예약 ID로 결제 조회 (단일 결제 가정)
    Optional<Payment> findByReservationId(String reservationId);

    // 결제 상태로 결제 목록 조회
    List<Payment> findByStatus(PaymentStatus status);

    // 모든 결제 삭제 (테스트 용도)
    void deleteAll();
}