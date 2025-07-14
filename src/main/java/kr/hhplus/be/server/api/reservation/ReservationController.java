package kr.hhplus.be.server.api.reservation;

import java.util.UUID;

import kr.hhplus.be.server.api.reservation.dto.request.ReservationRequest;
import kr.hhplus.be.server.api.reservation.dto.response.ReservationResponse;
import kr.hhplus.be.server.usecase.payment.input.PaymentCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.usecase.reservation.input.ReservationInput;
import kr.hhplus.be.server.usecase.reservation.input.ReserveSeatCommand;
import kr.hhplus.be.server.usecase.reservation.output.ReservationOutput;
import kr.hhplus.be.server.usecase.reservation.output.ReserveSeatResult;
import lombok.RequiredArgsConstructor;

@RestController
@RequestScope
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation API", description = "예약 관련 API")
public class ReservationController implements ReservationOutput {

    private final ReservationInput reservationInput;
    private ReservationResponse reservationResponse;

    @Operation(
            summary = "콘서트 좌석 예약 API",
            description = "콘서트 좌석 예약"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "예약 성공",
                    content = @Content(schema = @Schema(implementation = ReservationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404 - User",
                    description = "유저 찾을 수 없음"
            ),
            @ApiResponse(
                    responseCode = "404 - Concert",
                    description = "콘서트 찾을 수 없음"
            ),
            @ApiResponse(
                    responseCode = "404 - Seat",
                    description = "좌석 찾을 수 없음"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "좌석이 AVAILABLE 상태가 아님"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "토큰 만료 등 토큰이 잘못됨"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "락 획득 실패 (다른 사용자 점유중)"
            )
    })
    @PostMapping("/seats/{seatId}")
    public ResponseEntity<ReservationResponse> reservationConcert(
            @PathVariable UUID seatId,
            @RequestBody ReservationRequest request,
            @RequestHeader(value = "Authorization") String queueToken
    ) throws CustomException {
        final String parsedQueueToken = queueToken.startsWith("Bearer ")
                ? queueToken.substring("Bearer ".length())
                : queueToken;
        reservationInput.reserveSeat(ReserveSeatCommand.of(request, seatId, parsedQueueToken));

        return ResponseEntity.ok(reservationResponse);
    }

    @Override
    public void ok(ReserveSeatResult result) {
        reservationResponse = ReservationResponse.from(result);
    }
}
