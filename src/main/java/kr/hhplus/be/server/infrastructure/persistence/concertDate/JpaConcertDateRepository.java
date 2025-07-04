package kr.hhplus.be.server.infrastructure.persistence.concertDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaConcertDateRepository extends JpaRepository<ConcertDateEntity, String> {

    @Query("""
		select
			cd.id,
			cd.concertId,
			cd.date,
			cd.deadline,
			cd.createdAt,
			cd.updatedAt,
			(
				select count(s.id)
				from SeatEntity s
				where s.concertDateId = cd.id
					and s.status = 'AVAILABLE'
			) as remainingSeatCount
		from ConcertDateEntity cd
		where cd.concertId = :concertId
			and cd.deadline > CURRENT_TIMESTAMP
			and exists (
				select 1
				from SeatEntity s
				where s.concertDateId = cd.id
					and s.status = 'AVAILABLE'
			)
	""")
    List<Object[]> findAvailableDatesWithAvailableSeatCount(String concertId);
	
}
