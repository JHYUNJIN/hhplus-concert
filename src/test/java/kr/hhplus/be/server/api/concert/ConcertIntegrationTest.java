package kr.hhplus.be.server.api.concert;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

import kr.hhplus.be.server.api.TestDataFactory;
import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.port.out.ConcertRepository;
import kr.hhplus.be.server.concert.domain.ConcertDate;
import kr.hhplus.be.server.concert.port.out.ConcertDateRepository;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.port.out.SeatRepository;
import kr.hhplus.be.server.concert.domain.enums.SeatStatus;
import kr.hhplus.be.server.common.exception.ErrorCode;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class ConcertIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertDateRepository concertDateRepository;

    @Autowired
    private SeatRepository seatRepository;

    private Concert concert;
    private ConcertDate concertDate;
    private Seat seat;

    private UUID concertId;
    private UUID concertDateId;
    private UUID seatId;

    @BeforeEach
    void setUp() {
        concert = TestDataFactory.createConcert();
        Concert savedConcert = concertRepository.save(concert);
        concertId = savedConcert.id();

        concertDate = TestDataFactory.createConcertDate(concertId);
        ConcertDate savedConcertDate = concertDateRepository.save(concertDate);
        concertDateId = savedConcertDate.id();

        seat = TestDataFactory.createSeat(concertDateId);
        Seat savedSeat = seatRepository.save(seat);
        seatId = savedSeat.id();
    }

    @Test
    @DisplayName("예약가능_날짜조회_성공")
    void getAvailableConcertDate_Success() throws Exception {
        mockMvc.perform(get("/api/v1/concerts/{concertId}/dates", concertId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].concertId").value(concertId.toString()))
                .andExpect(jsonPath("$[0].concertDateId").value(concertDateId.toString()))
                .andExpect(jsonPath("$[0].remainingSeatCount").value(1))
        ;
    }

    @Test
    @DisplayName("예약가능_날짜조회_실패_콘서트찾지못함")
    void getAvailableConcertDate_Failure_ConcertNotFound() throws Exception {
        UUID concertId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/concerts/{concertId}/dates", concertId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.CONCERT_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.CONCERT_NOT_FOUND.getMessage()))
        ;
    }

    @Test
    @DisplayName("예약가능_날짜조회_성공(마감시간지남)")
    void getAvailableConcertDate_Success_AfterDeadline() throws Exception {
        ConcertDate expiredConcertDate = ConcertDate.builder()
                .concertId(concertId)
                .date(LocalDateTime.now().plusDays(1))
                .deadline(LocalDateTime.now().minusHours(1))
                .build();
        concertDateRepository.save(expiredConcertDate);

        mockMvc.perform(get("/api/v1/concerts/{concertId}/dates", concertId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))  // 기존 마감 시간 안지난 날짜 하나만 조회
                .andExpect(jsonPath("$[0].concertDateId").value(concertDateId.toString()))
        ;
    }

    @Test
    @DisplayName("예약가능_날짜조회_성공(좌석없음)")
    void getAvailableConcertDate_Success_NoAvailableSeat() throws Exception {
        // 콘서트별 좌석 수 조회
        List<ConcertDate> result  = concertDateRepository.findAvailableDates(concertId);
        Seat reservedSeat = Seat.builder()
                .id(this.seat.id()) // 기존 Seat의 ID 사용
                .concertDateId(this.seat.concertDateId()) // 기존 Seat의 concertDateId 사용
                .seatNo(this.seat.seatNo()) // 기존 Seat의 seatNo 사용
                .price(this.seat.price()) // 기존 Seat의 price 사용
                .seatGrade(this.seat.seatGrade()) // 기존 Seat의 seatGrade 사용
                .status(SeatStatus.RESERVED) // 💡 상태만 RESERVED로 변경
                .createdAt(this.seat.createdAt()) // 기존 Seat의 createdAt 사용
                .updatedAt(LocalDateTime.now()) // 💡 updatedAt 갱신
                .build();
        
        seatRepository.save(reservedSeat);
        mockMvc.perform(get("/api/v1/concerts/{concertId}/dates", concertId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty())
        ;
    }

    @Test
    @DisplayName("예약가능_좌석조회_성공")
    void getAvailableSeat_Success() throws Exception {
        mockMvc.perform(get("/api/v1/concerts/{concertId}/dates/{concertDateId}/seats", concertId, concertDateId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seatId").value(seatId.toString()))
                .andExpect(jsonPath("$[0].seatNo").value(seat.seatNo()))
                .andExpect(jsonPath("$[0].price").value(seat.price()))
                .andExpect(jsonPath("$[0].status").value(seat.status().toString()))
                .andExpect(jsonPath("$[0].seatGrade").value(seat.seatGrade().toString()))
        ;
    }

    @Test
    @DisplayName("예약가능_좌석조회_실패_가능한날짜아님")
    void getAvailableSeat_Failure_CannotAvailableDate() throws Exception {
        UUID concertDateId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/concerts/{concertId}/dates/{concertDateId}/seats", concertId, concertDateId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.CANNOT_RESERVATION_DATE.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.CANNOT_RESERVATION_DATE.getMessage()))
        ;
    }

    @Test
    @DisplayName("예약가능_좌석조회_성공(예약가능좌석없음)")
    void getAvailableSeat_Success_NoAvailableSeat() throws Exception {
        Seat reservedSeat = Seat.builder()
                .id(seatId)
                .concertDateId(concertDateId)
                .seatNo(seat.seatNo())
                .price(seat.price())
                .seatGrade(seat.seatGrade())
                .status(SeatStatus.RESERVED)
                .createdAt(seat.createdAt())
                .updatedAt(LocalDateTime.now())
                .build();

        seatRepository.save(reservedSeat);

        mockMvc.perform(get("/api/v1/concerts/{concertId}/dates/{concertDateId}/seats", concertId, concertDateId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty())
        ;
    }
}