package kr.hhplus.be.server.domain.concert;

import java.util.List;
import java.util.Optional;

public interface ConcertRepository {
    // 콘서트 저장 및 업데이트
    Concert save(Concert concert);

    // ID로 콘서트 조회
    Optional<Concert> findById(String concertId);

    // 모든 콘서트 조회
    List<Concert> findAll();

    // 콘서트 날짜 저장 및 업데이트
    ConcertDate saveConcertDate(ConcertDate concertDate);

    // ID로 콘서트 날짜 조회
    Optional<ConcertDate> findConcertDateById(String concertDateId);

    // 특정 콘서트의 모든 날짜 조회
    List<ConcertDate> findConcertDatesByConcertId(String concertId);

    // 좌석 저장 및 업데이트
    Seat saveSeat(Seat seat);

    // ID로 좌석 조회
    Optional<Seat> findSeatById(String seatId);

    // 특정 콘서트 날짜의 모든 좌석 조회
    List<Seat> findSeatsByConcertDateId(String concertDateId);

    // ID로 좌석 조회 (업데이트를 위한 락 포함)
    Optional<Seat> findSeatByIdForUpdate(String seatId);
}