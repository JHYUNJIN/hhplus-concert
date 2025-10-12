package kr.hhplus.be.server.queue.adapter.out.persistence;

import java.util.UUID;

import kr.hhplus.be.server.external.UserApiClient;
import kr.hhplus.be.server.queue.port.out.QueueTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.concert.port.out.ConcertRepository;
import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueueTokenManager {

    private static final int MAX_ACTIVE_TOKEN_SIZE = 50;
    private static final long QUEUE_EXPIRES_TIME = 10L * 60; // 10분
    private static final long WAITING_TOKEN_EXPIRES_TIME = 10L * 60; // 10분

    private final QueueTokenRepository queueTokenRepository;
    private final ConcertRepository concertRepository;
    private final UserApiClient userApiClient;

    @Transactional
    public QueueToken processIssueQueueToken(UUID userId, UUID concertId) throws CustomException {
        validateUserId(userId);
        validateConcertId(concertId);

        String findTokenId = queueTokenRepository.findTokenIdByUserIdAndConcertId(userId, concertId);
        if (findTokenId != null)
            return getQueueToken(findTokenId);

        Integer activeTokens = queueTokenRepository.countActiveTokens(concertId);
        QueueToken queueToken = createQueueToken(activeTokens, userId, concertId);

        log.debug("대기열 토큰 발급: USER_ID - {}, CONCERT_ID - {}, 상태 - {}", userId, concertId, queueToken.status());
        queueTokenRepository.save(queueToken);
        return queueToken;
    }

    public QueueToken getQueueInfo(UUID concertId, String tokenId) throws CustomException {
        validateConcertId(concertId);

        QueueToken queueToken = getQueueToken(tokenId);
        if (queueToken == null || queueToken.isExpired())
            throw new CustomException(ErrorCode.INVALID_QUEUE_TOKEN);

        if (queueToken.isActive())
            return queueToken;

        Integer waitingPosition = queueTokenRepository.findWaitingPosition(queueToken);

        return queueToken.withWaitingPosition(waitingPosition);
    }

    public QueueToken getQueueToken(String tokenId) {
        return queueTokenRepository.findQueueTokenByTokenId(tokenId);
    }

    private void validateUserId(UUID userId) throws CustomException {
        if (Boolean.FALSE.equals(userApiClient.checkUserExists(userId).block()))
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }

    private void validateConcertId(UUID concertId) throws CustomException {
        if (!concertRepository.existsById(concertId))
            throw new CustomException(ErrorCode.CONCERT_NOT_FOUND);
    }

    private QueueToken createQueueToken(Integer activeTokens, UUID userId, UUID concertId) {

        UUID tokenId = UUID.randomUUID();

        if (activeTokens < MAX_ACTIVE_TOKEN_SIZE)
            return QueueToken.activeTokenOf(tokenId, userId, concertId, QUEUE_EXPIRES_TIME);

        Integer waitingTokens = queueTokenRepository.countWaitingTokens(concertId);
        return QueueToken.waitingTokenOf(tokenId, userId, concertId, waitingTokens, WAITING_TOKEN_EXPIRES_TIME);
    }
}
