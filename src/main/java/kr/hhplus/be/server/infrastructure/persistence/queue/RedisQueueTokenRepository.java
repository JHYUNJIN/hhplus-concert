package kr.hhplus.be.server.infrastructure.persistence.queue;

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.queue.QueueStatus;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.queue.QueueTokenRepository;
import kr.hhplus.be.server.domain.queue.QueueTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
public class RedisQueueTokenRepository implements QueueTokenRepository { // Redis 기반 대기열 토큰 저장소 구현체

    private final RedisTemplate<String, String> redisTemplate; // String-String 타입 RedisTemplate (토큰 ID 저장용)
    private final RedisTemplate<String, Object> queueTokenRedisTemplate; // String-Object 타입 RedisTemplate (QueueToken 객체 저장용)

    // 생성자를 직접 작성하여 @Qualifier를 사용합니다.
    public RedisQueueTokenRepository(
            @Qualifier("luaScriptRedisTemplate") RedisTemplate<String, String> redisTemplate, // String-String 타입을 위한 템플릿 지정
            @Qualifier("queueTokenRedisTemplate") RedisTemplate<String, Object> queueTokenRedisTemplate) { // Object (QueueToken) 타입을 위한 템플릿 지정
        this.redisTemplate = redisTemplate;
        this.queueTokenRedisTemplate = queueTokenRedisTemplate;
    }

    @Override
    public void save(QueueToken queueToken) {
        String tokenInfoKey = QueueTokenUtil.formattingTokenInfoKey(queueToken.tokenId());
        String tokenIdKey = QueueTokenUtil.formattingTokenIdKey(queueToken.userId(), queueToken.concertId());

        /* redis에 저장된 토큰 정보 예시
        127.0.0.1:6379> keys *
        1) "queue:active:0064f93c-956b-4763-a22b-2eee4b0d7196" // 콘서트 아이디, 콘서트별 활성화된 토큰 수 조회 가능
        2) "token:info:cc93b32f-aaa6-4821-94cc-c88e4030c327"
        3) "token:id:47ead46e-5cc4-44e2-9eda-a7aebecff179:0064f93c-956b-4763-a22b-2eee4b0d7196" // 유저 ID와 콘서트 ID로 토큰 ID 저장
         */

        // Lua 스크립트에서 처리했기때문에, 아래 코드는 주석 처리함
//        redisTemplate.opsForValue().set(tokenIdKey, queueToken.tokenId().toString());
//        queueTokenRedisTemplate.opsForValue().set(tokenInfoKey, queueToken);

        if (queueToken.status().equals(QueueStatus.ACTIVE))
            saveActiveToken(queueToken, tokenInfoKey, tokenIdKey);
        else
            saveWaitingToken(queueToken, tokenInfoKey, tokenIdKey);
    }

    @Override
    public String findTokenIdByUserIdAndConcertId(UUID userId, UUID concertId) {
        String tokenIdKey = QueueTokenUtil.formattingTokenIdKey(userId, concertId);
        // String 값을 다루므로, String-String 타입인 redisTemplate 사용
        Object tokenId = redisTemplate.opsForValue().get(tokenIdKey);
        return tokenId != null ? tokenId.toString() : null;
    }

    @Override
    public QueueToken findQueueTokenByTokenId(String tokenId) {
        String tokenInfoKey = QueueTokenUtil.formattingTokenInfoKey(UUID.fromString(tokenId));
        // QueueToken 객체를 다루므로, String-Object 타입인 queueTokenRedisTemplate 사용
        Object tokenInfo = queueTokenRedisTemplate.opsForValue().get(tokenInfoKey);
        return tokenInfo != null ? (QueueToken) tokenInfo : null;
    }

    // 대기열 토큰 대기 순번 조회
    @Override
    public Integer findWaitingPosition(QueueToken queueToken) {
        String waitingTokenKey = QueueTokenUtil.formattingWaitingTokenKey(queueToken.concertId());
        String tokenIdKey = QueueTokenUtil.formattingTokenIdKey(queueToken.userId(), queueToken.concertId());

        // ZSet의 멤버는 String이므로, String-String 타입인 redisTemplate 사용
        Long rank = redisTemplate.opsForZSet().rank(waitingTokenKey, tokenIdKey);

        return rank != null ? rank.intValue() + 1 : null;
    }

