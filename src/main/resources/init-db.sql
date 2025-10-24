-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS seu_airline CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE seu_airline;

-- 创建用户表（满足登录需求）
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
    full_name VARCHAR(100) COMMENT '真实姓名',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    role VARCHAR(20) NOT NULL COMMENT '角色（ADMIN/PASSENGER/STAFF）',
    status TINYINT DEFAULT 1 COMMENT '状态（1-启用 0-禁用）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- 创建航空公司表（扩展需求）
CREATE TABLE IF NOT EXISTS airlines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE COMMENT '航空公司代码',
    name VARCHAR(100) NOT NULL COMMENT '航空公司名称',
    logo_url VARCHAR(255) COMMENT '公司logo',
    description TEXT COMMENT '公司描述',
    contact_number VARCHAR(20) COMMENT '联系电话',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '航空公司表';

-- 创建机场表（扩展需求）
CREATE TABLE IF NOT EXISTS airports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE COMMENT '机场代码',
    name VARCHAR(100) NOT NULL COMMENT '机场名称',
    city VARCHAR(50) NOT NULL COMMENT '所在城市',
    country VARCHAR(50) NOT NULL COMMENT '所在国家',
    timezone VARCHAR(50) COMMENT '时区',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_city (city),
    INDEX idx_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '机场表';

