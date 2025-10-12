package kr.hhplus.be.server.payment.port.in.dto;

import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.reservation.domain.Reservation;
// import kr.hhplus.be.server.user.domain.User; // 삭제
import kr.hhplus.be.server.payment.domain.Payment;

import java.util.UUID; // UUID 임포트 추가

public record PaymentDomainResult(
        QueueToken queueToken,
        UUID userId, // User 객체 대신 userId 사용
        Reservation reservation,
        Payment payment,
        Seat seat
) {
}
