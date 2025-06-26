package kr.hhplus.be.server.infrastructure.web.reservation;

import kr.hhplus.be.server.infrastructure.web.reservation.dto.ReservationWebRequest;
import kr.hhplus.be.server.infrastructure.web.reservation.dto.ReservationWebResponse;
import kr.hhplus.be.server.application.reservation.ReservationService;
import kr.hhplus.be.server.domain.reservation.Reservation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * 좌석을 임시 배정합니다.
     * POST /api/reservations
     * @param request 예약 요청 정보 (userId, seatId)
     * @return 임시 배정된 예약 정보
     */
    @PostMapping
    public ResponseEntity<ReservationWebResponse> reserveSeatTemporarily(@Valid @RequestBody ReservationWebRequest request) {
        try {
            Reservation reservation = reservationService.reserveSeatTemporarily(request.getUserId(), request.getSeatId());
            return new ResponseEntity<>(ReservationWebResponse.from(reservation), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 구체적인 예외 처리 필요
        }
    }

    /**
     * 임시 배정된 예약을 최종 확정합니다.
     * POST /api/reservations/{reservationId}/confirm
     * @param reservationId 확정할 예약 ID
     * @return 확정된 예약 정보
     */
    @PostMapping("/{reservationId}/confirm")
    public ResponseEntity<ReservationWebResponse> confirmReservation(@PathVariable String reservationId) {
        try {
            Reservation reservation = reservationService.confirmReservation(reservationId);
            return new ResponseEntity<>(ReservationWebResponse.from(reservation), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 구체적인 예외 처리 필요
        }
    }

    /**
     * 예약을 취소합니다.
     * POST /api/reservations/{reservationId}/cancel
     * @param reservationId 취소할 예약 ID
     * @return 취소된 예약 정보
     */
    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<ReservationWebResponse> cancelReservation(@PathVariable String reservationId) {
        try {
            Reservation reservation = reservationService.cancelReservation(reservationId);
            return new ResponseEntity<>(ReservationWebResponse.from(reservation), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 구체적인 예외 처리 필요
        }
    }

    /**
     * 특정 사용자 ID의 예약 목록을 조회합니다.
     * GET /api/reservations/users/{userId}
     * @param userId 사용자 ID
     * @return 예약 목록
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<ReservationWebResponse>> getReservationsByUserId(@PathVariable String userId) {
        List<Reservation> reservations = reservationService.getReservationsByUserId(userId);
        List<ReservationWebResponse> responses = reservations.stream()
                .map(ReservationWebResponse::from)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
}