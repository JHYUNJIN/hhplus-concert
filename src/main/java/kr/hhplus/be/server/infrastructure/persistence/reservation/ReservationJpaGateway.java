package kr.hhplus.be.server.infrastructure.persistence.reservation;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReservationJpaGateway implements ReservationRepository {

    private final JpaReservationRepository jpaReservationRepository;

    @Override
    public Reservation save(Reservation reservation) {
        ReservationEntity reservationEntity = ReservationEntity.from(reservation);
        return jpaReservationRepository.save(reservationEntity).toDomain();
    }

    @Override
    public Optional<Reservation> findById(UUID reservationId) throws CustomException {
        return jpaReservationRepository.findById(reservationId.toString())
                .map(ReservationEntity::toDomain);
    }

    @Override
    public List<Reservation> findAll() {
        return jpaReservationRepository.findAll().stream()
                .map(ReservationEntity::toDomain)
                .toList();
    }


    @Override
    public void deleteAll() {
        jpaReservationRepository.deleteAll();
    }


}
