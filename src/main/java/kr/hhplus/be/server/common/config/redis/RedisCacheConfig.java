package kr.hhplus.be.server.common.config.redis;

import java.time.Duration;

import kr.hhplus.be.server.concert.domain.Seats;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RedisCacheConfig {

    private final RedisConnectionFactory redisConnectionFactory;

    /**
     * 캐싱 설정
     */
    @Bean
    public CacheManager cacheManager(
            ObjectMapper redisObjectMapper,
            RedisCacheConfiguration availableSeatCacheConfig) {

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);
        RedisSerializationContext.SerializationPair<Object> pair = RedisSerializationContext.SerializationPair.fromSerializer(serializer);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(pair)
                .disableCachingNullValues();

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("cache:concert:available", availableSeatCacheConfig)
                .build();
    }

    /**
     * Available Seat Cache 설정
     * TTL 1분 설정
     */
    @Bean
    public RedisCacheConfiguration availableSeatCacheConfig(ObjectMapper redisObjectMapper) {
        Jackson2JsonRedisSerializer<Seats> serializer = new Jackson2JsonRedisSerializer<>(redisObjectMapper, Seats.class);
        RedisSerializationContext.SerializationPair<Seats> pair = RedisSerializationContext.SerializationPair.fromSerializer(serializer);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(1))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(pair)
                .disableCachingNullValues();
    }

}
