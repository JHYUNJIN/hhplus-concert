package kr.hhplus.be.server.payment.adapter.out.event;

import kr.hhplus.be.server.common.event.Event;
import kr.hhplus.be.server.payment.port.out.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher springEventPublisher;

    @Override
    public <T extends Event> void publish(T event) {
        log.info("{} 이벤트 발생", event.getTopic());
        // 임시 조치: Spring 이벤트 발행
        springEventPublisher.publishEvent(event);
    }
}

