// src/main/java/kr/hhplus/be/server/domain/enums/SeatStatus.java
package kr.hhplus.be.server.domain.enums;

public enum SeatStatus {
    AVAILABLE, // 예약 가능
    RESERVED,  // 예약됨 (예: 임시 배정, 결제 대기 중)
    BLOCKED    // 블록됨 (예: 시스템 오류, 유지보수)
}