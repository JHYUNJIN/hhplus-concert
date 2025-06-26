package kr.hhplus.be.server.biz.concert.repository;

import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConcertDateJpaRepository extends JpaRepository<ConcertDate, String> {
    List<ConcertDate> findByConcertId(String concertId);
}