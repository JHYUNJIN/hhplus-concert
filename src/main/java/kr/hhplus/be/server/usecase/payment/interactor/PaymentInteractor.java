package kr.hhplus.be.server.usecase.payment.interactor;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.hhplus.be.server.common.aop.lock.DistributedLock;
import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.event.payment.PaymentFailedEvent;
import kr.hhplus.be.server.domain.event.payment.PaymentSuccessEvent;
import kr.hhplus.be.server.domain.payment.*;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.queue.QueueTokenRepository;
import kr.hhplus.be.server.domain.queue.QueueTokenUtil;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatHoldRepository;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.infrastructure.persistence.payment.PaymentManager;
import kr.hhplus.be.server.infrastructure.persistence.payment.PaymentTransactionResult;
import kr.hhplus.be.server.infrastructure.persistence.queue.QueueTokenManager;
import kr.hhplus.be.server.usecase.event.EventPublisher;
import kr.hhplus.be.server.usecase.payment.input.PaymentCommand;
import kr.hhplus.be.server.usecase.payment.input.PaymentInput;
import kr.hhplus.be.server.usecase.payment.output.PaymentOutput;
import kr.hhplus.be.server.usecase.payment.output.PaymentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PaymentInteractor implements PaymentInput {

    private final PaymentOutput paymentOutput;
    private final EventPublisher eventPublisher;
    private final PaymentManager paymentManager;
    private final QueueTokenManager queueTokenManager;

    @Override
    @DistributedLock(key = "'payment:reservation:' + #command.reservationId()", waitTime = 3L, leaseTime = 10L)
    @Transactional // 이 트랜잭션은 분산락이 획득된 후 시작됩니다.
    public void payment(PaymentCommand command) throws CustomException {
        System.out.println("🚀[로그:정현진] 결제 요청: " + command);

        // 토큰 검증
        QueueToken queueToken = getQueueTokenAndValid(command.queueTokenId());

        // 결제 실행
        PaymentTransactionResult paymentTransactionResult = paymentManager.processPayment(command, queueToken);

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

