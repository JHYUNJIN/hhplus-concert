package kr.hhplus.be.server.application.concert;

import kr.hhplus.be.server.common.exception.ConcertException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.ConcertDate;
import kr.hhplus.be.server.domain.enums.SeatStatus;
import kr.hhplus.be.server.domain.concert.Seat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
     * @throws ConcertException 콘서트를 찾을 수 없을 때
     */
    public List<ConcertDate> getAvailableConcertDates(String concertId) {
        // 먼저 콘서트 존재 여부 확인 (옵션: 하지만 ID로 날짜를 찾기 전에 확인하는 것이 좋음)
        concertRepository.findById(concertId)
                .orElseThrow(() -> new ConcertException(ErrorCode.CONCERT_NOT_FOUND, "콘서트를 찾을 수 없습니다: " + concertId));

        List<ConcertDate> concertDates = concertRepository.findConcertDatesByConcertId(concertId);
        LocalDateTime now = LocalDateTime.now();
        return concertDates.stream()
                .filter(cd -> cd.getDate().isAfter(now) && (cd.getDeadline() == null || cd.getDeadline().isAfter(now)))
                .collect(Collectors.toList());
    }

    /**
     * 특정 콘서트 날짜의 이용 가능한 좌석 목록을 조회합니다.
     * @param concertDateId 콘서트 날짜 ID
     * @return 이용 가능한 좌석 목록 (SeatStatus.AVAILABLE)
     * @throws ConcertException 콘서트 날짜를 찾을 수 없을 때
     */
    public List<Seat> getAvailableSeats(String concertDateId) {
        // 콘서트 날짜 존재 여부 확인
        concertRepository.findConcertDateById(concertDateId)
                .orElseThrow(() -> new ConcertException(ErrorCode.CONCERT_DATE_NOT_FOUND, "콘서트 날짜를 찾을 수 없습니다: " + concertDateId));

        List<Seat> seats = concertRepository.findSeatsByConcertDateId(concertDateId);
        return seats.stream()
                .filter(seat -> seat.getStatus() == SeatStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    /**
     * 특정 ID의 콘서트를 조회합니다.
     * @param concertId 조회할 콘서트 ID
     * @return 조회된 Concert 객체
     * @throws ConcertException 콘서트를 찾을 수 없을 때
     */
    public Concert getConcertById(String concertId) { // Optional 대신 Concert를 직접 반환
        return concertRepository.findById(concertId)
                .orElseThrow(() -> new ConcertException(ErrorCode.CONCERT_NOT_FOUND, "콘서트를 찾을 수 없습니다: " + concertId));
    }

    /**
     * 특정 ID의 콘서트 날짜를 조회합니다.
     * @param concertDateId 조회할 콘서트 날짜 ID
     * @return 조회된 ConcertDate 객체
     * @throws ConcertException 콘서트 날짜를 찾을 수 없을 때
     */
    public ConcertDate getConcertDateById(String concertDateId) { // Optional 대신 ConcertDate를 직접 반환
        return concertRepository.findConcertDateById(concertDateId)
                .orElseThrow(() -> new ConcertException(ErrorCode.CONCERT_DATE_NOT_FOUND, "콘서트 날짜를 찾을 수 없습니다: " + concertDateId));
    }
}