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
