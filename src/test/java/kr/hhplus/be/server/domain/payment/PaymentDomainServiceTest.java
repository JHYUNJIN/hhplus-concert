package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.payment.domain.enums.PaymentStatus;
import kr.hhplus.be.server.payment.port.in.dto.PaymentDomainResult;
import kr.hhplus.be.server.payment.usecase.PaymentDomainService;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.payment.domain.Payment;
import kr.hhplus.be.server.reservation.domain.enums.ReservationStatus;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.domain.enums.SeatGrade;
import kr.hhplus.be.server.concert.domain.enums.SeatStatus;
import kr.hhplus.be.server.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentDomainServiceTest {

    @InjectMocks
    private PaymentDomainService paymentDomainService;

    private UUID userId;
    private UUID reservationId;
    private UUID paymentId;
    private UUID seatId;
    private UUID concertDateId;
    private User user;
    private Reservation reservation;
    private Payment payment;
    private Seat seat;

    @BeforeEach
    void beforeEach() {
        LocalDateTime now = LocalDateTime.now();
        userId = UUID.randomUUID();
        reservationId = UUID.randomUUID();
        paymentId = UUID.randomUUID();
        seatId = UUID.randomUUID();
        concertDateId = UUID.randomUUID();

        user = new User(userId, BigDecimal.valueOf(100000), now, now);
        reservation = new Reservation(reservationId, userId, seatId, ReservationStatus.PENDING, now, now);
        payment = new Payment(paymentId, userId, reservationId, BigDecimal.valueOf(10000), PaymentStatus.PENDING, null,
                now, now);
        seat = new Seat(seatId, concertDateId, 10, BigDecimal.valueOf(10000), SeatGrade.VIP, SeatStatus.RESERVED, now,
                now);
    }

    @Test
    @DisplayName("결제_처리_성공")
    void processPayment_Success() throws CustomException {
        PaymentDomainResult result = paymentDomainService.processPayment(reservation, payment, seat, user);

        assertThat(result).isNotNull();
        assertThat(result.user().amount()).isEqualTo(BigDecimal.valueOf(90000));
        assertThat(result.reservation().status()).isEqualTo(ReservationStatus.SUCCESS);
        assertThat(result.payment().status()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(result.seat().status()).isEqualTo(SeatStatus.ASSIGNED);
    }

    @Test
    @DisplayName("결제_처리_실패_이미결제됨")
    void processPayment_Failure_AlreadyPaid() {
        Payment paidPayment = mock(Payment.class);
        when(paidPayment.checkAmount()).thenReturn(true);
        when(paidPayment.isPaid()).thenReturn(true);

        CustomException customException = assertThrows(CustomException.class,
                () -> paymentDomainService.processPayment(reservation, paidPayment, seat, user));

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.ALREADY_PAID);
    }

    @Test
    @DisplayName("결제_처리_실패_잔액부족")
    void processPayment_Failure_InsufficientBalance() {
        User poorUser = new User(userId, BigDecimal.valueOf(5000), LocalDateTime.now(), LocalDateTime.now());

        CustomException customException = assertThrows(CustomException.class,
                () -> paymentDomainService.processPayment(reservation, payment, seat, poorUser));

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_BALANCE);
    }

    @Test
    @DisplayName("결제_처리_실패_금액0원")
    void processPayment_Failure_ZeroAmount() {
        Payment zeroPayment = new Payment(paymentId, userId, reservationId, BigDecimal.ZERO, PaymentStatus.PENDING,
                null, LocalDateTime.now(), LocalDateTime.now());

        CustomException customException = assertThrows(CustomException.class,
                () -> paymentDomainService.processPayment(reservation, zeroPayment, seat, user));

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PAYMENT_AMOUNT);
    }

    @Test
    @DisplayName("결제_처리_실패_음수금액")
    void processPayment_Failure_NegativeAmount() {
        Payment negativePayment = new Payment(paymentId, userId, reservationId, BigDecimal.valueOf(-1000),
                PaymentStatus.PENDING, null, LocalDateTime.now(), LocalDateTime.now());

        CustomException customException = assertThrows(CustomException.class,
                () -> paymentDomainService.processPayment(reservation, negativePayment, seat, user));

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PAYMENT_AMOUNT);
    }
}