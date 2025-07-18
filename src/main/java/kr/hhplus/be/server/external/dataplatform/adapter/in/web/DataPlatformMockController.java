package kr.hhplus.be.server.external.dataplatform.adapter.in.web;

import kr.hhplus.be.server.external.dataplatform.adapter.out.dataplatform.request.ReservationDataRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 외부 데이터 플랫폼을 모방하는 모의(Mock) 컨트롤러입니다.
 * DataPlatformClient로부터 오는 데이터 전송 요청을 수신하여 정상 응답(200 OK)을 반환합니다.
 */
@RestController
@RequestMapping("/v1") // DataPlatformClient 가 호출하는 경로와 일치시킴
@Slf4j
public class DataPlatformMockController {

    /**
     * /v1/reservations 경로로 들어오는 POST 요청을 처리합니다.
     * @param request DataPlatformClient가 보낸 예약 데이터
     * @return HTTP 200 OK 응답
     */
    @PostMapping("/reservations")
    public ResponseEntity<Void> receiveReservationData(@RequestBody ReservationDataRequest request) {
        // 실제 외부 시스템이라면 여기서 데이터를 저장하거나 처리하는 로직이 들어감
        // 현재는 모의 컨트롤러이므로, 요청을 잘 받았다는 로그만 남김
        log.info("[Mock] 데이터 플랫폼, 예약 정보 수신 성공: {}", request);

        // 클라이언트에게 성공적으로 처리되었음을 알리는 200 OK 응답 반환
        return ResponseEntity.ok().build();
    }
}
