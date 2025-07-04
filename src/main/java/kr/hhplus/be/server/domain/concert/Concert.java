package kr.hhplus.be.server.domain.concert;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;

@Builder
public record Concert(
        UUID id,
        String title,
        String artist,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
