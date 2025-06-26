package kr.hhplus.be.server.biz.reservation.service;

import kr.hhplus.be.server.common.exception.ConcertException;
import kr.hhplus.be.server.common.exception.domain.ReservationException;
import kr.hhplus.be.server.common.exception.domain.UserException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import kr.hhplus.be.server.domain.enums.ReservationStatus;
import kr.hhplus.be.server.domain.enums.SeatStatus;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository; // 사용자 금액 확인/차감을 위해
    private final ConcertRepository concertRepository; // 좌석 상태 변경을 위해

    public ReservationService(ReservationRepository reservationRepository,
                              UserRepository userRepository,
                              ConcertRepository concertRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.concertRepository = concertRepository;
    }

    /**
     * 좌석을 임시 배정하고 예약 상태를 PENDING으로 설정합니다. (실제 Redis 로직은 별도 구현)
     * 이 메서드는 DB의 Seat 상태를 RESERVED로 변경하고 Reservation 레코드를 생성합니다.
     * 동시성 제어가 매우 중요합니다.
     *
     * @param userId 사용자 ID
     * @param seatId 임시 배정할 좌석 ID
     * @return 생성된 Reservation 객체
     * @throws UserException 사용자를 찾을 수 없을 때
     * @throws ConcertException 좌석을 찾을 수 없거나 좌석이 AVAILABLE이 아닐 때
     * @throws ReservationException 예약 마감 기한이 지났을 때
     */
    @Transactional(isolation = Isolation.READ_COMMITTED) // 비관적 락 + 읽기 커밋된 데이터
    public Reservation reserveSeatTemporarily(String userId, String seatId) {
        // 1. 사용자 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND, userId));

        // 2. 좌석 상태 확인 및 락 (비관적 락)
        Seat seat = concertRepository.findSeatByIdForUpdate(seatId) // 좌석에 락을 걸고 조회
                .orElseThrow(() -> new ConcertException(ErrorCode.SEAT_NOT_FOUND, "좌석을 찾을 수 없습니다: " + seatId));

        // 3. 콘서트 날짜 확인 및 마감 기한 검사
        ConcertDate concertDate = concertRepository.findConcertDateById(seat.getConcertDate().getId())
                .orElseThrow(() -> new ConcertException(ErrorCode.CONCERT_DATE_NOT_FOUND, "연관된 콘서트 날짜를 찾을 수 없습니다."));

        if (concertDate.getDeadline() != null && concertDate.getDeadline().isBefore(LocalDateTime.now())) {
            throw new ReservationException(ErrorCode.DEADLINE_PASSED, "해당 콘서트 날짜는 예약 마감 기한이 지났습니다.");
        }

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            if(seat.getStatus() == SeatStatus.RESERVED) {
                throw new ConcertException(ErrorCode.SEAT_ALREADY_RESERVED);
            }
            throw new ConcertException(ErrorCode.SEAT_NOT_AVAILABLE);
        }

        // 4. 좌석 상태 변경 (AVAILABLE -> RESERVED)
        seat.setStatus(SeatStatus.RESERVED);
        concertRepository.saveSeat(seat); // 변경된 좌석 상태 저장

        // 5. 예약 레코드 생성 (상태: PENDING)
        String reservationId = UUID.randomUUID().toString();
        Reservation reservation = new Reservation(reservationId, user, seat, ReservationStatus.PENDING);
        return reservationRepository.save(reservation);

        // TODO: Redis에 임시 배정 정보 저장 로직 추가 (예: 만료 시간 설정)
        // RedisTemplate.opsForValue().set("temp_reservation:" + reservationId, userId, 5, TimeUnit.MINUTES);
    }

    /**
     * 임시 배정된 예약을 최종 확정합니다. (결제 완료 후 호출)
     * 사용자의 잔액을 차감하고, 결제 상태를 기록합니다.
     * @param reservationId 확정할 예약 ID
     * @return 확정된 Reservation 객체
     * @throws ReservationException 예약을 찾을 수 없거나 이미 확정된 경우, 또는 유효하지 않은 상태일 때
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Reservation confirmReservation(String reservationId) {
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId) // 예약에 락을 걸고 조회
                .orElseThrow(() -> new ReservationException(ErrorCode.RESERVATION_NOT_FOUND, reservationId));

        if (reservation.getStatus() == ReservationStatus.SUCCESS) {
            throw new ReservationException(ErrorCode.RESERVATION_ALREADY_CONFIRMED);
        }
        if (reservation.getStatus() == ReservationStatus.CANCELLED || reservation.getStatus() == ReservationStatus.FAILED) {
            throw new ReservationException(ErrorCode.RESERVATION_INVALID_STATUS, "취소되거나 실패한 예약은 확정할 수 없습니다.");
        }

        // TODO: 실제 결제 서비스 호출 및 결제 처리 로직 추가 (UserService.useBalance 호출)
        // 예를 들어, PaymentService를 주입받아 결제를 수행
        // Payment payment = paymentService.processPayment(reservation.getUser().getId(), reservationId, reservation.getSeat().getPrice());
        // if (payment.getStatus() != PaymentStatus.SUCCESS) {
        //     // 결제 실패 시 예약 상태를 FAILED로 변경하고 좌석 상태를 AVAILABLE로 되돌림
        //     reservation.setStatus(ReservationStatus.FAILED);
        //     reservationRepository.save(reservation);
        //     reservation.getSeat().setStatus(SeatStatus.AVAILABLE);
        //     concertRepository.saveSeat(reservation.getSeat());
        //     throw new IllegalArgumentException("결제에 실패했습니다.");
        // }

        // 결제가 성공했다고 가정하고 예약 및 좌석 상태 업데이트
        reservation.setStatus(ReservationStatus.SUCCESS); // 예약 확정
        reservation.getSeat().setStatus(SeatStatus.RESERVED); // 좌석 상태를 최종 RESERVED로 유지 (SOLD OUT과 구분할 수 있음)
        // 만약 SOLD_OUT 상태가 필요하다면 enum에 추가하고 적절히 사용
        // SeatStatus.RESERVED 대신 SeatStatus.SOLD_OUT 등으로 변경 고려

        concertRepository.saveSeat(reservation.getSeat()); // 좌석 상태 저장
        return reservationRepository.save(reservation); // 예약 상태 저장
    }

    /**
     * 예약을 취소하고 좌석 상태를 AVAILABLE로 되돌립니다.
     * @param reservationId 취소할 예약 ID
     * @return 취소된 Reservation 객체
     * @throws ReservationException 예약을 찾을 수 없거나 이미 취소된 경우
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Reservation cancelReservation(String reservationId) {
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId) // 예약에 락을 걸고 조회
                .orElseThrow(() -> new ReservationException(ErrorCode.RESERVATION_NOT_FOUND, "예약을 찾을 수 없습니다: " + reservationId));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ReservationException(ErrorCode.RESERVATION_INVALID_STATUS, "이미 취소된 예약입니다.");
        }

        // 예약 상태를 CANCELLED로 변경
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        // 좌석 상태를 AVAILABLE로 되돌림
        Seat seat = reservation.getSeat();
        seat.setStatus(SeatStatus.AVAILABLE);
        concertRepository.saveSeat(seat);

        // TODO: 사용자에게 금액 환불 로직 추가 (UserService.chargeBalance 호출)
        // paymentRepository.findByReservationId(reservationId).ifPresent(payment -> {
        //     if (payment.getStatus() == PaymentStatus.SUCCESS) {
        //         userService.chargeBalance(payment.getUser().getId(), payment.getAmount());
        //         payment.setStatus(PaymentStatus.CANCELLED);
        //         paymentRepository.save(payment);
        //     }
        // });

        return reservation;
    }

    /**
     * 특정 사용자 ID의 예약 목록을 조회합니다.
     * @param userId 사용자 ID
     * @return 예약 목록
     * @throws UserException 사용자를 찾을 수 없을 때 (선택 사항: 예외 대신 빈 리스트 반환도 고려)
     */
    public List<Reservation> getReservationsByUserId(String userId) {
        // 사용자 존재 여부 확인 (필수 아님, 사용자 없는 경우 빈 리스트 반환도 가능)
        userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND, userId));

        return reservationRepository.findByUserId(userId);
    }
}