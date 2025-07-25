package kr.hhplus.be.server.concert.adapter.out.persistence.concert;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.port.out.ConcertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConcertJpaGateway implements ConcertRepository {

    private final JpaConcertRepository jpaConcertRepository;

    @Override
    public Concert save(Concert concert) {
        if (concert.id() == null) return jpaConcertRepository.save(ConcertEntity.from(concert)).toDomain();
        // 이미 존재하는 콘서트는 업데이트
        ConcertEntity concertEntity = jpaConcertRepository.findById(concert.id().toString())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "업데이트 할 콘서트를 찾을 수 없습니다: " + concert.id()));
        concertEntity.updateSoldOutTime(concert.soldOutTime());
        return concertEntity.toDomain();
    }

    @Override
    public List<Concert> findAll() {
        return jpaConcertRepository.findAll()
                .stream()
                .map(ConcertEntity::toDomain)
                .toList();
    }

    @Override
    public List<Concert> findByOpenConcerts(LocalDateTime now) {
        // ⭐️ DB의 CURRENT_TIMESTAMP 대신, 애플리케이션의 현재 시간을 직접 전달합니다.
        return jpaConcertRepository.findByOpenConcerts(now)
                .stream()
                .map(ConcertEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Concert> findById(UUID concertId) {
        return jpaConcertRepository.findById(concertId.toString())
                .map(ConcertEntity::toDomain);
    }

    @Override
    public boolean existsById(UUID concertId) {
        return jpaConcertRepository.existsById(concertId.toString());
    }
    
    @Override
    public void deleteAll() {
        jpaConcertRepository.deleteAll();
    }
}
