package kr.hhplus.be.server.reservation.usecase;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockManager {

    private static final String LOCK_PREFIX = "lock:";
    private static final long WAIT_TIME = 3L; // 락 획득 대기 시간 (초)
    private static final long LEASE_TIME = 10L; // 락 유지 시간 (초)

    private final RedissonClient redissonClient;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 반환 값이 있는 락 프로세스
     *
     * @param key         락 키값
     * @param transaction 실행 로직
     * @return 반환 값
     */
    // T: executeWithLockHasReturn 메서드가 반환할 값의 타입, 즉, 이 메서드는 어떤 타입의 값이든 반환할 수 있으며, 호출 시점에 타입이 결정 됨
    public <T> T executeWithLockHasReturn(String key, Callable<T> transaction) throws Exception {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + key);

        try {
            // 락 획득 시도
            if (lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS))
                return transaction.call(); // 실행 로직 수행

            throw new CustomException(ErrorCode.LOCK_CONFLICT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 예외발생 시 스레드 중단
            log.warn("분산락 획득 대기중 인터럽트 발생: Key - {}", key);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (lock.isHeldByCurrentThread()) //락 해제
                lock.unlock();
        }
    }

    /**
     * 반환 값이 없는 락 프로세스
     *
     * @param key    락 키값
     * @param action 실행 로직
     */
    public void executeWithLock(String key, Runnable action) throws CustomException {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + key);

        try {
            if (lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS)) {
                action.run();
                return;
            }

            throw new CustomException(ErrorCode.LOCK_CONFLICT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("분산락 획득 대기중 인터럽트 발생: Key - {}", key);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (lock.isHeldByCurrentThread())
                lock.unlock();
        }
    }

    /**
     * 반환값이 있는 일반 Redis 락
     *
     * @param key         락 키값
     * @param transaction 실행 로직
     * @return 반환값
     * @throws Exception 락 획득 실패 시 예외 발생
     */
    public <T> T executeWithSimpleLockHasReturn(String key, Callable<T> transaction) throws Exception {
        String lockKey = LOCK_PREFIX + key;

        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent( // Redis 락 획득 시도
                    lockKey,
                    "lock",
                    Duration.ofSeconds(LEASE_TIME));

            if (Boolean.TRUE.equals(result)) {
                try {
                    return transaction.call(); // 실행 로직 수행
                } finally {
                    redisTemplate.delete(lockKey); // 락 해제
                }
            }

            throw new CustomException(ErrorCode.LOCK_CONFLICT);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
