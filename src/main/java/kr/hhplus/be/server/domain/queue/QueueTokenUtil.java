package kr.hhplus.be.server.domain.queue;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

// 토큰의 유효성을 검증하는 정적 메서드와 Redis 키 포맷팅을 위한 상수들을 정의합니다.
@NoArgsConstructor(access = AccessLevel.PRIVATE) // private 생성자를 사용하여 인스턴스 생성을 방지합니다.
public final class QueueTokenUtil {

    // Redis에서 사용하는 키 포맷을 정의합니다.
    private static final String ACTIVE_TOKEN_KEY = "queue:active:%s";
    private static final String WAITING_TOKEN_KEY = "queue:waiting:%s";
    private static final String TOKEN_INFO_KEY = "token:info:%s";
    private static final String TOKEN_ID_KEY = "token:id:%s:%s";

    // QueueToken이 유효한지 검증
    public static void validateActiveQueueToken(QueueToken queueToken) throws CustomException {
        if (queueToken == null || !queueToken.isActive())
            throw new CustomException(ErrorCode.INVALID_QUEUE_TOKEN);
    }


    public static String formattingTokenIdKey(UUID userId, UUID concertId) {
        return String.format(TOKEN_ID_KEY, userId, concertId);
    }


    public static String formattingTokenInfoKey(UUID tokenId) {
        return String.format(TOKEN_INFO_KEY, tokenId);
    }

    public static String formattingActiveTokenKey(UUID concertId) {
        return String.format(ACTIVE_TOKEN_KEY, concertId);
    }

    public static String formattingWaitingTokenKey(UUID concertId) {
        return String.format(WAITING_TOKEN_KEY, concertId);
    }
}
