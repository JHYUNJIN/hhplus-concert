import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { SharedArray } from 'k6/data';
import { Trend } from 'k6/metrics';

// --- 1. Custom Metrics: 상세 분석을 위한 사용자 정의 측정 지표 ---
const trends = {
  issueToken: new Trend('waiting_time_issue_token', true),
  checkStatus: new Trend('waiting_time_check_status', true),
  getDates: new Trend('waiting_time_get_dates', true),
  getSeats: new Trend('waiting_time_get_seats', true),
  reserveSeat: new Trend('waiting_time_reserve_seat', true),
  payment: new Trend('waiting_time_payment', true),
  queueActivationTime: new Trend('waiting_time_queue_activation', true),
};

// --- 2. 데이터 로드: 시나리오별로 사용할 데이터를 분리하여 로드합니다 ---
const richUsers = new SharedArray('richUsers', () =>
  JSON.parse(open('./test-userId-v2.json')).rich_users.map(user => user.id)
);
const poorUsers = new SharedArray('poorUsers', () =>
  JSON.parse(open('./test-userId-v2.json')).poor_users.map(user => user.id)
);
const concertData = new SharedArray('concertIds', () =>
  JSON.parse(open('./test-concertId.json')).concert.map(concert => concert.id)
);


// --- 3. 테스트 옵션: 1000 VU 고강도 테스트 ---
export const options = {
  scenarios: {
    happy_path_booking: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '2m', target: 600 }, // 300 -> 600 VUs
        { duration: '5m', target: 600 },
        { duration: '1m', target: 0 },
      ],
      exec: 'happyPathBooking',
    },
    browsing_user: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '2m', target: 300 }, // 150 -> 300 VUs
        { duration: '5m', target: 300 },
        { duration: '1m', target: 0 },
      ],
      exec: 'browsingUser',
    },
    failed_payment_user: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '2m', target: 100 }, // 50 -> 100 VUs
        { duration: '5m', target: 100 },
        { duration: '1m', target: 0 },
      ],
      exec: 'failedPaymentUser',
    },
  },
  thresholds: {
    'http_req_failed': ['rate<0.05'], // 실패율 5% 미만
    'http_req_duration{scenario:happy_path_booking}': ['p(95)<3000'], // 95%의 요청이 3초 안에 처리
    'http_req_duration{scenario:browsing_user}': ['p(95)<1000'], // 95%의 요청이 1초 안에 처리
  },
};

// --- 공통 함수: 토큰 발급 및 활성화 대기 ---
function issueAndWaitForActiveToken(userId, concertId) {
  let queueToken = '';
  const startTime = Date.now();

  const issueTokenRes = http.post(`http://localhost:8080/api/v1/queue/concerts/${concertId}/users/${userId}`, null, { tags: { name: 'IssueQueueToken' } });
  trends.issueToken.add(issueTokenRes.timings.duration);
  check(issueTokenRes, { '토큰 발급 성공': (r) => r.status === 201 });

  if (issueTokenRes.status === 201 && issueTokenRes.json('tokenId')) {
    queueToken = issueTokenRes.json('tokenId');
    let isActive = false;
    // 활성화 대기 시간을 15회(약 45초)로 늘려 더 많은 트래픽을 수용
    for (let i = 0; i < 15; i++) {
      const statusRes = http.get(`http://localhost:8080/api/v1/queue/concerts/${concertId}`, { headers: { 'Authorization': `Bearer ${queueToken}` }, tags: { name: 'CheckQueueStatus' } });
      trends.checkStatus.add(statusRes.timings.duration);
      if (statusRes.status === 200 && statusRes.json('status') === 'ACTIVE') {
        isActive = true;
        const activationTime = Date.now() - startTime;
        trends.queueActivationTime.add(activationTime);
        break;
      }
      sleep(3);
    }
    check(isActive, { '토큰 활성화 성공': (active) => active });
  }
  return queueToken;
}

