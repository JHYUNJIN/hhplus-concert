package kr.hhplus.be.server.usecase.payment.interactor;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.event.payment.PaymentSuccessEvent;
import kr.hhplus.be.server.domain.payment.*;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.queue.QueueTokenRepository;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import kr.hhplus.be.server.domain.seat.*;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.usecase.event.EventPublisher;
import kr.hhplus.be.server.usecase.payment.input.PaymentCommand;
import kr.hhplus.be.server.usecase.payment.output.PaymentOutput;
import kr.hhplus.be.server.usecase.payment.output.PaymentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentInteractorTest {

    @InjectMocks
    private PaymentInteractor paymentInteractor;

    @Mock
    private QueueTokenRepository queueTokenRepository;
    @Mock
    private SeatHoldRepository seatHoldRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentOutput paymentOutput;
    @Mock
    private PaymentDomainService paymentDomainService;
    @Mock
    private EventPublisher eventPublisher;

    // 결제 테스트에 필요한 데이터
    private UUID reservationId;
    private UUID queueTokenId;
    private UUID userId;
    private UUID concertId;
    private UUID seatId;
    private UUID paymentId;
    private UUID concertDateId;
    private PaymentCommand paymentCommand;
    private QueueToken queueToken;
    private Reservation reservation;
    private User user;
    private Payment payment;
    private Seat seat;

    @BeforeEach
    void beforeEach() {
        reservationId = UUID.randomUUID();
        queueTokenId = UUID.randomUUID();
        userId = UUID.randomUUID();
        concertId = UUID.randomUUID();
        seatId = UUID.randomUUID();
        paymentId = UUID.randomUUID();
        concertDateId = UUID.randomUUID();

        LocalDateTime now = LocalDateTime.now();
        paymentCommand = new PaymentCommand(reservationId, queueTokenId.toString());
        queueToken = QueueToken.activeTokenOf(queueTokenId, userId, concertId, 1000000);
        reservation = new Reservation(reservationId, userId, seatId, ReservationStatus.PENDING, now, now); // 예약 대기 상태로 설정
        user = new User(userId, BigDecimal.valueOf(100000), now, now);
        payment = new Payment(paymentId, userId, reservationId, BigDecimal.valueOf(10000), PaymentStatus.PENDING, null,
                now, now); // 결제 대기 상태로 설정
        seat = new Seat(seatId, concertDateId, 10, BigDecimal.valueOf(10000), SeatGrade.VIP, SeatStatus.RESERVED, now,
                now); // 좌석 예약 상태로 설정
    }

    @Test
    @DisplayName("결제_성공")
    void payment_Success() throws Exception {
        Payment successPayment = new Payment(paymentId, userId, reservationId, BigDecimal.valueOf(10000),
                PaymentStatus.SUCCESS, null, LocalDateTime.now(), LocalDateTime.now()); // 결제 성공 상태로 설정
        Reservation successReservation = new Reservation(reservationId, userId, seatId, ReservationStatus.SUCCESS,
                LocalDateTime.now(), LocalDateTime.now()); // 예약 성공 상태로 설정
        Seat successSeat = new Seat(seatId, concertDateId, 10, BigDecimal.valueOf(10000), SeatGrade.VIP,
                SeatStatus.ASSIGNED, LocalDateTime.now(), LocalDateTime.now()); // 좌석 배정 완료 상태로 설정
        User successUser = new User(userId, BigDecimal.valueOf(90000), LocalDateTime.now(), LocalDateTime.now()); // 잔액 차감 후 상태

        PaymentDomainResult domainResult = new PaymentDomainResult(successUser, successReservation, successPayment,
                successSeat);

        when(queueTokenRepository.findQueueTokenByTokenId(paymentCommand.queueTokenId())).thenReturn(queueToken);
        when(reservationRepository.findById(paymentCommand.reservationId())).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByReservationId(reservation.id())).thenReturn(Optional.of(payment));
        when(seatRepository.findById(reservation.seatId())).thenReturn(Optional.of(seat));
        when(userRepository.findById(queueToken.userId())).thenReturn(Optional.of(user));
        when(seatHoldRepository.isHoldSeat(seat.id(), user.id())).thenReturn(true);
        when(paymentDomainService.processPayment(reservation, payment, seat, user)).thenReturn(domainResult);
        when(reservationRepository.save(successReservation)).thenReturn(successReservation);
        when(paymentRepository.save(successPayment)).thenReturn(successPayment);
        when(seatRepository.save(successSeat)).thenReturn(successSeat);
        when(userRepository.save(successUser)).thenReturn(successUser);

        paymentInteractor.payment(paymentCommand);

        verify(queueTokenRepository, times(1)).findQueueTokenByTokenId(paymentCommand.queueTokenId());
        verify(reservationRepository, times(1)).findById(paymentCommand.reservationId());
        verify(paymentRepository, times(1)).findByReservationId(reservation.id());
        verify(seatRepository, times(1)).findById(reservation.seatId());
        verify(userRepository, times(1)).findById(queueToken.userId());
        verify(seatHoldRepository, times(1)).isHoldSeat(seat.id(), user.id());
        verify(paymentDomainService, times(1)).processPayment(reservation, payment, seat, user);
        verify(userRepository, times(1)).save(successUser);
        verify(paymentRepository, times(1)).save(successPayment);
        verify(reservationRepository, times(1)).save(successReservation);
        verify(seatRepository, times(1)).save(successSeat);
        verify(seatHoldRepository, times(1)).deleteHold(seat.id(), user.id()); // Redis에 저장된 좌석 예약 해제
        verify(queueTokenRepository, times(1)).expiresQueueToken(queueToken.tokenId().toString());
        verify(eventPublisher, times(1)).publish(any(PaymentSuccessEvent.class));
        verify(paymentOutput, times(1)).ok(any(PaymentResult.class));

    }

    @Test
    @DisplayName("결제_실패_대기열토큰유효하지않음")
    void payment_Failure_InvalidQueueToken() throws Exception {
        QueueToken waitingToken = QueueToken.waitingTokenOf(queueTokenId, userId, concertId, 10);

        when(queueTokenRepository.findQueueTokenByTokenId(paymentCommand.queueTokenId())).thenReturn(waitingToken);

        CustomException customException = assertThrows(CustomException.class,
                () -> paymentInteractor.payment(paymentCommand));

        verify(queueTokenRepository, times(1)).findQueueTokenByTokenId(paymentCommand.queueTokenId());
        verify(reservationRepository, never()).findById(any());
        verify(paymentRepository, never()).findByReservationId(any());
        verify(seatRepository, never()).findById(any());
        verify(userRepository, never()).findById(any());
        verify(seatHoldRepository, never()).isHoldSeat(any(), any());
        verify(paymentDomainService, never()).processPayment(any(), any(), any(), any());
        verify(userRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
        verify(reservationRepository, never()).save(any());
        verify(seatRepository, never()).save(any());
        verify(seatHoldRepository, never()).deleteHold(any(), any());
        verify(queueTokenRepository, never()).expiresQueueToken(any());
        verify(eventPublisher, never()).publish(any(PaymentSuccessEvent.class));
        verify(paymentOutput, never()).ok(any());

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INVALID_QUEUE_TOKEN);
    }

    @Test
    @DisplayName("결제_실패_예약정보찾지못함")
    void payment_Failure_ReservationNotFound() throws Exception {
        when(queueTokenRepository.findQueueTokenByTokenId(paymentCommand.queueTokenId())).thenReturn(queueToken);
        when(reservationRepository.findById(paymentCommand.reservationId())).thenReturn(Optional.empty());

        CustomException customException = assertThrows(CustomException.class,
                () -> paymentInteractor.payment(paymentCommand));

        verify(queueTokenRepository, times(1)).findQueueTokenByTokenId(paymentCommand.queueTokenId());
        verify(reservationRepository, times(1)).findById(paymentCommand.reservationId());
        verify(paymentRepository, never()).findByReservationId(any());
        verify(seatRepository, never()).findById(any());
        verify(userRepository, never()).findById(any());
        verify(seatHoldRepository, never()).isHoldSeat(any(), any());
        verify(paymentDomainService, never()).processPayment(any(), any(), any(), any());

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.RESERVATION_NOT_FOUND);
    }

    @Test
    @DisplayName("결제_실패_결제정보찾지못함")
    void payment_Failure_PaymentNotFound() throws Exception {
        when(queueTokenRepository.findQueueTokenByTokenId(paymentCommand.queueTokenId())).thenReturn(queueToken);
        when(reservationRepository.findById(paymentCommand.reservationId())).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByReservationId(reservation.id())).thenReturn(Optional.empty());

        CustomException customException = assertThrows(CustomException.class,
                () -> paymentInteractor.payment(paymentCommand));

        verify(queueTokenRepository, times(1)).findQueueTokenByTokenId(paymentCommand.queueTokenId());
        verify(reservationRepository, times(1)).findById(paymentCommand.reservationId());
        verify(paymentRepository, times(1)).findByReservationId(reservation.id());
        verify(seatRepository, never()).findById(any());
        verify(userRepository, never()).findById(any());
        verify(seatHoldRepository, never()).isHoldSeat(any(), any());
        verify(paymentDomainService, never()).processPayment(any(), any(), any(), any());
        verify(userRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
        verify(reservationRepository, never()).save(any());
        verify(seatRepository, never()).save(any());
        verify(seatHoldRepository, never()).deleteHold(any(), any());
        verify(queueTokenRepository, never()).expiresQueueToken(any());
        verify(eventPublisher, never()).publish(any(PaymentSuccessEvent.class));
        verify(paymentOutput, never()).ok(any());

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("결제_실패_좌석정보찾지못함")
    void payment_Failure_SeatNotFound() throws Exception {
        when(queueTokenRepository.findQueueTokenByTokenId(paymentCommand.queueTokenId())).thenReturn(queueToken);
        when(reservationRepository.findById(paymentCommand.reservationId())).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByReservationId(reservation.id())).thenReturn(Optional.of(payment));
        when(seatRepository.findById(reservation.seatId())).thenReturn(Optional.empty());

        CustomException customException = assertThrows(CustomException.class,
                () -> paymentInteractor.payment(paymentCommand));

        verify(queueTokenRepository, times(1)).findQueueTokenByTokenId(paymentCommand.queueTokenId());
        verify(reservationRepository, times(1)).findById(paymentCommand.reservationId());
        verify(paymentRepository, times(1)).findByReservationId(reservation.id());
        verify(seatRepository, times(1)).findById(reservation.seatId());
        verify(userRepository, never()).findById(any());
        verify(seatHoldRepository, never()).isHoldSeat(any(), any());
        verify(eventPublisher, never()).publish(any(PaymentSuccessEvent.class));
        verify(paymentDomainService, never()).processPayment(any(), any(), any(), any());
        verify(userRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
        verify(reservationRepository, never()).save(any());
        verify(seatRepository, never()).save(any());
        verify(seatHoldRepository, never()).deleteHold(any(), any());
        verify(queueTokenRepository, never()).expiresQueueToken(any());
        verify(eventPublisher, never()).publish(any(PaymentSuccessEvent.class));
        verify(paymentOutput, never()).ok(any());

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.SEAT_NOT_FOUND);
    }

    @Test
    @DisplayName("결제_실패_유저정보찾지못함")
    void payment_Failure_UserNotFound() throws Exception {
        when(queueTokenRepository.findQueueTokenByTokenId(paymentCommand.queueTokenId())).thenReturn(queueToken);
        when(reservationRepository.findById(paymentCommand.reservationId())).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByReservationId(reservation.id())).thenReturn(Optional.of(payment));
        when(seatRepository.findById(reservation.seatId())).thenReturn(Optional.of(seat));
        when(userRepository.findById(queueToken.userId())).thenReturn(Optional.empty());

        CustomException customException = assertThrows(CustomException.class,
                () -> paymentInteractor.payment(paymentCommand));

        verify(queueTokenRepository, times(1)).findQueueTokenByTokenId(paymentCommand.queueTokenId());
        verify(reservationRepository, times(1)).findById(paymentCommand.reservationId());
        verify(paymentRepository, times(1)).findByReservationId(reservation.id());
        verify(seatRepository, times(1)).findById(reservation.seatId());
        verify(userRepository, times(1)).findById(queueToken.userId());
        verify(seatHoldRepository, never()).isHoldSeat(any(), any());
        verify(paymentDomainService, never()).processPayment(any(), any(), any(), any());
        verify(userRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
        verify(reservationRepository, never()).save(any());
        verify(seatRepository, never()).save(any());
        verify(seatHoldRepository, never()).deleteHold(any(), any());
        verify(queueTokenRepository, never()).expiresQueueToken(any());
        verify(eventPublisher, never()).publish(any(PaymentSuccessEvent.class));
        verify(paymentOutput, never()).ok(any());

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("결제_실패_임시배정끝남")
    void payment_Failure_ExpiredSeatHold() throws Exception {
        when(queueTokenRepository.findQueueTokenByTokenId(paymentCommand.queueTokenId())).thenReturn(queueToken);
        when(reservationRepository.findById(paymentCommand.reservationId())).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByReservationId(reservation.id())).thenReturn(Optional.of(payment));
        when(seatRepository.findById(reservation.seatId())).thenReturn(Optional.of(seat));
        when(userRepository.findById(queueToken.userId())).thenReturn(Optional.of(user));
        when(seatHoldRepository.isHoldSeat(seat.id(), user.id())).thenReturn(false);

        CustomException customException = assertThrows(CustomException.class,
                () -> paymentInteractor.payment(paymentCommand));

        verify(queueTokenRepository, times(1)).findQueueTokenByTokenId(paymentCommand.queueTokenId());
        verify(reservationRepository, times(1)).findById(paymentCommand.reservationId());
        verify(paymentRepository, times(1)).findByReservationId(reservation.id());
        verify(seatRepository, times(1)).findById(reservation.seatId());
        verify(userRepository, times(1)).findById(queueToken.userId());
        verify(seatHoldRepository, times(1)).isHoldSeat(seat.id(), user.id());
        verify(paymentDomainService, never()).processPayment(any(), any(), any(), any());
        verify(userRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
        verify(reservationRepository, never()).save(any());
        verify(seatRepository, never()).save(any());
        verify(seatHoldRepository, never()).deleteHold(any(), any());
        verify(queueTokenRepository, never()).expiresQueueToken(any());
        verify(eventPublisher, never()).publish(any(PaymentSuccessEvent.class));
        verify(paymentOutput, never()).ok(any());

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.SEAT_NOT_HOLD);
    }
}