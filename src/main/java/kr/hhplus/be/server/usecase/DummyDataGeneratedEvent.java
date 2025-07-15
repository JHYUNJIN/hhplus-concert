package kr.hhplus.be.server.usecase;

import kr.hhplus.be.server.domain.event.Event;
import kr.hhplus.be.server.domain.event.EventTopic;

import java.util.List;
import java.util.UUID;

/**
 * 더미 데이터 생성이 완료되었음을 알리는 이벤트입니다.
 * 업데이트에 필요한 콘서트 날짜 ID 리스트를 담고 있습니다.
 */
public record DummyDataGeneratedEvent(List<UUID> concertDateIds) implements Event {

    private static final String DUMMY_DATA_KEY = "dummy-data-generator";

    @Override
    public EventTopic getTopic() {
        return EventTopic.DUMMY_DATA_GENERATED;
    }

    @Override
    public String getKey() {
        // 이 이벤트는 특정 사용자나 객체에 종속되지 않으므로 고정된 키를 사용합니다.
        return DUMMY_DATA_KEY;
    }
}