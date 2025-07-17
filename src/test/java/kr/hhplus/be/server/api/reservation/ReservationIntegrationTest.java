package kr.hhplus.be.server.api.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.api.TestDataFactory;
import kr.hhplus.be.server.api.reservation.dto.request.ReservationRequest;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import kr.hhplus.be.server.domain.concertDate.ConcertDateRepository;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.queue.QueueTokenRepository;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatHoldRepository;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.infrastructure.persistence.queue.RedisAtomicQueueTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class ReservationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // RedisTemplate<String, Object>는 Testcontainers의 Redis에 연결됩니다.
    @Autowired
    private RedisTemplate<String, Object> redisTemplate; // 이 템플릿은 flushAll 용도로 사용

    @Autowired
    private QueueTokenRepository queueTokenRepository;

    @Autowired
    private SeatHoldRepository seatHoldRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertDateRepository concertDateRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private RedisAtomicQueueTokenRepository redisAtomicQueueTokenRepository; // RedisAtomicQueueTokenRepository 주입

    private UUID concertId;
    private UUID concertDateId;
    private UUID seatId;
    private UUID userId;
    private UUID activeTokenId;

    private ReservationRequest request;

    @BeforeEach
    void beforeEach() {
        // 매 테스트 시작 전 Redis 데이터 초기화
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // H2 DB에 테스트 데이터 저장
        Concert concert = TestDataFactory.createConcert();
        Concert savedConcert = concertRepository.save(concert);
        concertId = savedConcert.id();

        ConcertDate concertDate = TestDataFactory.createConcertDate(concertId);
        ConcertDate savedConcertDate = concertDateRepository.save(concertDate);
        concertDateId = savedConcertDate.id();

        Seat seat = TestDataFactory.createSeat(concertDateId);
        Seat savedSeat = seatRepository.save(seat);
        seatId = savedSeat.id(); // 이 seatId가 MockMvc 요청 및 응답 검증에 사용될 실제 좌석 ID

        User user = TestDataFactory.createUser();
        User savedUser = userRepository.save(user);
        userId = savedUser.id();

        // 큐 토큰 발급
        QueueToken queueToken = TestDataFactory.createQueueToken(userId, concertId);
        String issuedTokenIdString = redisAtomicQueueTokenRepository.issueTokenAtomic(userId, concertId, queueToken);
        activeTokenId = UUID.fromString(issuedTokenIdString);


        // MockMvc 요청을 위한 ReservationRequest 객체 생성
        request = new ReservationRequest(concertId, concertDateId);
    }

    @Test
    @DisplayName("좌석_예약_성공")
    void reserveSeat_Success() throws Exception {
        mockMvc.perform(post("/api/v1/reservations/seats/{seatId}", seatId) // PathVariable로 seatId 전달
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", activeTokenId.toString()) // Authorization 헤더에 토큰 ID 문자열로 전달
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").exists())
                .andExpect(jsonPath("$.seatId").value(seatId.toString())) // DB에 저장된 seatId와 응답의 seatId가 일치하는지 검증
                .andExpect(jsonPath("$.seatNo").value(1))
                .andExpect(jsonPath("$.price").value(50000))
                .andExpect(jsonPath("$.status").value(ReservationStatus.PENDING.toString()))
                .andExpect(jsonPath("$.createdAt").exists());

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        // DB에서 좌석 상태 확인
        Seat seat = seatRepository.findById(seatId).orElseThrow(); // orElseThrow() 추가
        assertThat(seat.status()).isEqualTo(SeatStatus.RESERVED);

        // Redis에서 좌석 홀드 상태 확인
        boolean isHoldSeat = seatHoldRepository.isHoldSeat(seatId, userId);
        assertThat(isHoldSeat).isTrue();

        TestTransaction.end();
    }

    @Test
    @DisplayName("좌석_예약_실패_대기열토큰활성상태아님")
    void reserveSeat_Failure_InvalidToken() throws Exception {
        UUID tokenId = UUID.randomUUID();
        queueTokenRepository.save(QueueToken.waitingTokenOf(tokenId, userId, concertId, 1));

        mockMvc.perform(post("/api/v1/reservations/seats/{seatId}", seatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", tokenId)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_QUEUE_TOKEN.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_QUEUE_TOKEN.getMessage()));

        Seat seat = seatRepository.findById(seatId).get();
        assertThat(seat.status()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    @DisplayName("좌석_예약_실패_토큰없음")
    void reserveSeat_Failure_TokenNotFound() throws Exception {
        UUID otherTokenId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/reservations/seats/{seatId}", seatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", otherTokenId)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_QUEUE_TOKEN.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_QUEUE_TOKEN.getMessage()));

        Seat seat = seatRepository.findById(seatId).get();
        assertThat(seat.status()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    @DisplayName("좌석_예약_실패_콘서트찾지못함")
    void reserveSeat_Failure_ConcertNotFound() throws Exception {
        UUID otherConcertId = UUID.randomUUID();
        ReservationRequest invalidRequest = new ReservationRequest(otherConcertId, concertDateId);

        mockMvc.perform(post("/api/v1/reservations/seats/{seatId}", seatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", activeTokenId)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.CONCERT_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.CONCERT_NOT_FOUND.getMessage()));

        Seat seat = seatRepository.findById(seatId).get();
        assertThat(seat.status()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    @DisplayName("좌석_예약_실패_콘서트날짜찾지못함")
    void reserveSeat_Failure_ConcertDateNotFound() throws Exception {
        UUID otherConcertDateId = UUID.randomUUID();
        ReservationRequest invalidRequest = new ReservationRequest(concertId, otherConcertDateId);

        mockMvc.perform(post("/api/v1/reservations/seats/{seatId}", seatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", activeTokenId)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.CONCERT_DATE_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.CONCERT_DATE_NOT_FOUND.getMessage()));

        Seat seat = seatRepository.findById(seatId).get();
        assertThat(seat.status()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    @DisplayName("좌석_예약_실패_좌석찾지못함")
    void reserveSeat_Failure_SeatNotFound() throws Exception {
        UUID otherSeatId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/reservations/seats/{seatId}", otherSeatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", activeTokenId)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.SEAT_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.SEAT_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("좌석_예약_실패_이미예약된좌석")
    void reserveSeat_Failure_AlreadyReservedSeat() throws Exception {
        Seat seat = seatRepository.findById(seatId).get();
        seatRepository.save(seat.reserve());

        mockMvc.perform(post("/api/v1/reservations/seats/{seatId}", seatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", activeTokenId)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.ALREADY_RESERVED_SEAT.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_RESERVED_SEAT.getMessage()));
    }

    @Test
    @DisplayName("좌석_예약_실패_좌석이미배정됨")
    void reserveSeat_Failure_SeatAlreadyAssigned() throws Exception {
        Seat seat = seatRepository.findById(seatId).get();
        seatRepository.save(seat.payment());

        mockMvc.perform(post("/api/v1/reservations/seats/{seatId}", seatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", activeTokenId)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.ALREADY_RESERVED_SEAT.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_RESERVED_SEAT.getMessage()));
    }

    @Test
    @DisplayName("좌석_예약_실패_예약마감시간초과")
    void reserveSeat_Failure_OverDeadline() throws Exception {
        ConcertDate expiredConcertDate = ConcertDate.builder()
                .id(concertDateId)
                .concertId(concertId)
                .date(LocalDateTime.now().plusDays(1))
                .deadline(LocalDateTime.now().minusHours(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        concertDateRepository.save(expiredConcertDate);

        mockMvc.perform(post("/api/v1/reservations/seats/{seatId}", seatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", activeTokenId)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.OVER_DEADLINE.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.OVER_DEADLINE.getMessage()));
    }

    @Test
    @DisplayName("좌석_예약_실패_잘못된콘서트날짜와좌석조합")
    void reserveSeat_Failure_InvalidConcertDateSeatCombination() throws Exception {
        ConcertDate anotherConcertDate = TestDataFactory.createConcertDate(concertId);
        ConcertDate savedAnotherConcertDate = concertDateRepository.save(anotherConcertDate);

        ReservationRequest invalidRequest = new ReservationRequest(concertId, savedAnotherConcertDate.id());

        mockMvc.perform(post("/api/v1/reservations/seats/{seatId}", seatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", activeTokenId)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.SEAT_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.SEAT_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("좌석_예약_실패_만료된토큰")
    void reserveSeat_Failure_ExpiredToken() throws Exception {
        UUID expiredTokenId = UUID.randomUUID();
        QueueToken expiredToken = QueueToken.builder()
                .tokenId(expiredTokenId)
                .userId(userId)
                .concertId(concertId)
                .status(kr.hhplus.be.server.domain.queue.QueueStatus.ACTIVE)
                .position(0)
                .issuedAt(LocalDateTime.now().minusHours(2))
                .enteredAt(LocalDateTime.now().minusHours(2))
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();
        queueTokenRepository.save(expiredToken);

        mockMvc.perform(post("/api/v1/reservations/seats/{seatId}", seatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", expiredTokenId)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_QUEUE_TOKEN.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_QUEUE_TOKEN.getMessage()));
    }

}