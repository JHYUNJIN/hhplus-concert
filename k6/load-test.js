import http from 'k6/http';
import { check, sleep } from 'k6';

// 테스트 옵션을 설정합니다.
export const options = {
  // 시나리오 정의
  scenarios: {
    // 'constant_load'라는 이름의 시나리오
    constant_load: {
      executor: 'constant-vus', // '고정 가상 유저' 실행기 사용
      vus: 5,                   // 5명의 가상 유저를 사용
      duration: '10m',          // 10분 동안 테스트 실행
    },
  },
};

// 모든 가상 유저가 반복적으로 실행할 메인 함수
export default function () {
  // ⭐️ 여기에 부하를 주고 싶은 API의 엔드포인트를 입력하세요.
  // 예시: 특정 콘서트의 예약 가능 날짜를 조회하는 API
  // URL의 UUID는 실제 DB에 존재하는 콘서트 ID로 변경해야 합니다.
  const concertId = '00a42089-8d5d-4caa-85a3-8594b86e477c';
  const res = http.get(`http://localhost:8080/api/v1/concerts/${concertId}/dates`);

  // 응답이 성공(HTTP 200)했는지 확인합니다.
  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  // 다음 요청 전 1초 동안 대기합니다.
  sleep(1);
}
