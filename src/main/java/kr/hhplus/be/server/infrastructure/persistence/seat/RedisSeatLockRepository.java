package kr.hhplus.be.server.infrastructure.persistence.seat;

import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import kr.hhplus.be.server.domain.seat.SeatLockRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisSeatLockRepository implements SeatLockRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean acquisitionLock(UUID seatId) {
        return true;
    }

    @Override
    public void releaseLock(UUID seatId) {

    }
}
