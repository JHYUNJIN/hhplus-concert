package kr.hhplus.be.server.domain.event;

public interface Event {
    EventTopic getTopic();
    String getKey();
}
