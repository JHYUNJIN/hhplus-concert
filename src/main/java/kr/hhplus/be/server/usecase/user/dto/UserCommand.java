package kr.hhplus.be.server.usecase.user.dto;

import lombok.*;

import java.util.UUID;

@Getter
@ToString
@RequiredArgsConstructor
public class UserCommand {
    private final UUID userId;

}