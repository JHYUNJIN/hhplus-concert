package kr.hhplus.be.server.infrastructure.api.concert.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ConcertDateResponse(
        @Schema(description = "콘서트 ID") UUID concertId,
        @Schema(description = "콘서트 날짜 ID") UUID concertDateId,
        @Schema(description = "공연 일시") LocalDateTime date,
        @Schema(description = "예약 마감 일시") LocalDateTime deadline,
        @Schema(description = "잔여 좌석 수") Integer remainingSeatCount) {

    public static ConcertDateResponse from(ConcertDate concertDate) {
        return ConcertDateResponse.builder()
                .concertId(concertDate.concertId())
                .concertDateId(concertDate.id())
                .date(concertDate.date())
                .deadline(concertDate.deadline())
                .remainingSeatCount(concertDate.remainingSeatCount())
                .build();
    }
}
