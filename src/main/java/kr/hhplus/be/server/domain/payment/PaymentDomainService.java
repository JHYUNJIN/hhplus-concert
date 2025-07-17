package kr.hhplus.be.server.domain.payment;

import org.springframework.stereotype.Service;

import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentDomainService {

    public PaymentDomainResult processPayment(Reservation reservation, Payment payment, Seat seat, User user) throws CustomException {

        // 결제 상태가 PROCESSING이 아니면 예외 발생
        if (payment.status() != PaymentStatus.PROCESSING) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED, "결제가 이미 처리 중이거나 완료되었습니다.");
        }
        validatePayment(payment);
        validateUserBalance(payment, user);

        // 각 도메인 객체가 외부에 의존하지 않고 자신의 상태를 직접 변경하는 방식으로 결제 처리
        User paidUser = user.payment(payment.amount());
        Reservation paidReservation = reservation.payment();
        Payment paidPayment = payment.success();
        Seat paidSeat = seat.payment();

        return new PaymentDomainResult(paidUser, paidReservation, paidPayment, paidSeat);
    }

    private void validateUserBalance(Payment payment, User user) throws CustomException {
        if (!user.checkEnoughAmount(payment.amount()))
            throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
    }

    private void validatePayment(Payment payment) throws CustomException {
        if (!payment.checkAmount())
            throw new CustomException(ErrorCode.INVALID_PAYMENT_AMOUNT);

        if (payment.isPaid())
            throw new CustomException(ErrorCode.ALREADY_PAID);
    }
}
