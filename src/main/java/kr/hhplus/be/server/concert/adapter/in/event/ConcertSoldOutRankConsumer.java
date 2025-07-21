package kr.hhplus.be.server.concert.adapter.in.event;

import kr.hhplus.be.server.concert.port.in.RankingUpdateUseCase;
import kr.hhplus.be.server.payment.domain.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConcertSoldOutRankConsumer {

    private final RankingUpdateUseCase rankingUpdateUseCase;

    @KafkaListener(topics = "payment.success", groupId = "${spring.kafka.consumer.group-id.concert-rank}")
    public void handleEvent(PaymentSuccessEvent event) {
        log.info("결제 성공 이벤트 수신 (Kafka). 랭킹 업데이트 프로세스를 시작합니다. Event: {}", event);
        try {
            // Consumer는 외부 이벤트 객체에서 내부 시스템에 필요한 데이터만 추출하여
            // 유즈케이스(서비스)에 전달하는 책임을 가집니다. (Anti-Corruption Layer)
            rankingUpdateUseCase.updateRankingIfNeeded(
                    event.seat().concertDateId(),
                    event.occurredAt()
            );
        } catch (Exception e) {
            log.error("결제 성공 후처리 중 최상위 레벨에서 오류 발생. Event: {}", event, e);
            throw e;
        }
    }
}