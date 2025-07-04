package kr.hhplus.be.server.api.concert.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatGrade;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record SeatResponse(
        @Schema(description = "좌석 ID")
        UUID seatId,
        @Schema(description = "좌석 번호")
        Integer seatNo,
        @Schema(description = "좌석 가격")
        BigDecimal price,
        @Schema(description = "좌석 등급")
        SeatGrade seatGrade,
        @Schema(description = "좌석 상태")
        SeatStatus status
) {
    public static SeatResponse from(Seat seat) {
        return SeatResponse.builder()
                .seatId(seat.id())
                .seatNo(seat.seatNo())
                .price(seat.price())
                .seatGrade(seat.seatGrade())
                .status(seat.status())
                .build();
    }
}
