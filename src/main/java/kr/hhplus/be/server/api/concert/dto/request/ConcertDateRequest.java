package kr.hhplus.be.server.api.concert.dto.request;

import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import lombok.Builder;

@Builder
public record ConcertDateRequest(
    String date,
    String deadline
) {
    public static ConcertDateRequest from(ConcertDate concertDate) {
        return ConcertDateRequest.builder()
                .date(concertDate.date().toString())
                .deadline(concertDate.deadline().toString())
                .build();
    }
}
