package kr.hhplus.be.server.common.config.jpa;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "kr.hhplus.be.server.*.*.out.persistence")
public class JpaConfig {
    /**
     * JPA Auditing을 사용하기 위한 설정입니다.
     * 이 설정을 통해 엔티티의 생성 및 수정 시간을 자동으로 관리할 수 있습니다.
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new JpaTransactionManager();
    }
}