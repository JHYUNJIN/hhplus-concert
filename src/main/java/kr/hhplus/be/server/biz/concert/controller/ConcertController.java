package kr.hhplus.be.server.biz.concert.controller;

import kr.hhplus.be.server.biz.concert.dto.ConcertDateResponse;
import kr.hhplus.be.server.biz.concert.dto.ConcertResponse;
import kr.hhplus.be.server.biz.concert.dto.SeatResponse;
import kr.hhplus.be.server.biz.concert.service.ConcertService;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import kr.hhplus.be.server.domain.seat.Seat;
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
    public ResponseEntity<List<ConcertResponse>> getAllConcerts() {
        List<Concert> concerts = concertService.getAllConcerts();
        List<ConcertResponse> responses = concerts.stream()
                .map(ConcertResponse::from)
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
    public ResponseEntity<List<ConcertDateResponse>> getAvailableConcertDates(@PathVariable String concertId) {
        List<ConcertDate> concertDates = concertService.getAvailableConcertDates(concertId);
        List<ConcertDateResponse> responses = concertDates.stream()
                .map(ConcertDateResponse::from)
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
    public ResponseEntity<List<SeatResponse>> getAvailableSeats(@PathVariable String concertDateId) {
        List<Seat> seats = concertService.getAvailableSeats(concertDateId);
        List<SeatResponse> responses = seats.stream()
                .map(SeatResponse::from)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
}