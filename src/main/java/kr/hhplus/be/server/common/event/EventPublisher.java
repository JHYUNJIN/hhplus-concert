package kr.hhplus.be.server.common.event;

public interface EventPublisher {
    <T extends Event> void publish(T event);
}
