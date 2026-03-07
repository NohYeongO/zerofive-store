import http from 'k6/http';
import { sleep, check } from 'k6';
import { Counter, Trend } from 'k6/metrics';
import exec from 'k6/execution';
import { SharedArray } from 'k6/data';

// =============================================================================
// 설정
// =============================================================================
const BASE_URL = __ENV.BASE_URL || 'http://api:8080';
const PRODUCT_STOCK = 50;    // 각 상품 재고
const TOTAL_USERS = 100;     // 동시 주문 사용자 수

// =============================================================================
// 유저 배열 생성
// =============================================================================
const users = new SharedArray('users', function() {
    const arr = [];
    for (let i = 1; i <= TOTAL_USERS; i++) {
        arr.push(i);
    }
    return arr;
});

// =============================================================================
// 커스텀 메트릭
// =============================================================================
const orderSuccess = new Counter('order_success');
const orderStockFail = new Counter('order_stock_fail');
const orderFail = new Counter('order_fail');
const orderDuration = new Trend('order_duration');
const deadlockDetected = new Counter('deadlock_detected');

// =============================================================================
// 시나리오 설정
// - 100명 동시 주문: 상품 1,2,3을 랜덤 순서로 주문
// - 재고 50개씩이므로 50명만 성공해야 함
// =============================================================================
export const options = {
    scenarios: {
        concurrent_orders: {
            executor: 'per-vu-iterations',
            vus: TOTAL_USERS,
            iterations: 1,
            maxDuration: '3m',
            exec: 'concurrentOrder',
            startTime: '0s',
        },
    },
    thresholds: {
        'order_duration': ['p(95)<5000'],
        'deadlock_detected': ['count==0'],
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
// 랜덤 순서로 상품 배열 생성 (Fisher-Yates 셔플)
// =============================================================================
function shuffleProducts() {
    const products = [1, 2, 3];
    for (let i = products.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [products[i], products[j]] = [products[j], products[i]];
    }
    return products;
}

// =============================================================================
// 주문 생성
// =============================================================================
function createOrder(token) {
    const headers = {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
    };

    // 상품 1, 2, 3을 랜덤 순서로 배치
    const productOrder = shuffleProducts();

    const orderItems = productOrder.map(productId => ({
        productId: productId,
        quantity: 1
    }));

    const orderStart = Date.now();
    const orderRes = http.post(
        `${BASE_URL}/api/orders`,
        JSON.stringify(orderItems),
        { headers, timeout: '30s' }
    );
    orderDuration.add(Date.now() - orderStart);

    // 응답 분석
    if (orderRes.status === 200) {
        return { success: true, status: 'SUCCESS' };
    }

    // 에러 응답 파싱
    try {
        const body = JSON.parse(orderRes.body);
        const message = body.message || '';

        // 재고 부족
        if (message.includes('재고') || message.includes('stock') || orderRes.status === 400) {
            return { success: false, status: 'STOCK_FAIL' };
        }

        // 데드락 감지
        if (message.includes('deadlock') || message.includes('Deadlock')) {
            return { success: false, status: 'DEADLOCK' };
        }
    } catch (e) {}

    return { success: false, status: 'FAIL' };
}

// =============================================================================
// 결과 집계
// =============================================================================
function countResult(result) {
    switch (result.status) {
        case 'SUCCESS':
            orderSuccess.add(1);
            break;
        case 'STOCK_FAIL':
            orderStockFail.add(1);
            break;
        case 'DEADLOCK':
            deadlockDetected.add(1);
            orderFail.add(1);
            break;
        default:
            orderFail.add(1);
    }
}

// =============================================================================
// 동시 주문 테스트
// =============================================================================
export function concurrentOrder(data) {
    if (!data.ready) return;

    const index = exec.scenario.iterationInInstance;
    const userNum = users[index];

    const token = login(userNum);
    if (!token) {
        orderFail.add(1);
        return;
    }

    const result = createOrder(token);
    countResult(result);
}

// =============================================================================
// 결과 요약
// =============================================================================
export function handleSummary(data) {
    const m = data.metrics;
    const success = m.order_success?.values?.count || 0;
    const stockFail = m.order_stock_fail?.values?.count || 0;
    const failed = m.order_fail?.values?.count || 0;
    const deadlock = m.deadlock_detected?.values?.count || 0;
    const orderP95 = m.order_duration?.values?.['p(95)'] || 0;

    const expectedSuccess = PRODUCT_STOCK;  // 재고 50개 = 50명 성공

    let output = '\n';
    output += '╔═══════════════════════════════════════════════════════════╗\n';
    output += '║          주문 동시성 테스트 결과                            ║\n';
    output += '╠═══════════════════════════════════════════════════════════╣\n';
    output += `║  총 사용자:    ${String(TOTAL_USERS).padStart(4)}명                               ║\n`;
    output += `║  상품 재고:    각 ${PRODUCT_STOCK}개 (상품 1, 2, 3)                     ║\n`;
    output += '╠═══════════════════════════════════════════════════════════╣\n';
    output += `║  SUCCESS:     ${String(success).padStart(4)}  (예상: ${expectedSuccess})                        ║\n`;
    output += `║  STOCK_FAIL:  ${String(stockFail).padStart(4)}  (예상: ${TOTAL_USERS - expectedSuccess})                        ║\n`;
    output += `║  FAILED:      ${String(failed).padStart(4)}  (예상: 0)                         ║\n`;
    output += '╠═══════════════════════════════════════════════════════════╣\n';
    output += `║  DEADLOCK:    ${String(deadlock).padStart(4)}  (예상: 0) ${deadlock === 0 ? '✓' : '✗'}                     ║\n`;
    output += '╠═══════════════════════════════════════════════════════════╣\n';
    output += `║  주문 처리 p95: ${String(orderP95.toFixed(0)).padStart(5)}ms                           ║\n`;
    output += '╚═══════════════════════════════════════════════════════════╝\n';

    // 검증
    if (deadlock > 0) {
        output += '\n⚠️  데드락이 감지되었습니다! 락 순서 점검 필요\n';
    }
    if (success !== expectedSuccess) {
        output += `\n⚠️  성공 건수 불일치: 실제 ${success} vs 예상 ${expectedSuccess}\n`;
    }

    return { stdout: output };
}
