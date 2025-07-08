package kr.hhplus.be.server.domain.seat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SeatStatus {
    AVAILABLE("예약가능"),
    RESERVED("예약됨"), // 결제가 완료되지 않아 예약만 된 상태로 좌석에 락이 걸린 상태
    ASSIGNED("배정됨") // 결제가 완료되어 좌석이 배정된 상태
    ;

    private final String message;

}
