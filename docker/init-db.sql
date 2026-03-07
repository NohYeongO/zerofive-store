-- =====================================================
-- Zerofive Store 초기 스키마 및 테스트 데이터
-- =====================================================

-- 계정 테이블
CREATE TABLE IF NOT EXISTS account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6)
);

-- 이벤트 테이블
CREATE TABLE IF NOT EXISTS event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    description TEXT,
    start_at DATETIME(6),
    end_at DATETIME(6),
    active BOOLEAN DEFAULT TRUE,
    threshold INT DEFAULT 200,
    created_at DATETIME(6),
    updated_at DATETIME(6)
);

-- 쿠폰 테이블
CREATE TABLE IF NOT EXISTS coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    discount_amount INT,
    coupon_type VARCHAR(50),
    total_quantity INT,
    issued_quantity INT DEFAULT 0,
    valid_from DATETIME(6),
    valid_until DATETIME(6),
    active BOOLEAN DEFAULT TRUE,
    event_id BIGINT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    FOREIGN KEY (event_id) REFERENCES event(id)
);

-- 발급된 쿠폰 테이블 (유니크 제약조건 포함)
CREATE TABLE IF NOT EXISTS issued_coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    coupon_id BIGINT,
    account_id BIGINT,
    used BOOLEAN DEFAULT FALSE,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    FOREIGN KEY (coupon_id) REFERENCES coupon(id),
    CONSTRAINT uk_issued_coupon_coupon_account UNIQUE (coupon_id, account_id)
);

-- =====================================================
-- 테스트 데이터 삽입
-- =====================================================

-- 부하테스트용 계정 1000명 생성 (user1@test.com ~ user1000@test.com)
-- 비밀번호: test1234 (BCrypt 해시)
DELIMITER //
CREATE PROCEDURE create_test_accounts()
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= 1000 DO
        INSERT INTO account (email, password, name, role, created_at, updated_at)
        VALUES (
            CONCAT('user', i, '@test.com'),
            '$2a$10$6AcxJHoXmuWvmsJAdt6wTeCNvgx4u0zxwCew6HPwu2cpbukqycs6O',
            CONCAT('TestUser', i),
            'USER',
            NOW(),
            NOW()
        );
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

CALL create_test_accounts();
DROP PROCEDURE create_test_accounts;

-- 관리자 계정
INSERT INTO account (email, password, name, role, created_at, updated_at) VALUES
('admin@zerofive.com', '$2a$10$6AcxJHoXmuWvmsJAdt6wTeCNvgx4u0zxwCew6HPwu2cpbukqycs6O', 'Admin', 'ADMIN', NOW(), NOW());

-- 테스트 이벤트 생성
INSERT INTO event (id, name, description, start_at, end_at, active, threshold, created_at, updated_at) VALUES
(1, '선착순 쿠폰 이벤트', '선착순 200명에게 쿠폰 지급!', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 7 DAY), TRUE, 200, NOW(), NOW());

-- 테스트 쿠폰 생성 (200개 한정)
INSERT INTO coupon (id, name, discount_amount, coupon_type, total_quantity, issued_quantity, valid_from, valid_until, active, event_id, created_at, updated_at) VALUES
(1, '선착순 5000원 할인 쿠폰', 5000, 'EVENT', 200, 0, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), TRUE, 1, NOW(), NOW());

-- =====================================================
-- 상품 테이블 및 테스트 데이터 (주문 부하테스트용)
-- =====================================================

-- 상품 테이블
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price INT NOT NULL,
    stock INT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6)
);

-- 테스트 상품 3개 생성 (재고 각 50개)
INSERT INTO products (id, name, price, stock, created_at, updated_at) VALUES
(1, '테스트 상품 1', 10000, 50, NOW(), NOW()),
(2, '테스트 상품 2', 20000, 50, NOW(), NOW()),
(3, '테스트 상품 3', 30000, 50, NOW(), NOW());

-- =====================================================
-- 배송지 테이블
-- =====================================================
CREATE TABLE IF NOT EXISTS shipping_addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    address VARCHAR(500) NOT NULL,
    detail_address VARCHAR(500),
    postal_code VARCHAR(20) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6),
    updated_at DATETIME(6)
);

-- 부하테스트용 배송지 1000명분 생성
DELIMITER //
CREATE PROCEDURE create_test_addresses()
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= 1000 DO
        INSERT INTO shipping_addresses (account_id, name, phone, address, detail_address, postal_code, is_default, created_at, updated_at)
        VALUES (
            i,
            CONCAT('TestUser', i),
            '010-1234-5678',
            '서울시 강남구 테헤란로 123',
            CONCAT(i, '호'),
            '06234',
            TRUE,
            NOW(),
            NOW()
        );
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

CALL create_test_addresses();
DROP PROCEDURE create_test_addresses;

-- =====================================================
-- 장바구니 테이블
-- =====================================================
CREATE TABLE IF NOT EXISTS carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL UNIQUE,
    created_at DATETIME(6),
    updated_at DATETIME(6)
);

CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    FOREIGN KEY (cart_id) REFERENCES carts(id)
);

-- =====================================================
-- 주문 테이블
-- =====================================================
CREATE TABLE IF NOT EXISTS order_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    account_id BIGINT NOT NULL,
    last_polled_at DATETIME(6) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6)
);

CREATE TABLE IF NOT EXISTS order_session_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_session_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    price INT NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    FOREIGN KEY (order_session_id) REFERENCES order_sessions(id)
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    total_price INT NOT NULL,
    discount_amount INT DEFAULT 0,
    payment_amount INT NOT NULL,
    coupon_id BIGINT,
    address_id BIGINT NOT NULL,
    transaction_id VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6)
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    price INT NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    FOREIGN KEY (order_id) REFERENCES orders(id)
);
