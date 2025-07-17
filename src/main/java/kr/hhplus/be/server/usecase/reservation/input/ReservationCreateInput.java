package kr.hhplus.be.server.usecase.reservation.input;

public interface ReservationCreateInput {
    void reserveSeat(ReserveSeatCommand command) throws Exception;

}
