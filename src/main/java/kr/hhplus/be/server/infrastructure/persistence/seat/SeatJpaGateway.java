package kr.hhplus.be.server.infrastructure.persistence.seat;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SeatJpaGateway implements SeatRepository {

    private final JpaSeatRepository jpaSeatRepository;

    // 캐시에서 해당 콘서트 날짜의 좌석 정보를 제거
    @CacheEvict(value = "cache:seat:available", key   = "#seat.concertDateId")
    @Override
    public Seat save(Seat seat) {
        if (seat.id() == null) return jpaSeatRepository.save(SeatEntity.from(seat)).toDomain();
        // 1. DB에서 영속 상태의 엔티티를 조회
        SeatEntity seatEntity = jpaSeatRepository.findById(seat.id().toString()).orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, seat.id() + " 좌석을 업데이트 할 수 없습니다."));
        // 2. 영속 상태의 엔티티를 직접 수정
        seatEntity.changeStatus(seat.status());
        // 3. 트랜잭션 커밋 시, JPA의 변경 감지(Dirty Checking)에 의해 자동으로 UPDATE 쿼리가 실행
        return seatEntity.toDomain();
    }

    @Override
    public List<Seat> findByConcertDateId(UUID concertDateId) {
        return jpaSeatRepository.findByConcertDateId(concertDateId.toString()).stream()
                .map(SeatEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Seat> findBySeatIdAndConcertDateId(UUID seatId, UUID concertDateId) {
        return jpaSeatRepository.findBySeatIdAndConcertDateId(seatId.toString(), concertDateId.toString())
                .map(SeatEntity::toDomain);
    }

    @Override
    public List<Seat> findAvailableSeats(UUID concertId, UUID concertDateId) {
        return jpaSeatRepository.findAvailableSeats(concertId.toString(), concertDateId.toString()).stream()
                .map(SeatEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Seat> findById(UUID seatId) {
        return jpaSeatRepository.findById(seatId.toString())
                .map(SeatEntity::toDomain);
    }

    @Override
    public void deleteAll() {
        jpaSeatRepository.deleteAll();
    }


    @Override
    public List<Seat> findByConcertDateIds(List<UUID> concertDateIds) {
        return jpaSeatRepository.findByConcertDateIds(concertDateIds.stream()
                        .map(UUID::toString)
                        .toList()).stream()
                .map(SeatEntity::toDomain)
                .toList();
    }
}