// src/main/java/kr/hhplus.be.server/infrastructure/configuration/JpaConfig.java
package kr.hhplus.be.server.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaAuditing // JPA Auditing 기능 활성화 (created_at, updated_at 자동 관리)
// Clean Architecture 구조에 맞게 basePackages를 변경합니다.
// JpaRepository 인터페이스 구현체들이 위치할 새로운 경로를 정확히 명시합니다.
@EnableJpaRepositories(basePackages = "kr.hhplus.be.infrastructure.persistence") 
public class JpaConfig {

    // PlatformTransactionManager 빈을 명시적으로 정의합니다.
    // Spring Boot는 보통 자동으로 이를 구성하지만, 명시적으로 정의하는 것은 좋은 습관입니다.
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new JpaTransactionManager();
    }
}