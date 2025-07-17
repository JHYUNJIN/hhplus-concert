package kr.hhplus.be.server.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EventTopic {

    RESERVATION_CREATED("reservation.created"),
    PAYMENT_SUCCESS("payment.success"),
    PAYMENT_FAILED("payment.failed"),

    DUMMY_DATA_GENERATED("dummy.data.generated");



    private final String topicName;
}
