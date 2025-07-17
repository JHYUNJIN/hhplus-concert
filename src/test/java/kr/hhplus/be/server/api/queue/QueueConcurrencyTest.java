package kr.hhplus.be.server.api.queue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.api.TestDataFactory;
import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.port.out.ConcertRepository;
import kr.hhplus.be.server.concert.domain.ConcertDate;
import kr.hhplus.be.server.concert.port.out.ConcertDateRepository;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class QueueConcurrencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConcertDateRepository concertDateRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final int THREAD_SIZE = 5;

    private UUID concertId;
    private UUID userId;
    private UUID concertDateId;

    private User user;
    private Concert concert;
    private ConcertDate concertDate;

    @BeforeEach
    void beforeEach() {
        redisTemplate.getConnectionFactory().getConnection().flushAll(); // Redis 데이터 초기화

        concert = TestDataFactory.createConcert();
        Concert savedConcert = concertRepository.save(concert);
        concertId = savedConcert.id();

        concertDate = TestDataFactory.createConcertDate(concertId);
        ConcertDate savedConcertDate = concertDateRepository.save(concertDate);
        concertDateId = savedConcertDate.id();

        user = TestDataFactory.createUser();
        User savedUser = userRepository.save(user);
        userId = savedUser.id();
    }

    @Test
    @DisplayName("대기열_토큰_동시발급_동일사용자")
    void issueQueueToken_Concurrency_Test() throws Exception {
        List<String> tokenIdList = Collections.synchronizedList(new ArrayList<>()); // 동시성 문제를 피하기 위해 스레드 안전한 리스트 사용
        List<CompletableFuture<Void>> futures = new ArrayList<>(); // 동시 실행을 위한 CompletableFuture 리스트

        for (int i = 0; i <THREAD_SIZE; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    MvcResult mvcResult = mockMvc.perform(
                                    post("/api/v1/queue/concerts/{concertId}/users/{userId}", concertId, userId)
                                            .contentType(MediaType.APPLICATION_JSON))
                            .andExpect(status().isCreated())
                            .andReturn();

                    String responseContent = mvcResult.getResponse().getContentAsString();
                    JsonNode jsonNode = objectMapper.readTree(responseContent);
                    String tokenId = jsonNode.get("tokenId").asText();
                    tokenIdList.add(tokenId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);
        assertThat(tokenIdList.get(0)).isEqualTo(tokenIdList.get(1)); // 기존 토큰이 있을 시 기존 토큰 사용
    }

    @Test
    @DisplayName("대기열_토큰_동시발급_여러사용자")
    void issueQueueToken_Concurrency_MultipleUsers_Test() throws Exception {
        List<String> tokenIdList = Collections.synchronizedList(new ArrayList<>()); // 동시성 문제를 피하기 위해 스레드 안전한 리스트 사용
        List<CompletableFuture<Void>> futures = new ArrayList<>(); // 동시 실행을 위한 CompletableFuture 리스트

        for (int i = 0; i < THREAD_SIZE; i++) {
            User user = TestDataFactory.createUser();
            User savedUser = userRepository.save(user);
            UUID userId = savedUser.id();

            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    MvcResult mvcResult = mockMvc.perform(
                                    post("/api/v1/queue/concerts/{concertId}/users/{userId}", concertId, userId)
                                            .contentType(MediaType.APPLICATION_JSON))
                            .andExpect(status().isCreated())
                            .andReturn();

                    String responseContent = mvcResult.getResponse().getContentAsString();
                    JsonNode jsonNode = objectMapper.readTree(responseContent);
                    String tokenId = jsonNode.get("tokenId").asText();
                    tokenIdList.add(tokenId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);
        assertThat(tokenIdList.size()).isEqualTo(THREAD_SIZE); // 모든 사용자에 대해 토큰이 발급되어야 함
    }
}
