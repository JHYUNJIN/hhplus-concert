package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.application.reservation.ReservationService;
import kr.hhplus.be.server.application.user.UserService;
import kr.hhplus.be.server.common.exception.domain.PaymentException;
import kr.hhplus.be.server.common.exception.domain.ReservationException;
import kr.hhplus.be.server.common.exception.domain.UserException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.enums.PaymentStatus;
import kr.hhplus.be.server.domain.enums.ReservationStatus;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserService userService; // User의 잔액을 사용하기 위해
    private final ReservationService reservationService; // 예약 상태를 변경하기 위해
    private final ReservationRepository reservationRepository; // 추가적으로 Reservation 엔티티를 찾기 위해 (순환 참조 방지 등)
    private final ConcertRepository concertRepository; // 좌석 상태 변경을 위해

    public PaymentService(PaymentRepository paymentRepository,
                          UserService userService,
                          ReservationService reservationService,
                          ReservationRepository reservationRepository
    , ConcertRepository concertRepository) {
        this.paymentRepository = paymentRepository;
        this.userService = userService;
        this.reservationService = reservationService;
        this.reservationRepository = reservationRepository;
        this.concertRepository = concertRepository; // 좌석 상태 변경을 위해
    }

    /**
     * 결제 요청을 처리하고 사용자의 잔액을 차감하며 예약 상태를 확정합니다.
     * @param userId 결제 요청 사용자 ID
     * @param reservationId 결제 대상 예약 ID
     * @param paymentAmount 결제 금액 (예약된 좌석의 가격과 일치해야 함)
     * @return 생성된 Payment 객체 (결제 결과 포함)
     * @throws ReservationException 예약 불일치 또는 잘못된 상태일 때
     * @throws UserException        사용자 또는 잔액 부족 시
     * @throws PaymentException     결제 금액 불일치 또는 기타 결제 실패 시
     */
    @Transactional
    public Payment processPayment(String userId, String reservationId, BigDecimal paymentAmount) {
        // 1. 예약 및 사용자 유효성 검사
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException(ErrorCode.RESERVATION_NOT_FOUND, "유효하지 않은 예약 ID입니다: " + reservationId));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new ReservationException(ErrorCode.RESERVATION_OWNERSHIP_MISMATCH, "해당 예약은 요청한 사용자의 소유가 아닙니다.");
        }
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new ReservationException(ErrorCode.RESERVATION_INVALID_STATUS, "결제할 수 없는 예약 상태입니다. 현재 상태: " + reservation.getStatus());
        }
        if (reservation.getSeat().getPrice().compareTo(paymentAmount) != 0) {
            throw new PaymentException(ErrorCode.PAYMENT_INVALID_AMOUNT, "결제 금액이 예약된 좌석 가격과 일치하지 않습니다. 요청 금액: " + paymentAmount + ", 좌석 가격: " + reservation.getSeat().getPrice());
        }

        // 2. 사용자 잔액 사용 (UserService에 위임 - 여기서 잔액 부족 시 UserException 발생)
        // 이 트랜잭션 내에서 useBalance 호출 시, User 엔티티에 대한 락이 걸리므로 동시성 안전
        PaymentStatus paymentStatus;
        String failureReason = null;
        Payment savedPayment;

        try {
            userService.useBalance(userId, paymentAmount); // 동시성 제어는 UserService에서 담당, 잔액 부족 시 UserException 발생
            paymentStatus = PaymentStatus.SUCCESS;
        } catch (UserException e) {
            paymentStatus = PaymentStatus.FAILED;
            failureReason = e.getMessage();
            // UserException이 발생하면 결제 실패로 기록하고 예외를 다시 던짐
            // TODO: 결제 실패 시 예약 상태를 FAILED로 변경하고 좌석 상태를 AVAILABLE로 되돌리는 로직 추가
            // reservationService.cancelReservation(reservationId); // cancelReservation에 내부적으로 환불이 있다면 주의

            // 결제 실패 레코드만 생성 후 예외 던지기
            String paymentId = UUID.randomUUID().toString();
            savedPayment = paymentRepository.save(new Payment(paymentId, reservation.getUser(), reservation, paymentAmount, paymentStatus, failureReason));
            throw e; // 사용자에게 잔액 부족 예외를 다시 던져 응답
        } catch (Exception e) { // 그 외 예상치 못한 모든 예외
            paymentStatus = PaymentStatus.FAILED;
            failureReason = e.getMessage();
            String paymentId = UUID.randomUUID().toString();
            savedPayment = paymentRepository.save(new Payment(paymentId, reservation.getUser(), reservation, paymentAmount, paymentStatus, failureReason));
            throw new PaymentException(ErrorCode.PAYMENT_FAILED, "예상치 못한 오류로 결제에 실패했습니다: " + e.getMessage());
        }

        // 3. 결제 레코드 생성 및 저장
        String paymentId = UUID.randomUUID().toString();
        savedPayment = paymentRepository.save(new Payment(paymentId, reservation.getUser(), reservation, paymentAmount, paymentStatus, failureReason));

        // 4. 예약 상태 확정
        // 결제가 성공했을 때만 예약 상태를 확정 (ReservationService의 confirmReservation)
        // confirmReservation은 이미 락을 건 예약을 파라미터로 받을 수도 있고, ID를 받을 수도 있음
        // 여기서는 ID를 넘겨주는 것이 서비스 계층 간의 의존성을 더 낮게 유지하는 방법.
        // confirmReservation 내에서 다시 락을 걸기 때문에 중복 락은 발생하지만 문제는 없음.
        reservation.setStatus(ReservationStatus.SUCCESS); // 엔티티 상태 변경
        // reservationService.confirmReservation(reservationId); // -> ReservationService 내부에 Transactional이 있어 별도 락 걸림
        // 여기서는 직접 엔티티 상태를 변경 후 저장 (현재 트랜잭션 범위 내에서)
        reservationRepository.save(reservation);
        // 좌석 상태도 업데이트 (Reservation 엔티티의 연관 관계를 통해 Seat 상태 변경 가능)
        reservation.getSeat().setStatus(kr.hhplus.be.server.domain.enums.SeatStatus.RESERVED); // 좌석 상태 최종 확정
        // userService.userRepository.save(reservation.getSeat()); // Seat Repository가 없으므로 User repo를 통해 save (비추천)
        // 또는 concertRepository를 통해 saveSeat 호출 (추천)
        concertRepository.saveSeat(reservation.getSeat()); // Seat 업데이트를 위한 별도 호출

        /*
        * 함수를 사용하지 않은 이유:

트랜잭션 중복 및 복잡성:

PaymentService.processPayment 메서드는 이미 @Transactional 어노테이션이 붙어 있습니다. 이 메서드가 실행되는 동안 하나의 큰 트랜잭션이 시작됩니다.
만약 이 안에서 reservationService.confirmReservation(reservationId);를 호출했다면, confirmReservation 메서드에도 @Transactional이 붙어 있기 때문에 새로운 트랜잭션이 중첩되어 시작되거나 (REQUIRES_NEW일 경우), 기존 트랜잭션에 참여하게 됩니다.
confirmReservation 내부에서 다시 reservationRepository.findByIdForUpdate()를 호출하면 불필요하게 동일한 Reservation 엔티티에 대한 락을 다시 시도하게 됩니다. 이는 성능 저하나 데드락 가능성을 높일 수 있습니다. 이미 processPayment 트랜잭션 내에서 reservationRepository.findByIdForUpdate(reservationId)를 통해 reservation 엔티티에 락을 걸고 가져왔기 때문에, 해당 엔티티는 이미 트랜잭션 컨텍스트 내에서 관리되고 있습니다.
현재 트랜잭션 내에서의 일관성 유지:

processPayment 트랜잭션은 사용자의 잔액을 차감하고, 결제 내역을 저장하며, 예약 상태를 확정하는 일련의 과정을 하나의 원자적인 작업으로 처리해야 합니다.
reservation.setStatus(ReservationStatus.SUCCESS);와 reservationRepository.save(reservation);를 직접 호출하는 것은 이 processPayment 트랜잭션 내에서 Reservation 엔티티의 상태 변경을 직접 영속성 컨텍스트에 반영하겠다는 의미입니다. 이렇게 하면 사용자 잔액 차감, 결제 기록, 예약 확정 이 세 가지 작업이 모두 동일한 하나의 트랜잭션 안에서 커밋되거나 롤백됩니다.
만약 reservationService.confirmReservation을 호출했다면, ReservationService가 자체적인 비즈니스 로직과 트랜잭션을 가질 수 있어 PaymentService의 트랜잭션 흐름을 복잡하게 만들 수 있습니다.
서비스 간의 의존성 관리:

confirmReservation은 ReservationService의 핵심 비즈니스 로직으로, 예약 확정 자체에 필요한 모든 검증과 상태 변경을 담당합니다.
PaymentService는 confirmReservation의 모든 내부 로직을 알 필요 없이 "예약을 확정해야 한다"는 결과만 알면 됩니다. 하지만 현재 PaymentService의 트랜잭션에서 이미 예약 엔티티를 직접 변경하고 있기 때문에, confirmReservation의 전체 로직(검증 등)은 중복이 됩니다.
만약 confirmReservation이 추가적인 결제 관련 검증을 포함하거나, 락을 거는 역할만 담당한다면 호출하는 것이 적절하지만, 여기서는 이미 PaymentService에서 모든 검증과 락을 처리했으므로 직접 상태를 변경하는 것이 간결합니다.
더 나은 설계 고려사항 (TODO 주석 참고):
코드에 달린 TODO 주석처럼, PaymentService에서 예약과 좌석 상태를 변경하는 방식은 약간의 아쉬운 점이 있습니다.

userService.userRepository.save(reservation.getSeat()); 이 부분은 PaymentService가 Seat 엔티티를 userRepository를 통해 저장하는, 어색한 의존성을 만듭니다. Seat는 ConcertRepository에 의해 관리되는 것이 더 자연스럽습니다.

이상적인 해결책: PaymentService에서는 UserService를 통해 금액을 차감하고, ReservationService를 통해 예약을 확정하는 '조직' 역할에 집중하는 것이 좋습니다.

Java

// PaymentService.java 내 processPayment 메서드 (권장하는 개선 방향)
@Transactional
public Payment processPayment(String userId, String reservationId, BigDecimal paymentAmount) {
    // ... (예약 및 사용자 유효성 검사, 결제 금액 검사) ...

    PaymentStatus paymentStatus;
    String failureReason = null;
    Payment savedPayment;

    try {
        // 1. 사용자 잔액 사용 (UserService에 위임)
        userService.useBalance(userId, paymentAmount);
        paymentStatus = PaymentStatus.SUCCESS;
    } catch (UserException e) {
        paymentStatus = PaymentStatus.FAILED;
        failureReason = e.getMessage();
        // 2. 결제 실패 시 결제 레코드 생성 및 예외 던지기
        String paymentId = UUID.randomUUID().toString();
        savedPayment = paymentRepository.save(new Payment(paymentId, reservation.getUser(), reservation, paymentAmount, paymentStatus, failureReason));

        // TODO: 결제 실패 시 예약/좌석 상태 롤백을 예약 서비스에 위임
        // reservationService.handlePaymentFailure(reservationId);
        throw e; // 사용자에게 잔액 부족 예외를 다시 던져 응답
    } catch (Exception e) { // 그 외 예상치 못한 모든 예외
        // ... (동일하게 결제 실패 처리) ...
        throw new PaymentException(ErrorCode.PAYMENT_FAILED, "예상치 못한 오류로 결제에 실패했습니다: " + e.getMessage());
    }

    // 3. 결제 레코드 생성 및 저장 (성공 시)
    String paymentId = UUID.randomUUID().toString();
    savedPayment = paymentRepository.save(new Payment(paymentId, reservation.getUser(), reservation, paymentAmount, paymentStatus, failureReason));

    // 4. 예약 상태 확정 (ReservationService에 위임)
    // 이 부분이 핵심: ReservationService가 자신의 트랜잭션과 락을 사용하여 예약을 확정하도록 함
    // PaymentService는 그 결과를 확인
    reservationService.confirmReservation(reservationId);

    return savedPayment;
}
이 방식은 각 서비스가 자신의 도메인 로직과 트랜잭션을 책임지도록 하여 서비스 간의 응집도는 높이고 결합도는 낮추는 데 더 기여합니다. 중복 락은 여전히 발생할 수 있지만, 각 서비스의 책임이 명확해지므로 장기적으로 유지보수에 더 유리할 수 있습니다.
        *
        * */

        return savedPayment;
    }

    /**
     * 특정 결제를 조회합니다.
     * @param paymentId 조회할 결제 ID
     * @return 조회된 Payment 객체
     * @throws PaymentException 결제 내역을 찾을 수 없을 때
     */
    public Payment getPayment(String paymentId) { // Optional 대신 Payment를 직접 반환
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException(ErrorCode.PAYMENT_NOT_FOUND, "결제 내역을 찾을 수 없습니다: " + paymentId));
    }

    /**
     * 특정 사용자의 결제 내역을 조회합니다.
     * @param userId 사용자 ID
     * @return 결제 내역 목록
     * @throws UserException 사용자를 찾을 수 없을 때
     */
    public List<Payment> getPaymentsByUserId(String userId) {
        User user = userService.getUser(userId);

        // 사용자가 존재하면 해당 사용자의 결제 내역을 리포지토리에서 조회
        return paymentRepository.findByUserId(user.getId()); // user.getId()를 사용하여 명확성 높임
    }
}