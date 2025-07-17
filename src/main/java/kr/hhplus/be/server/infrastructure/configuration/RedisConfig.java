package kr.hhplus.be.server.infrastructure.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import kr.hhplus.be.server.domain.queue.QueueToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    // Lua 스크립트 로딩을 위한 필드
    private DefaultRedisScript<String> issueQueueTokenAtomicScript;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    /**
     * Lua 스크립트에서 사용될 ObjectMapper 빈.
     * QueueToken 객체를 JSON 문자열로 직렬화하는 데 사용됩니다.
     */
    @Bean
    public ObjectMapper objectMapperForLua() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // Java 8 날짜/시간 API 지원
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO 8601 형식으로 날짜 출력
        // DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES 설정은 JSON 직렬화에 필요하지 않습니다.
        // 역직렬화 시에 필요합니다. 여기서는 직렬화만 고려합니다.
        return mapper;
    }

    // 애플리케이션 시작 시 Lua 스크립트 파일을 로드
    @PostConstruct
    public void initLuaScripts() {
        issueQueueTokenAtomicScript = new DefaultRedisScript<>();
        // scripts/issueQueueTokenAtomic.lua 파일 경로 지정
        issueQueueTokenAtomicScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/issueQueueTokenAtomic.lua")));
        issueQueueTokenAtomicScript.setResultType(String.class); // 스크립트의 최종 반환 타입 (토큰 ID가 String이므로)
//        log.info("Redis Lua Script 'issueQueueTokenAtomic.lua' loaded successfully in RedisConfig.");
    }

    /**
     * Lua 스크립트 실행에 사용될 DefaultRedisScript 빈.
     * 이 빈을 주입받아 RedisTemplate.execute() 메서드에 전달합니다.
     */
    @Bean
    public DefaultRedisScript<String> issueQueueTokenAtomicScript() {
        return issueQueueTokenAtomicScript;
    }

    /**
     * Lua 스크립트 실행 등 String 타입만 다루는 Redis 작업용 템플릿.
     * ScriptExecutor는 ARGV 인자를 이 템플릿의 ValueSerializer로 직렬화합니다.
     */
    @Bean
    public RedisTemplate<String, String> luaScriptRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * QueueToken 직렬화 용 redisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> queueTokenRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // CustomObjectMapper 사용
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // LocalDateTime 직렬화 문제로 JavaTimeModule 포함
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 알 수 없는 필드 직렬화 무시

        // QueueToken 직렬화 설정
        Jackson2JsonRedisSerializer<QueueToken> serializer = new Jackson2JsonRedisSerializer<>(objectMapper,
                QueueToken.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Seat Hold용 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, String> seatHoldRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 기본 redisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}
