package kr.hhplus.be.server.concert.port.out;

import kr.hhplus.be.server.concert.domain.Concert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConcertRepository {
    Concert save(Concert concert);
    boolean existsById(UUID concertId);
    List<Concert> findAll();

    Optional<Concert> findById(UUID concertId);

    List<Concert> findByOpenConcerts(LocalDateTime now);

    void deleteAll();
}
