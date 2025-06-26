package kr.hhplus.be.server.infrastructure.persistence.concert;

import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concert.ConcertDate;
import kr.hhplus.be.server.domain.concert.Seat;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ConcertRepositoryJpaAdapter implements ConcertRepository {

    private final ConcertJpaRepository concertJpaRepository;
    private final ConcertDateJpaRepository concertDateJpaRepository;
    private final SeatJpaRepository seatJpaRepository;

    public ConcertRepositoryJpaAdapter(ConcertJpaRepository concertJpaRepository,
                                       ConcertDateJpaRepository concertDateJpaRepository,
                                       SeatJpaRepository seatJpaRepository) {
        this.concertJpaRepository = concertJpaRepository;
        this.concertDateJpaRepository = concertDateJpaRepository;
        this.seatJpaRepository = seatJpaRepository;
    }

    @Override
    public Concert save(Concert concert) {
        return concertJpaRepository.save(concert);
    }

    @Override
    public Optional<Concert> findById(String concertId) {
        return concertJpaRepository.findById(concertId);
    }

    @Override
    public List<Concert> findAll() {
        return concertJpaRepository.findAll();
    }

    @Override
    public ConcertDate saveConcertDate(ConcertDate concertDate) {
        return concertDateJpaRepository.save(concertDate);
    }

    @Override
    public Optional<ConcertDate> findConcertDateById(String concertDateId) {
        return concertDateJpaRepository.findById(concertDateId);
    }

    @Override
    public List<ConcertDate> findConcertDatesByConcertId(String concertId) {
        return concertDateJpaRepository.findByConcertId(concertId);
    }

    @Override
    public Seat saveSeat(Seat seat) {
        return seatJpaRepository.save(seat);
    }

    @Override
    public Optional<Seat> findSeatById(String seatId) {
        return seatJpaRepository.findById(seatId);
    }

    @Override
    public List<Seat> findSeatsByConcertDateId(String concertDateId) {
        return seatJpaRepository.findByConcertDateId(concertDateId);
    }

    @Override
    public Optional<Seat> findSeatByIdForUpdate(String seatId) {
        return seatJpaRepository.findByIdWithPessimisticLock(seatId);
    }
}