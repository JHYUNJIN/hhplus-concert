package kr.hhplus.be.server.biz.concert.dto;

import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.enums.SeatGrade;
import kr.hhplus.be.server.domain.enums.SeatStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {
    private String seatId;
    private String concertDateId;
    private Integer seatNo;
    private BigDecimal price;
    private SeatGrade seatGrade;
    private SeatStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SeatResponse from(Seat seat) {
        return new SeatResponse(
                seat.getId(),
                seat.getConcertDate().getId(), // 연관된 ConcertDate ID
                seat.getSeatNo(),
                seat.getPrice(),
                seat.getSeatGrade(),
                seat.getStatus(),
                seat.getCreatedAt(),
                seat.getUpdatedAt()
        );
    }
}