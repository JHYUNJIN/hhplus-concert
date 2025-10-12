# 마이크로서비스 아키텍처(MSA) 전환 보고서

본 문서는 기존의 분산형 모놀리스 구조에서 벗어나, 완전한 마이크로서비스 아키텍처의 주요 패턴을 적용하는 리팩토링 과정을 기록합니다.

## 1단계: 데이터베이스 분리 (Database per Service)

### 목표
각 서비스가 독립적인 데이터 저장소를 소유하여 서비스 간의 데이터 결합도를 제거합니다. 이를 통해 서비스의 자율성을 보장하고, 한 서비스의 데이터 변경이 다른 서비스에 미치는 영향을 원천적으로 차단합니다.

### 변경 사항
1.  **`user-db` 컨테이너 추가:** `docker-compose.yml`에 `user-service`만을 위한 별도의 MySQL 컨테이너(`user-db`)를 정의했습니다. 이 컨테이너는 `3307` 포트를 사용하며, `user_db`라는 데이터베이스를 생성합니다.
2.  **`user-service` 설정 변경:** `user-service`의 `application.yml`에 있는 `spring.datasource.url`이 `user_db` 컨테이너(`jdbc:mysql://localhost:3307/user_db`)를 바라보도록 수정했습니다.
3.  **`main-server` 마이그레이션 확인:** `main-server`의 Flyway 마이그레이션 스크립트에 `USERS` 테이블 관련 내용이 없음을 확인하여, 데이터베이스 스키마가 명확히 분리되었음을 보장했습니다.

### 결과
`main-server`는 `hhplus` 데이터베이스를, `user-service`는 `user_db` 데이터베이스를 사용하게 되어 데이터 독립성을 확보했습니다.

---

## 2단계: 서비스 디스커버리 (Service Discovery)

### 목표
서비스 간 통신 시 IP 주소나 포트 번호를 하드코딩하는 대신, 서비스의 논리적인 이름을 기반으로 동적으로 네트워크 위치를 찾아 통신할 수 있는 환경을 구축합니다.

### 변경 사항
1.  **`discovery-service` 모듈 생성:** Spring Cloud Netflix Eureka를 사용하여 모든 마이크로서비스를 등록하고 관리할 Eureka 서버(`discovery-service`)를 새로운 Spring Boot 애플리케이션으로 구현했습니다. (포트: `8761`)
2.  **Eureka 클라이언트 설정:** `main-server`와 `user-service`에 `spring-cloud-starter-netflix-eureka-client` 의존성을 추가하고, Eureka 서버에 자신을 등록하도록 설정을 추가했습니다.
3.  **서비스 간 통신 방식 변경:**
    *   `main-server`의 `WebClientConfig`에 `@LoadBalanced` 어노테이션을 추가했습니다.
    *   `user-service.url`의 값을 하드코딩된 `http://localhost:8081`에서 서비스의 논리적 이름인 `http://user-service`로 변경했습니다.

### 결과
이제 `main-server`는 Eureka 서버를 통해 `user-service`의 실제 주소를 동적으로 찾아 통신합니다. 이를 통해 특정 서비스의 인스턴스가 추가되거나 IP/포트가 변경되어도 유연하게 대처할 수 있는 확장성과 탄력성을 확보했습니다.

---

## 3단계: API 게이트웨이 (API Gateway)

### 목표
마이크로서비스 시스템의 모든 외부 요청을 처리하는 단일 진입점을 마련합니다. 이를 통해 클라이언트는 복잡한 내부 구조를 알 필요 없이 일관된 주소로 시스템과 상호작용할 수 있습니다.

### 변경 사항
1.  **`gateway-service` 모듈 생성:** Spring Cloud Gateway를 사용하여 외부 요청의 진입점 역할을 할 `gateway-service`를 새로운 Spring Boot 애플리케이션으로 구현했습니다. (포트: `8080`)
2.  **포트 재조정:** 게이트웨이가 외부 대표 포트인 `8080`을 사용하게 되면서, 기존 `main-server`의 포트는 `8082`로 변경했습니다.
3.  **라우팅 규칙 정의:** `gateway-service`의 `application.yml`에 경로 기반 라우팅 규칙을 설정했습니다. 예를 들어, `/api/v1/users/**` 패턴의 요청은 `user-service`로, `/api/v1/concerts/**` 등의 요청은 `main-server`로 전달됩니다.

### 결과
모든 외부 트래픽은 API 게이트웨이를 통해 내부 서비스로 전달됩니다. 이를 통해 내부 구조를 캡슐화하고, 향후 인증/인가, 로깅, 속도 제한 등 공통 횡단 관심사를 처리할 수 있는 중앙 지점을 확보했습니다.

---

## 4단계: 중앙화된 설정 관리 (Centralized Configuration)

### 목표
여러 서비스에 흩어져 있는 `application.yml` 설정 파일들을 한 곳에서 통합 관리하여 운영 효율성을 높이고, 설정 변경의 용이성을 확보합니다.

### 변경 사항
1.  **`config-service` 모듈 생성:** Spring Cloud Config를 사용하여 설정 정보를 제공하는 `config-service`를 새로운 Spring Boot 애플리케이션으로 구현했습니다. (포트: `8888`)
2.  **`config-repo` 디렉토리 생성:** 프로젝트 루트에 `config-repo` 디렉토리를 만들고, 각 서비스(`gateway-service.yml`, `main-server.yml`, `user-service.yml`)의 설정과 공통 설정(`application.yml`)을 분리하여 저장했습니다.
3.  **클라이언트 설정 변경:**
    *   모든 서비스(`gateway`, `main`, `user`)에 `spring-cloud-starter-config` 의존성을 추가했습니다.
    *   기존 `application.yml`을 삭제하고, 대신 Config 서버의 주소를 가리키는 `bootstrap.yml` 파일을 생성했습니다.

### 결과
이제 모든 서비스는 시작 시 `config-service`로부터 자신의 설정을 동적으로 받아옵니다. 설정 변경이 필요할 때 각 서비스를 재배포할 필요 없이 중앙 `config-repo`의 파일만 수정하면 되므로, 시스템의 유지보수성과 운영 효율성이 크게 향상되었습니다.

---

## 최종 아키텍처 구성

```
[ 외부 클라이언트 ]
       |
       v (port 8080)
[ API Gateway ] -----------------┐
       | (Load Balancing)        |
       v                         |
/api/v1/users/**  /api/v1/concerts/**...           |
       |                         |
       v                         v
[ User Service ] <-----> [ Main Server ]          | (모든 서비스가 등록 및 조회)
 (port 8081)     (port 8082)           |
       |               |                 |
       v               v                 v
  [ user_db ]     [ hhplus_db ]    [ Eureka Server ]
 (MySQL:3307)    (MySQL:3306)        (port 8761)
                                         ^
                                         |
                                [ Config Server ]
                                    (port 8888)
                                         ^
                                         |
                                  [ config-repo ]
                                  (File System)
```

## 실행 방법

프로젝트 루트 디렉터리에서 다음 명령어를 실행하면 위에서 구성한 모든 마이크로서비스 환경이 Docker Compose를 통해 함께 실행됩니다.

```bash
docker-compose up --build
```
