package kr.hhplus.be.server.payment.usecase;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.payment.domain.PaymentSuccessEvent;
import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.queue.domain.QueueTokenUtil;
import kr.hhplus.be.server.reservation.usecase.DistributedLockManager;
import kr.hhplus.be.server.payment.port.in.dto.PaymentTransactionResult;
import kr.hhplus.be.server.queue.adapter.out.persistence.QueueTokenManager;
import kr.hhplus.be.server.common.event.EventPublisher;
import kr.hhplus.be.server.payment.port.in.dto.PaymentCommand;
import kr.hhplus.be.server.payment.port.in.PaymentInput;
import kr.hhplus.be.server.payment.port.in.PaymentOutput;
import kr.hhplus.be.server.payment.port.in.dto.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentInteractor implements PaymentInput {

    private static final String RESERVATION_LOCK_KEY = "reservation:";
    private static final String USER_LOCK_KEY = "user:";

    private final PaymentOutput paymentOutput;
    private final EventPublisher eventPublisher;
    private final PaymentManager paymentManager;
    private final QueueTokenManager queueTokenManager;
    private final DistributedLockManager distributedLockManager;

    @Override
    @Transactional
    public void payment(PaymentCommand command) throws Exception {
        QueueToken queueToken = getQueueTokenAndValid(command.queueTokenId());
        String reservationLockKey = RESERVATION_LOCK_KEY + command.reservationId();
        String userLockKey = USER_LOCK_KEY + queueToken.userId();

        /* 분산락 획득 후 결제 트랜잭션 수행
         * 1. user:{userId} 락 획득
         * 2. reservation:{reservationId} 락 획득
         * 3. 결제 트랜잭션 수행
         */
        PaymentTransactionResult paymentTransactionResult = distributedLockManager.executeWithLockHasReturn(
                userLockKey,
                () -> distributedLockManager.executeWithLockHasReturn(
                        reservationLockKey,
                        () -> paymentManager.processPayment(command, queueToken)));

        // 결제 성공 이벤트 발행 및 결과 반환
        eventPublisher.publish(PaymentSuccessEvent.from(paymentTransactionResult));
        paymentOutput.ok(PaymentResult.from(paymentTransactionResult));
    }

    private QueueToken getQueueTokenAndValid(String tokenId) throws CustomException {
        QueueToken queueToken = queueTokenManager.getQueueToken(tokenId);
        QueueTokenUtil.validateActiveQueueToken(queueToken);
        return queueToken;
    }

}


/* 서비스와 인터렉터의 차이점 **
서비스와 인터랙터의 주요 차이점은 의존성 관리와 책임 분리에 있습니다.

기존 서비스는 입출력 처리와 비즈니스 로직이 명확히 분리되지 않아 컨트롤러와의 의존성이 높았습니다. 하지만 인터랙터는 입출력(Input/Output)과 핵심 비즈니스 로직을 명확하게 구분합니다.

이러한 구조는 **의존성 역전 원칙(DIP)**을 준수하여 아키텍처의 안정성과 확장성을 크게 높입니다. 인터랙터는 비즈니스 로직을 외부 계층(컨트롤러, 영속성 등)으로부터 독립적으로 분리시켜, 다음 이점들을 제공합니다.

테스트 용이성 확장: 비즈니스 로직이 독립적이므로 단위 테스트를 더 쉽고 효과적으로 수행할 수 있습니다.

높은 재사용성: 외부 환경(예: 데이터베이스, UI 프레임워크)이 변경되더라도 핵심 비즈니스 로직은 수정 없이 재사용될 수 있습니다.

명확한 책임: 각 계층의 책임이 명확해져 코드의 가독성과 유지보수성이 향상됩니다.

결과적으로 인터랙터는 비즈니스 로직의 독립성을 보장하고, 시스템의 견고함과 유연성을 증대시키는 데 중요한 역할을 합니다.
 */

