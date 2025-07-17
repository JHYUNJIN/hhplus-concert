package kr.hhplus.be.server.payment.port.out;

import kr.hhplus.be.server.common.event.Event;

public interface EventPublisher {
    <T extends Event> void publish(T event);
}
