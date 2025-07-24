package kr.hhplus.be.server.concert.adapter.out.persistence.concertDate;

import kr.hhplus.be.server.concert.domain.ConcertDate;
import kr.hhplus.be.server.concert.port.out.ConcertDateRepository;
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
        return jpaConcertDateRepository.findAvailableDates(concertId.toString()).stream()
                .map(ConcertDateEntity::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(UUID concertDateId) {
        return jpaConcertDateRepository.existsById(concertDateId.toString());
    }
    

    @Override
    public void deleteAll() {
        jpaConcertDateRepository.deleteAll();
    }


    @Override
    public void updateAvailableSeatCount(UUID dateId, Long count) {
        jpaConcertDateRepository.updateAvailableSeatCount(dateId.toString(), count);
    }
}