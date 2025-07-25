package kr.hhplus.be.server.concert.adapter.out.persistence.concertDate;

import kr.hhplus.be.server.concert.domain.ConcertDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaConcertDateRepository extends JpaRepository<ConcertDateEntity, String> {

	/**
	 * 예약 가능한 콘서트 날짜 목록을 조회합니다.
	 * 비정규화된 availableSeatCount 컬럼을 사용하여 성능을 최적화합니다.
	 */
	@Query(value = """
       SELECT cd
       FROM ConcertDateEntity cd
       WHERE cd.concertId = :concertId
          AND cd.deadline > CURRENT_TIMESTAMP()
          AND cd.availableSeatCount > 0
    """)
	List<ConcertDateEntity> findAvailableDates(String concertId);


	@Modifying(clearAutomatically = true)
	@Query("UPDATE ConcertDateEntity cd SET cd.availableSeatCount = :count WHERE cd.id = :id")
	void updateAvailableSeatCount(@Param("id") String id, @Param("count") Long count);
}