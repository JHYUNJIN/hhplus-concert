package kr.hhplus.be.server.concert.port.in;

import kr.hhplus.be.server.concert.domain.ConcertDate;
import java.util.UUID;

public interface GetConcertDateUseCase {
    ConcertDate findById(UUID concertDateId);
}