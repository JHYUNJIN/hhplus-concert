package kr.hhplus.be.server.concert.adapter.out.persistence.concert;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaConcertRepository extends JpaRepository<ConcertEntity, String> {

    @Query(value = """
				select c
				from ConcertEntity c
				where c.openTime <= CURRENT_TIMESTAMP
					and c.soldOutTime is null
			""")
    List<ConcertEntity> findByOpenConcerts();
}
