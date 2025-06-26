package kr.hhplus.be.server.infrastructure.web.concert;

import kr.hhplus.be.server.application.concert.ConcertService;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertDate;
import kr.hhplus.be.server.domain.concert.Seat;
import kr.hhplus.be.server.infrastructure.web.concert.dto.ConcertDateWebResponse;
import kr.hhplus.be.server.infrastructure.web.concert.dto.ConcertWebResponse;
import kr.hhplus.be.server.infrastructure.web.concert.dto.SeatWebResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/concerts")
public class ConcertController {

    private final ConcertService concertService;

    public ConcertController(ConcertService concertService) {
        this.concertService = concertService;
    }

    /**
     * 모든 콘서트 목록을 조회합니다.
     * GET /api/concerts
     * @return 콘서트 목록
     */
    @GetMapping
    public ResponseEntity<List<ConcertWebResponse>> getAllConcerts() {
        List<Concert> concerts = concertService.getAllConcerts();
        List<ConcertWebResponse> responses = concerts.stream()
                .map(ConcertWebResponse::from)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    /**
     * 특정 콘서트의 예매 가능한 날짜 목록을 조회합니다.
     * GET /api/concerts/{concertId}/dates
     * @param concertId 콘서트 ID
     * @return 예매 가능한 날짜 목록
     */
    @GetMapping("/{concertId}/dates")
    public ResponseEntity<List<ConcertDateWebResponse>> getAvailableConcertDates(@PathVariable String concertId) {
        List<ConcertDate> concertDates = concertService.getAvailableConcertDates(concertId);
        List<ConcertDateWebResponse> responses = concertDates.stream()
                .map(ConcertDateWebResponse::from)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    /**
     * 특정 콘서트 날짜의 이용 가능한 좌석 목록을 조회합니다.
     * GET /api/concerts/dates/{concertDateId}/seats
     * @param concertDateId 콘서트 날짜 ID
     * @return 이용 가능한 좌석 목록
     */
    @GetMapping("/dates/{concertDateId}/seats")
    public ResponseEntity<List<SeatWebResponse>> getAvailableSeats(@PathVariable String concertDateId) {
        List<Seat> seats = concertService.getAvailableSeats(concertDateId);
        List<SeatWebResponse> responses = seats.stream()
                .map(SeatWebResponse::from)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
}