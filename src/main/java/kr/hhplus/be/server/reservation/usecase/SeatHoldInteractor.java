package kr.hhplus.be.server.reservation.usecase;


import kr.hhplus.be.server.reservation.port.in.SeatHoldReleaseUseCase;
import kr.hhplus.be.server.reservation.port.in.SeatHoldUseCase;
import kr.hhplus.be.server.reservation.port.out.SeatHoldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeatHoldInteractor implements SeatHoldUseCase, SeatHoldReleaseUseCase {

    private final SeatHoldRepository seatHoldRepository;

    @Override
    public void hold(UUID seatId, UUID userId) {
        seatHoldRepository.hold(seatId, userId);
    }

    @Override
    public void releaseHold(UUID seatId, UUID userId) {
        seatHoldRepository.deleteHold(seatId, userId);
    }
}
