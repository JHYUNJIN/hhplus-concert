package kr.hhplus.be.server.api.payment;

import kr.hhplus.be.server.api.TestDataFactory;
import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.port.out.ConcertRepository;
import kr.hhplus.be.server.concert.domain.ConcertDate;
import kr.hhplus.be.server.concert.port.out.ConcertDateRepository;
import kr.hhplus.be.server.payment.domain.Payment;
import kr.hhplus.be.server.payment.port.out.PaymentRepository;
import kr.hhplus.be.server.payment.domain.enums.PaymentStatus;
import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.queue.port.out.QueueTokenRepository;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.port.out.ReservationRepository;
import kr.hhplus.be.server.reservation.domain.enums.ReservationStatus;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.reservation.port.out.SeatHoldRepository;
import kr.hhplus.be.server.concert.port.out.SeatRepository;
import kr.hhplus.be.server.concert.domain.enums.SeatStatus;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.port.out.UserRepository;
import kr.hhplus.be.server.queue.adapter.out.persistence.RedisAtomicQueueTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class PaymentConcurrencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisAtomicQueueTokenRepository redisAtomicQueueTokenRepository;

    @Autowired
    private QueueTokenRepository queueTokenRepository;

    @Autowired
    private SeatHoldRepository seatHoldRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertDateRepository concertDateRepository;

    @Autowired
    private SeatRepository seatRepository;

    private static final int THREAD_SIZE = 5;

    private UUID concertId;
    private UUID concertDateId;
    private UUID userId;
    private UUID seatId;
    private UUID reservationId;
    private UUID activeTokenId;

    private User user;

    @BeforeEach
    void beforeEach() {
        redisTemplate.getConnectionFactory().getConnection().flushAll(); // Redis 데이터 초기화

        user = TestDataFactory.createUserWithAmount(BigDecimal.valueOf(1_000_000));
        User savedUser = userRepository.save(user);
        userId = savedUser.id();

        Concert concert = TestDataFactory.createConcert();
        Concert savedConcert = concertRepository.save(concert);
        concertId = savedConcert.id();

        ConcertDate concertDate = TestDataFactory.createConcertDate(concertId);
        ConcertDate savedConcertDate = concertDateRepository.save(concertDate);
        concertDateId = savedConcertDate.id();

        Seat seat = TestDataFactory.createReservedSeat(concertDateId);
        Seat savedSeat = seatRepository.save(seat);
        seatId = savedSeat.id();

        Reservation reservation = TestDataFactory.createReservation(userId, seatId);
        Reservation savedReservation = reservationRepository.save(reservation);
        reservationId = savedReservation.id();

        Payment payment = TestDataFactory.createPayment(userId, reservationId);
        paymentRepository.save(payment);

        // 큐 토큰 발급 및 activeTokenId에 할당
        QueueToken queueToken = TestDataFactory.createQueueToken(savedUser.id(), concertId);
        String issuedTokenIdString = redisAtomicQueueTokenRepository.issueTokenAtomic(savedUser.id(), concertId, queueToken);
        activeTokenId = UUID.fromString(issuedTokenIdString);

        // 좌석 홀드 상태로 설정 (테스트 시나리오에 따라 이 라인을 제거하거나 수정 필요)
        // 예약이 이미 생성된 상태이므로, 좌석은 이미 홀드/예약 상태여야 함
        seatHoldRepository.hold(seatId, userId);
    }

    @Test
    @DisplayName("동시_결제")
    void payment_Concurrency_Test() throws Exception {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_SIZE; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    int status = mockMvc.perform(post("/api/v1/payments/{reservationId}", reservationId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .header("Authorization", activeTokenId.toString()))
                            .andReturn()
                            .getResponse()
                            .getStatus();

                    if (status == 200)
                        successCount.incrementAndGet();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);

        // 결제 성공 후 최종 상태 검증
        Payment updatedPayment = paymentRepository.findByReservationId(reservationId).get();
        assertThat(updatedPayment.status()).isEqualTo(PaymentStatus.SUCCESS); // 결제 성공 확인

        Reservation updatedReservation = reservationRepository.findById(reservationId).get();
        assertThat(updatedReservation.status()).isEqualTo(ReservationStatus.SUCCESS); // 결제 후 예약 성공 확인

        Seat updatedSeat = seatRepository.findById(seatId).get();
        assertThat(updatedSeat.status()).isEqualTo(SeatStatus.ASSIGNED); // 결제 후 좌석 배정 확인

        User updatedUser = userRepository.findById(userId).get();
        // 사용자 잔액은 초기 금액에서 좌석 가격을 뺀 값이어야 함.
        // BigDecimal 비교 시 equals 대신 compareTo를 사용하는 것이 안전함
        assertThat(updatedUser.amount().compareTo(user.amount().subtract(updatedSeat.price()))).isEqualTo(0); // 잔액 확인

        assertThat(successCount.get()).isEqualTo(1);
    }
}