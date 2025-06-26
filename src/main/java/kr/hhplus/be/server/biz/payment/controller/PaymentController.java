package kr.hhplus.be.server.biz.payment.controller;

import kr.hhplus.be.server.biz.payment.dto.PaymentRequest;
import kr.hhplus.be.server.biz.payment.dto.PaymentResponse;
import kr.hhplus.be.server.biz.payment.service.PaymentService;
import kr.hhplus.be.server.domain.payment.Payment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 결제 요청을 처리합니다.
     * POST /api/payments
     * @param request 결제 요청 정보 (userId, reservationId, amount)
     * @return 결제 처리 결과
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        try {
            Payment payment = paymentService.processPayment(request.getUserId(), request.getReservationId(), request.getAmount());
            return new ResponseEntity<>(PaymentResponse.from(payment), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // 더 구체적인 예외 처리 및 에러 메시지 반환 로직 필요
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 특정 결제 정보를 조회합니다.
     * GET /api/payments/{paymentId}
     * @param paymentId 조회할 결제 ID
     * @return 결제 정보
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String paymentId) {
        return paymentService.getPayment(paymentId)
                .map(PaymentResponse::from)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * 특정 사용자의 결제 내역을 조회합니다.
     * GET /api/payments/users/{userId}
     * @param userId 사용자 ID
     * @return 결제 내역 목록
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(@PathVariable String userId) {
        List<Payment> payments = paymentService.getPaymentsByUserId(userId);
        List<PaymentResponse> responses = payments.stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
}