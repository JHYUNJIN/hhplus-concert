package kr.hhplus.be.server.infrastructure.web.concert.dto;

import kr.hhplus.be.server.domain.concert.ConcertDate;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConcertDateWebResponse {
    private String concertDateId;
    private String concertId;
    private LocalDateTime date;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ConcertDateWebResponse from(ConcertDate concertDate) {
        return new ConcertDateWebResponse(
                concertDate.getId(),
                concertDate.getConcert().getId(), // 연관된 Concert ID
                concertDate.getDate(),
                concertDate.getDeadline(),
                concertDate.getCreatedAt(),
                concertDate.getUpdatedAt()
        );
    }
}