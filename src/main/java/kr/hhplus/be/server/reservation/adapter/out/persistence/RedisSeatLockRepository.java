package kr.hhplus.be.server.reservation.adapter.out.persistence;

import kr.hhplus.be.server.reservation.port.out.SeatLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisSeatLockRepository implements SeatLockRepository {

    @Override
    public boolean acquisitionLock(UUID seatId) {
        return true;
    }

    @Override
    public void releaseLock(UUID seatId) {

    }
}
