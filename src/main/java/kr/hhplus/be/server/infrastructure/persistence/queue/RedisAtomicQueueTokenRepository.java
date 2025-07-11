package kr.hhplus.be.server.infrastructure.persistence.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.queue.QueueTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
public class RedisAtomicQueueTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapperForLua;
    private final DefaultRedisScript<String> issueQueueTokenAtomicScript;

    // 생성자 주입
    // Qualifier를 사용하여 RedisTemplate과 ObjectMapper를 주입
    public RedisAtomicQueueTokenRepository(
            @Qualifier("luaScriptRedisTemplate") RedisTemplate<String, String> redisTemplate,
            @Qualifier("objectMapperForLua") ObjectMapper objectMapperForLua,
            DefaultRedisScript<String> issueQueueTokenAtomicScript) {
        this.redisTemplate = redisTemplate;
        this.objectMapperForLua = objectMapperForLua;
        this.issueQueueTokenAtomicScript = issueQueueTokenAtomicScript;
    }


    /**
     * 유저 ID와 콘서트 ID에 대해 토큰을 원자적으로 발급하거나 기존 토큰 ID를 반환합니다.
     * 이 메서드는 Redis Lua 스크립트를 사용하여 SETNX (tokenIdKey)와 SET (tokenInfoKey) 작업을
     * 단일 원자적 연산으로 처리하여 경쟁 조건을 방지합니다.
     *
     * @param userId         유저 ID
     * @param concertId      콘서트 ID
     * @param newQueueToken  새로 생성된 QueueToken 객체
     * @return 발급 성공 시 새로 생성된 토큰 ID (String) 또는 이미 존재하는 토큰의 ID (String)
     */
    public String issueTokenAtomic(UUID userId, UUID concertId, QueueToken newQueueToken) {
        String tokenIdKey = QueueTokenUtil.formattingTokenIdKey(userId, concertId);
        System.out.println("🚀[로그:정현진] tokenIdKey : " + tokenIdKey);
        String tokenInfoKey = QueueTokenUtil.formattingTokenInfoKey(newQueueToken.tokenId());
        System.out.println("🚀[로그:정현진] tokenInfoKey : " + tokenInfoKey);

        String serializedQueueTokenJson;
        try {
            // ObjectMapperForLua를 사용하여 QueueToken 객체를 JSON 문자열로 직렬화
            // 이 ObjectMapper는 Redis ValueSerializer와 독립적으로, Lua 스크립트에 전달될 JSON 문자열을 생성합니다.
            serializedQueueTokenJson = objectMapperForLua.writeValueAsString(newQueueToken);
        } catch (JsonProcessingException e) {
            log.error("QueueToken 객체를 JSON으로 직렬화하는데 실패했습니다: {}", newQueueToken, e);
            throw new CustomException(ErrorCode.QUEUE_TOKEN_SERIALIZATION_ERROR);
        }

        // 토큰 만료 시간 계산 (초 단위)
        // newQueueToken.expiresAt()이 LocalDateTime 이므로, Instant로 변환 후 초 계산
        long expirationSeconds = Duration.between(Instant.now(), newQueueToken.expiresAt().atZone(ZoneOffset.UTC).toInstant()).getSeconds();
        if (expirationSeconds <= 0) {
            expirationSeconds = 1; // 최소 1초 TTL 보장
        }

        // KEYS 인자: Redis 키 목록
        List<String> keys = Arrays.asList(tokenIdKey, tokenInfoKey);

        // ARGV 인자: Lua 스크립트에 전달될 값 목록
        List<String> args = Arrays.asList(
                newQueueToken.tokenId().toString(), // ARGV[1]
                serializedQueueTokenJson,           // ARGV[2]
                String.valueOf(expirationSeconds),  // ARGV[3]
                String.valueOf(expirationSeconds)   // ARGV[4]
        );

        // Lua 스크립트 실행
        String resultTokenId = redisTemplate.execute(issueQueueTokenAtomicScript, keys, args.toArray());
        log.debug("Lua script executed for userId={}, concertId={}, resultTokenId={}", userId, concertId, resultTokenId);
        return resultTokenId;
    }
}