package kr.hhplus.be.server.reservation.usecase;

import kr.hhplus.be.server.reservation.port.in.SeatHoldReleaseUseCase;
import kr.hhplus.be.server.reservation.port.out.SeatHoldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService implements SeatHoldReleaseUseCase {
    private final SeatHoldRepository seatHoldRepository;

    @Override
    public void releaseHold(UUID seatId, UUID userId) {
        seatHoldRepository.deleteHold(seatId, userId);
    }
}
