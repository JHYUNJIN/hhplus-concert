package kr.hhplus.be.server.infrastructure.persistence.concertDate;

import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JpaConcertDateRepository extends JpaRepository<ConcertDateEntity, String> {

	@Query(value = """
       SELECT NEW kr.hhplus.be.server.domain.concertDate.ConcertDate(
           CAST(cd.id AS java.util.UUID),
           CAST(cd.concertId AS java.util.UUID),
           CAST((
             SELECT COUNT(s.id)
             FROM SeatEntity s
             WHERE s.concertDateId = cd.id
                AND s.status = 'AVAILABLE'
           ) AS java.lang.Integer),
           cd.date,
           cd.deadline,
           cd.createdAt,
           cd.updatedAt
       )
       FROM ConcertDateEntity cd
       WHERE cd.concertId = :concertId
          AND cd.deadline > CURRENT_TIMESTAMP()
          AND EXISTS (
             SELECT 1
             FROM SeatEntity s
             WHERE s.concertDateId = cd.id
                AND s.status = 'AVAILABLE'
          )
    """)
	List<ConcertDate> findAvailableDatesWithAvailableSeatCount(String concertId);
}

/*
효율성 비교 EXISTS, COUNT > 0
일반적으로 EXISTS 쿼리가 COUNT > 0 쿼리보다 더 효율적입니다.
EXISTS가 효율적인 이유:
EXISTS는 **단락 평가(short-circuit evaluation)**를 합니다. 즉, 조건을 만족하는 첫 번째 레코드를 발견하는 순간 더 이상의 검색을 멈추고 true를 반환합니다. 대규모 데이터셋에서 조건에 맞는 레코드가 일찍 발견될수록 성능 이점을 가집니다.
COUNT와 달리 불필요하게 모든 레코드를 세는 오버헤드가 없습니다.
COUNT > 0의 비효율성:
COUNT 함수는 항상 조건을 만족하는 모든 레코드를 세야 합니다. 이는 조건에 맞는 레코드가 많이 존재할수록 불필요한 작업량이 증가하게 됩니다.
 */
