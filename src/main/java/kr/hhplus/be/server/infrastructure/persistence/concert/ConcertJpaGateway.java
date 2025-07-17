package kr.hhplus.be.server.infrastructure.persistence.concert;

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
                .orElseThrow(() -> new IllegalStateException("업데이트할 콘서트를 찾을 수 없습니다: " + concert.id()));
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
    public boolean existsById(UUID concertId) {
        return jpaConcertRepository.existsById(concertId.toString());
    }

    @Override
    public Optional<Concert> findById(UUID concertId) {
        return jpaConcertRepository.findById(concertId.toString())
                .map(ConcertEntity::toDomain);
    }

    @Override
    public List<Concert> findByOpenConcerts() {
        return jpaConcertRepository.findByOpenConcerts()
                .stream()
                .map(ConcertEntity::toDomain)
                .toList();
    }


    @Override
    public void deleteAll() {
        jpaConcertRepository.deleteAll();
    }
}