// --- 시나리오 A: 정상 예매 및 결제 ---
export function happyPathBooking() {
    const user = richUsers[Math.floor(Math.random() * richUsers.length)];
    const concertId = concertData[Math.floor(Math.random() * concertData.length)];
    let concertDateId = '';
    let seatId = '';
    let reservationId = '';

    const queueToken = issueAndWaitForActiveToken(user, concertId);
    if (!queueToken) return;
    const authHeaders = { headers: { 'Authorization': `Bearer ${queueToken}` } };

    group('HappyPath - 2. Seat Selection', function () {
        const datesRes = http.get(`http://localhost:8080/api/v1/concerts/${concertId}/dates`, authHeaders);
        trends.getDates.add(datesRes.timings.duration);
        check(datesRes, { '날짜 조회 성공 (HappyPath)': (r) => r.status === 200 });

        if (datesRes.status === 200) {
            const availableDates = datesRes.json();
            if (availableDates && availableDates.length > 0) {
                const randomDate = availableDates[Math.floor(Math.random() * availableDates.length)];
                if (randomDate && randomDate.concertDateId) {
                    concertDateId = randomDate.concertDateId;
                    const seatsRes = http.get(`http://localhost:8080/api/v1/concerts/${concertId}/dates/${concertDateId}/seats`, authHeaders);
                    trends.getSeats.add(seatsRes.timings.duration);
                    check(seatsRes, { '좌석 조회 성공 (HappyPath)': (r) => r.status === 200 });

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

    group('HappyPath - 3. Reservation', function () {
        const reservationPayload = JSON.stringify({ concertId: concertId, concertDateId: concertDateId });
        const reservationRes = http.post(`http://localhost:8080/api/v1/reservations/seats/${seatId}`, reservationPayload, { headers: { ...authHeaders.headers, 'Content-Type': 'application/json' } });
        trends.reserveSeat.add(reservationRes.timings.duration);
        check(reservationRes, { '좌석 예약 성공 (HappyPath)': (r) => r.status === 200 });
        if (reservationRes.status === 200 && reservationRes.json('reservationId')) {
            reservationId = reservationRes.json('reservationId');
        }
    });

    if (!reservationId) return;
    sleep(2);

    group('HappyPath - 4. Payment', function () {
        const paymentRes = http.post(`http://localhost:8080/api/v1/payments/${reservationId}`, null, { headers: { 'Authorization': `Bearer ${queueToken}` }, tags: { name: 'Payment' } });
        trends.payment.add(paymentRes.timings.duration);
        check(paymentRes, { '결제 성공 (HappyPath)': (r) => r.status === 200 });
    });
}

// --- 시나리오 B: 단순 조회 ---
export function browsingUser() {
    const user = richUsers[Math.floor(Math.random() * richUsers.length)];
    const concertId = concertData[Math.floor(Math.random() * concertData.length)];
    let concertDateId = '';

    const queueToken = issueAndWaitForActiveToken(user, concertId);
    if (!queueToken) return;
    const authHeaders = { headers: { 'Authorization': `Bearer ${queueToken}` } };

    group('Browsing - 2. Seat Selection', function () {
        const datesRes = http.get(`http://localhost:8080/api/v1/concerts/${concertId}/dates`, authHeaders);
        trends.getDates.add(datesRes.timings.duration);
        check(datesRes, { '날짜 조회 성공 (Browsing)': (r) => r.status === 200 });

        if (datesRes.status === 200) {
            const availableDates = datesRes.json();
            if (availableDates && availableDates.length > 0) {
                const randomDate = availableDates[Math.floor(Math.random() * availableDates.length)];
                if (randomDate && randomDate.concertDateId) {
                    concertDateId = randomDate.concertDateId;
                    const seatsRes = http.get(`http://localhost:8080/api/v1/concerts/${concertId}/dates/${concertDateId}/seats`, authHeaders);
                    trends.getSeats.add(seatsRes.timings.duration);
                    check(seatsRes, { '좌석 조회 성공 (Browsing)': (r) => r.status === 200 });
                }
            }
        }
    });
    sleep(5);
}

// --- 시나리오 C: 잔액 부족으로 결제 실패 ---
export function failedPaymentUser() {
    const user = poorUsers[Math.floor(Math.random() * poorUsers.length)];
    const concertId = concertData[Math.floor(Math.random() * concertData.length)];
    let concertDateId = '';
    let seatId = '';
    let reservationId = '';

    const queueToken = issueAndWaitForActiveToken(user, concertId);
    if (!queueToken) return;
    const authHeaders = { headers: { 'Authorization': `Bearer ${queueToken}` } };

    group('FailedPayment - 2. Seat Selection', function () {
        const datesRes = http.get(`http://localhost:8080/api/v1/concerts/${concertId}/dates`, authHeaders);
        trends.getDates.add(datesRes.timings.duration);
        check(datesRes, { '날짜 조회 성공 (FailedPayment)': (r) => r.status === 200 });

        if (datesRes.status === 200) {
            const availableDates = datesRes.json();
            if (availableDates && availableDates.length > 0) {
                const randomDate = availableDates[Math.floor(Math.random() * availableDates.length)];
                if (randomDate && randomDate.concertDateId) {
                    concertDateId = randomDate.concertDateId;
                    const seatsRes = http.get(`http://localhost:8080/api/v1/concerts/${concertId}/dates/${concertDateId}/seats`, authHeaders);
                    trends.getSeats.add(seatsRes.timings.duration);
                    check(seatsRes, { '좌석 조회 성공 (FailedPayment)': (r) => r.status === 200 });

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

    group('FailedPayment - 3. Reservation', function () {
        const reservationPayload = JSON.stringify({ concertId: concertId, concertDateId: concertDateId });
        const reservationRes = http.post(`http://localhost:8080/api/v1/reservations/seats/${seatId}`, reservationPayload, { headers: { ...authHeaders.headers, 'Content-Type': 'application/json' } });
        trends.reserveSeat.add(reservationRes.timings.duration);
        check(reservationRes, { '좌석 예약 성공 (FailedPayment)': (r) => r.status === 200 });
        if (reservationRes.status === 200 && reservationRes.json('reservationId')) {
            reservationId = reservationRes.json('reservationId');
        }
    });

    if (!reservationId) return;
    sleep(2);

    group('FailedPayment - 4. Payment', function () {
        const paymentRes = http.post(`http://localhost:8080/api/v1/payments/${reservationId}`, null, { headers: { 'Authorization': `Bearer ${queueToken}` }, tags: { name: 'Payment' } });
        trends.payment.add(paymentRes.timings.duration);
        check(paymentRes, { '결제 실패 (잔액 부족) 성공': (r) => [400, 409, 422].includes(r.status) });
    });
}