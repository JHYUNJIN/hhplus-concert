package kr.hhplus.be.server.infrastructure.persistence.rank.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSoldOutRankRepository extends JpaRepository<SoldOutRankEntity, String> {
}
