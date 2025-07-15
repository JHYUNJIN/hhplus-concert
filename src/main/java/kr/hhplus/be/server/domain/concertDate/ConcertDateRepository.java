package kr.hhplus.be.server.domain.concertDate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConcertDateRepository {
    Optional<ConcertDate> findById(UUID concertDateId);

    // "콘서트 ID로 콘서트 날짜 조회"
    List<ConcertDate> findAvailableDates(UUID concertId);

    ConcertDate save(ConcertDate concertDate);

    boolean existsById(UUID concertDateId);

    void deleteAll();


    // 더미데이터 전용 메소드
    void updateAvailableSeatCount(UUID dateId, Long count);
}
