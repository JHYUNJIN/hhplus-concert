package kr.hhplus.be.server.concert.adapter.out.persistence.concert;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaConcertRepository extends JpaRepository<ConcertEntity, String> {

	/** 고 튜터님 피드백: 테스트 용이성과 성능 최적화 개선
	 * 지정된 시간을 기준으로, 티켓 판매가 시작되었고 아직 매진되지 않은 콘서트 목록을 조회합니다.
	 * @param now 현재 시간 (애플리케이션 레벨에서 주입), 명확성: 항상 애플리케이션 서버의 시간을 기준으로 동작하므로 결과가 명확함 -> 다양한 시간대별 시나리오를 손쉽게 테스트할 수 있게 됨
	 * @return 콘서트 엔티티 목록
	 */
	@Query(value = """
             select c
             from ConcertEntity c
             where c.openTime <= :now
                and c.soldOutTime is null
          """)
	List<ConcertEntity> findByOpenConcerts(LocalDateTime now);
}
