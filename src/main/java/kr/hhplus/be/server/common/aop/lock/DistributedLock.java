package kr.hhplus.be.server.common.aop.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    /**
     * 락 키
     */
    String key();

    /**
     * 락 대기 시간 (초)
     */
    long waitTime() default 3L;

    /**
     * 락 유지 시간 (초)
     */
    long leaseTime() default 10L;

    /**
     * 락 접두사
     */
    String prefix() default "lock:";
}
