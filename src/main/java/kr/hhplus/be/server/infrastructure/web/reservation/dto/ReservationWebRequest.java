package kr.hhplus.be.server.infrastructure.web.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationWebRequest {
    @NotBlank(message = "사용자 ID는 필수입니다.")
    private String userId;

    @NotBlank(message = "좌석 ID는 필수입니다.")
    private String seatId;
}