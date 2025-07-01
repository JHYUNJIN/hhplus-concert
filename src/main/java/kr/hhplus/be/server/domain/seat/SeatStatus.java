package kr.hhplus.be.server.domain.seat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SeatStatus {
    AVAILABLE("예약가능"),
    RESERVED("예약됨"),
    ASSIGNED("배정됨")
    ;

    private final String message;

}
