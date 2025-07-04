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
        validatePayment(payment);
        validateUserBalance(payment, user);

        User paidUser 				= user.payment(payment.amount());
        Reservation paidReservation = reservation.payment();
        Payment paidPayment 		= payment.success();
        Seat paidSeat 				= seat.payment();

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
