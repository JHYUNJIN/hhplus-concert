package kr.hhplus.be.server.application.event;

import kr.hhplus.be.server.domain.event.Event;

public interface EventPublisher {
    <T extends Event> void publish(T event);
}
