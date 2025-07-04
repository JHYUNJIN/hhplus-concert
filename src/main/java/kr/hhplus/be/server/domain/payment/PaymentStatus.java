// src/main/java/kr/hhplus/be/server/domain/enums/PaymentStatus.java
package kr.hhplus.be.server.domain.payment;

public enum PaymentStatus {
    PENDING,   // 결제 대기 중 (시도되었으나 아직 완료되지 않음)
    SUCCESS,   // 결제 성공
    FAILED,    // 결제 실패
    CANCELLED  // 결제 취소됨
}