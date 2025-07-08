package kr.hhplus.be.server.usecase.concert;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import kr.hhplus.be.server.domain.concertDate.ConcertDateRepository;
import kr.hhplus.be.server.domain.seat.*;
import kr.hhplus.be.server.infrastructure.persistence.concert.ConcertEntity;
import kr.hhplus.be.server.infrastructure.persistence.concertDate.ConcertDateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = false)
@Slf4j
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final ConcertDateRepository concertDateRepository;
    private final SeatRepository seatRepository;

    // 콘서트 생성
    public Concert createConcert(String title, String artist) {
        Concert concert = Concert.builder()
                .title(title)
                .artist(artist)
                .createdAt(LocalDateTime.now())
                .build();
        Concert savedConcert = concertRepository.save(concert);
        ConcertEntity concertEntity = ConcertEntity.from(savedConcert);
        if (concertEntity == null) {
            log.error("ConcertEntity 변환 실패: savedConcert = {}", savedConcert);
            throw new CustomException(ErrorCode.CONCERT_CREATION_FAILED);
        }
        return concertEntity.toDomain();
    }

    @Transactional
    public ConcertDate createConcertDateWithSeat(UUID concertId, LocalDateTime date, LocalDateTime deadline) {
        // 1. 콘서트 존재 여부 확인
        existsConcert(concertId);

        ConcertDate concertDate = ConcertDate.builder()
                .concertId(concertId)
                .date(date)
                .deadline(deadline)
                .build();

        // 2. 콘서트 날짜 생성
        ConcertDate savedConcertDate = concertDateRepository.save(concertDate);
        if (savedConcertDate == null || savedConcertDate.id() == null) {
            log.error("콘서트 날짜 생성 실패: savedConcertDate = {}", savedConcertDate);
            throw new CustomException(ErrorCode.CONCERT_DATE_CREATION_FAILED);
        }

        // 3. 콘서트 날짜에 대한 좌석 생성
        createSeatsForConcertDate(savedConcertDate.id());

        ConcertDateEntity concertDateEntity = ConcertDateEntity.from(savedConcertDate);
        if (concertDateEntity == null) {
            log.error("ConcertDateEntity 변환 실패: savedConcertDate = {}", savedConcertDate);
            throw new CustomException(ErrorCode.CONCERT_DATE_CREATION_FAILED);
        }

        return concertDateEntity.toDomain();
    }

    // 콘서트 목록 조회
    public List<Concert> getConcerts() {
        List<Concert> concerts = concertRepository.findAll();
        if (concerts.isEmpty()) {
            log.debug("콘서트 목록 조회 - 없음");
            return Collections.emptyList();
        }
        log.debug("콘서트 목록 조회: {}", concerts);
        return concerts;
    }

    // 콘서트 예약 가능한 날짜 조회
    public List<ConcertDate> getAvailableConcertDates(UUID concertId) throws CustomException {
        existsConcert(concertId);
        log.debug("예약 가능한 콘서트 날짜 조회: CONCERT_ID - {}", concertId);
        return concertDateRepository.findAvailableDatesWithAvailableSeatCount(concertId);
    }

    // 콘서트 예약 가능한 좌석 조회
    public List<Seat> getAvailableSeats(UUID concertId, UUID concertDateId) throws CustomException {
        existsConcert(concertId);

        List<Seat> availableSeats = seatRepository.findAvailableSeats(concertId, concertDateId);

        if (availableSeats.isEmpty()) {
            existsConcertDate(concertDateId);

            log.debug("콘서트 예약 가능 좌석 조회 - 없음: CONCERT_DATE_ID - {}", concertDateId);
            return Collections.emptyList();
        }

        return availableSeats;
    }

    // 콘서트 조회
    private void existsConcert(UUID concertId) throws CustomException {
        if (!concertRepository.existsById(concertId)) {
            log.warn("콘서트 조회 실패: CONCERT_ID - {}", concertId);
            throw new CustomException(ErrorCode.CONCERT_NOT_FOUND);
        }

        log.debug("콘서트 조회: CONCERT_ID - {}", concertId);
    }

    // 콘서트 날짜 조회
    private void existsConcertDate(UUID concertDateId) throws CustomException {
        if (!concertDateRepository.existsById(concertDateId)) {
            log.warn("콘서트 예약 가능 좌석 조회 실패: CONCERT_DATE_ID - {}", concertDateId);
            throw new CustomException(ErrorCode.CANNOT_RESERVATION_DATE);
        }
    }


    // 콘서트 날짜에 대한 좌석 생성
    public void createSeatsForConcertDate(UUID concertDateId) {
        ConcertDate concertDate = concertDateRepository.findById(concertDateId)
                .orElseThrow(() -> new CustomException(ErrorCode.CANNOT_RESERVATION_DATE));

        // 좌석 생성 로직 (예: 50개의 좌석 생성) 1~10:vip 11~30:premium 31~50:normal
        // 좌석별 가격 설정 (예: vip 170000, premium 130000, normal 90000)
        SeatStatus seatStatus = SeatStatus.AVAILABLE;
        System.out.println("🚀[로그:정현진] 01");
        for (int i = 1; i <= 50; i++) {
            System.out.println("🚀[로그:정현진] " + i + "번째 반복");
            SeatGrade seatGrade;
            BigDecimal price;
            if (i <= 10) {
                seatGrade = SeatGrade.VIP;
            } else if (i <= 30) {
                seatGrade = SeatGrade.PREMIUM;
            } else {
                seatGrade = SeatGrade.NORMAL;
            }
            price = SeatPrice.getPriceByGrade(seatGrade);


        System.out.println("🚀[로그:정현진] 02");
            Seat seat = Seat.builder()
                    .concertDateId(concertDateId)
                    .seatNo(i)
                    .seatGrade(seatGrade)
                    .price(price)
                    .status(seatStatus)
                    .build();

        System.out.println("🚀[로그:정현진] 03");
            log.debug("좌석 저장 시도: concertDateId={}, seatNo={}, seatId={}", concertDateId, i, seat.id());
            seatRepository.save(seat);
        }

        log.info("콘서트 날짜에 대한 좌석 생성 완료: CONCERT_DATE_ID - {}", concertDateId);
    }
}
