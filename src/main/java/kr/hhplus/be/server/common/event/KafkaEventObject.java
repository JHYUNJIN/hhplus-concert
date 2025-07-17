package kr.hhplus.be.server.common.event;

public record KafkaEventObject<T extends Event>(
        EventTopic topic,
        String key,
        T payload
) {
    public static <T extends Event> KafkaEventObject<T> from(T eventObject) {
        return new KafkaEventObject<>(eventObject.getTopic(), eventObject.getKey(), eventObject);
    }
}