    // 대기열 토큰 수 조회
    @Override
    public Integer countWaitingTokens(UUID concertId) {
        // ZSet의 멤버는 String이므로, String-String 타입인 redisTemplate 사용
        Long count = redisTemplate.opsForZSet().count(QueueTokenUtil.formattingWaitingTokenKey(concertId), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        return count != null ? count.intValue() : 0;
    }

    // 활성화된 토큰 수 조회
    @Override
    public Integer countActiveTokens(UUID concertId) {
        // ZSet의 멤버는 String이므로, String-String 타입인 redisTemplate 사용
        Long count = redisTemplate.opsForZSet().count(QueueTokenUtil.formattingActiveTokenKey(concertId), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        if (count == null) return 0;
        return count.intValue(); // Long을 Integer로 변환
    }

    // 토큰 만료 처리
    @Override
    public void expiresQueueToken(String tokenId) {
        QueueToken queueToken = findQueueTokenByTokenId(tokenId);
        if (queueToken == null) return;

        String tokenInfoKey = QueueTokenUtil.formattingTokenInfoKey(queueToken.tokenId());
        String tokenIdKey = QueueTokenUtil.formattingTokenIdKey(queueToken.userId(), queueToken.concertId());

        queueTokenRedisTemplate.delete(tokenInfoKey); // QueueToken 객체 키 삭제
        redisTemplate.delete(tokenIdKey); // String UUID 키 삭제

        if (queueToken.status().equals(QueueStatus.ACTIVE))
            redisTemplate.opsForZSet().remove(QueueTokenUtil.formattingActiveTokenKey(queueToken.concertId()), tokenIdKey);
        else
            redisTemplate.opsForZSet().remove(QueueTokenUtil.formattingWaitingTokenKey(queueToken.concertId()), tokenIdKey);
    }

    // 대기 토큰을 활성 상태로 승격
    @Override
    public void promoteQueueToken(List<Concert> openConcerts) {
        System.out.println("🚀[로그:정현진] promoteQueueToken");
        for (Concert openConcert : openConcerts) {
            String activeTokenKey = QueueTokenUtil.formattingActiveTokenKey(openConcert.id());
            String waitingTokenKey = QueueTokenUtil.formattingWaitingTokenKey(openConcert.id());

            List<String> keys = List.of(activeTokenKey, waitingTokenKey);
            System.out.println("🚀[로그:정현진] keys : " + keys);

            Long promotedCount = redisTemplate.execute(QueueTokenUtil.promoteWaitingTokenScript(), keys, String.valueOf(2));

            // 저장된 값을 로그로 출력합니다.
            if (promotedCount != null && promotedCount > 0) {
                log.info("✅ [승격 완료] 콘서트 ID {}: {}개의 토큰이 활성 상태로 전환되었습니다.", openConcert.id(), promotedCount);
            } else {
                log.info("❌ [승격 실패] 콘서트 ID {}: 활성 상태로 전환된 토큰이 없습니다.", openConcert.id());
            }
        }
    }

    // 대기 토큰 Redis 저장
    private void saveWaitingToken(QueueToken queueToken, String tokenInfoKey, String tokenIdKey) {
        String waitingTokenKey = QueueTokenUtil.formattingWaitingTokenKey(queueToken.concertId());
        Instant issuedInstant = queueToken.issuedAt()
                .atZone(ZoneOffset.UTC)
                .toInstant();
        double score = issuedInstant.getEpochSecond();
        redisTemplate.opsForZSet().add(waitingTokenKey, tokenIdKey, score);

        redisTemplate.expire(tokenInfoKey, Duration.ofHours(24));
        redisTemplate.expire(tokenIdKey, Duration.ofHours(24));
    }

    // 활성 토큰 Redis 저장
    private void saveActiveToken(QueueToken queueToken, String tokenInfoKey, String tokenIdKey) {
        String activeTokenKey = QueueTokenUtil.formattingActiveTokenKey(queueToken.concertId());
        Instant expiresInstant = queueToken.expiresAt()
                .atZone(ZoneOffset.UTC)
                .toInstant();
        double score = expiresInstant.getEpochSecond();
        redisTemplate.opsForZSet().add(activeTokenKey, tokenIdKey, score);

        redisTemplate.expireAt(tokenInfoKey, expiresInstant);
        redisTemplate.expireAt(tokenIdKey, expiresInstant);
    }
}