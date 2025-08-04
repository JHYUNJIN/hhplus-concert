-- ====================================================================================
-- USERS 테이블: 사용자 및 잔액 정보
-- ====================================================================================
CREATE TABLE USERS (
    id VARCHAR(36) NOT NULL COMMENT '사용자 UUID',
    amount DECIMAL(10, 0) NOT NULL DEFAULT 0 COMMENT '사용자 잔액',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id)
) COMMENT '사용자 정보 테이블';


-- ====================================================================================
-- CONCERT 테이블: 콘서트 기본 정보
-- ====================================================================================
CREATE TABLE CONCERT (
    id VARCHAR(36) NOT NULL COMMENT '콘서트 UUID',
    title VARCHAR(100) NOT NULL COMMENT '콘서트 제목',
    artist VARCHAR(50) NOT NULL COMMENT '아티스트명',
    open_time DATETIME NOT NULL COMMENT '티켓 오픈 시간',
    sold_out_time DATETIME NULL COMMENT '매진 완료 시간',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id)
) COMMENT '콘서트 정보 테이블';

-- 피드백 반영: 예약 가능한 콘서트 조회 성능 최적화를 위한 복합 인덱스
CREATE INDEX idx_concert_open_soldout ON CONCERT (open_time, sold_out_time);


-- ====================================================================================
-- CONCERT_DATE 테이블: 콘서트 날짜 및 잔여 좌석 정보
-- ====================================================================================
CREATE TABLE CONCERT_DATE (
    id VARCHAR(36) NOT NULL COMMENT '콘서트 날짜 UUID',
    concert_id VARCHAR(36) NOT NULL COMMENT '콘서트 UUID',
    date DATETIME NOT NULL COMMENT '공연 일시',
    deadline DATETIME NOT NULL COMMENT '예약 마감 일시',
    available_seat_count BIGINT COMMENT '예약 가능 좌석 수 (비정규화)',
    version BIGINT COMMENT '낙관적 락을 위한 버전',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    FOREIGN KEY (concert_id) REFERENCES CONCERT(id) ON DELETE CASCADE
) COMMENT '콘서트 날짜 정보 테이블';


-- ====================================================================================
-- SEAT 테이블: 좌석 정보
-- ====================================================================================
CREATE TABLE SEAT (
    id VARCHAR(36) NOT NULL COMMENT '좌석 UUID',
    concert_date_id VARCHAR(36) NOT NULL COMMENT '콘서트 날짜 UUID',
    seat_no INT NOT NULL COMMENT '좌석 번호',
    price DECIMAL(8, 0) NOT NULL COMMENT '좌석 가격',
    seat_class VARCHAR(255) NOT NULL COMMENT '좌석 등급',
    status VARCHAR(10) NOT NULL DEFAULT 'AVAILABLE' COMMENT '좌석 상태 (AVAILABLE, RESERVED, ASSIGNED)',
    version BIGINT COMMENT '낙관적 락을 위한 버전',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    FOREIGN KEY (concert_date_id) REFERENCES CONCERT_DATE(id) ON DELETE CASCADE
) COMMENT '좌석 정보 테이블';


-- ====================================================================================
-- RESERVATION 테이블: 예약 정보
-- ====================================================================================
CREATE TABLE RESERVATION (
    id VARCHAR(36) NOT NULL COMMENT '예약 UUID',
    user_id VARCHAR(36) NOT NULL COMMENT '사용자 UUID',
    seat_id VARCHAR(36) NOT NULL COMMENT '좌석 UUID',
    status VARCHAR(10) NOT NULL DEFAULT 'PENDING' COMMENT '예약 상태 (PENDING, SUCCESS, FAILED, EXPIRED)',
    expires_at DATETIME NOT NULL COMMENT '예약 만료 시간',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES USERS(id),
    FOREIGN KEY (seat_id) REFERENCES SEAT(id)
) COMMENT '예약 정보 테이블';

-- 배치 스케줄러의 만료된 예약 조회 성능 최적화를 위한 복합 인덱스
CREATE INDEX idx_reservation_status_expires ON RESERVATION (status, expires_at);


-- ====================================================================================
-- PAYMENT 테이블: 결제 정보
-- ====================================================================================
CREATE TABLE PAYMENT (
    id VARCHAR(36) NOT NULL COMMENT '결제 UUID',
    user_id VARCHAR(36) NOT NULL COMMENT '사용자 UUID',
    reservation_id VARCHAR(36) NOT NULL COMMENT '예약 UUID',
    amount DECIMAL(10, 0) NOT NULL COMMENT '결제 금액',
    status VARCHAR(10) NOT NULL DEFAULT 'PENDING' COMMENT '결제 상태 (PENDING, SUCCESS, FAILED)',
    failure_reason VARCHAR(255) NULL COMMENT '실패 사유',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES USERS(id),
    FOREIGN KEY (reservation_id) REFERENCES RESERVATION(id)
) COMMENT '결제 정보 테이블';


-- ====================================================================================
-- SOLD_OUT_RANK 테이블: 콘서트 매진 랭킹 정보
-- ====================================================================================
CREATE TABLE SOLD_OUT_RANK (
    id VARCHAR(36) NOT NULL COMMENT '매진 랭킹 UUID',
    concert_id VARCHAR(36) NOT NULL COMMENT '콘서트 UUID',
    score BIGINT NOT NULL COMMENT '랭킹 점수',
    sold_out_time BIGINT NOT NULL COMMENT '매진 소요 시간(초)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (id),
    UNIQUE KEY uk_soldout_concert_id (concert_id), -- 콘서트별 랭킹은 하나만 존재
    FOREIGN KEY (concert_id) REFERENCES CONCERT(id) ON DELETE CASCADE
) COMMENT '콘서트 매진 랭킹 정보 테이블';
