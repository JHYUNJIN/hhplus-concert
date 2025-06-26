// src/main/java/kr/hhplus/be/server/domain/ConcertDate.java
package kr.hhplus.be.server.domain.concertDate;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.seat.Seat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CONCERT_DATE")
@Getter
@Setter
@NoArgsConstructor
public class ConcertDate {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id; // 콘서트 날짜 UUID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", nullable = false)
    private Concert concert; // 콘서트 ID (Concert 엔티티 참조)

    @Column(name = "date", nullable = false)
    private LocalDateTime date; // 공연 일시

    @Column(name = "deadline")
    private LocalDateTime deadline; // 예약 마감일시

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 생성일시

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 수정일시

    // 양방향 관계 설정
    @OneToMany(mappedBy = "concertDate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Seat> seats = new ArrayList<>();

    // 편의를 위한 생성자
    public ConcertDate(String id, Concert concert, LocalDateTime date, LocalDateTime deadline) {
        this.id = id;
        this.concert = concert;
        this.date = date;
        this.deadline = deadline;
    }
}