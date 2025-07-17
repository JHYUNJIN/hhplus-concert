// src/main/java/kr/hhplus/be/server/domain/enums/ReservationStatus.java
package kr.hhplus.be.server.reservation.domain.enums;

public enum ReservationStatus {
    PENDING,   // 예약 대기 중 (좌석 선택 후 결제 전 단계 등)
    SUCCESS,   // 예약 성공 (결제까지 완료되어 최종 확정)
    FAILED,    // 예약 실패 (결제 실패)
    CANCELLED,  // 예약 취소됨 (사용자 취소, 관리자 취소 등)
    EXPIRED    // 예약 만료됨 (결제 시간 초과 등)
}