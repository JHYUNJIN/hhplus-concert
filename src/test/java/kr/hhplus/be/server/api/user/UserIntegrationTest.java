package kr.hhplus.be.server.api.user;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.UUID;

import kr.hhplus.be.server.api.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.utility.TestcontainersConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.hhplus.be.server.api.user.dto.request.ChargePointRequest;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop" // DB 초기화(테이블 생성 후 테스트 종료 시 삭제됨)
})
@AutoConfigureMockMvc // MockMvc를 사용하여 컨트롤러 테스트를 위한 설정
@Import(TestcontainersConfiguration.class)
@Transactional
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // MockMvc를 사용하여 HTTP 요청을 테스트할 수 있도록 설정

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper; // JSON 변환을 위한 ObjectMapper

    private UUID userId;
    private BigDecimal initPoint;

    @BeforeEach // 테스트 실행마다 새로운 사용자 생성
    void setUp() {
        User savedUser = userRepository.save(TestDataFactory.createUser());
        userId = savedUser.id();
        initPoint = savedUser.amount();
    }

    @Test
    @DisplayName("유저_포인트_조회_성공")
    void getUserPoint_Success() throws Exception {
        mockMvc.perform(get("/api/v1/users/{userId}/points", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // HTTP 응답이 200인지 확인
                .andExpect(jsonPath("$.userId").value(userId.toString())) // userId가 같은지 확인
                .andExpect(jsonPath("$.amount").value(initPoint)); // 포인트 금액이 같은지 확인
    }

    @Test
    @DisplayName("유저_포인트_조회_실패_유저못찾음")
    void getUserPoint_Failure_UserNotFound() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/users/{userId}/points", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // HTTP 응답이 404인지 확인
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()))
        ;
    }

    @Test
    @DisplayName("유저_포인트_충전_성공")
    void chargeUserPoint_Success() throws Exception {
        BigDecimal chargePoint = BigDecimal.valueOf(5000);
        ChargePointRequest request = new ChargePointRequest(chargePoint);

        mockMvc.perform(post("/api/v1/users/{userId}/points", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.amount").value(initPoint.add(chargePoint)))
        ;

        // DB에서 유저 정보 조회
        User findUser = userRepository.findById(userId).orElseThrow();
        // 충전 후 포인트 금액이 초기 포인트 + 충전 포인트와 같은지 확인
        assertThat(findUser.amount()).isEqualTo(initPoint.add(chargePoint));
    }

    @Test
    @DisplayName("유저_포인트_충전_실패_유저못찾음")
    void chargeUserPoint_Failure_UserNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        BigDecimal chargePoint = BigDecimal.valueOf(5000);
        ChargePointRequest request = new ChargePointRequest(chargePoint);

        mockMvc.perform(post("/api/v1/users/{userId}/points", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("유저_포인트_충전_실패_최소충전금액미만(1000원)")
    void chargeUserPoint_Failure_NotEnoughMinChargePoint() throws Exception {
        UUID otherUserId = UUID.randomUUID();
        BigDecimal chargePoint = BigDecimal.valueOf(500);
        ChargePointRequest request = new ChargePointRequest(chargePoint);

        mockMvc.perform(post("/api/v1/users/{userId}/points", otherUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOT_ENOUGH_MIN_CHARGE_POINT.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.NOT_ENOUGH_MIN_CHARGE_POINT.getMessage()));

        User findUser = userRepository.findById(userId).orElseThrow();
        assertThat(findUser.amount()).isEqualTo(initPoint);
    }

    @Test
    @DisplayName("유저_포인트_충전_성공_충전금액경계값(1000원)")
    void chargeUserPoint_Success_ChargePoint_1000Won() throws Exception {
        BigDecimal chargePoint = BigDecimal.valueOf(1000);
        ChargePointRequest request = new ChargePointRequest(chargePoint);

        mockMvc.perform(post("/api/v1/users/{userId}/points", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.amount").value(initPoint.add(chargePoint)));

        User findUser = userRepository.findById(userId).orElseThrow();
        assertThat(findUser.amount()).isEqualTo(initPoint.add(chargePoint));
    }

}