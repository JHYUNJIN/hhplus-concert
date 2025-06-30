package kr.hhplus.be.server.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("티켓팅 서비스 API 문서")
                        .version("1.0")
                        .description("hhplus 티켓팅 서비스 API 문서"))
                .components(new Components()
                        .addSecuritySchemes("queue_token",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("Queue-Token")
                                        .description("대기열 토큰을 Authorization 헤더에 Bearer 방식으로 전송")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("queue_token"));
    }
}
