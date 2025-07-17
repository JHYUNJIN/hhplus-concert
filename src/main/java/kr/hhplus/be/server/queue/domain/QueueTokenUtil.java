package kr.hhplus.be.server.queue.domain;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.script.DefaultRedisScript;

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

    public static DefaultRedisScript<Long> promoteWaitingTokenScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(PROMOTE_WAITING_TOKEN_SCRIPT);

        script.setResultType(Long.class);

        return script;
    }

    public static final String PROMOTE_WAITING_TOKEN_SCRIPT = """
          local activeTokenKey = KEYS[1]
          local waitingTokenKey = KEYS[2]
          local maxActiveTokenSize = tonumber(ARGV[1])

          -- 활성 토큰 개수 조회 (SCARD -> ZCARD 로 변경)
          local activeCount = redis.call('ZCARD', activeTokenKey)
          local leftActiveCount = maxActiveTokenSize - activeCount

          if leftActiveCount <= 0 then
              return 0
          end

          -- 남은 활성 토큰 개수 만큼 대기 토큰 조회
          local waitingTokens = redis.call('ZRANGE', waitingTokenKey, 0, leftActiveCount - 1)
          if #waitingTokens == 0 then
              return 0
          end
          
          -- 현재 시간을 점수(score)로 사용하기 위해 조회
          local currentTime = redis.call('TIME')
          local score = currentTime[1]

          -- 대기 토큰 활성 토큰으로 승급
          for i, tokenId in ipairs(waitingTokens) do
              -- SADD -> ZADD 로 변경하고, score를 함께 전달
              redis.call('ZADD', activeTokenKey, score, tokenId)
              redis.call('ZREM', waitingTokenKey, tokenId)
          end
          return #waitingTokens
          """;
}
