// src/main/java/kr/hhplus/be/server/domain/enums/PaymentStatus.java
package kr.hhplus.be.server.domain.payment;

public enum PaymentStatus {
    PENDING,   // 결제 대기 중 (시도되었으나 아직 완료되지 않음)
    PROCESSING, // 결제 처리 중, 낙관적 락을 사용하여 결제 진행 중임을 나타냄
    SUCCESS,   // 결제 성공
    FAILED,    // 결제 실패, -> 실패 이벤트 발생 후 복구처리
    CANCELLED  // 결제 취소, -> 사용자가 취소하거나 시스템에 의해 취소된 경우
}
