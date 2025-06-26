package kr.hhplus.be.server.infrastructure.persistence.concert;

import kr.hhplus.be.server.domain.concert.ConcertDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConcertDateJpaRepository extends JpaRepository<ConcertDate, String> {
    List<ConcertDate> findByConcertId(String concertId);
}