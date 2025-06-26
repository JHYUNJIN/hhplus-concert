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
//        return paymentService.getPayment(paymentId)
//                .map(PaymentResponse::from)
//                .map(response -> new ResponseEntity<>(response, HttpStatus.OK))
//                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // 1. PaymentService에서 Payment 객체를 직접 가져옵니다.
        Payment payment = paymentService.getPayment(paymentId);
        // 2. 가져온 Payment 객체를 PaymentResponse DTO로 변환합니다.
        PaymentResponse response = PaymentResponse.from(payment);
        // 3. 변환된 DTO를 OK 상태 코드와 함께 반환합니다.
        return new ResponseEntity<>(response, HttpStatus.OK);

        /*
        * 변경된 이유와 장점
        책임 분리: 서비스는 비즈니스 로직(조회, 예외 처리)에 집중, 컨트롤러는 응답 생성에 집중합니다.
        명확성: Optional을 컨트롤러까지 노출하지 않고, 서비스에서 예외로 처리해 일관된 흐름을 유지합니다.
        확장성: 예외 발생 시 글로벌 예외 핸들러에서 일관된 에러 응답을 관리할 수 있습니다.
        *
        기존에는 Optional을 사용하여 결제 정보를 조회했지만, 이제는 서비스에서 직접 Payment 객체를 반환합니다. Optional은 불필요한 복잡성을 초래할 수 있습니다.
        명확한 책임 분리:
        서비스 계층: 비즈니스 로직(데이터 조회 및 예외 발생)에만 집중하고, Optional을 통한 "값이 없을 수도 있음" 처리는 이제 서비스 내부에서 orElseThrow()를 통해 예외로 명확히 처리합니다.
        컨트롤러 계층: 서비스가 던진 예외는 GlobalExceptionHandler가 알아서 처리하므로, 컨트롤러는 오직 정상적인 경우에만 데이터 변환 및 응답에 집중할 수 있습니다. 코드가 훨씬 간결하고 읽기 쉬워져요.
        일관된 오류 응답: 리소스(Payment)를 찾지 못했을 때 GlobalExceptionHandler에서 정의한 ErrorResponse 형식으로 HttpStatus.NOT_FOUND를 반환하게 되므로, 클라이언트는 어떤 종류의 오류든 일관된 응답을 받게 됩니다.
        */
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