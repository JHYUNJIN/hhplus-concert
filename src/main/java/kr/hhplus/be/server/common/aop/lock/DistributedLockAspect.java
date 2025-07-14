//package kr.hhplus.be.server.common.aop.lock;
//
//import kr.hhplus.be.server.common.exception.CustomException;
//import kr.hhplus.be.server.common.exception.enums.ErrorCode;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.redisson.api.RLock;
//import org.redisson.api.RedissonClient;
//import org.springframework.expression.EvaluationContext;
//import org.springframework.expression.Expression;
//import org.springframework.expression.spel.standard.SpelExpressionParser;
//import org.springframework.expression.spel.support.StandardEvaluationContext;
//import org.springframework.stereotype.Component;
//
//import java.lang.reflect.Method;
//import java.util.concurrent.TimeUnit;
//
//@Aspect
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class DistributedLockAspect {
//
//    private final RedissonClient redissonClient;
//    private final SpelExpressionParser parser = new SpelExpressionParser();
//
//    // 분산락 획득을 위한 키 생성, around 어드바이스는 메소드 실행 전후에 동작함
//    @Around("@annotation(distributedLock)")
//    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
//        // 1. 분산락 객체 생성
//        String lockKey = distributedLock.prefix() + parseLockKey(distributedLock.key(), joinPoint);
//        RLock lock = redissonClient.getLock(lockKey);
//
//        // 2. 분산락 획득 시도
//        try {
//            boolean isLocked = lock.tryLock(
//                    distributedLock.waitTime(),
//                    distributedLock.leaseTime(),
//                    TimeUnit.SECONDS
//            );
//
//            // 3. 분산락 획득 실패 시 예외 처리
//            if (!isLocked) {
//                log.warn("분산락 획득 실패: Key - {}", lockKey);
//                throw new CustomException(ErrorCode.LOCK_CONFLICT);
//            }
//
//            // 4. 분산락 획득 성공 시 메소드 실행
//            return joinPoint.proceed(); // joinPoint.proceed()는 Aspect를 호출한 메소드를 의미함 -> 분산락 획득 후 원래 메소드가 실행됨
//
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            log.warn("분산락 획득 대기중 인터럽트 발생: Key - {}", lockKey);
//            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
//        } finally { // 5. 분산락 해제
//            if (lock.isHeldByCurrentThread()) {
//                lock.unlock();
//            }
//        }
//    }
//
//    private String parseLockKey(String key, ProceedingJoinPoint joinPoint) {
//        /*
//        parseLockKey 메소드는 메서드 파라미터 이름과 값을 자바 메모리 내 EvaluationContext에 등록해서,
//        SpEL(Spring Expression Language) 표현식에서 #파라미터명으로 동적으로 값을 참조할 수 있게 해줍니다.
//        이 과정을 통해 락 키를 메서드 호출 시점의 파라미터 값에 따라 동적으로 생성할 수 있게 됩니다.
//         */
//        if (!key.contains("#")) // key에 #이 포함되어 있지 않으면 그대로 반환
//            // 예시: #command.reservationId()
//            /*
//            #은 Spring Expression Language(SpEL)에서 메서드 파라미터 값을 참조할 때 사용하는 접두사입니다.
//            예를 들어, key = "#userId"와 같이 작성하면, 실제 메서드의 userId 파라미터 값을 동적으로 lock key에 사용할 수 있습니다.
//            즉, #이 있으면 SpEL로 파싱해서 파라미터 값을 추출하고, 없으면 단순 문자열로 key를 사용하기 위해 구분하는 용도입니다.
//             */
//            return key;
//
//        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); // 메소드 상세정보 캐스팅
//        Method method = signature.getMethod(); // 메소드 정보 가져오기
//        String[] parameterNames = signature.getParameterNames(); // 메소드 파라미터 이름 가져오기
//        Object[] args = joinPoint.getArgs(); // 메소드 파라미터 값 가져오기
//
//        EvaluationContext context = new StandardEvaluationContext(); // SpEL 표현식을 평가하기 위한 컨텍스트 생성
//        for (int i = 0; i < parameterNames.length; i++) // 메소드의 모든 파라미터 이름과 값을 SpEL에서 사용할 수 있도록 매핑하는 과정
//            context.setVariable(parameterNames[i], args[i]); // 파라미터 이름을 키로, 파라미터 값을 값으로 설정
//
//        Expression expression = parser.parseExpression(key); // SpEL 표현식 파싱
//        // SpEL 표현식을 평가하여 최종 lock key 생성
//        return expression.getValue(context, String.class);
//    }
//}
