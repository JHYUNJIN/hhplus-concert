package kr.hhplus.be.server.api.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.usecase.payment.input.PaymentCommand;
import kr.hhplus.be.server.usecase.payment.input.PaymentInput;
import kr.hhplus.be.server.usecase.payment.output.PaymentOutput;
import kr.hhplus.be.server.usecase.payment.output.PaymentResult;
import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.api.payment.dto.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

@RestController
@RequestScope
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment API", description = "결제 관련 API")
public class PaymentController implements PaymentOutput {

    private final PaymentInput paymentInput;
    private PaymentResponse paymentResponse;

    @Operation(
            summary = "예약 결제 API",
            description = "콘서트 좌석 예약 결제"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "결제 성공",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404 - User",
                    description = "유저 찾을 수 없음"
            ),
            @ApiResponse(
                    responseCode = "404 - Reservation",
                    description = "예약 찾을 수 없음"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "사용자 잔액 부족"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "토큰 만료 등 토큰이 잘못됨"
            )
    })
    @PostMapping("/{reservationId}")
    public ResponseEntity<PaymentResponse> payReservation(
            @PathVariable UUID reservationId,
            @RequestHeader(value = "Authorization") String queueToken
    ) throws CustomException {
        String parsedQueueToken = queueToken.substring("Bearer ".length());
        paymentInput.payment(PaymentCommand.of(reservationId, parsedQueueToken));

        return ResponseEntity.ok(paymentResponse);
    }

    @Override
    public void ok(PaymentResult paymentResult) {
        paymentResponse = PaymentResponse.from(paymentResult);
    }
}
