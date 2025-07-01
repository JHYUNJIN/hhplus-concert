package kr.hhplus.be.server.infrastructure.api.concert;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import kr.hhplus.be.server.application.concert.ConcertService;
import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.infrastructure.api.concert.dto.request.ConcertDateRequest;
import kr.hhplus.be.server.infrastructure.api.concert.dto.request.ConcertRequest;
import kr.hhplus.be.server.infrastructure.api.concert.dto.response.ConcertDateResponse;
import kr.hhplus.be.server.infrastructure.api.concert.dto.response.ConcertResponse;
import kr.hhplus.be.server.infrastructure.api.concert.dto.response.SeatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@Slf4j // 로깅 활성화
@RequestMapping("/api/v1/concerts")
@RequiredArgsConstructor
@Tag(name = "Concert API", description = "콘서트 관련 API")
public class ConcertController {

    private final ConcertService concertService;


    // 콘서트 생성
    @PostMapping
    @Operation(summary = "콘서트 생성 API", description = "새로운 콘서트를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "콘서트 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ConcertResponse> createConcert(
            @RequestBody ConcertRequest concertRequest
    ) {

        // concertRequest에 대한 로그 추가
        log.info("Creating concert with request: {}", concertRequest);


        Concert concert = concertService.createConcert(
                concertRequest.title(),
                concertRequest.artist()
        );
        return ResponseEntity
                .status(201) // 201 Created
                .body(ConcertResponse.from(concert));
    }

    // 콘서트 날짜 생성
    @PostMapping("/{concertId}/dates")
    @Operation(summary = "콘서트 날짜 생성 API", description = "특정 콘서트에 대한 예약 가능한 날짜를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "콘서트 날짜 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "콘서트 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ConcertDateResponse> createConcertDateWithSeat(
            @PathVariable UUID concertId,
            @RequestBody ConcertDateRequest concertDateRequest
    ) throws CustomException {
        // 콘서트 날짜 생성 로깅
        log.info("콘서트 날짜 생성 요청 - concertId: {}, request: {}", concertId, concertDateRequest);

        ConcertDate concertDate = concertService.createConcertDateWithSeat(
            concertId,
            LocalDateTime.parse(concertDateRequest.date()),
            LocalDateTime.parse(concertDateRequest.deadline())
        );

        log.info("콘서트 날짜 생성 성공 - concertDateId: {}", concertDate.id());
        return ResponseEntity
                .status(201) // 201 Created
                .body(ConcertDateResponse.from(concertDate));
    }

    // 콘서트 목록 조회
    @GetMapping
    @Operation(summary = "콘서트 목록 조회 API", description = "모든 콘서트의 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "콘서트 목록 조회 성공", content = @Content(schema = @Schema(implementation = ConcertResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<List<ConcertResponse>> getConcerts() {
        List<Concert> concerts = concertService.getConcerts();
        List<ConcertResponse> responses = concerts.stream()
                .map(ConcertResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }


    @Operation(summary = "콘서트 예약 가능 날짜 조회 API", description = "헤당 콘서트 예약 가능한 날짜 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ConcertDateResponse.class))),
            @ApiResponse(responseCode = "404 - Concert", description = "콘서트 찾을 수 없음"),
    })
    @GetMapping("/{concertId}/dates")
    public ResponseEntity<List<ConcertDateResponse>> getAvailableDates(
            @PathVariable UUID concertId) throws CustomException {
        List<ConcertDate> availableConcertDates = concertService.getAvailableConcertDates(concertId);
        List<ConcertDateResponse> responses = availableConcertDates.stream()
                .map(ConcertDateResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "콘서트 예약 가능 좌석 조회 API", description = "해당 콘서트, 해당 날짜의 예약 가능한 좌석 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = SeatResponse.class))),
            @ApiResponse(responseCode = "404 - Concert", description = "콘서트 찾을 수 없음"),
            @ApiResponse(responseCode = "404 - ConcertDate", description = "콘서트 날짜 찾을 수 없음"),
    })
    @GetMapping("/{concertId}/dates/{concertDateId}/seats")
    public ResponseEntity<List<SeatResponse>> getAvailableSeats(
            @PathVariable UUID concertId,
            @PathVariable UUID concertDateId) throws CustomException {
        List<Seat> availableSeats = concertService.getAvailableSeats(concertId, concertDateId);
        List<SeatResponse> response = availableSeats.stream()
                .map(SeatResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }
}
