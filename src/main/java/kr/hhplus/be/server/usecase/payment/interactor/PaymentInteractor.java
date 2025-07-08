package kr.hhplus.be.server.usecase.payment.interactor;

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
import kr.hhplus.be.server.usecase.event.EventPublisher;
import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import kr.hhplus.be.server.usecase.payment.input.PaymentCommand;
import kr.hhplus.be.server.usecase.payment.input.PaymentInput;
import kr.hhplus.be.server.usecase.payment.output.PaymentOutput;
import kr.hhplus.be.server.usecase.payment.output.PaymentResult;
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
            seatHoldRepository.deleteHold(savedSeat.id(), savedUser.id()); // Redis에 저장된 좌석 예약 해제
            queueTokenRepository.expiresQueueToken(queueToken.tokenId().toString()); // 토큰 만료 처리

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


/* 서비스와 인터렉터의 차이점 **
서비스와 인터랙터의 주요 차이점은 의존성 관리와 책임 분리에 있습니다.

기존 서비스는 입출력 처리와 비즈니스 로직이 명확히 분리되지 않아 컨트롤러와의 의존성이 높았습니다. 하지만 인터랙터는 입출력(Input/Output)과 핵심 비즈니스 로직을 명확하게 구분합니다.

이러한 구조는 **의존성 역전 원칙(DIP)**을 준수하여 아키텍처의 안정성과 확장성을 크게 높입니다. 인터랙터는 비즈니스 로직을 외부 계층(컨트롤러, 영속성 등)으로부터 독립적으로 분리시켜, 다음 이점들을 제공합니다.

테스트 용이성 확장: 비즈니스 로직이 독립적이므로 단위 테스트를 더 쉽고 효과적으로 수행할 수 있습니다.

높은 재사용성: 외부 환경(예: 데이터베이스, UI 프레임워크)이 변경되더라도 핵심 비즈니스 로직은 수정 없이 재사용될 수 있습니다.

명확한 책임: 각 계층의 책임이 명확해져 코드의 가독성과 유지보수성이 향상됩니다.

결과적으로 인터랙터는 비즈니스 로직의 독립성을 보장하고, 시스템의 견고함과 유연성을 증대시키는 데 중요한 역할을 합니다.
 */