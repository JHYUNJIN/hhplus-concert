// src/main/java/kr/hhplus/be/server/domain/Concert.java
package kr.hhplus.be.server.domain.concert;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CONCERT")
@Getter
@Setter
@NoArgsConstructor
public class Concert {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id; // 콘서트 UUID

    @Column(name = "title", length = 100, nullable = false)
    private String title; // 콘서트 제목

    @Column(name = "artist", length = 50, nullable = false)
    private String artist; // 아티스트명

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 생성일시

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 수정일시

    // 양방향 관계 설정
    @OneToMany(mappedBy = "concert", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConcertDate> concertDates = new ArrayList<>();

    // 편의를 위한 생성자
    public Concert(String id, String title, String artist) {
        this.id = id;
        this.title = title;
        this.artist = artist;
    }
}