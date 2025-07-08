package kr.hhplus.be.server.infrastructure.persistence.concertDate;

import kr.hhplus.be.server.infrastructure.persistence.concertDate.dto.ConcertDateWithSeatCountDto;
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
	List<ConcertDateWithSeatCountDto> findAvailableDatesWithAvailableSeatCount(String concertId);
	/*
	* 고 튜터님 피드백 수정 (Object -> DTO)
	이 쿼리는 콘서트 날짜와 해당 날짜에 남아있는 좌석 수를 조회합니다.
	Object반환 시 런타임 오류가 발생할 위험이 있으므로 DTO 클래스 사용
	* 타입 안전성 부족
	* 가독성 및 유지보수성 저하
	 */
	
}
