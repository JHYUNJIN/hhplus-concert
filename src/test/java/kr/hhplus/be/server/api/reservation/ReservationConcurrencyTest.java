package kr.hhplus.be.server.api.reservation;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.utility.TestcontainersConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.server.api.TestDataFactory;
import kr.hhplus.be.server.api.reservation.dto.request.ReservationRequest;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import kr.hhplus.be.server.domain.concertDate.ConcertDateRepository;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.queue.QueueTokenRepository;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ReservationConcurrencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisAtomicQueueTokenRepository redisAtomicQueueTokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QueueTokenRepository queueTokenRepository;

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

    private static final int THREAD_SIZE = 10;

    private UUID concertId;
    private UUID concertDateId;
    private UUID seatId;
    private ReservationRequest request;

    @BeforeEach
    void setUp() {
        cleanUp();
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        Concert concert = TestDataFactory.createConcert();
        Concert savedConcert = concertRepository.save(concert);
        concertId = savedConcert.id();

        ConcertDate concertDate = TestDataFactory.createConcertDate(concertId);
        ConcertDate savedConcertDate = concertDateRepository.save(concertDate);
        concertDateId = savedConcertDate.id();

        Seat seat = TestDataFactory.createSeat(concertDateId);
        Seat savedSeat = seatRepository.save(seat);
        seatId = savedSeat.id();

        request = new ReservationRequest(concertId, concertDateId);
    }

    private void cleanUp() {
        userRepository.deleteAll();
        reservationRepository.deleteAll();
        paymentRepository.deleteAll();
        seatRepository.deleteAll();
        concertDateRepository.deleteAll();
        concertRepository.deleteAll();
    }

    @Test
    @DisplayName("동시_예약")
    void reserveSeat_Concurrency_Test() throws Exception {
        List<UUID> tokenIds = new ArrayList<>();
        for (int i = 0; i < THREAD_SIZE; i++) {
            User user = TestDataFactory.createUser();
            User savedUser = userRepository.save(user);

            QueueToken queueToken = TestDataFactory.createQueueToken(savedUser.id(), concertId);
            String issuedTokenIdString = redisAtomicQueueTokenRepository.issueTokenAtomic(savedUser.id(), concertId, queueToken);
            UUID tokenId = UUID.fromString(issuedTokenIdString);
            tokenIds.add(tokenId);
        }
        System.out.println("🚀[로그:정현진] tokenIds : " + tokenIds);
        System.out.println("🚀[로그:정현진] tokenIds 갯수 : " + tokenIds.size());

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);

        for (UUID tokenId : tokenIds) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    int status = mockMvc.perform(post("/api/v1/reservations/seats/{seatId}", seatId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .header("Authorization", tokenId)
                                    .content(objectMapper.writeValueAsString(request)))
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

        Seat updatedSeat = seatRepository.findById(seatId).get();
        assertThat(updatedSeat.status()).isEqualTo(SeatStatus.RESERVED);

        // 에약은 하나만 생성되어야함
        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations.size()).isEqualTo(1);

        assertThat(successCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("동시_예약(다른좌석)")
    void reserveSeat_Concurrency_Test_OtherSeats() throws Exception {
        List<UUID> seatIds = new ArrayList<>();
        for (int i = 0; i < THREAD_SIZE; i++) {
            Seat seat = TestDataFactory.createSeatWithSeatNo(concertDateId, i);
            Seat savedSeat = seatRepository.save(seat);
            seatIds.add(savedSeat.id());
        }

        List<UUID> tokenIds = new ArrayList<>();
        for (int i = 0; i < THREAD_SIZE; i++) {
            User user = TestDataFactory.createUser();
            User savedUser = userRepository.save(user);

            QueueToken queueToken = TestDataFactory.createQueueToken(savedUser.id(), concertId);
            String issuedTokenIdString = redisAtomicQueueTokenRepository.issueTokenAtomic(savedUser.id(), concertId, queueToken);
            UUID tokenId = UUID.fromString(issuedTokenIdString);
            tokenIds.add(tokenId);
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_SIZE; i++) {
            final int index = i;
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    int status = mockMvc.perform(post("/api/v1/reservations/seats/{seatId}", seatIds.get(index))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .header("Authorization", tokenIds.get(index))
                                    .content(objectMapper.writeValueAsString(request)))
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

        // 모든 좌석이 예약됨 상태인지 확인
        for (UUID seatId : seatIds) {
            Seat updatedSeat = seatRepository.findById(seatId).get();
            assertThat(updatedSeat.status()).isEqualTo(SeatStatus.RESERVED);
        }

        // THREAD_SIZE 만큼 예약 생성되어야함
        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations.size()).isEqualTo(THREAD_SIZE);

        assertThat(successCount.get()).isEqualTo(THREAD_SIZE);
    }

    @Test
    @DisplayName("동시_예약(여러좌석)")
    void reserveSeat_Concurrency_Test_SameUser_OtherSeats() throws Exception {
        // 여러 좌석 생성
        List<UUID> seatIds = new ArrayList<>();
        for (int i = 0; i < THREAD_SIZE; i++) {
            Seat seat = TestDataFactory.createSeatWithSeatNo(concertDateId, i);
            Seat savedSeat = seatRepository.save(seat);
            seatIds.add(savedSeat.id());
        }

        User user = TestDataFactory.createUser();
        User savedUser = userRepository.save(user);
        UUID userId = savedUser.id();

        QueueToken queueToken = TestDataFactory.createQueueToken(savedUser.id(), concertId);
        String issuedTokenIdString = redisAtomicQueueTokenRepository.issueTokenAtomic(savedUser.id(), concertId, queueToken);
        UUID tokenId = UUID.fromString(issuedTokenIdString);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_SIZE; i++) {
            final int index = i;
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    int status = mockMvc.perform(post("/api/v1/reservations/seats/{seatId}", seatIds.get(index))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .header("Authorization", tokenId)
                                    .content(objectMapper.writeValueAsString(request)))
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
        System.out.println("🚀[로그:정현진] THREAD_SIZE : " + THREAD_SIZE);
        System.out.println("🚀[로그:정현진] successCount : " + successCount);

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);

        // 모든 좌석이 예약됨 상태인지 확인
        for (UUID seatId : seatIds) {
            Seat updatedSeat = seatRepository.findById(seatId).get();
            assertThat(updatedSeat.status()).isEqualTo(SeatStatus.RESERVED);
        }




        // THREAD_SIZE 만큼 예약 생성되어야함
        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations.size()).isEqualTo(THREAD_SIZE);

        assertThat(successCount.get()).isEqualTo(THREAD_SIZE);
    }
}