package kr.hhplus.be.server.common.aop.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// @Aspect: 이 클래스가 Aspect임을 나타냅니다.
// @Component: 스프링 컨테이너에 빈으로 등록되도록 합니다.
@Aspect
@Component
public class LoggingAspect {

    // 각 클래스별로 로거 인스턴스를 얻는 대신, Aspect에서 공통 로거를 사용하거나,
    // JoinPoint를 통해 대상 클래스의 이름을 가져와 해당 클래스 이름으로 로거를 생성할 수 있습니다.
    // 여기서는 대상 클래스 이름으로 로거를 생성하는 방식을 사용합니다.

    // @Around: 대상 메서드의 실행 전후에 Advice를 적용합니다.
    // execution(* kr.hhplus.be.application..*Service.*(..)):
    //   - *: 모든 반환 타입
    //   - kr.hhplus.be.application..*: kr.hhplus.be.application 패키지 및 모든 하위 패키지
    //   - *Service: 이름이 "Service"로 끝나는 모든 클래스
    //   - .*: 모든 메서드
    //   - (..): 모든 타입의 인자
    @Around("execution(* kr.hhplus.be.server.application..*Service.*(..))")
    public Object logServiceMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());

        // 메서드 실행 전
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        logger.info("메서드 실행 시작: {} (인자: {})", methodName, args);

        Object result = null;
        try {
            // 대상 메서드 실행
            result = joinPoint.proceed();
        } catch (Throwable e) {
            // 예외 발생 시 로그
            logger.error("메서드 실행 중 예외 발생: {} (예외: {})", methodName, e.getMessage(), e);
            throw e; // 예외를 다시 던져서 호출자에게 전달
        } finally {
            // 메서드 실행 후 (성공/실패 무관)
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            logger.info("메서드 실행 완료: {} (실행 시간: {}ms, 결과: {})", methodName, executionTime, result);
        }
        return result;
    }
}