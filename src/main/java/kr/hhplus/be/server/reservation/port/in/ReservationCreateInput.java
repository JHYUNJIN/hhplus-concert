package kr.hhplus.be.server.reservation.port.in;

import kr.hhplus.be.server.reservation.port.in.dto.ReserveSeatCommand;

public interface ReservationCreateInput {
    void reserveSeat(ReserveSeatCommand command) throws Exception;

}
