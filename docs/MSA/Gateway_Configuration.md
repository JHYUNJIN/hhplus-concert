````markdown
# MSA 분리 후 게이트웨이 작업 내용

MSA 아키텍처로 전환하면서 API 게이트웨이를 도입하여 시스템의 단일 진입점을 제공하고, 라우팅, 인증, 로드 밸런싱 등 공통 기능을 중앙에서 처리하도록 구성했습니다.

## 1. API 게이트웨이 도입

-   **기술 스택**: `Spring Cloud Gateway`를 사용하여 API 게이트웨이를 구축했습니다.
-   **역할**: 모든 클라이언트 요청에 대한 단일 진입점(Single Point of Entry) 역할을 수행하며, 내부 마이크로서비스들의 엔드포인트를 외부로부터 숨깁니다.

## 2. 서비스 디스커버리와의 연동

-   게이트웨이는 `Eureka`와 연동하여 등록된 서비스들을 동적으로 찾고, 요청을 해당 서비스의 사용 가능한 인스턴스로 라우팅합니다.
-   `application.yml`에 `spring.cloud.gateway.discovery.locator.enabled: true` 설정을 통해 서비스 디스커버리 기반의 라우팅을 활성화했습니다.

## 3. 라우팅 규칙 설정

`gateway-service.yml` 파일에 각 마이크로서비스에 대한 라우팅 규칙을 정의했습니다.

-   **main-server**:
    -   **ID**: `main-server`
    -   **URI**: `lb://MAIN-SERVER` (Eureka에 등록된 `MAIN-SERVER` 서비스)
    -   **경로**: `/api/v1/concerts/**`, `/api/v1/reservations/**`, `/api/v1/payments/**`
-   **user-service**:
    -   **ID**: `user-service`
    -   **URI**: `lb://USER-SERVICE` (Eureka에 등록된 `USER-SERVICE` 서비스)
    -   **경로**: `/api/v1/users/**`

## 4. 기대 효과

-   **중앙 집중식 관리**: 라우팅, 필터링 등 공통 정책을 게이트웨이에서 일괄적으로 관리하여 유지보수성을 높입니다.
-   **유연성 및 확장성**: 신규 서비스가 추가되더라도 게이트웨이에 라우팅 규칙만 추가하면 되므로 유연하고 확장성 있는 구조를 가집니다.
-   **보안 강화**: 내부 서비스들의 엔드포인트를 외부에 직접 노출하지 않아 보안이 강화됩니다.
````