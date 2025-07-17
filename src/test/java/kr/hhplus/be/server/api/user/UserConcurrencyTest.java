package kr.hhplus.be.server.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.api.TestDataFactory;
import kr.hhplus.be.server.api.user.dto.request.ChargePointRequest;
import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class UserConcurrencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final int THREAD_SIZE = 5; // 동시 충전 테스트를 위한 스레드 수

    private UUID userId;
    private BigDecimal initPoint;

    @BeforeEach
    void setUp() {
        User savedUser = userRepository.save(TestDataFactory.createUser());
        userId = savedUser.id();
        initPoint = savedUser.amount();
    }


    @Test
    @DisplayName("유저_포인트_동시충전")
    void chargePoint_concurrency_test() throws Exception {
        BigDecimal chargePoint = BigDecimal.valueOf(5000);
        ChargePointRequest request = new ChargePointRequest(chargePoint);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        CountDownLatch startLatch = new CountDownLatch(1); // 모든 스레드를 동시에 시작하기 위한 카운트다운 래치
        AtomicInteger successfulCharges = new AtomicInteger(0); // 성공한 충전 횟수를 세는 변수

        for (int i = 0; i < THREAD_SIZE; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    startLatch.await(); // 모든 스레드가 여기서 대기
                    int maxRetries = 5; // 최대 재시도 횟수
                    for (int retry = 0; retry < maxRetries; retry++) {
                        try {
                            mockMvc.perform(post("/api/v1/users/{userId}/points", userId)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(request)))
                                    .andExpect(status().isOk());
                            successfulCharges.incrementAndGet(); // 성공 시 카운트 증가
                            break; // 성공하면 루프 종료
                        } catch (Exception e) {
                            // 락 충돌 예외인 경우 재시도
                            if (e.getCause() instanceof CustomException && ((CustomException) e.getCause()).getErrorCode() == ErrorCode.LOCK_CONFLICT) {
                                System.out.println("LOCK CONFLICT 발생, 재시도 중... (Retry: " + (retry + 1) + ")");
                                Thread.sleep(100); // 잠시 대기 후 재시도 (백오프 전략)
                                if (retry == maxRetries -1) { // 마지막 재시도도 실패하면 예외 던지기
                                    throw e;
                                }
                            } else {
                                // 다른 종류의 예외는 바로 던짐
                                System.err.println("예외 발생: " + e.getMessage());
                                throw e;
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("스레드 실행 중 오류 발생: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }));
        }

        startLatch.countDown(); // 해당 함수가 실행될때 await을 해제하여 모든 스레드가 동시에 시작하도록 함
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS); // 모든 스레드가 완료될 때까지 대기

        assertThat(successfulCharges.get()).isEqualTo(THREAD_SIZE);
        User chargedUser = userRepository.findById(userId).orElseThrow();

        assertThat(chargedUser.amount())
                .isEqualTo(initPoint.add(chargePoint.multiply(BigDecimal.valueOf(THREAD_SIZE))));
    }

}
