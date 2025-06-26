package kr.hhplus.be.server.infrastructure.persistence.concert;

import kr.hhplus.be.server.domain.concert.Concert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertJpaRepository extends JpaRepository<Concert, String> {
}