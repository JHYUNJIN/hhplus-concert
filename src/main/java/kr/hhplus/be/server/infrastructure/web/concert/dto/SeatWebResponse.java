package kr.hhplus.be.server.infrastructure.web.concert.dto;

import kr.hhplus.be.server.domain.concert.Seat;
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
public class SeatWebResponse {
    private String seatId;
    private String concertDateId;
    private Integer seatNo;
    private BigDecimal price;
    private SeatGrade seatGrade;
    private SeatStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SeatWebResponse from(Seat seat) {
        return new SeatWebResponse(
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