package kr.hhplus.be.server.dummy;

import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertDate;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.domain.enums.SeatGrade;
import kr.hhplus.be.server.concert.domain.enums.SeatStatus;
import kr.hhplus.be.server.concert.port.out.ConcertDateRepository;
import kr.hhplus.be.server.concert.port.out.ConcertRepository;
import kr.hhplus.be.server.concert.port.out.SeatRepository;
import kr.hhplus.be.server.payment.domain.Payment;
import kr.hhplus.be.server.payment.port.out.PaymentRepository;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.domain.enums.ReservationStatus;
import kr.hhplus.be.server.reservation.port.out.ReservationRepository;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@Slf4j
public class DummyDateGenerator {

    private final UserRepository userRepository;
    private final ConcertRepository concertRepository;
    private final ConcertDateRepository concertDateRepository;
    private final SeatRepository seatRepository;
    private final AsyncSeatCountUpdater asyncSeatCountUpdater;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    private final Faker faker = new Faker(new Locale("ko", "ko"));

    public void generateDummyData() {
        final int PENDING_RESERVATION_COUNT = 1000; // 부하 테스트를 위한 만료 예정 예약 데이터 개수
        long start = System.currentTimeMillis();
        log.info("===============더미데이터 생성시작===============");
        generateUsers();
        List<Concert> concerts = generateConcert();
        List<ConcertDate> concertDates = generateConcertDates(concerts);
        List<Seat> allSeats = generateSeats(concertDates);

        // 비동기로 좌석 수 업데이트 로직을 호출합니다.
        List<UUID> concertDateIds = concertDates.stream().map(ConcertDate::id).toList();
        asyncSeatCountUpdater.updateAvailableSeatCounts(concertDateIds);
        // 비동기 작업이 완료될 때까지 기다리지 않고 메소드가 종료됩니다.

        // ⭐️ 부하 테스트를 위한 만료 예정 예약 데이터를 추가로 생성합니다.
        generatePendingReservationsForLoadTest(allSeats, PENDING_RESERVATION_COUNT);


        log.info("===============더미데이터 생성종료===============");
        log.info("소요 시간 : {}ms", System.currentTimeMillis() - start);
    }

    private void generateUsers() {
        log.info("유저 더미 데이터 삽입중....");
        for (int i = 0; i < 10000; i++) {
            BigDecimal amount = (i > 1000) ? BigDecimal.valueOf(999999999) : BigDecimal.ZERO;
            User user = User.builder()
                    .amount(amount)
                    .build();

            userRepository.save(user);
        }
        log.info("유저 더미 데이터 삽입 완료");
    }

    private List<Concert> generateConcert() {
        log.info("콘서트 더미 데이터 삽입중....");
        List<Concert> concerts = new ArrayList<>();

        // 랜덤 날짜 범위 설정 (현재로부터 1년 전 ~ 1년 후)
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(5);

        for (int i = 0; i < 1000; i++) {
            String artist = faker.music().genre() + " " + faker.name().firstName();
            String title = artist + " 콘서트 " + faker.music().instrument();

            // 랜덤 날짜 생성 (시간은 10:00:00으로 통일)
            long randomDayEpoch = ThreadLocalRandom.current().nextLong(start.toLocalDate().toEpochDay(), end.toLocalDate().toEpochDay());
            LocalDateTime openTime = LocalDateTime.ofEpochSecond(randomDayEpoch * 24 * 60 * 60, 0, java.time.ZoneOffset.UTC)
                    .withHour(10).withMinute(0).withSecond(0);
//            LocalDateTime soldOutTime = openTime.plusDays(3)
//                .withHour(faker.number().numberBetween(18, 22)).withMinute(0).withSecond(0);

            Concert concert = Concert.builder()
                    .title(title)
                    .artist(artist)
                    .openTime(openTime)
                    .soldOutTime(null)
                    .build();

            concerts.add(concertRepository.save(concert));
        }
        log.info("콘서트 더미 데이터 삽입 완료");
        return concerts;
    }

    private List<ConcertDate> generateConcertDates(List<Concert> concerts) {
        log.info("콘서트 날짜 더미 데이터 삽입중....");
        List<ConcertDate> concertDates = new ArrayList<>();

        for (Concert concert : concerts) {
            int dateCount = faker.number().numberBetween(2, 4); // 콘서트당 2-4개 날짜

            for (int i = 0; i < dateCount; i++) {
                LocalDateTime concertDate = LocalDateTime.now()
                        .plusDays(faker.number().numberBetween(1, 365))
                        .plusHours(faker.number().numberBetween(18, 22))
                        .withMinute(0).withSecond(0).withNano(0);

                LocalDateTime deadline = concertDate.minusDays(1);

                // 여기서는 각 날짜마다 50개의 좌석이 있다고 가정합니다.
                ConcertDate date = ConcertDate.builder()
                        .concertId(concert.id())
                        .date(concertDate)
                        .deadline(deadline)
                        .availableSeatCount(50L) // 좌석 수 초기값 설정 (null 방지)
                        .version(0L)             // 버전 초기값 설정
                        .build();

                concertDates.add(concertDateRepository.save(date));
            }
        }

        log.info("콘서트 날짜 더미 데이터 삽입 완료");
        return concertDates;
    }


