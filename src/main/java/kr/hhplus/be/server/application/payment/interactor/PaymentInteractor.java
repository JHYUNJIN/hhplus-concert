package kr.hhplus.be.server.application.payment.interactor;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.event.payment.PaymentSuccessEvent;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentDomainResult;
import kr.hhplus.be.server.domain.payment.PaymentDomainService;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import kr.hhplus.be.server.application.event.EventPublisher;
import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import kr.hhplus.be.server.application.payment.input.PaymentCommand;
import kr.hhplus.be.server.application.payment.input.PaymentInput;
import kr.hhplus.be.server.application.payment.output.PaymentOutput;
import kr.hhplus.be.server.application.payment.output.PaymentResult;
import kr.hhplus.be.server.domain.queue.QueueTokenRepository;
import kr.hhplus.be.server.domain.queue.QueueTokenUtil;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.seat.SeatHoldRepository;
import kr.hhplus.be.server.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PaymentInteractor implements PaymentInput {

    private final QueueTokenRepository queueTokenRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentOutput paymentOutput;
    private final PaymentDomainService paymentDomainService;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public void payment(PaymentCommand command) throws CustomException {
        try {
            // 토큰 검증
            QueueToken queueToken = getQueueTokenAndValid(command);
            System.out.println("🚀[로그:정현진] queueToken : " + queueToken);

            // 예약, 결제, 좌석, 사용자 정보 조회
            Reservation reservation = getReservation(command);
            Payment payment = getPayment(reservation);
            Seat seat = getSeat(reservation);
            User user = getUser(queueToken.userId());

            // 좌석 예약 상태 확인
            validateSeatHold(seat.id(), user.id());

            System.out.println("🚀[로그:정현진] @01");
            // 결제 진행
            PaymentDomainResult result = paymentDomainService.processPayment(reservation, payment, seat, user);

            System.out.println("🚀[로그:정현진] @02");
            // 결제 성공 시 데이터 저장 및 이벤트 발행
            User        savedUser        = userRepository.save(result.user());
            Reservation savedReservation = reservationRepository.save(result.reservation());
            Payment     savedPayment     = paymentRepository.save(result.payment());
            Seat        savedSeat        = seatRepository.save(result.seat());

            System.out.println("🚀[로그:정현진] @03");
            seatHoldRepository.deleteHold(savedSeat.id(), savedUser.id());
            queueTokenRepository.expiresQueueToken(queueToken.tokenId().toString());

            eventPublisher.publish(PaymentSuccessEvent.of(savedPayment, savedReservation, savedSeat, savedUser));
            paymentOutput.ok(PaymentResult.of(savedPayment, savedSeat, savedReservation.id(), savedUser.id()));
        } catch (CustomException e) {
            log.warn("결제 진행 중 비즈니스 예외 발생 - {}", e.getErrorCode().name());
            throw e;
        } catch (Exception e) {
            log.error("결제 진행 중 예외 발생 - {}", ErrorCode.INTERNAL_SERVER_ERROR, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private User getUser(UUID userId) throws CustomException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Seat getSeat(Reservation reservation) throws CustomException {
        return seatRepository.findById(reservation.seatId())
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));
    }

    private Payment getPayment(Reservation reservation) throws CustomException {
        return paymentRepository.findByReservationId(reservation.id())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    private Reservation getReservation(PaymentCommand command) throws CustomException {
        return reservationRepository.findById(command.reservationId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    private QueueToken getQueueTokenAndValid(PaymentCommand command) throws CustomException, JsonProcessingException {
        QueueToken queueToken = queueTokenRepository.findQueueTokenByTokenId(command.queueTokenId());
        QueueTokenUtil.validateActiveQueueToken(queueToken);
        return queueToken;
    }

    private void validateSeatHold(UUID seatId, UUID userId) throws CustomException {
        if (!seatHoldRepository.isHoldSeat(seatId, userId))
            throw new CustomException(ErrorCode.SEAT_NOT_HOLD);
    }
}
