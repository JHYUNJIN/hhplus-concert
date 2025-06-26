package kr.hhplus.be.server.biz.concert.dto;

import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConcertDateResponse {
    private String concertDateId;
    private String concertId;
    private LocalDateTime date;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ConcertDateResponse from(ConcertDate concertDate) {
        return new ConcertDateResponse(
                concertDate.getId(),
                concertDate.getConcert().getId(), // 연관된 Concert ID
                concertDate.getDate(),
                concertDate.getDeadline(),
                concertDate.getCreatedAt(),
                concertDate.getUpdatedAt()
        );
    }
}