package kr.hhplus.be.server.payment.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import kr.hhplus.be.server.payment.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaPaymentRepository extends JpaRepository<PaymentEntity, String> {
    Optional<PaymentEntity> findByReservationId(String reservationId);

    @Query("select p from PaymentEntity p where p.reservationId = :reservationId")
    @Lock(LockModeType.PESSIMISTIC_WRITE) // 비관적 락을 사용하여 동시성 제어
    @QueryHints({
            @QueryHint(name = "javax.persistence.lock.timeout", value = "5000")
    })
    Optional<PaymentEntity> findByReservationIdForUpdate(String reservationId);


    /**
     * 원자성을 보장하는 UPDATE 쿼리
     * 낙관적 락을 활용하여 결제 상태 업데이트, 결제서비스에서 분산락과 함께 사용 됨
     * **분산 락과의 시너지**:
     *  * 기존의 `DistributedLock` (Redisson)이 `reservationId` 기준으로 여러 인스턴스 간의 동시 접근을 제어합니다.
     *  * 낙관적 락은 분산 락을 획득한 후에도,
     *  * 데이터베이스 레벨에서 최종적으로 상태를 업데이트하기 전에
     *  * 읽었던 시점 이후로 데이터가 변경되지 않았는지 한 번 더 확인하여 데이터 일관성을 극대화합니다.
     *  * 이는 락을 우회하는 외부 요인이나 예상치 못한 데이터 변경에 대한 방어막을 제공합니다.
     *
     * @param id 결제 ID
     * @param newStatus 새로운 상태 값
     * @param expectedStatus 기대하는 현재 상태 값
     * @return 업데이트된 행의 수 (1: 성공, 0: 조건 불일치로 실패)
     */
    @Modifying
    @Query("UPDATE PaymentEntity p SET p.status = :newStatus WHERE p.id = :id AND p.status = :expectedStatus")
    int updateStatusIfExpected(@Param("id") String id,
                               @Param("newStatus") PaymentStatus newStatus,
                               @Param("expectedStatus") PaymentStatus expectedStatus);
}
