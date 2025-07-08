package kr.hhplus.be.server.infrastructure.persistence.queue;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import kr.hhplus.be.server.domain.queue.QueueStatus;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.queue.QueueTokenRepository;
import kr.hhplus.be.server.domain.queue.QueueTokenUtil;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RedisQueueTokenRepository implements QueueTokenRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, Object> queueTokenRedisTemplate;

    @Override
    public void save(QueueToken queueToken) {
        String tokenInfoKey = QueueTokenUtil.formattingTokenInfoKey(queueToken.tokenId());
        String tokenIdKey = QueueTokenUtil.formattingTokenIdKey(queueToken.userId(), queueToken.concertId());

        queueTokenRedisTemplate.opsForValue().set(tokenInfoKey, queueToken);
        redisTemplate.opsForValue().set(tokenIdKey, queueToken.tokenId().toString());

        if (queueToken.status().equals(QueueStatus.ACTIVE))
            saveActiveToken(queueToken, tokenInfoKey, tokenIdKey);
        else
            saveWaitingToken(queueToken, tokenInfoKey, tokenIdKey);
    }

    @Override
    public String findTokenIdByUserIdAndConcertId(UUID userId, UUID concertId) {
        String tokenIdKey = QueueTokenUtil.formattingTokenIdKey(userId, concertId);
        Object tokenId = redisTemplate.opsForValue().get(tokenIdKey);
        return tokenId != null ? tokenId.toString() : null;
    }

    @Override
    public QueueToken findQueueTokenByTokenId(String tokenId) {
        String tokenInfoKey = QueueTokenUtil.formattingTokenInfoKey(UUID.fromString(tokenId));
        Object tokenInfo = queueTokenRedisTemplate.opsForValue().get(tokenInfoKey);
        return tokenInfo != null ? (QueueToken) tokenInfo : null;
    }

    @Override
    public Integer findWaitingPosition(QueueToken queueToken) {
        String waitingTokenKey = QueueTokenUtil.formattingWaitingTokenKey(queueToken.concertId());
        String tokenIdKey = QueueTokenUtil.formattingTokenIdKey(queueToken.userId(), queueToken.concertId());

        Long rank = redisTemplate.opsForZSet().rank(waitingTokenKey, tokenIdKey);

        return rank != null ? rank.intValue() + 1 : null;
    }

    @Override
    public Integer countWaitingTokens(UUID concertId) {
        Long count = redisTemplate.opsForZSet().count(QueueTokenUtil.formattingWaitingTokenKey(concertId), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        return count != null ? count.intValue() : 0;
    }

    @Override
    public Integer countActiveTokens(UUID concertId) {
        Object count = redisTemplate.opsForZSet().count(QueueTokenUtil.formattingActiveTokenKey(concertId), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        if (count == null) return 0;
        return Integer.parseInt(count.toString());
    }

    @Override
    public void expiresQueueToken(String tokenId) {
        QueueToken queueToken = findQueueTokenByTokenId(tokenId);
        if (queueToken == null) return;

        String tokenInfoKey = QueueTokenUtil.formattingTokenInfoKey(queueToken.tokenId());
        String tokenIdKey = QueueTokenUtil.formattingTokenIdKey(queueToken.userId(), queueToken.concertId());

        queueTokenRedisTemplate.delete(tokenInfoKey);
        redisTemplate.delete(tokenIdKey);

        if (queueToken.status().equals(QueueStatus.ACTIVE))
            redisTemplate.opsForZSet().remove(QueueTokenUtil.formattingActiveTokenKey(queueToken.concertId()), tokenIdKey);
        else
            redisTemplate.opsForZSet().remove(QueueTokenUtil.formattingWaitingTokenKey(queueToken.concertId()), tokenIdKey);
    }

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
