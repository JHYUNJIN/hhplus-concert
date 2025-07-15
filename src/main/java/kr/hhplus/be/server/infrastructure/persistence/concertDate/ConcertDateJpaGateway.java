package kr.hhplus.be.server.infrastructure.persistence.concertDate;

import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import kr.hhplus.be.server.domain.concertDate.ConcertDateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ConcertDateJpaGateway implements ConcertDateRepository {

    private final JpaConcertDateRepository jpaConcertDateRepository;

    @Override
    public ConcertDate save(ConcertDate concertDate) {
        ConcertDateEntity concertDateEntity = jpaConcertDateRepository.save(ConcertDateEntity.from(concertDate));
        return concertDateEntity.toDomain();
    }

    @Override
    public Optional<ConcertDate> findById(UUID concertDateId) {
        return jpaConcertDateRepository.findById(concertDateId.toString())
                .map(ConcertDateEntity::toDomain);
    }

    @Override
    public List<ConcertDate> findAvailableDates(UUID concertId) {
        return jpaConcertDateRepository
                .findAvailableDates(concertId.toString());
    }

    @Override
    public boolean existsById(UUID concertDateId) {
        return jpaConcertDateRepository.existsById(concertDateId.toString());
    }
    

    @Override
    public void deleteAll() {
        jpaConcertDateRepository.deleteAll();
    }
}
