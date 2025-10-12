//package kr.hhplus.be.server.dummy;
//
//import kr.hhplus.be.server.concert.domain.Concert;
//import kr.hhplus.be.server.concert.domain.ConcertDate;
//import kr.hhplus.be.server.concert.domain.Seat;
//import kr.hhplus.be.server.concert.domain.enums.SeatGrade;
//import kr.hhplus.be.server.concert.port.out.ConcertDateRepository;
//import kr.hhplus.be.server.concert.port.out.ConcertRepository;
//import kr.hhplus.be.server.concert.port.out.SeatRepository;
//import kr.hhplus.be.server.payment.domain.Payment;
//import kr.hhplus.be.server.payment.port.out.PaymentRepository;
//import kr.hhplus.be.server.queue.domain.QueueToken;
//import kr.hhplus.be.server.queue.port.out.QueueTokenRepository;
//import kr.hhplus.be.server.reservation.domain.Reservation;
//import kr.hhplus.be.server.reservation.port.out.ReservationRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import net.datafaker.Faker;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.TimeUnit;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class DummyDateGenerator {
//
//    private final ConcertRepository concertRepository;
//    private final ConcertDateRepository concertDateRepository;
//    private final SeatRepository seatRepository;
//    private final ReservationRepository reservationRepository;
//    private final PaymentRepository paymentRepository;
//    private final QueueTokenRepository queueTokenRepository;
//
//    private final Faker faker = new Faker();
//
//    @EventListener(DummyDataGeneratedEvent.class)
//    @Transactional
//    public void generateDummyData(DummyDataGeneratedEvent event) {
//        log.info("더미 데이터 생성을 시작합니다.");
//
//        generateConcerts();
//        generateConcertDates();
//        generateSeats();
//        generateReservations();
//        generatePayments();
//        generateQueueTokens();
//
//        log.info("더미 데이터 생성을 완료했습니다.");
//    }
//
//    private void generateConcerts() {
//        log.info("더미 콘서트 데이터를 생성합니다.");
//        for (int i = 0; i < 5; i++) {
//            Concert concert = Concert.builder()
//                    .title(faker.music().genre() + " Concert")
////                    .artist(faker.music().artist())
//                    .openTime(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 10)))
//                    .build();
//            concertRepository.save(concert);
//        }
//        log.info("더미 콘서트 데이터 생성 완료.");
//    }
//
//    private void generateConcertDates() {
//        log.info("더미 콘서트 날짜 데이터를 생성합니다.");
//        List<Concert> concerts = concertRepository.findAll();
//        for (Concert concert : concerts) {
//            for (int i = 0; i < 3; i++) {
//                ConcertDate concertDate = ConcertDate.builder()
//                        .concertId(concert.id())
//                        .date(LocalDateTime.now().plusDays(faker.number().numberBetween(1, 30)))
//                        .deadline(LocalDateTime.now().plusDays(faker.number().numberBetween(1, 29)))
//                        .availableSeatCount((long)faker.number().numberBetween(50, 200))
//                        .build();
//                concertDateRepository.save(concertDate);
//            }
//        }
//        log.info("더미 콘서트 날짜 데이터 생성 완료.");
//    }
//
//    private void generateSeats() {
//        log.info("더미 좌석 데이터를 생성합니다.");
//        List<ConcertDate> concertDates = concertDateRepository.findAll();
//        for (ConcertDate concertDate : concertDates) {
//            for (int i = 0; i < concertDate.availableSeatCount(); i++) {
//                Seat seat = Seat.builder()
//                        .concertDateId(concertDate.id())
//                        .seatNo(i + 1)
//                        .price(BigDecimal.valueOf(faker.number().numberBetween(50000, 150000)))
//                        .seatGrade(faker.options().option(SeatGrade.VIP, SeatGrade.PREMIUM, SeatGrade.NORMAL))
//                        .build();
//                seatRepository.save(seat);
//            }
//        }
//        log.info("더미 좌석 데이터 생성 완료.");
//    }
//
//    private void generateReservations() {
//        log.info("더미 예약 데이터를 생성합니다.");
//        List<Seat> seats = seatRepository.findAll();
//        // Note: These UUIDs are random and do not correspond to actual users in user-service.
//        List<UUID> userIds = new ArrayList<>();
//        for (int i = 0; i < 10; i++) {
//            userIds.add(UUID.randomUUID());
//        }
//
//        for (int i = 0; i < 20; i++) {
//            if (seats.isEmpty() || userIds.isEmpty()) break;
//
//            Seat randomSeat = seats.remove(faker.number().numberBetween(0, seats.size() - 1));
//            UUID randomUserId = userIds.get(faker.number().numberBetween(0, userIds.size() - 1));
//
//            Reservation reservation = Reservation.of(randomUserId, randomSeat.id());
//            reservationRepository.save(reservation);
//        }
//        log.info("더미 예약 데이터 생성 완료.");
//    }
//
//    private void generatePayments() {
//        log.info("더미 결제 데이터를 생성합니다.");
//        List<Reservation> reservations = reservationRepository.findAll();
//        for (Reservation reservation : reservations) {
//            Seat seat = seatRepository.findById(reservation.seatId()).orElse(null);
//            if (seat == null) continue;
//
//            Payment payment = Payment.of(reservation.userId(), reservation.id(), seat.price());
//            paymentRepository.save(payment);
//        }
//        log.info("더미 결제 데이터 생성 완료.");
//    }
//
//    private void generateQueueTokens() {
//        log.info("더미 대기열 토큰 데이터를 생성합니다.");
//        List<Concert> concerts = concertRepository.findAll();
//        // Note: These UUIDs are random and do not correspond to actual users in user-service.
//        List<UUID> userIds = new ArrayList<>();
//        for (int i = 0; i < 10; i++) {
//            userIds.add(UUID.randomUUID());
//        }
//
//        for (Concert concert : concerts) {
//            for (int i = 0; i < 5; i++) {
//                UUID randomUserId = userIds.get(faker.number().numberBetween(0, userIds.size() - 1));
//                QueueToken queueToken = QueueToken.activeTokenOf(UUID.randomUUID(), randomUserId, concert.id(), 60L);
//                queueTokenRepository.save(queueToken);
//            }
//        }
//        log.info("더미 대기열 토큰 데이터 생성 완료.");
//    }
//}
