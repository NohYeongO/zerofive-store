import http from 'k6/http';
import { sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';
import exec from 'k6/execution';
import { SharedArray } from 'k6/data';

// =============================================================================
// 설정
// =============================================================================
const BASE_URL = __ENV.BASE_URL || 'http://api:8080';
const COUPON_STOCK = 200;
const FIRST_BATCH = 100;
const SECOND_BATCH = 900;
const EVENT_ID = 1;
const COUPON_ID = 1;

// =============================================================================
// 유저 배열 생성 (for문으로 명시적 생성)
// =============================================================================
const phase1Users = new SharedArray('phase1Users', function() {
    const users = [];
    for (let i = 1; i <= 100; i++) {
        users.push(i);
    }
    return users;  // [1, 2, 3, ..., 100]
});

const phase2Users = new SharedArray('phase2Users', function() {
    const users = [];
    for (let i = 1; i <= 100; i++) {
        users.push(i);
    }
    return users;  // [1, 2, 3, ..., 100]
});

const phase3Users = new SharedArray('phase3Users', function() {
    const users = [];
    for (let i = 101; i <= 1000; i++) {
        users.push(i);
    }
    return users;  // [101, 102, ..., 1000]
});

// =============================================================================
// 커스텀 메트릭
// =============================================================================
const couponIssueSuccess = new Counter('coupon_issue_success');
const couponIssueSoldOut = new Counter('coupon_issue_soldout');
const couponIssueDuplicate = new Counter('coupon_issue_duplicate');
const couponIssueFail = new Counter('coupon_issue_fail');
const couponIssueDuration = new Trend('coupon_issue_duration');
const queueEntrySuccess = new Counter('queue_entry_success');
const queueEntryFail = new Counter('queue_entry_fail');
const queueEntryDuration = new Trend('queue_entry_duration');

// =============================================================================
// 시나리오 설정
// - Phase 1: user1~100 대기열 입장 후 쿠폰 발급 (100 SUCCESS)
// - Phase 2: user1~100 중복 발급 시도 (100 DUPLICATE)
// - Phase 3: user101~1000 동시 부하 테스트 (100 SUCCESS, 800 SOLD_OUT)
// =============================================================================
export const options = {
    scenarios: {
        phase1_first_issue: {
            executor: 'per-vu-iterations',
            vus: FIRST_BATCH,
            iterations: 1,
            maxDuration: '2m',
            exec: 'phase1FirstIssue',
            startTime: '0s',
        },
        phase2_duplicate: {
            executor: 'per-vu-iterations',
            vus: FIRST_BATCH,
            iterations: 1,
            maxDuration: '1m',
            exec: 'phase2Duplicate',
            startTime: '15s',
        },
        phase3_load_test: {
            executor: 'per-vu-iterations',
            vus: SECOND_BATCH,
            iterations: 1,
            maxDuration: '3m',
            exec: 'phase3LoadTest',
            startTime: '20s',
        },
    },
    thresholds: {
        'coupon_issue_duration': ['p(95)<3000'],
    },
};

// =============================================================================
// Setup - API 서버 준비 대기
// =============================================================================
export function setup() {
    let ready = false;
    for (let i = 0; i < 30; i++) {
        try {
            const res = http.post(
                `${BASE_URL}/api/accounts/login`,
                JSON.stringify({ email: 'user1@test.com', password: 'test1234' }),
                { headers: { 'Content-Type': 'application/json' }, timeout: '10s' }
            );
            if (res.status === 200) {
                ready = true;
                break;
            }
        } catch (e) {}
        sleep(2);
    }
    return { ready };
}

// =============================================================================
// 로그인
// =============================================================================
function login(userNum) {
    const res = http.post(
        `${BASE_URL}/api/accounts/login`,
        JSON.stringify({ email: `user${userNum}@test.com`, password: 'test1234' }),
        { headers: { 'Content-Type': 'application/json' } }
    );
    if (res.status === 200) {
        try {
            return JSON.parse(res.body).data?.accessToken;
        } catch (e) {
            return null;
        }
    }
    return null;
}

// =============================================================================
// 대기열 입장 + 쿠폰 발급
// =============================================================================
function enterQueueAndIssueCoupon(token) {
    const headers = {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
    };

    // Step 1: 대기열 진입
    const entryStart = Date.now();
    const entryRes = http.post(`${BASE_URL}/api/events/${EVENT_ID}/queue`, null, { headers });
    queueEntryDuration.add(Date.now() - entryStart);

    if (entryRes.status !== 200) {
        queueEntryFail.add(1);
        return { success: false };
    }
    queueEntrySuccess.add(1);

    // Step 2: ADMITTED 상태까지 폴링
    let currentStatus = JSON.parse(entryRes.body).data?.status;
    const maxWaitTime = 60000;
    const pollInterval = 300;
    const pollStart = Date.now();

    while (currentStatus !== 'ADMITTED' && Date.now() - pollStart < maxWaitTime) {
        sleep(pollInterval / 1000);
        const statusRes = http.get(`${BASE_URL}/api/events/${EVENT_ID}/queue/status`, { headers });
        if (statusRes.status !== 200) continue;
        currentStatus = JSON.parse(statusRes.body).data?.status;
        if (currentStatus === 'NOT_IN_QUEUE') {
            return { success: false };
        }
    }

    if (currentStatus !== 'ADMITTED') {
        return { success: false };
    }

    // Step 3: 쿠폰 발급
    const issueStart = Date.now();
    const issueRes = http.post(
        `${BASE_URL}/api/events/${EVENT_ID}/coupons/${COUPON_ID}/issue`,
        null,
        { headers }
    );
    couponIssueDuration.add(Date.now() - issueStart);

    if (issueRes.status !== 200) {
        return { success: false };
    }

    return { success: true, status: JSON.parse(issueRes.body).data?.status };
}

// =============================================================================
// 쿠폰 발급 (대기열 없이 직접)
// =============================================================================
function issueCouponDirect(token) {
    const headers = {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
    };

    const issueStart = Date.now();
    const issueRes = http.post(
        `${BASE_URL}/api/events/${EVENT_ID}/coupons/${COUPON_ID}/issue`,
        null,
        { headers }
    );
    couponIssueDuration.add(Date.now() - issueStart);

    if (issueRes.status !== 200) {
        return { success: false };
    }

    return { success: true, status: JSON.parse(issueRes.body).data?.status };
}

// =============================================================================
// 결과 집계
// =============================================================================
function countResult(result) {
    if (!result.success) {
        couponIssueFail.add(1);
        return;
    }
    switch (result.status) {
        case 'SUCCESS':
            couponIssueSuccess.add(1);
            break;
        case 'SOLD_OUT':
            couponIssueSoldOut.add(1);
            break;
        case 'DUPLICATE':
            couponIssueDuplicate.add(1);
            break;
        default:
            couponIssueFail.add(1);
    }
}

// =============================================================================
// Phase 1: user1~100 대기열 입장 후 쿠폰 발급
// =============================================================================
export function phase1FirstIssue(data) {
    if (!data.ready) return;

    const index = exec.scenario.iterationInInstance;
    const userNum = phase1Users[index];  // [1, 2, ..., 100]
    const token = login(userNum);
    if (!token) {
        couponIssueFail.add(1);
        return;
    }

    const result = enterQueueAndIssueCoupon(token);
    countResult(result);
}

// =============================================================================
// Phase 2: user1~100 중복 발급 시도
// =============================================================================
export function phase2Duplicate(data) {
    if (!data.ready) return;

    const index = exec.scenario.iterationInInstance;
    const userNum = phase2Users[index];  // [1, 2, ..., 100]
    const token = login(userNum);
    if (!token) {
        couponIssueFail.add(1);
        return;
    }

    const result = issueCouponDirect(token);
    countResult(result);
}

// =============================================================================
// Phase 3: user101~1000 동시 부하 테스트
// =============================================================================
export function phase3LoadTest(data) {
    if (!data.ready) return;

    const index = exec.scenario.iterationInInstance;
    const userNum = phase3Users[index];  // [101, 102, ..., 1000]
    const token = login(userNum);
    if (!token) {
        couponIssueFail.add(1);
        return;
    }

    const result = enterQueueAndIssueCoupon(token);
    countResult(result);
}

// =============================================================================
// 결과 요약
// =============================================================================
export function handleSummary(data) {
    const m = data.metrics;
    const success = m.coupon_issue_success?.values?.count || 0;
    const soldout = m.coupon_issue_soldout?.values?.count || 0;
    const duplicate = m.coupon_issue_duplicate?.values?.count || 0;
    const failed = m.coupon_issue_fail?.values?.count || 0;
    const issueP95 = m.coupon_issue_duration?.values?.['p(95)'] || 0;
    const queueP95 = m.queue_entry_duration?.values?.['p(95)'] || 0;

    const expectedSuccess = COUPON_STOCK;
    const expectedDuplicate = FIRST_BATCH;
    const expectedSoldOut = SECOND_BATCH - (COUPON_STOCK - FIRST_BATCH);

    let output = '\n';
    output += '╔═══════════════════════════════════════════════════════════╗\n';
    output += '║            쿠폰 발급 테스트 결과                           ║\n';
    output += '╠═══════════════════════════════════════════════════════════╣\n';
    output += `║  SUCCESS:   ${String(success).padStart(4)}  (예상: ${expectedSuccess})                        ║\n`;
    output += `║  DUPLICATE: ${String(duplicate).padStart(4)}  (예상: ${expectedDuplicate})                        ║\n`;
    output += `║  SOLD_OUT:  ${String(soldout).padStart(4)}  (예상: ${expectedSoldOut})                       ║\n`;
    output += `║  FAILED:    ${String(failed).padStart(4)}                                    ║\n`;
    output += '╠═══════════════════════════════════════════════════════════╣\n';
    output += `║  대기열 진입 p95: ${String(queueP95.toFixed(0)).padStart(5)}ms                        ║\n`;
    output += `║  쿠폰 발급 p95:   ${String(issueP95.toFixed(0)).padStart(5)}ms                        ║\n`;
    output += '╚═══════════════════════════════════════════════════════════╝\n';

    return { stdout: output };
}
