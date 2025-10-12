package kr.hhplus.be.server.user.port.in.dto;

import lombok.*;

import java.util.UUID;

@Getter
@ToString
@RequiredArgsConstructor
public class UserCommand {
    private final UUID userId;

}