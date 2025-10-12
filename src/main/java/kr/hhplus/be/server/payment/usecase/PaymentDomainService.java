package kr.hhplus.be.server.payment.usecase;

import kr.hhplus.be.server.payment.port.in.dto.PaymentDomainResult;
import kr.hhplus.be.server.payment.domain.Payment;
import kr.hhplus.be.server.payment.domain.enums.PaymentStatus;
import kr.hhplus.be.server.queue.domain.QueueToken;
import org.springframework.stereotype.Service;

import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.concert.domain.Seat;
// import kr.hhplus.be.server.user.domain.User; // 삭제
import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal; // BigDecimal 임포트 추가
import java.util.UUID; // UUID 임포트 추가

@Service
@RequiredArgsConstructor
public class PaymentDomainService {

    // User 객체 대신 userId와 userBalance를 받도록 시그니처 변경
    public PaymentDomainResult processPayment(Reservation reservation, Payment payment, Seat seat, UUID userId, BigDecimal userBalance, QueueToken queueToken) throws CustomException {

        // 결제 상태가 PROCESSING이 아니면 예외 발생
        if (payment.status() != PaymentStatus.PROCESSING) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED, "결제가 이미 처리 중이거나 완료되었습니다.");
        }
        validatePayment(payment);
        validateUserBalance(userBalance, payment.amount()); // userBalance와 payment.amount() 사용

        // 각 도메인 객체가 외부에 의존하지 않고 자신의 상태를 직접 변경하는 방식으로 결제 처리
        // User paidUser = user.payment(payment.amount()); // User 객체 변경 로직 제거
        Reservation paidReservation = reservation.payment();
        Payment paidPayment = payment.success();
        Seat paidSeat = seat.payment();

        // PaymentDomainResult도 User 객체 대신 userId를 포함하도록 변경해야 함
        return new PaymentDomainResult(queueToken, userId, paidReservation, paidPayment, paidSeat);
    }

    // User 객체 대신 userBalance와 paymentAmount를 받도록 시그니처 변경
    private void validateUserBalance(BigDecimal userBalance, BigDecimal paymentAmount) throws CustomException {
        if (userBalance.compareTo(paymentAmount) < 0) // 잔액 비교
            throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
    }

    private void validatePayment(Payment payment) throws CustomException {
        if (!payment.checkAmount())
            throw new CustomException(ErrorCode.INVALID_PAYMENT_AMOUNT);

        if (payment.isPaid())
            throw new CustomException(ErrorCode.ALREADY_PAID);
    }
}
