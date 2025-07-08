package kr.hhplus.be.server.infrastructure.persistence.concertDate;

import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import kr.hhplus.be.server.domain.concertDate.ConcertDateRepository;
import kr.hhplus.be.server.infrastructure.persistence.concertDate.dto.ConcertDateWithSeatCountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
    public List<ConcertDate> findAvailableDatesWithAvailableSeatCount(UUID concertId) {
        List<ConcertDateWithSeatCountDto> results = jpaConcertDateRepository
                .findAvailableDatesWithAvailableSeatCount(concertId.toString());

        return results.stream()
                .map(ConcertDateWithSeatCountDto::toDomainConcertDate) // DTO 내부의 팩토리 메서드 활용
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
}
