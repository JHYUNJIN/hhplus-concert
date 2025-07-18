package kr.hhplus.be.server.external.dataplatform.adapter.out.dataplatform;

import kr.hhplus.be.server.external.dataplatform.adapter.out.dataplatform.request.ReservationDataRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class DataPlatformClient {

	private final WebClient webClient;

	/**
	 * WebClient.Builder 를 주입받고, 설정 파일(application.yml)에서
	 * 외부 데이터 플랫폼의 기본 URL(base-url)을 가져와 WebClient 인스턴스를 생성합니다.
	 */
	public DataPlatformClient(WebClient.Builder webClientBuilder,
							  @Value("${external-api.data-platform.base-url}") String baseUrl) { // yml 파일에서 URL 가져옴
		this.webClient = webClientBuilder.baseUrl(baseUrl).build();
	}

	/**
	 * WebClient를 사용하여 외부 데이터 플랫폼에 예약 정보를 비동기적으로 전송합니다.
	 * @param reservationDataRequest 전송할 예약 데이터
	 */
	public void sendReservationData(ReservationDataRequest reservationDataRequest) {
		log.info("데이터 플랫폼 예약 정보 전송 시도 - 예약 정보: {}", reservationDataRequest);

		webClient.post()
				.uri("/v1/reservations") // 외부 플랫폼의 구체적인 API 엔드포인트
				.bodyValue(reservationDataRequest) // 전송할 데이터를 body에 담음
				.retrieve() // 응답을 받기 시작
				.toBodilessEntity() // 응답 본문은 필요 없으므로 생략
				.doOnSuccess(response -> {
					// 성공 시 (HTTP 2xx) 로깅
					if (response.getStatusCode().is2xxSuccessful()) {
						log.info("데이터 플랫폼 전송 성공. Status: {}", response.getStatusCode());
					} else {
						// 실패 시 (HTTP 4xx, 5xx) 로깅
						log.warn("데이터 플랫폼 전송 실패. Status: {}", response.getStatusCode());
					}
				})
				.doOnError(error -> {
					log.error("데이터 플랫폼 전송 중 예외 발생.", error);
				})
				.subscribe(); // 비동기 요청을 실제로 실행 (Fire-and-forget)
	}
}