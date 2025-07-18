package kr.hhplus.be.server.queue.usecase;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.concert.port.out.ConcertRepository;
import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.queue.port.in.QueueTokenExpirationUseCase;
import kr.hhplus.be.server.queue.port.out.QueueTokenRepository;
import kr.hhplus.be.server.user.port.out.UserRepository;
import kr.hhplus.be.server.queue.adapter.out.persistence.RedisAtomicQueueTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueService implements QueueTokenExpirationUseCase {

    private static final int MAX_ACTIVE_TOKEN_SIZE = 50; // 동시 접속자 최대 수
    private static final long QUEUE_EXPIRES_TIME = 60L;

    private final QueueTokenRepository queueTokenRepository;
    private final ConcertRepository concertRepository;
    private final UserRepository userRepository;
    private final RedisAtomicQueueTokenRepository redisAtomicQueueTokenRepository;

    // 대기열 토큰 발급
    @Transactional
    public QueueToken issueQueueToken(UUID userId, UUID concertId) throws CustomException {
        // 유저와 콘서트 ID 유효성 검사
        validateUserId(userId);
        validateConcertId(concertId);

        // 이미 발급된 토큰이 있는지 확인
        String findTokenId = queueTokenRepository.findTokenIdByUserIdAndConcertId(userId, concertId);
        // 로그 출력
        if (findTokenId != null) {
            log.debug("이미 발급된 토큰이 있습니다: USER_ID - {}, CONCERT_ID - {}, TOKEN_ID - {}", userId, concertId, findTokenId);
            return queueTokenRepository.findQueueTokenByTokenId(findTokenId);
        }

        Integer activeTokenCount = queueTokenRepository.countActiveTokens(concertId);
        // 1. 토큰이 없는 경우, 새 토큰을 생성하고 SETNX 시도
        QueueToken newQueueToken = createQueueToken(activeTokenCount, userId, concertId);

        // 2. Lua 스크립트를 사용하여 토큰 ID 및 정보를 원자적으로 저장 시도, 동시성 문제 해결
        // 새로 발급된 토큰 ID 반환
        // SETNX와 SET (토큰 정보 저장)이 Redis 내부에서 원자적으로 처리됩니다.
        String resultTokenId = redisAtomicQueueTokenRepository.issueTokenAtomic(userId, concertId, newQueueToken);

        // 3. Lua 스크립트로부터 받은 resultTokenId로 실제 QueueToken 객체 조회
        // 이 시점에서는 Redis에 토큰 정보가 확실히 저장되어 있어야 합니다 (Lua 스크립트의 원자성 덕분).
        QueueToken finalQueueToken = queueTokenRepository.findQueueTokenByTokenId(resultTokenId);
        if (finalQueueToken == null) {
            throw new CustomException(ErrorCode.QUEUE_TOKEN_NOT_FOUND, "발급된 토큰 정보를 찾을 수 없습니다.");
        }

        // 4. Redis ZSET을 이용한 큐 토큰 상태 관리 및 저장
        queueTokenRepository.save(finalQueueToken);

        // 5. 최종 반환
        log.debug("최종 발급/조회된 대기열 토큰: USER_ID - {}, CONCERT_ID - {}, TOKEN_ID - {}, 상태 - {}",
                finalQueueToken.userId(), finalQueueToken.concertId(), finalQueueToken.tokenId(), finalQueueToken.status());

        return finalQueueToken;
    }

    public QueueToken getQueueInfo(UUID concertId, String tokenId) throws CustomException {
        validateConcertId(concertId);
        QueueToken queueToken = queueTokenRepository.findQueueTokenByTokenId(tokenId);
        if (queueToken == null || queueToken.isExpired())
            throw new CustomException(ErrorCode.INVALID_QUEUE_TOKEN);

        if (queueToken.isActive())
            return queueToken;

        Integer waitingPosition = queueTokenRepository.findWaitingPosition(queueToken);

        return queueToken.withWaitingPosition(waitingPosition);
    }

    private void validateUserId(UUID userId) throws CustomException {
        if (!userRepository.existsById(userId))
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }

    private void validateConcertId(UUID concertId) throws CustomException {
        if (!concertRepository.existsById(concertId))
            throw new CustomException(ErrorCode.CONCERT_NOT_FOUND);
    }

    private QueueToken createQueueToken(Integer activeTokenCount, UUID userId, UUID concertId) {
        UUID tokenId = UUID.randomUUID();
        if (activeTokenCount < MAX_ACTIVE_TOKEN_SIZE)
            return QueueToken.activeTokenOf(tokenId, userId, concertId, QUEUE_EXPIRES_TIME); // 활성 토큰 발급

        Integer waitingTokenCount = queueTokenRepository.countWaitingTokens(concertId);
        return QueueToken.waitingTokenOf(tokenId, userId, concertId, waitingTokenCount); // 대기 토큰 발급
    }


    @Override
    public void expiresQueueToken(String tokenId) {
        queueTokenRepository.expiresQueueToken(tokenId);
    }
}
