import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend } from 'k6/metrics';

// --- 설정: 각 API 엔드포인트별 응답 시간 측정을 위한 Custom Trend Metrics ---
const trends = {
  issueToken: new Trend('waiting_time_issue_token'),
  checkStatus: new Trend('waiting_time_check_status'),
  getDates: new Trend('waiting_time_get_dates'),
  getSeats: new Trend('waiting_time_get_seats'),
  reserveSeat: new Trend('waiting_time_reserve_seat'),
  payment: new Trend('waiting_time_payment'),
};

// --- 데이터 로드: 미리 준비한 테스트 데이터를 불러옵니다 ---
const testData = JSON.parse(open('./test-data.json'));

// --- 테스트 옵션: ⭐️ 부하 강도를 5배 높였습니다 ---
export const options = {
  stages: [
    { duration: '2m', target: 50 }, // 2분 동안 가상 유저 50명까지 서서히 증가
    { duration: '5m', target: 50 }, // 50명의 유저로 5분 동안 부하 유지
    { duration: '1m', target: 0 },  // 1분 동안 서서히 유저 수 감소
  ],
  thresholds: {
    'http_req_failed': ['rate<0.02'],      // 전체 HTTP 에러율은 2% 미만이어야 합니다.
    'http_req_duration': ['p(95)<1500'],    // 95%의 요청은 1.5초 안에 처리되어야 합니다.
    'waiting_time_reserve_seat': ['p(95)<2000'], // 좌석 예약은 2초 안에 처리되어야 합니다.
    'waiting_time_payment': ['p(95)<2500'],      // 결제는 2.5초 안에 처리되어야 합니다.
  },
};

// --- 메인 테스트 로직: 각 가상 유저가 이 함수를 반복 실행합니다 ---
export default function () {
  // 0. 테스트에 사용할 유저와 콘서트를 무작위로 선택합니다.
  const user = testData.userIds[Math.floor(Math.random() * testData.userIds.length)];
  const concertId = testData.concertIds[Math.floor(Math.random() * testData.concertIds.length)];

  let queueToken = '';
  let concertDateId = '';
  let seatId = '';
  let reservationId = '';

  // 그룹 1: 대기열 진입 및 토큰 활성화
  group('1. Queue & Activation', function () {
    const issueTokenRes = http.post(
      `http://localhost:8080/api/v1/queue/concerts/${concertId}/users/${user}`,
      null,
      { tags: { name: 'IssueQueueToken' } }
    );
    trends.issueToken.add(issueTokenRes.timings.duration);
    check(issueTokenRes, { '토큰 발급 성공': (r) => r.status === 201 });

    if (issueTokenRes.status === 201 && issueTokenRes.json('tokenId')) {
      queueToken = issueTokenRes.json('tokenId');

      let isActive = false;
      for (let i = 0; i < 10; i++) {
        const statusRes = http.get(
          `http://localhost:8080/api/v1/queue/concerts/${concertId}`,
          {
            headers: { 'Authorization': `Bearer ${queueToken}` },
            tags: { name: 'CheckQueueStatus' }
          }
        );
        trends.checkStatus.add(statusRes.timings.duration);
        if (statusRes.status === 200 && statusRes.json('status') === 'ACTIVE') {
          isActive = true;
          break;
        }
        sleep(3);
      }
      check(isActive, { '토큰 활성화 성공': (active) => active });
    }
  });

  if (!queueToken) return;

  const authHeaders = { headers: { 'Authorization': `Bearer ${queueToken}` } };

  // 그룹 2: 날짜 및 좌석 선택
  group('2. Seat Selection', function () {
    const datesRes = http.get(`http://localhost:8080/api/v1/concerts/${concertId}/dates`, authHeaders);
    trends.getDates.add(datesRes.timings.duration);
    check(datesRes, { '날짜 조회 성공': (r) => r.status === 200 });

    if (datesRes.status === 200) {
        const availableDates = datesRes.json();
        if (availableDates && availableDates.length > 0) {
            const randomDate = availableDates[Math.floor(Math.random() * availableDates.length)];
            if (randomDate && randomDate.concertDateId) {
                concertDateId = randomDate.concertDateId;

                const seatsRes = http.get(`http://localhost:8080/api/v1/concerts/${concertId}/dates/${concertDateId}/seats`, authHeaders);
                trends.getSeats.add(seatsRes.timings.duration);
                check(seatsRes, { '좌석 조회 성공': (r) => r.status === 200 });

                if (seatsRes.status === 200) {
                    const availableSeats = seatsRes.json();
                    if (availableSeats && availableSeats.length > 0) {
                        const randomSeat = availableSeats[Math.floor(Math.random() * availableSeats.length)];
                        if (randomSeat && randomSeat.seatId) {
                            seatId = randomSeat.seatId;
                        }
                    }
                }
            }
        }
    }
  });

  if (!seatId) return;

  // 그룹 3: 좌석 예약
  group('3. Reservation', function () {
    const reservationPayload = JSON.stringify({ concertId: concertId, concertDateId: concertDateId });
    const reservationRes = http.post(`http://localhost:8080/api/v1/reservations/seats/${seatId}`, reservationPayload, { headers: { ...authHeaders.headers, 'Content-Type': 'application/json' } });
    trends.reserveSeat.add(reservationRes.timings.duration);
    check(reservationRes, { '좌석 예약 성공': (r) => r.status === 200 });

    if (reservationRes.status === 200 && reservationRes.json('reservationId')) {
      reservationId = reservationRes.json('reservationId');
    }
  });

  if (!reservationId) return;

  sleep(2);

  // 그룹 4: 결제
  group('4. Payment', function () {
    const paymentRes = http.post(
      `http://localhost:8080/api/v1/payments/${reservationId}`,
      null,
      {
        headers: { 'Authorization': `Bearer ${queueToken}` },
        tags: { name: 'Payment' }
      }
    );
    trends.payment.add(paymentRes.timings.duration);
    check(paymentRes, { '결제 성공': (r) => r.status === 200 });
  });
}
