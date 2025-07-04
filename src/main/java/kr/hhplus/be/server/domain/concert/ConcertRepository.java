package kr.hhplus.be.server.domain.concert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConcertRepository {
    Concert save(Concert concert);
    boolean existsById(UUID concertId);
    List<Concert> findAll();

    Optional<Concert> findById(UUID concertId);

    void deleteAll();
}
