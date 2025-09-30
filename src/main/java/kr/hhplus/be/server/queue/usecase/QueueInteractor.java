package kr.hhplus.be.server.queue.usecase;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.concert.port.out.ConcertRepository;
import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.queue.port.in.GetQueueInfoUseCase;
import kr.hhplus.be.server.queue.port.in.IssueTokenUseCase;
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
public class QueueInteractor implements IssueTokenUseCase, GetQueueInfoUseCase, QueueTokenExpirationUseCase {

    private static final int MAX_ACTIVE_TOKEN_SIZE = 50;
    private static final long QUEUE_EXPIRES_TIME = 60L;

    private final QueueTokenRepository queueTokenRepository;
    private final ConcertRepository concertRepository;
    private final UserRepository userRepository;
    private final RedisAtomicQueueTokenRepository redisAtomicQueueTokenRepository;

    @Override
    @Transactional
    public QueueToken issueQueueToken(UUID userId, UUID concertId) {
        validateUserId(userId);
        validateConcertId(concertId);

        String findTokenId = queueTokenRepository.findTokenIdByUserIdAndConcertId(userId, concertId);
        if (findTokenId != null) {
            log.debug("이미 발급된 토큰이 있습니다: USER_ID - {}, CONCERT_ID - {}, TOKEN_ID - {}", userId, concertId, findTokenId);
            return queueTokenRepository.findQueueTokenByTokenId(findTokenId);
        }

        Integer activeTokenCount = queueTokenRepository.countActiveTokens(concertId);
        QueueToken newQueueToken = createQueueToken(activeTokenCount, userId, concertId);
        String resultTokenId = redisAtomicQueueTokenRepository.issueTokenAtomic(userId, concertId, newQueueToken);
        QueueToken finalQueueToken = queueTokenRepository.findQueueTokenByTokenId(resultTokenId);

        if (finalQueueToken == null) {
            throw new CustomException(ErrorCode.QUEUE_TOKEN_NOT_FOUND, "발급된 토큰 정보를 찾을 수 없습니다.");
        }

        queueTokenRepository.save(finalQueueToken);
        log.debug("최종 발급/조회된 대기열 토큰: {}", finalQueueToken);
        return finalQueueToken;
    }

    @Override
    public QueueToken getQueueInfo(UUID concertId, String tokenId) {
        validateConcertId(concertId);
        QueueToken queueToken = queueTokenRepository.findQueueTokenByTokenId(tokenId);
        if (queueToken == null || queueToken.isExpired()) {
            throw new CustomException(ErrorCode.INVALID_QUEUE_TOKEN);
        }
        if (queueToken.isActive()) {
            return queueToken;
        }
        Integer waitingPosition = queueTokenRepository.findWaitingPosition(queueToken);
        return queueToken.withWaitingPosition(waitingPosition);
    }

    @Override
    public void expiresQueueToken(String tokenId) {
        queueTokenRepository.expiresQueueToken(tokenId);
    }

    private void validateUserId(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }

    private void validateConcertId(UUID concertId) {
        if (!concertRepository.existsById(concertId)) {
            throw new CustomException(ErrorCode.CONCERT_NOT_FOUND);
        }
    }

    private QueueToken createQueueToken(Integer activeTokenCount, UUID userId, UUID concertId) {
        final long WAITING_QUEUE_EXPIRES_TIME = 10 * 60L; // 10분(초 단위)

        UUID tokenId = UUID.randomUUID();
        if (activeTokenCount < MAX_ACTIVE_TOKEN_SIZE) {
            return QueueToken.activeTokenOf(tokenId, userId, concertId, QUEUE_EXPIRES_TIME);
        }
        Integer waitingTokenCount = queueTokenRepository.countWaitingTokens(concertId);
        return QueueToken.waitingTokenOf(tokenId, userId, concertId, waitingTokenCount, WAITING_QUEUE_EXPIRES_TIME);
    }
}