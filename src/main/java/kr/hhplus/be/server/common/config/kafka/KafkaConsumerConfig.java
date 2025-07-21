// 카프카 메시지를 구독(Consume)하기 위한 설정을 정의합니다.
package kr.hhplus.be.server.common.config.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // ⭐️ JSON 형태의 메시지를 다시 객체로 변환하기 위해 JsonDeserializer를 사용
        JsonDeserializer<Object> deserializer = new JsonDeserializer<>();
        // ⭐️ 보안 강화를 위해, 신뢰할 수 있는 이벤트 클래스가 위치한 패키지만 명시적으로 지정 함
        // 이를 통해 알 수 없는 클래스의 역직렬화를 방지하여 보안 취약점 예뱡 가능
        deserializer.addTrustedPackages(
                "kr.hhplus.be.server.payment.domain",     // PaymentSuccessEvent, PaymentFailedEvent
                "kr.hhplus.be.server.reservation.domain", // ReservationCreatedEvent
                "kr.hhplus.be.server.dummy"               // DummyDataGeneratedEvent
                // 새로운 이벤트가 다른 패키지에 추가될 경우, 여기에 해당 패키지 경로를 추가해야 함
        );
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}