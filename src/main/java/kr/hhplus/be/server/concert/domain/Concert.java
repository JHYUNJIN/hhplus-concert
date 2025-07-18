package kr.hhplus.be.server.concert.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;

@Builder
public record Concert(
        UUID id,
        String title,
        String artist,
        LocalDateTime openTime,
        LocalDateTime soldOutTime,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public boolean isOpen() {
        return openTime.isAfter(LocalDateTime.now());
    }

    public Concert soldOut(LocalDateTime soldOutTime) {
        return Concert.builder()
                .id(id)
                .title(title)
                .artist(artist)
                .openTime(openTime)
                .soldOutTime(soldOutTime)
                .createdAt(createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