    private List<Seat> generateSeats(List<ConcertDate> concertDates) {
        log.info("좌석 더미 데이터 삽입중....");
        List<Seat> allSavedSeats = new ArrayList<>();
        for (ConcertDate concertDate : concertDates) {
            List<Seat> seatsForDate = new ArrayList<>();
            for (int seatNo = 1; seatNo <= 50; seatNo++) {
                SeatGrade seatGrade;
                BigDecimal price;

                if (seatNo <= 10) {
                    seatGrade = SeatGrade.VIP;
                    price = BigDecimal.valueOf(faker.number().numberBetween(100000, 200000));
                } else if (seatNo <= 30) {
                    seatGrade = SeatGrade.PREMIUM;
                    price = BigDecimal.valueOf(faker.number().numberBetween(70000, 100000));
                } else {
                    seatGrade = SeatGrade.NORMAL;
                    price = BigDecimal.valueOf(faker.number().numberBetween(40000, 70000));
                }

                // 80% 확률로 AVAILABLE, 15% RESERVED, 5% ASSIGNED
                SeatStatus status = getRandomSeatStatus();

                Seat seat = Seat.builder()
                        .concertDateId(concertDate.id())
                        .seatNo(seatNo)
                        .price(price)
                        .seatGrade(seatGrade)
                        .status(status)
                        .build();
            allSavedSeats.add(seatRepository.save(seat));
            }
        }
        log.info("좌석 더미 데이터 삽입 완료");
        return allSavedSeats;
    }

    private SeatStatus getRandomSeatStatus() {
        int random = faker.number().numberBetween(1, 101);

        if (random <= 80) {
            return SeatStatus.AVAILABLE;
        }
//        else if (random <= 95) {
//            return SeatStatus.RESERVED;
//        }
        else {
            return SeatStatus.ASSIGNED;
        }
    }

    private void generatePendingReservationsForLoadTest(List<Seat> allSeats, int count) {
        log.info("부하 테스트를 위한 만료 예정 예약 데이터 생성 시작...");
        List<Seat> availableSeats = allSeats.stream()
                .filter(Seat::isAvailable)
                .limit(count)
                .toList();
        if (availableSeats.size() < count) {
            log.warn("생성 실패: 예약 가능한 좌석이 요청한 개수({})보다 적습니다. (현재 {}개)", count, availableSeats.size());
            return;
        }

        User testUser = userRepository.save(User.builder().amount(BigDecimal.valueOf(999999999)).build());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationTime = now.plusSeconds(10); // 10초 뒤에 만료되도록 설정

        List<Reservation> newReservations = new ArrayList<>();
        for (Seat seat : availableSeats) {
            newReservations.add(Reservation.builder()
                    .userId(testUser.id())
                    .seatId(seat.id())
                    .status(ReservationStatus.PENDING)
                    .expiresAt(expirationTime)
                    .build());
        }
        // 2. Reservation을 먼저 저장하여 DB로부터 ID를 부여받습니다.
        List<Reservation> savedReservations = new ArrayList<>();
        for (Reservation reservation : newReservations) {
            Reservation savedReservation = reservationRepository.save(reservation);
            savedReservations.add(savedReservation);
        }


        // 3. ID가 부여된 Reservation 객체들을 사용하여 Payment 객체들을 생성합니다.
        for (int i = 0; i < savedReservations.size(); i++) {
            Reservation reservation = savedReservations.get(i);
            Seat correspondingSeat = availableSeats.get(i);
            Payment payment = Payment.of(testUser.id(), reservation.id(), correspondingSeat.price());
            paymentRepository.save(payment);
        }

        // 4. 마지막으로 좌석들의 상태를 RESERVED로 변경하여 저장합니다.
        List<Seat> seatsToUpdate = availableSeats.stream()
                .map(Seat::reserve)
                .toList();
        for( Seat seat : seatsToUpdate) seatRepository.save(seat);

        log.info("{}개의 만료 예정 예약 데이터를 성공적으로 생성했습니다.", availableSeats.size());
    }

}
