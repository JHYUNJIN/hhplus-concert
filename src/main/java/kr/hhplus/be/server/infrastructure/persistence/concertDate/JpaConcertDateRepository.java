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

