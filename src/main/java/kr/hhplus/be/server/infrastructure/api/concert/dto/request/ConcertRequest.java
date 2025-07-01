package kr.hhplus.be.server.infrastructure.api.concert.dto.request;

import lombok.Builder;

@Builder
public record ConcertRequest(
    String title,
    String artist
) {
    public static ConcertRequest from(kr.hhplus.be.server.domain.concert.Concert concert) {
        return ConcertRequest.builder()
                .title(concert.title())
                .artist(concert.artist())
                .build();
    }
}
