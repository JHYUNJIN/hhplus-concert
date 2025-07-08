package kr.hhplus.be.server.usecase.queue;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.queue.QueueTokenRepository;
import kr.hhplus.be.server.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueService {

    private static final int MAX_ACTIVE_TOKEN_SIZE = 50; // 동시 접속자 최대 수
    private static final long QUEUE_EXPIRES_TIME = 60L;

    private final QueueTokenRepository queueTokenRepository;
    private final ConcertRepository concertRepository;
    private final UserRepository userRepository;

    // 대기열 토큰 발급
    @Transactional
    public QueueToken issueQueueToken(UUID userId, UUID concertId) throws CustomException {
        // 유저와 콘서트 ID 유효성 검사
        validateUserId(userId);
        validateConcertId(concertId);

        // 이미 발급된 토큰이 있는지 확인
        String findTokenId = queueTokenRepository.findTokenIdByUserIdAndConcertId(userId, concertId);
        if (findTokenId != null)
            return queueTokenRepository.findQueueTokenByTokenId(findTokenId);

        // 콘서트 ID에 대한 활성화된 토큰 수 조회
        Integer activeTokens = queueTokenRepository.countActiveTokens(concertId);
        System.out.println("🚀[로그:정현진] activeTokens : " + activeTokens);
        QueueToken queueToken = createQueueToken(activeTokens, userId, concertId);

        log.debug("대기열 토큰 발급: USER_ID - {}, CONCERT_ID - {}, 상태 - {}", userId, concertId, queueToken.status());
        queueTokenRepository.save(queueToken);
        return queueToken;
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

    private QueueToken createQueueToken(Integer activeTokens, UUID userId, UUID concertId) {
        UUID tokenId = UUID.randomUUID();

        if (activeTokens < MAX_ACTIVE_TOKEN_SIZE)
            return QueueToken.activeTokenOf(tokenId, userId, concertId, QUEUE_EXPIRES_TIME); // 활성 토큰 발급

        Integer waitingTokens = queueTokenRepository.countWaitingTokens(concertId);
        return QueueToken.waitingTokenOf(tokenId, userId, concertId, waitingTokens); // 대기 토큰 발급
    }
}
