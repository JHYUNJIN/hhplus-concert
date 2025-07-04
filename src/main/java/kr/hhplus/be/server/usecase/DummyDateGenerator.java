package kr.hhplus.be.server.usecase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import net.datafaker.Faker;

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import kr.hhplus.be.server.domain.concertDate.ConcertDateRepository;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatGrade;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DummyDateGenerator {

    private final UserRepository userRepository;
    private final ConcertRepository concertRepository;
    private final ConcertDateRepository concertDateRepository;
    private final SeatRepository seatRepository;

    private final Faker faker = new Faker(new Locale("ko", "ko"));

    public void generateDummyData() {
        long start = System.currentTimeMillis();
        log.info("===============더미데이터 생성시작===============");
        generateUsers();
        List<Concert> concerts = generateConcert();
        List<ConcertDate> concertDates = generateConcertDates(concerts);
        generateSeats(concertDates);
        log.info("===============더미데이터 생성종료===============");
        log.info("소요 시간 : {}ms", System.currentTimeMillis() - start);
    }

    private void generateUsers() {
        log.info("유저 더미 데이터 삽입중....");
        for (int i = 0; i < 10000; i++) {
            BigDecimal amount = BigDecimal.valueOf(faker.number().numberBetween(0, 1000000));

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

        for (int i = 0; i < 1000; i++) {
            String artist = faker.music().genre() + " " + faker.name().firstName();
            String title = artist + " 콘서트 " + faker.music().instrument();

            Concert concert = Concert.builder()
                    .title(title)
                    .artist(artist)
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
            int dateCount = faker.number().numberBetween(2, 4); // 콘서트당 2-5개 날짜

            for (int i = 0; i < dateCount; i++) {
                LocalDateTime concertDate = LocalDateTime.now()
                        .plusDays(faker.number().numberBetween(1, 365))
                        .plusHours(faker.number().numberBetween(18, 22))
                        .withMinute(0).withSecond(0).withNano(0);

                LocalDateTime deadline = concertDate.minusDays(1);

                ConcertDate date = ConcertDate.builder()
                        .concertId(concert.id())
                        .date(concertDate)
                        .deadline(deadline)
                        .build();

                concertDates.add(concertDateRepository.save(date));
            }
        }

        log.info("콘서트 날짜 더미 데이터 삽입 완료");
        return concertDates;
    }

    private void generateSeats(List<ConcertDate> concertDates) {
        log.info("좌석 더미 데이터 삽입중....");
        for (ConcertDate concertDate : concertDates) {
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

                seatRepository.save(seat);
            }
        }
        log.info("좌석 더미 데이터 삽입 완료");
    }

    private SeatStatus getRandomSeatStatus() {
        int random = faker.number().numberBetween(1, 101);

        if (random <= 80) {
            return SeatStatus.AVAILABLE;
        } else if (random <= 95) {
            return SeatStatus.RESERVED;
        } else {
            return SeatStatus.ASSIGNED;
        }
    }
}
