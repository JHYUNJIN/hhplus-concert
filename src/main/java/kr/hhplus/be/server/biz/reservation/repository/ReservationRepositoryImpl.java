package kr.hhplus.be.server.biz.reservation.repository;

import kr.hhplus.be.server.domain.enums.ReservationStatus;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository reservationJpaRepository;

    public ReservationRepositoryImpl(ReservationJpaRepository reservationJpaRepository) {
        this.reservationJpaRepository = reservationJpaRepository;
    }

    @Override
    public Reservation save(Reservation reservation) {
        return reservationJpaRepository.save(reservation);
    }

    @Override
    public Optional<Reservation> findById(String reservationId) {
        return reservationJpaRepository.findById(reservationId);
    }

    @Override
    public List<Reservation> findByUserId(String userId) {
        return reservationJpaRepository.findByUserId(userId);
    }

    @Override
    public List<Reservation> findBySeatId(String seatId) {
        return reservationJpaRepository.findBySeatId(seatId);
    }

    @Override
    public List<Reservation> findByStatus(ReservationStatus status) {
        return reservationJpaRepository.findByStatus(status);
    }

    @Override
    public Optional<Reservation> findByIdForUpdate(String reservationId) {
        return reservationJpaRepository.findByIdWithPessimisticLock(reservationId);
    }

    @Override
    public void deleteAll() {
        reservationJpaRepository.deleteAll();
    }
}