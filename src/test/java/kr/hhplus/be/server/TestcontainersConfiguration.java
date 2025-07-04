package kr.hhplus.be.server;

import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import jakarta.annotation.PreDestroy;

@Configuration
class TestcontainersConfiguration {

	public static final MySQLContainer<?> MYSQL_CONTAINER;
	public static final GenericContainer<?> REDIS_CONTAINER;

	static {
		MYSQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
				.withDatabaseName("hhplus")
				.withUsername("test")
				.withPassword("test");
		MYSQL_CONTAINER.start();

		System.setProperty("spring.datasource.url",
				MYSQL_CONTAINER.getJdbcUrl() + "?characterEncoding=UTF-8&serverTimezone=UTC");
		System.setProperty("spring.datasource.username", MYSQL_CONTAINER.getUsername());
		System.setProperty("spring.datasource.password", MYSQL_CONTAINER.getPassword());

		REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("redis:7.0-alpine"))
				.withExposedPorts(6379)
				.withReuse(true);
		REDIS_CONTAINER.start();

		System.setProperty("spring.redis.host", REDIS_CONTAINER.getHost());
		System.setProperty("spring.redis.port", String.valueOf(REDIS_CONTAINER.getMappedPort(6379)));
	}


	/*
	Testcontainers는 이미 Ryuk이라는 자체 메커니즘을 통해 JVM이 종료될 때 모든 관련 컨테이너를 자동으로 정리하고 삭제하기 때문에,
	@PreDestroy 어노테이션을 사용하여 컨테이너를 수동으로 정리할 필요 없음
	 */
//	@PreDestroy
//	public void preDestroy() {
////		if (MYSQL_CONTAINER.isRunning()) {
////			MYSQL_CONTAINER.stop();
////		}
//		if (REDIS_CONTAINER.isRunning()) {
//			REDIS_CONTAINER.stop();
//		}
//	}
}