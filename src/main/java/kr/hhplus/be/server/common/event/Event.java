package kr.hhplus.be.server.common.event;

public interface Event {
    EventTopic getTopic();
    String getKey();
}
