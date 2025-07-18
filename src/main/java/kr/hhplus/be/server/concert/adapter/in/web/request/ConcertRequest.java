package kr.hhplus.be.server.concert.adapter.in.web.request;

import kr.hhplus.be.server.concert.domain.Concert;
import lombok.Builder;

@Builder
public record ConcertRequest(
    String title,
    String artist
) {
    public static ConcertRequest from(Concert concert) {
        return ConcertRequest.builder()
                .title(concert.title())
                .artist(concert.artist())
                .build();
    }
}
