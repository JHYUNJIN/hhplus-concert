package kr.hhplus.be.server.infrastructure.web.concert.dto;

import kr.hhplus.be.server.domain.concert.Concert;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConcertWebResponse {
    private String concertId;
    private String title;
    private String artist;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ConcertWebResponse from(Concert concert) {
        return new ConcertWebResponse(concert.getId(), concert.getTitle(), concert.getArtist(), concert.getCreatedAt(), concert.getUpdatedAt());
    }
}