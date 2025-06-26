package kr.hhplus.be.server.biz.concert.service;

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import kr.hhplus.be.server.domain.enums.SeatStatus;
import kr.hhplus.be.server.domain.seat.Seat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ConcertService {

    private final ConcertRepository concertRepository;

    public ConcertService(ConcertRepository concertRepository) {
        this.concertRepository = concertRepository;
    }

    /**
     * 모든 콘서트 목록을 조회합니다.
     * @return 콘서트 목록
     */
    public List<Concert> getAllConcerts() {
        return concertRepository.findAll();
    }

    /**
     * 특정 콘서트의 모든 공연 가능한 날짜를 조회합니다.
     * 예약 마감 기한(deadline)이 지나지 않은 날짜만 반환합니다.
     * @param concertId 콘서트 ID
     * @return 해당 콘서트의 공연 가능한 날짜 목록
     */
    public List<ConcertDate> getAvailableConcertDates(String concertId) {
        List<ConcertDate> concertDates = concertRepository.findConcertDatesByConcertId(concertId);
        // 현재 시간 이후이면서, 예약 마감 기한이 아직 지나지 않은 날짜만 필터링
        LocalDateTime now = LocalDateTime.now();
        return concertDates.stream()
                .filter(cd -> cd.getDate().isAfter(now) && (cd.getDeadline() == null || cd.getDeadline().isAfter(now)))
                .collect(Collectors.toList());
    }

    /**
     * 특정 콘서트 날짜의 이용 가능한 좌석 목록을 조회합니다.
     * @param concertDateId 콘서트 날짜 ID
     * @return 이용 가능한 좌석 목록 (SeatStatus.AVAILABLE)
     */
    public List<Seat> getAvailableSeats(String concertDateId) {
        List<Seat> seats = concertRepository.findSeatsByConcertDateId(concertDateId);
        // AVAILABLE 상태의 좌석만 필터링
        return seats.stream()
                .filter(seat -> seat.getStatus() == SeatStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    /**
     * 특정 ID의 콘서트를 조회합니다.
     * @param concertId 조회할 콘서트 ID
     * @return 조회된 Concert 객체 (Optional)
     */
    public Optional<Concert> getConcertById(String concertId) {
        return concertRepository.findById(concertId);
    }

    /**
     * 특정 ID의 콘서트 날짜를 조회합니다.
     * @param concertDateId 조회할 콘서트 날짜 ID
     * @return 조회된 ConcertDate 객체 (Optional)
     */
    public Optional<ConcertDate> getConcertDateById(String concertDateId) {
        return concertRepository.findConcertDateById(concertDateId);
    }
}