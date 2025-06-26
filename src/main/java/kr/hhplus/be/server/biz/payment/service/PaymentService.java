package kr.hhplus.be.server.biz.payment.service;

import kr.hhplus.be.server.biz.reservation.service.ReservationService;
import kr.hhplus.be.server.biz.user.service.UserService;
import kr.hhplus.be.server.domain.enums.PaymentStatus;
import kr.hhplus.be.server.domain.enums.ReservationStatus;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserService userService; // User의 잔액을 사용하기 위해
    private final ReservationService reservationService; // 예약 상태를 변경하기 위해
    private final ReservationRepository reservationRepository; // 추가적으로 Reservation 엔티티를 찾기 위해 (순환 참조 방지 등)

    public PaymentService(PaymentRepository paymentRepository,
                          UserService userService,
                          ReservationService reservationService,
                          ReservationRepository reservationRepository) {
        this.paymentRepository = paymentRepository;
        this.userService = userService;
        this.reservationService = reservationService;
        this.reservationRepository = reservationRepository;
    }

    /**
     * 결제 요청을 처리하고 사용자의 잔액을 차감하며 예약 상태를 확정합니다.
     * @param userId 결제 요청 사용자 ID
     * @param reservationId 결제 대상 예약 ID
     * @param paymentAmount 결제 금액 (예약된 좌석의 가격과 일치해야 함)
     * @return 생성된 Payment 객체 (결제 결과 포함)
     * @throws IllegalArgumentException 예약/사용자/금액 불일치 또는 결제 실패 시
     */
    @Transactional
    public Payment processPayment(String userId, String reservationId, BigDecimal paymentAmount) {
        // 1. 예약 및 사용자 유효성 검사
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 예약 ID입니다: " + reservationId));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 예약은 요청한 사용자의 소유가 아닙니다.");
        }
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalArgumentException("결제할 수 없는 예약 상태입니다. 현재 상태: " + reservation.getStatus());
        }
        if (reservation.getSeat().getPrice().compareTo(paymentAmount) != 0) {
            throw new IllegalArgumentException("결제 금액이 예약된 좌석 가격과 일치하지 않습니다. 요청 금액: " + paymentAmount + ", 좌석 가격: " + reservation.getSeat().getPrice());
        }

        // 2. 사용자 잔액 사용 (UserService에 위임)
        PaymentStatus paymentStatus;
        String failureReason = null;
        try {
            userService.useBalance(userId, paymentAmount); // 동시성 제어는 UserService에서 담당
            paymentStatus = PaymentStatus.SUCCESS;
        } catch (IllegalArgumentException e) {
            paymentStatus = PaymentStatus.FAILED;
            failureReason = e.getMessage();
            // TODO: 결제 실패 시 ReservationService의 cancelReservation 등을 호출하여 예약 및 좌석 상태 롤백 로직 추가
            // reservationService.cancelReservation(reservationId);
            throw e; // 결제 실패 예외 다시 던지기
        }

        // 3. 결제 레코드 생성 및 저장
        String paymentId = UUID.randomUUID().toString();
        Payment payment = new Payment(paymentId, reservation.getUser(), reservation, paymentAmount, paymentStatus, failureReason);
        Payment savedPayment = paymentRepository.save(payment);

        // 4. 예약 상태 확정 (ReservationService에 위임)
        if (paymentStatus == PaymentStatus.SUCCESS) {
            reservationService.confirmReservation(reservationId); // 예약 확정 (좌석 상태 변경 포함)
        }

        return savedPayment;
    }

    /**
     * 특정 결제를 조회합니다.
     * @param paymentId 조회할 결제 ID
     * @return 조회된 Payment 객체 (Optional)
     */
    public Optional<Payment> getPayment(String paymentId) {
        return paymentRepository.findById(paymentId);
    }

    /**
     * 특정 사용자의 결제 내역을 조회합니다.
     * @param userId 사용자 ID
     * @return 결제 내역 목록
     */
    public List<Payment> getPaymentsByUserId(String userId) {
        return paymentRepository.findByUserId(userId);
    }
}