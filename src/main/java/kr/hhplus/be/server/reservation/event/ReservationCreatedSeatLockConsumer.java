// 역할: 예약 생성 시 Redis에 좌석 임시 점유(hold) 정보를 등록합니다.
package kr.hhplus.be.server.reservation.event;

import kr.hhplus.be.server.reservation.domain.ReservationCreatedEvent;
import kr.hhplus.be.server.reservation.port.out.SeatHoldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationCreatedSeatLockConsumer { // ⭐️ 클래스 이름을 Consumer로 변경

    private final SeatHoldRepository seatHoldRepository;

    /**
     * 'reservation.created' 토픽을 구독하여 예약 생성 이벤트를 수신하고,
     * Redis에 좌석 잠금(hold) 정보를 등록합니다.
     * @param event 카프카로부터 수신한 예약 생성 이벤트
     */
    @KafkaListener(topics = "reservation.created", groupId = "${spring.kafka.consumer.group-id}")
    public void handleReservationCreated(ReservationCreatedEvent event) {
        try {
            log.info("예약 생성 이벤트 수신 (Kafka): {}", event);
            seatHoldRepository.hold(event.seatId(), event.userId());
            log.info("Redis 좌석 잠금(hold) 성공. SeatId: {}", event.seatId());
        } catch (Exception e) {
            log.error("Redis 좌석 잠금(hold) 처리 중 오류 발생. Event: {}", event, e);
            // TODO: 실패한 메시지 재처리(Retry) 또는 Dead Letter Queue(DLQ)로 전송
        }
    }
}