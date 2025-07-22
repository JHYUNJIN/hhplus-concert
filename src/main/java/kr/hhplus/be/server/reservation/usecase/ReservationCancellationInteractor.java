package kr.hhplus.be.server.reservation.usecase;

import kr.hhplus.be.server.concert.domain.ConcertDate;
import kr.hhplus.be.server.concert.port.out.ConcertDateRepository;
import kr.hhplus.be.server.payment.domain.Payment;
import kr.hhplus.be.server.payment.port.out.PaymentRepository;
import kr.hhplus.be.server.payment.domain.enums.PaymentStatus;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.port.out.ReservationRepository;
import kr.hhplus.be.server.reservation.domain.enums.ReservationStatus;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.port.out.SeatRepository;
import kr.hhplus.be.server.reservation.port.in.ReservationCancellationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationCancellationInteractor implements ReservationCancellationUseCase {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final ConcertDateRepository concertDateRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public void cancelIfUnpaid(UUID reservationId) {
        // 1. 예약 정보 조회
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);

        // 예약이 없거나, 이미 처리된 상태(결제 완료, 취소 등)이면 아무것도 하지 않고 종료합니다.
        if (reservation == null || reservation.status() != ReservationStatus.PENDING) {
            log.info("예약 ID {}는 이미 처리되었거나 존재하지 않아 만료 로직을 건너뜁니다.", reservationId);
            return;
        }

        // 5, 결제 만료 처리
        Payment payment = paymentRepository.findByReservationId(reservationId).orElse(null);
        if (payment != null && payment.status()== PaymentStatus.PENDING) {
            paymentRepository.save(payment.expire());
        }

        // 2. 예약 만료 처리
        Reservation expiredReservation = reservation.expire();
        reservationRepository.save(expiredReservation);

        // 3. 좌석의 상태를 AVAILABLE 변경
        Seat seat = seatRepository.findById(reservation.seatId()).orElseThrow();
        Seat availableSeat = seat.expire();
        seatRepository.save(availableSeat);

        // 4. 해당 날짜의 잔여 좌석 수 1 증가
        ConcertDate concertDate = concertDateRepository.findById(seat.concertDateId()).orElseThrow();
        ConcertDate updatedConcertDate = concertDate.increaseAvailableSeatCount();
        concertDateRepository.save(updatedConcertDate);

    }
}