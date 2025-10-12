package kr.hhplus.be.server.payment.usecase;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.concert.domain.ConcertDate;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.port.out.ConcertDateRepository;
import kr.hhplus.be.server.concert.port.out.SeatRepository;
import kr.hhplus.be.server.external.UserApiClient; // UserApiClient 임포트
import kr.hhplus.be.server.payment.domain.Payment;
import kr.hhplus.be.server.payment.port.in.PaymentFailureCompensationUseCase;
import kr.hhplus.be.server.payment.port.out.PaymentRepository;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.port.out.ReservationRepository;
// import kr.hhplus.be.server.user.domain.User; // 삭제
// import kr.hhplus.be.server.user.port.out.UserRepository; // 삭제
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentFailureCompensationInteractor implements PaymentFailureCompensationUseCase {

    // private final UserRepository userRepository; // 삭제
    private final UserApiClient userApiClient; // 추가
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final SeatRepository seatRepository;
    private final ConcertDateRepository concertDateRepository;

    private static final Set<ErrorCode> PRE_CHARGE_ERRORS = Set.of(
            ErrorCode.INSUFFICIENT_BALANCE, ErrorCode.INVALID_PAYMENT_AMOUNT, ErrorCode.ALREADY_PAID,
            ErrorCode.ALREADY_PROCESSED, ErrorCode.RESERVATION_NOT_FOUND, ErrorCode.PAYMENT_NOT_FOUND,
            ErrorCode.SEAT_NOT_FOUND, ErrorCode.SEAT_NOT_HOLD, ErrorCode.USER_NOT_FOUND, ErrorCode.INVALID_QUEUE_TOKEN
    );

    @Override
    @Transactional // DB 관련 작업을 하나의 트랜잭션으로 묶습니다.
    public void compensate(UUID paymentId, UUID userId, UUID reservationId, UUID seatId, UUID concertDateId, String tokenId, BigDecimal amount, ErrorCode errorCode) {
        try {
            // --- DB 상태 복원 로직만 수행 ---
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
            paymentRepository.save(payment.fail());

            if (!PRE_CHARGE_ERRORS.contains(errorCode)) {
                // User user = userRepository.findById(userId) // 기존 코드
                //         .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                // userRepository.save(user.refund(amount)); // 기존 코드
                userApiClient.refundUserBalance(userId, amount).block(); // UserApiClient 사용
            }

            Reservation reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
            reservationRepository.save(reservation.fail());

            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));
            seatRepository.save(seat.expire());

            ConcertDate concertDate = concertDateRepository.findById(concertDateId)
                    .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_DATE_NOT_FOUND));
            concertDateRepository.save(concertDate.increaseAvailableSeatCount());
            log.info("DB 보상 처리 완료. PaymentId: {}", paymentId);
        } catch (Exception e) {
            log.error("DB 보상 처리 중 예외 발생. PaymentId: {}", paymentId, e);
            // DB 작업 실패 시에는 예외를 다시 던져서, 메시지 재처리를 유도하는 것이 좋습니다.
            throw e;
        }
    }
}