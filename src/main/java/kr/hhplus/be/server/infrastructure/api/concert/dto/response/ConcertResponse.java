package kr.hhplus.be.server.infrastructure.api.concert.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ConcertResponse(

    // 콘서트에 대한 정보
    // 예: 콘서트 ID, 제목, 아티스트, 생성일, 수정일
    @Schema(description = "콘서트 ID") UUID concertId,
    @Schema(description = "콘서트 제목") String title,
    @Schema(description = "아티스트 이름") String artist,
    @Schema(description = "콘서트 생성일") LocalDateTime createdAt,
    @Schema(description = "콘서트 수정일") LocalDateTime updatedAt) {

    public static ConcertResponse from(kr.hhplus.be.server.domain.concert.Concert concert) {
        return ConcertResponse.builder()
                .concertId(concert.id())
                .title(concert.title())
                .artist(concert.artist())
                .createdAt(concert.createdAt())
                .updatedAt(concert.updatedAt())
                .build();
    }
}
