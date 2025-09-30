plugins {
	java
	id("org.springframework.boot") version "3.4.1"
	id("io.spring.dependency-management") version "1.1.7"
}

fun getGitHash(): String {
	return providers.exec {
		commandLine("git", "rev-parse", "--short", "HEAD")
	}.standardOutput.asText.get().trim()
}

group = "kr.hhplus.be"
version = getGitHash()

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:2024.0.0")
	}
}

dependencies {
	// Spring
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-aop")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.redisson:redisson-spring-boot-starter:3.24.3") // 분산락 제어를 위해 사용
	implementation ("org.springframework.boot:spring-boot-starter-webflux") // WebFlux를 사용하여 비동기 처리를 지원
//	implementation("org.springframework.boot:spring-boot-starter-security")
//	developmentOnly("org.springframework.boot:spring-boot-devtools")





	// swagger
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")

	// lombok
	implementation("org.projectlombok:lombok")
	annotationProcessor ("org.projectlombok:lombok")

	// DB
	runtimeOnly("com.mysql:mysql-connector-j")
	implementation("org.flywaydb:flyway-mysql")

	// Kafka
	implementation("org.springframework.kafka:spring-kafka")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("org.testcontainers:kafka")

	// QueryDSL
	implementation ("io.micrometer:micrometer-registry-prometheus")
	implementation ("org.springframework.boot:spring-boot-starter-actuator")

	// 더미 데이터 생성 라이브러리
	implementation("net.datafaker:datafaker:2.4.2")

	// Test
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:mysql")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
	systemProperty("user.timezone", "UTC")
}