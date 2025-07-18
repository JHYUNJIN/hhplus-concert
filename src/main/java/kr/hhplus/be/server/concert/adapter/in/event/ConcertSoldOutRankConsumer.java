package kr.hhplus.be.server.concert.adapter.in.event;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.concert.domain.ConcertDate;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.domain.enums.SeatStatus;
import kr.hhplus.be.server.concert.port.out.ConcertDateRepository;
import kr.hhplus.be.server.concert.port.out.SeatRepository;
import kr.hhplus.be.server.concert.usecase.ConcertSoldOutManager;
import kr.hhplus.be.server.payment.domain.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConcertSoldOutRankConsumer {

    private final ConcertDateRepository concertDateRepository;
    private final SeatRepository seatRepository;
    private final ConcertSoldOutManager concertSoldOutManager;

    /**
     * 결제 성공시 이벤트 수신
     * 1. 해당 콘서트 매진 체크
     * 2. 매진시 랭킹 업데이트
     * @param event 결제 성공 이벤트 정보
     */
    @KafkaListener(topics = "payment.success", groupId = "${spring.kafka.consumer.group-id}")
    public void handleEvent(PaymentSuccessEvent event) {
        log.info("🚀[로그:정현진] 결제 성공 이벤트 수신 (Kafka). Event: {}", event);
        try {
            ConcertDate concertDate = getConcertDate(event.seat().concertDateId());

            // 모든 좌석이 매진 되었는지 확인
            List<Seat> allSeats = seatRepository.findByConcertDateId(concertDate.id());
            boolean isAllSeatsAssigned = allSeats.stream()
                    .allMatch(seatItem -> seatItem.status() == SeatStatus.ASSIGNED);

            if (!isAllSeatsAssigned)
                return;

            // 매진된 경우 랭킹 업데이트
            concertSoldOutManager.processUpdateRanking(event, concertDate.concertId(), allSeats.size());

        } catch (Exception e) {
            log.error("랭킹 업데이트 처리 중 오류 발생. Event: {}", event, e);
            // TODO: 실패한 메시지 재처리(Retry) 또는 Dead Letter Queue(DLQ)로 전송
        }
    }

    private ConcertDate getConcertDate(UUID concertDateId) throws CustomException {
        return concertDateRepository.findById(concertDateId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_DATE_NOT_FOUND));
    }
}