-- 创建航班表（扩展需求）
CREATE TABLE IF NOT EXISTS flights (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    flight_number VARCHAR(20) NOT NULL UNIQUE COMMENT '航班号',
    airline_id BIGINT NOT NULL COMMENT '所属航空公司',
    departure_airport_id BIGINT NOT NULL COMMENT '出发机场',
    arrival_airport_id BIGINT NOT NULL COMMENT '到达机场',
    departure_time DATETIME NOT NULL COMMENT '出发时间',
    arrival_time DATETIME NOT NULL COMMENT '到达时间',
    aircraft_type VARCHAR(50) COMMENT '机型',
    status VARCHAR(20) DEFAULT 'SCHEDULED' COMMENT '状态（SCHEDULED/DELAYED/CANCELLED/DEPARTED/ARRIVED）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (airline_id) REFERENCES airlines (id),
    FOREIGN KEY (departure_airport_id) REFERENCES airports (id),
    FOREIGN KEY (arrival_airport_id) REFERENCES airports (id),
    INDEX idx_flight_number (flight_number),
    INDEX idx_departure_airport (departure_airport_id),
    INDEX idx_arrival_airport (arrival_airport_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '航班表';

-- 创建座位表（扩展需求）
CREATE TABLE IF NOT EXISTS seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    flight_id BIGINT NOT NULL COMMENT '航班ID',
    seat_number VARCHAR(10) NOT NULL COMMENT '座位号',
    seat_type VARCHAR(20) NOT NULL COMMENT '座位类型（ECONOMY/BUSINESS/FIRST）',
    price DECIMAL(10, 2) NOT NULL COMMENT '座位价格',
    status VARCHAR(20) DEFAULT 'AVAILABLE' COMMENT '状态（AVAILABLE/RESERVED/OCCUPIED）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_flight_seat (flight_id, seat_number),
    FOREIGN KEY (flight_id) REFERENCES flights (id),
    INDEX idx_flight_id (flight_id),
    INDEX idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '座位表';

-- 创建订单表（扩展需求）
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态（PENDING/PAID/CANCELLED/REFUNDED）',
    payment_time DATETIME COMMENT '支付时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_order_number (order_number),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单表';

-- 创建订单详情表（扩展需求）
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    seat_id BIGINT NOT NULL COMMENT '座位ID',
    passenger_name VARCHAR(100) NOT NULL COMMENT '乘客姓名',
    passenger_id_card VARCHAR(50) COMMENT '乘客身份证号',
    price DECIMAL(10, 2) NOT NULL COMMENT '票价',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders (id),
    FOREIGN KEY (seat_id) REFERENCES seats (id),
    INDEX idx_order_id (order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单详情表';

-- 插入管理员用户（密码为：admin123，已加密）
INSERT INTO
    users (
        username,
        password,
        full_name,
        email,
        phone,
        role,
        status
    )
VALUES (
        'admin',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTBv.09h90p8tV98a4OVaHfQn5L34Fxy',
        '系统管理员',
        'admin@seuairline.com',
        '13800138000',
        'ADMIN',
        1
    )
ON DUPLICATE KEY UPDATE
    updated_at = CURRENT_TIMESTAMP;

-- 插入示例乘客用户（密码为：passenger123，已加密）
INSERT INTO
    users (
        username,
        password,
        full_name,
        email,
        phone,
        role,
        status
    )
VALUES (
        'passenger1',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTBv.09h90p8tV98a4OVaHfQn5L34Fxy',
        '张三',
        'zhangsan@example.com',
        '13900139000',
        'PASSENGER',
        1
    )
ON DUPLICATE KEY UPDATE
    updated_at = CURRENT_TIMESTAMP;

-- 插入示例航空公司数据
INSERT INTO
    airlines (code, name, description)
VALUES (
        'CA',
        '中国国际航空',
        '中国国际航空股份有限公司'
    ),
    ('MU', '东方航空', '中国东方航空股份有限公司'),
    ('CZ', '南方航空', '中国南方航空股份有限公司')
ON DUPLICATE KEY UPDATE
    updated_at = CURRENT_TIMESTAMP;

-- 插入示例机场数据
INSERT INTO
    airports (
        code,
        name,
        city,
        country,
        timezone
    )
VALUES (
        'PEK',
        '首都国际机场',
        '北京',
        '中国',
        'Asia/Shanghai'
    ),
    (
        'SHA',
        '虹桥国际机场',
        '上海',
        '中国',
        'Asia/Shanghai'
    ),
    (
        'CAN',
        '白云国际机场',
        '广州',
        '中国',
        'Asia/Shanghai'
    ),
    (
        'SZX',
        '宝安国际机场',
        '深圳',
        '中国',
        'Asia/Shanghai'
    )
ON DUPLICATE KEY UPDATE
    updated_at = CURRENT_TIMESTAMP;

-- 显示创建的表结构
SHOW TABLES;

-- 显示用户表数据
SELECT id, username, role, status FROM users;

-- ==================================================================
-- Additional sample data derived from frontend mock files
-- These INSERTs are written to be idempotent where possible so re-running
-- this script won't create duplicate records.
-- ==================================================================

-- Add missing airlines found in frontend mock
INSERT INTO
    airlines (code, name, description)
VALUES ('HU', '海南航空', '海南航空股份有限公司'),
    ('MF', '厦门航空', '厦门航空有限公司'),
    ('SC', '四川航空', '四川航空股份有限公司'),
    ('ZH', '深圳航空', '深圳航空有限公司')
ON DUPLICATE KEY UPDATE
    updated_at = CURRENT_TIMESTAMP;

-- Add airports that appear in mock (preserve existing ones too)
INSERT INTO
    airports (
        code,
        name,
        city,
        country,
        timezone
    )
VALUES (
        'PVG',
        '浦东国际机场',
        '上海',
        '中国',
        'Asia/Shanghai'
    ),
    (
        'CTU',
        '双流国际机场',
        '成都',
        '中国',
        'Asia/Shanghai'
    ),
    (
        'HGH',
        '萧山国际机场',
        '杭州',
        '中国',
        'Asia/Shanghai'
    ),
    (
        'XIY',
        '咸阳国际机场',
        '西安',
        '中国',
        'Asia/Shanghai'
    ),
    (
        'CKG',
        '江北国际机场',
        '重庆',
        '中国',
        'Asia/Shanghai'
    ),
    (
        'XMN',
        '高崎国际机场',
        '厦门',
        '中国',
        'Asia/Shanghai'
    ),
    (
        'NKG',
        '禄口国际机场',
        '南京',
        '中国',
        'Asia/Shanghai'
    )
ON DUPLICATE KEY UPDATE
    updated_at = CURRENT_TIMESTAMP;

-- Add example passenger from mock (user001)
-- Note: password hash reused from sample users above so a known password can be used for testing.
INSERT INTO
    users (
        username,
        password,
        full_name,
        email,
        phone,
        role,
        status
    )
VALUES (
        'user001',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTBv.09h90p8tV98a4OVaHfQn5L34Fxy',
        '张三',
        'user001@example.com',
        '13800138000',
        'PASSENGER',
        1
    )
ON DUPLICATE KEY UPDATE
    updated_at = CURRENT_TIMESTAMP;

-- Insert sample flights for common routes (use subqueries to resolve foreign keys)
INSERT INTO
    flights (
        flight_number,
        airline_id,
        departure_airport_id,
        arrival_airport_id,
        departure_time,
        arrival_time,
        aircraft_type,
        status
    )
VALUES (
        'CA1001',
        (
            SELECT id
            FROM airlines
            WHERE
                code = 'CA'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'PEK'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'PVG'
        ),
        '2025-11-01 08:00:00',
        '2025-11-01 10:10:00',
        'A320',
        'SCHEDULED'
    ),
    (
        'MU2002',
        (
            SELECT id
            FROM airlines
            WHERE
                code = 'MU'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'PVG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'CAN'
        ),
        '2025-11-01 09:30:00',
        '2025-11-01 11:50:00',
        'A321',
        'SCHEDULED'
    ),
    (
        'CA3003',
        (
            SELECT id
            FROM airlines
            WHERE
                code = 'CA'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'PEK'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'SZX'
        ),
        '2025-11-02 07:00:00',
        '2025-11-02 09:30:00',
        'B737',
        'SCHEDULED'
    ),
    (
        'CZ4004',
        (
            SELECT id
            FROM airlines
            WHERE
                code = 'CZ'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'CTU'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'PVG'
        ),
        '2025-11-03 12:00:00',
        '2025-11-03 14:20:00',
        'A330',
        'SCHEDULED'
    ),
    (
        'MF5005',
        (
            SELECT id
            FROM airlines
            WHERE
                code = 'MF'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'XMN'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'NKG'
        ),
        '2025-11-04 15:00:00',
        '2025-11-04 16:40:00',
        'A320',
        'SCHEDULED'
    ),
    (
        'SC6006',
        (
            SELECT id
            FROM airlines
            WHERE
                code = 'SC'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'HGH'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'PEK'
        ),
        '2025-11-05 06:30:00',
        '2025-11-05 08:40:00',
        'B737',
        'SCHEDULED'
    )
ON DUPLICATE KEY UPDATE
    updated_at = CURRENT_TIMESTAMP;

-- Add sample seats for those flights (use unique flight+seat constraint to avoid duplicates)
INSERT INTO
    seats (
        flight_id,
        seat_number,
        seat_type,
        price,
        status
    )
VALUES (
        (
            SELECT id
            FROM flights
            WHERE
                flight_number = 'CA1001'
        ),
        '1A',
        'FIRST',
        2800.00,
        'AVAILABLE'
    ),
    (
        (
            SELECT id
            FROM flights
            WHERE
                flight_number = 'CA1001'
        ),
        '12A',
        'ECONOMY',
        600.00,
        'AVAILABLE'
    ),
    (
        (
            SELECT id
            FROM flights
            WHERE
                flight_number = 'MU2002'
        ),
        '14C',
        'ECONOMY',
        520.00,
        'AVAILABLE'
    ),
    (
        (
            SELECT id
            FROM flights
            WHERE
                flight_number = 'CA3003'
        ),
        '10B',
        'ECONOMY',
        650.00,
        'AVAILABLE'
    ),
    (
        (
            SELECT id
            FROM flights
            WHERE
                flight_number = 'CZ4004'
        ),
        '2A',
        'BUSINESS',
        1800.00,
        'AVAILABLE'
    ),
    (
        (
            SELECT id
            FROM flights
            WHERE
                flight_number = 'MF5005'
        ),
        '20F',
        'ECONOMY',
        420.00,
        'AVAILABLE'
    ),
    (
        (
            SELECT id
            FROM flights
            WHERE
                flight_number = 'SC6006'
        ),
        '15D',
        'ECONOMY',
        480.00,
        'AVAILABLE'
    )
ON DUPLICATE KEY UPDATE
    price = VALUES(price),
    status = VALUES(status),
    updated_at = CURRENT_TIMESTAMP;

-- Add a sample paid order for user001 booking CA1001 seat 12A
INSERT INTO
    orders (
        order_number,
        user_id,
        total_amount,
        status,
        payment_time,
        created_at,
        updated_at
    )
VALUES (
        '20251101000001',
        (
            SELECT id
            FROM users
            WHERE
                username = 'user001'
        ),
        600.00,
        'PAID',
        '2025-10-20 10:00:00',
        '2025-10-20 10:00:00',
        '2025-10-20 10:00:00'
    )
ON DUPLICATE KEY UPDATE
    updated_at = CURRENT_TIMESTAMP,
    status = VALUES(status),
    total_amount = VALUES(total_amount);

-- Insert order item if not exists (avoid duplicate order_items)
INSERT INTO
    order_items (
        order_id,
        seat_id,
        passenger_name,
        passenger_id_card,
        price
    )
SELECT o.id, s.id, '张三', '110101199001011234', 600.00
FROM orders o
    JOIN seats s ON s.seat_number = '12A'
    AND s.flight_id = (
        SELECT id
        FROM flights
        WHERE
            flight_number = 'CA1001'
    )
WHERE
    o.order_number = '20251101000001'
    AND NOT EXISTS (
        SELECT 1
        FROM order_items oi
        WHERE
            oi.order_id = o.id
            AND oi.seat_id = s.id
    );