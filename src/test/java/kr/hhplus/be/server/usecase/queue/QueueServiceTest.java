package kr.hhplus.be.server.usecase.queue;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.queue.QueueStatus;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.queue.QueueTokenRepository;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;

@ExtendWith(MockitoExtension.class)
public class QueueServiceTest {

    @InjectMocks
    private QueueService queueService;

    @Mock
    private QueueTokenRepository queueTokenRepository;

    @Mock
    private ConcertRepository concertRepository;

    @Mock
    private UserRepository userRepository;

    // 테스트에 사용할 유저와 콘서트 정보
    private UUID userId;
    private UUID concertId;
    private UUID tokenId;
    private QueueToken existingToken; // 이미 발급된 토큰 정보

    // 테스트에 사용할 대기열 토큰 정보 초기화
    @BeforeEach
    void beforeEach() {
        userId = UUID.randomUUID();
        concertId = UUID.randomUUID();
        tokenId = UUID.randomUUID();

        existingToken = QueueToken.builder()
                .tokenId(tokenId)
                .userId(userId)
                .concertId(concertId)
                .position(0)
                .issuedAt(LocalDateTime.now().minusMinutes(10))
                .expiresAt(LocalDateTime.now().plusMinutes(25))
                .enteredAt(LocalDateTime.now().minusMinutes(5))
                .status(QueueStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("콘서트_대기열_토큰_발급_성공(대기상태)") // 최대 활성 토큰 수 50개
    void issueQueueToken_Success_Waiting() throws CustomException {
        // Integer는 null 값을 가질 수 있어 대기 중인 토큰 수가 없거나, 값이 없음을 표현해야되기에 Integer를 사용
        Integer waitingCount = 10; // 현재 대기중인 토큰 수

        when(userRepository.existsById(userId)).thenReturn(true);
        when(concertRepository.existsById(concertId)).thenReturn(true);
        when(queueTokenRepository.findTokenIdByUserIdAndConcertId(userId, concertId)).thenReturn(null); // 토큰이 없는 경우
        when(queueTokenRepository.countActiveTokens(concertId)).thenReturn(50);
        when(queueTokenRepository.countWaitingTokens(concertId)).thenReturn(waitingCount);

        QueueToken queueToken = queueService.issueQueueToken(userId, concertId); // 대기열 토큰 발급

        verify(userRepository, times(1)).existsById(userId);
        verify(concertRepository, times(1)).existsById(concertId);
        verify(queueTokenRepository, times(1)).findTokenIdByUserIdAndConcertId(userId, concertId);
        verify(queueTokenRepository, times(1)).countActiveTokens(concertId);
        verify(queueTokenRepository, times(1)).countWaitingTokens(concertId);
        verify(queueTokenRepository, times(1)).save(any(QueueToken.class)); // redis에 토큰 저장

        verify(queueTokenRepository, never()).findQueueTokenByTokenId(tokenId.toString()); // 이미 발급되어 있던 토큰이 없으므로 호출되지 않아야 함

        assertThat(queueToken.status()).isEqualTo(QueueStatus.WAITING); // 토큰상태가 대기 중인지 확인
        assertThat(queueToken.position()).isEqualTo(waitingCount + 1); // 대기 중인 토큰 수 + 1이 현재 순서
        assertThat(queueToken.issuedAt()).isNotNull();
        assertThat(queueToken.expiresAt()).isNull();
        assertThat(queueToken.enteredAt()).isNull();
    }

    @Test
    @DisplayName("콘서트_대기열_토큰_발급_성공(활성상태)") // 최대 활성 토큰 수 50개
    void issueQueueToken_Success_Active() throws CustomException {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(concertRepository.existsById(concertId)).thenReturn(true);
        when(queueTokenRepository.findTokenIdByUserIdAndConcertId(userId, concertId)).thenReturn(null);
        when(queueTokenRepository.countActiveTokens(concertId)).thenReturn(30);

        QueueToken queueToken = queueService.issueQueueToken(userId, concertId);
        // 대기중인 토큰이 없으므로 토큰 발급 시 활성 상태로 발급됨

        verify(userRepository, times(1)).existsById(userId);
        verify(concertRepository, times(1)).existsById(concertId);
        verify(queueTokenRepository, times(1)).findTokenIdByUserIdAndConcertId(userId, concertId);
        verify(queueTokenRepository, times(1)).countActiveTokens(concertId);
        verify(queueTokenRepository, times(1)).save(any(QueueToken.class));

        verify(queueTokenRepository, never()).findQueueTokenByTokenId(tokenId.toString());

        assertThat(queueToken.status()).isEqualTo(QueueStatus.ACTIVE);
        assertThat(queueToken.issuedAt()).isNotNull();
        assertThat(queueToken.expiresAt()).isNotNull();
        assertThat(queueToken.enteredAt()).isNotNull();
    }

    @Test
    @DisplayName("콘서트_대기열_토큰_발급_성공(기존_토큰_존재)")
    void issueQueueToken_Success_existsToken() throws CustomException {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(concertRepository.existsById(concertId)).thenReturn(true);
        when(queueTokenRepository.findTokenIdByUserIdAndConcertId(userId, concertId)).thenReturn(tokenId.toString()); // 이미 발급된 토큰이 존재하는 경우
        when(queueTokenRepository.findQueueTokenByTokenId(tokenId.toString())).thenReturn(existingToken); // 기존 토큰 정보 조회

        QueueToken queueToken = queueService.issueQueueToken(userId, concertId);

        verify(userRepository, times(1)).existsById(userId);
        verify(concertRepository, times(1)).existsById(concertId);
        verify(queueTokenRepository, times(1)).findTokenIdByUserIdAndConcertId(userId, concertId);
        verify(queueTokenRepository, times(1)).findQueueTokenByTokenId(tokenId.toString());

        verify(queueTokenRepository, never()).countActiveTokens(concertId); // 이미 발급된 토큰이 존재하므로 활성 토큰 수 조회는 호출되지 않아야 함
        verify(queueTokenRepository, never()).save(any(QueueToken.class)); // redis에 이미 저장된 토큰이 있으므로 저장하지 않아야 함

        assertThat(queueToken.tokenId()).isEqualTo(tokenId);
        assertThat(queueToken.status()).isEqualTo(QueueStatus.ACTIVE);
        assertThat(queueToken.issuedAt()).isNotNull();
        assertThat(queueToken.expiresAt()).isNotNull();
        assertThat(queueToken.enteredAt()).isNotNull();
    }

    @Test
    @DisplayName("콘서트_대기열_토큰_발급_실패_유저못찾음")
    void issueQueueToken_Failure_UserNotFound() {
        /*
        실제 서비스 환경에서 잘못된 userId가 들어올 수 있는 경우, 아래 상황에 대비해 예외 처리가 필요함
        클라이언트(앱, 웹 등)에서 버그로 잘못된 UUID를 전송하는 경우
        인증 토큰이 변조되거나 만료되어 잘못된 userId가 추출된 경우
        악의적인 사용자가 임의의 userId로 요청을 시도하는 경우(보안 공격)
        테스트나 개발 환경에서 잘못된 데이터가 유입된 경우
        DB에서 유저가 삭제되었지만, 캐시나 세션에 남아있는 경우
         */
        when(userRepository.existsById(userId)).thenReturn(false);
        // 유저 존재만 확인하고 이후 메소드는 모두 실행되면 안된다.

        CustomException customException = assertThrows(CustomException.class,
                () -> queueService.issueQueueToken(userId, concertId));

        verify(userRepository, times(1)).existsById(userId);
        verify(concertRepository, never()).existsById(concertId);
        verify(queueTokenRepository, never()).findTokenIdByUserIdAndConcertId(userId, concertId);
        verify(queueTokenRepository, never()).findQueueTokenByTokenId(tokenId.toString());
        verify(queueTokenRepository, never()).countActiveTokens(concertId);
        verify(queueTokenRepository, never()).save(any(QueueToken.class));

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }


    @Test
    @DisplayName("콘서트_대기열_토큰_발급_실패_콘서트못찾음")
    void issueQueueToken_Failure_ConcertNotFound() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(concertRepository.existsById(concertId)).thenReturn(false);

        CustomException customException = assertThrows(CustomException.class,
                () -> queueService.issueQueueToken(userId, concertId));

        verify(userRepository, times(1)).existsById(userId);
        verify(concertRepository, times(1)).existsById(concertId);
        verify(queueTokenRepository, never()).findTokenIdByUserIdAndConcertId(userId, concertId);
        verify(queueTokenRepository, never()).findQueueTokenByTokenId(tokenId.toString());
        verify(queueTokenRepository, never()).countActiveTokens(concertId);
        verify(queueTokenRepository, never()).save(any(QueueToken.class));

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.CONCERT_NOT_FOUND);
    }





}
