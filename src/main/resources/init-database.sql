-- ====================================================================
-- SEUAirline 数据库初始化脚本（完整版）
-- 包含：数据库创建、表结构、初始数据、测试数据、座位数据、乘客信息
-- 此脚本可重复执行，使用 ON DUPLICATE KEY UPDATE 避免重复插入
-- 更新日期：2025-11-12
-- 新增：乘客信息表（passengers），用于保存用户的常用乘客信息
-- ====================================================================

-- 设置SQL模式，确保兼容性
SET sql_mode = '';
SET NAMES utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS seu_airline CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE seu_airline;

-- ====================================================================
-- 第一部分：表结构创建
-- ====================================================================

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
    full_name VARCHAR(100) COMMENT '真实姓名',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    id_card VARCHAR(50) COMMENT '身份证号',
    role VARCHAR(20) NOT NULL COMMENT '角色（ADMIN/PASSENGER/STAFF）',
    status TINYINT DEFAULT 1 COMMENT '状态（1-启用 0-禁用）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- 创建航空公司表
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

-- 创建机场表
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

-- 创建航班表
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
    INDEX idx_arrival_airport (arrival_airport_id),
    INDEX idx_departure_time (departure_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '航班表';

-- 创建座位表
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
    INDEX idx_status (status),
    INDEX idx_seat_type (seat_type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '座位表';

-- 创建订单表
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

-- 创建乘客信息表
CREATE TABLE IF NOT EXISTS passengers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '乘客ID',
    user_id BIGINT NOT NULL COMMENT '所属用户ID',
    passenger_name VARCHAR(100) NOT NULL COMMENT '乘客姓名',
    id_type VARCHAR(20) NOT NULL DEFAULT 'ID_CARD' COMMENT '证件类型（ID_CARD-身份证/PASSPORT-护照/OTHER-其他）',
    id_card VARCHAR(50) NOT NULL COMMENT '证件号码',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱地址',
    passenger_type VARCHAR(20) NOT NULL DEFAULT 'ADULT' COMMENT '乘客类型（ADULT-成人/CHILD-儿童/INFANT-婴儿）',
    is_default BOOLEAN DEFAULT FALSE COMMENT '是否默认乘客（每个用户只能有一个默认乘客）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_id_card (id_card),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='乘客信息表';

-- 创建订单详情表
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    seat_id BIGINT NOT NULL COMMENT '座位ID',
    passenger_name VARCHAR(100) NOT NULL COMMENT '乘客姓名',
    passenger_id_card VARCHAR(50) COMMENT '乘客身份证号',
    passenger_type VARCHAR(20) COMMENT '乘客类型（ADULT-成人/CHILD-儿童/INFANT-婴儿）',
    passenger_id BIGINT COMMENT '关联的乘客ID（可选）',
    price DECIMAL(10, 2) NOT NULL COMMENT '票价',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders (id),
    FOREIGN KEY (seat_id) REFERENCES seats (id),
    FOREIGN KEY (passenger_id) REFERENCES passengers(id) ON DELETE SET NULL,
    INDEX idx_order_id (order_id),
    INDEX idx_seat_id (seat_id),
    INDEX idx_passenger_id (passenger_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单详情表';

-- 创建用户消息表
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    title VARCHAR(200) NOT NULL COMMENT '消息标题',
    content TEXT NOT NULL COMMENT '消息内容',
    message_type VARCHAR(20) NOT NULL COMMENT '消息类型：ORDER-订单/FLIGHT-航班/SYSTEM-系统/PROMOTION-促销',
    related_id BIGINT COMMENT '关联ID（订单ID/航班ID等，可选）',
    priority TINYINT DEFAULT 1 COMMENT '优先级：1-普通/2-重要/3-紧急',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    read_time DATETIME COMMENT '阅读时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_message_type (message_type),
    INDEX idx_created_at (created_at),
    INDEX idx_user_read (user_id, is_read) COMMENT '优化查询未读消息'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户消息表';

-- 创建系统公告表
CREATE TABLE IF NOT EXISTS announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '公告ID',
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容（支持HTML）',
    announcement_type VARCHAR(20) NOT NULL COMMENT '公告类型：MAINTENANCE-系统维护/PROMOTION-促销活动/NOTICE-通知公告',
    priority TINYINT DEFAULT 1 COMMENT '优先级：1-普通/2-重要/3-紧急',
    start_time DATETIME NOT NULL COMMENT '生效开始时间',
    end_time DATETIME NOT NULL COMMENT '生效结束时间',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用/0-禁用',
    creator_id BIGINT COMMENT '创建人ID（管理员）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_time_range (start_time, end_time) COMMENT '优化查询有效公告',
    INDEX idx_announcement_type (announcement_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

-- 创建公告阅读记录表
CREATE TABLE IF NOT EXISTS announcement_reads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    announcement_id BIGINT NOT NULL COMMENT '公告ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    read_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    UNIQUE KEY uk_announcement_user (announcement_id, user_id) COMMENT '防止重复记录',
    FOREIGN KEY (announcement_id) REFERENCES announcements(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_announcement_id (announcement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告阅读记录表';

-- ====================================================================
-- 第二部分：基础数据初始化
-- ====================================================================

-- 插入用户数据（密码统一为：admin123 或 passenger123，已加密）
-- 密码哈希：$2a$10$S3dlkLncuBuBNDbq.gDiVupoIaCfkjPBSfnw2gDQKFWEnodqtEIry
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
        '$2a$10$blLCUSMOPLzMICzsaMVEIuU5dCurVoOokym7l.LYyVIbkW31yD.R2',
        '系统管理员',
        'admin@seuairline.com',
        '13800138000',
        'ADMIN',
        1
    ),
    (
        'passenger1',
        '$2a$10$S3dlkLncuBuBNDbq.gDiVupoIaCfkjPBSfnw2gDQKFWEnodqtEIry',
        '张三',
        'zhangsan@example.com',
        '13900139001',
        'PASSENGER',
        1
    ),
    (
        'passenger2',
        '$2a$10$S3dlkLncuBuBNDbq.gDiVupoIaCfkjPBSfnw2gDQKFWEnodqtEIry',
        '李四',
        'lisi@example.com',
        '13900139002',
        'PASSENGER',
        1
    ),
    (
        'staff1',
        '$2a$10$S3dlkLncuBuBNDbq.gDiVupoIaCfkjPBSfnw2gDQKFWEnodqtEIry',
        '王五',
        'wangwu@seuairline.com',
        '13900139003',
        'STAFF',
        1
    )
ON DUPLICATE KEY UPDATE
    updated_at = CURRENT_TIMESTAMP;

-- 插入乘客信息数据（为测试用户添加常用乘客）
INSERT INTO
    passengers (
        user_id,
        passenger_name,
        id_type,
        id_card,
        phone,
        email,
        passenger_type,
        is_default
    )
VALUES
    -- passenger1 (user_id=2) 的常用乘客
    (2, '张三', 'ID_CARD', '320106199001011234', '13800138000', 'zhangsan@example.com', 'ADULT', TRUE),
    (2, '李四', 'ID_CARD', '320106199502021234', '13900139000', 'lisi@example.com', 'ADULT', FALSE),
    (2, '王小明', 'ID_CARD', '320106201501011234', '13700137000', NULL, 'CHILD', FALSE),
    -- passenger2 (user_id=3) 的常用乘客
    (3, '赵六', 'PASSPORT', 'E12345678', '13600136000', 'zhaoliu@example.com', 'ADULT', TRUE),
    (3, '孙七', 'ID_CARD', '320106199203031234', '13500135000', 'sunqi@example.com', 'ADULT', FALSE)
ON DUPLICATE KEY UPDATE
    passenger_name = VALUES(passenger_name),
    phone = VALUES(phone),
    email = VALUES(email),
    updated_at = CURRENT_TIMESTAMP;

-- 插入航空公司数据
INSERT INTO
    airlines (code, name, description)
VALUES (
        'CA',
        '中国国际航空',
        '中国国际航空股份有限公司'
    ),
    ('MU', '东方航空', '中国东方航空股份有限公司'),
    ('CZ', '南方航空', '中国南方航空股份有限公司'),
    ('HU', '海南航空', '海南航空股份有限公司'),
    ('MF', '厦门航空', '厦门航空有限公司'),
    ('SC', '四川航空', '四川航空股份有限公司'),
    ('ZH', '深圳航空', '深圳航空有限公司')
ON DUPLICATE KEY UPDATE
    updated_at = CURRENT_TIMESTAMP;

-- 插入机场数据
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
        'PVG',
        '浦东国际机场',
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

-- ====================================================================
-- 第三部分：航班数据初始化
-- ====================================================================

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
VALUES
    -- 北京 → 上海
    (
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

-- 上海 → 广州
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

-- 北京 → 深圳
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

-- 广州 → 成都
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
            code = 'CAN'
    ),
    (
        SELECT id
        FROM airports
        WHERE
            code = 'CTU'
    ),
    '2025-11-03 12:00:00',
    '2025-11-03 14:20:00',
    'A330',
    'SCHEDULED'
),

-- 上海 → 杭州
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
            code = 'PVG'
    ),
    (
        SELECT id
        FROM airports
        WHERE
            code = 'HGH'
    ),
    '2025-11-04 15:00:00',
    '2025-11-04 16:40:00',
    'A320',
    'SCHEDULED'
),

-- 成都 → 北京
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
            code = 'CTU'
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
    updated_at = CURRENT_TIMESTAMP,
    status = VALUES(status);

-- ====================================================================
-- 第四部分：消息系统测试数据初始化
-- ====================================================================

-- 插入测试消息（给 passenger1 用户，user_id=2）
INSERT INTO messages (user_id, title, content, message_type, related_id, priority, is_read)
VALUES 
    (2, '订单支付成功', '您的订单 ORD20251112001 已支付成功，请准时登机。', 'ORDER', 1, 2, FALSE),
    (2, '航班延误通知', 'CA1001 航班因天气原因延误 30 分钟，预计 10:40 起飞。', 'FLIGHT', 1, 3, FALSE),
    (2, '系统升级通知', '系统将于今晚 23:00-01:00 进行维护升级，期间部分功能不可用。', 'SYSTEM', NULL, 1, TRUE),
    (2, '双十二特惠活动', '双十二机票限时五折优惠，热门航线抢购中！', 'PROMOTION', NULL, 1, FALSE)
ON DUPLICATE KEY UPDATE
    updated_at = CURRENT_TIMESTAMP;

-- 插入测试公告（面向全体用户）
INSERT INTO announcements (title, content, announcement_type, priority, start_time, end_time, status)
VALUES 
    ('系统维护通知', 
     '<p>尊敬的用户：</p><p>系统将于 <strong>2025年11月15日 23:00 - 11月16日 01:00</strong> 进行维护升级。</p><p>维护期间，订票、支付等功能暂时不可用，敬请谅解。</p>', 
     'MAINTENANCE', 
     2, 
     '2025-11-12 00:00:00', 
     '2025-11-16 23:59:59', 
     1),
    ('双十二大促', 
     '<p>🎉 双十二机票大促销！</p><ul><li>国内航线 5 折起</li><li>国际航线 7 折起</li><li>活动时间：11月12日-11月20日</li></ul>', 
     'PROMOTION', 
     1, 
     '2025-11-12 00:00:00', 
     '2025-11-20 23:59:59', 
     1),
    ('防疫政策更新', 
     '<p>根据最新防疫政策，自即日起取消行程码查验，保留健康码绿码通行。</p>', 
     'NOTICE', 
     3, 
     '2025-11-10 00:00:00', 
     '2025-12-31 23:59:59', 
     1)
ON DUPLICATE KEY UPDATE
    updated_at = CURRENT_TIMESTAMP;

-- 插入公告阅读记录（user_id=2 已读第一条公告）
INSERT INTO announcement_reads (announcement_id, user_id)
VALUES (1, 2)
ON DUPLICATE KEY UPDATE
    read_time = CURRENT_TIMESTAMP;

-- ====================================================================
-- 第五部分：座位数据初始化（完整版）
-- CA1001: 北京→上海 (A320: 经济舱30座, 商务舱20座, 头等舱8座)
-- MU2002: 上海→广州 (A321: 经济舱32座, 商务舱7座, 头等舱5座)
-- CA3003: 北京→深圳 (B737: 经济舱20座, 商务舱6座, 头等舱2座)
-- ====================================================================

-- CA1001 座位数据
INSERT INTO
    seats (
        flight_id,
        seat_number,
        seat_type,
        price,
        status
    )
SELECT
    id,
    seat_number,
    seat_type,
    price,
    'AVAILABLE'
FROM (
        -- 头等舱 1A-2D (8座)
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ) as id, '1A' as seat_number, 'FIRST' as seat_type, 2800.00 as price
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '1B', 'FIRST', 2800.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '1C', 'FIRST', 2800.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '1D', 'FIRST', 2800.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '2A', 'FIRST', 2800.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '2B', 'FIRST', 2800.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '2C', 'FIRST', 2800.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '2D', 'FIRST', 2800.00
            -- 商务舱 3A-7D (20座)
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '3A', 'BUSINESS', 1500.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '3B', 'BUSINESS', 1500.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '3C', 'BUSINESS', 1500.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '3D', 'BUSINESS', 1500.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '4A', 'BUSINESS', 1500.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '4B', 'BUSINESS', 1500.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '4C', 'BUSINESS', 1500.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '4D', 'BUSINESS', 1500.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '5A', 'BUSINESS', 1500.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '5B', 'BUSINESS', 1500.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '5C', 'BUSINESS', 1500.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '5D', 'BUSINESS', 1500.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '6A', 'BUSINESS', 1500.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '6B', 'BUSINESS', 1500.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '6C', 'BUSINESS', 1500.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '6D', 'BUSINESS', 1500.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '7A', 'BUSINESS', 1500.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '7B', 'BUSINESS', 1500.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '7C', 'BUSINESS', 1500.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '7D', 'BUSINESS', 1500.00
            -- 经济舱 10A-14F (30座)
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '10A', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '10B', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '10C', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '10D', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '10E', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '10F', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '11A', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '11B', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '11C', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '11D', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '11E', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '11F', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '12A', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '12B', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '12C', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '12D', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '12E', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '12F', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '13A', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '13B', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '13C', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '13D', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '13E', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '13F', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '14A', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '14B', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '14C', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '14D', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '14E', 'ECONOMY', 600.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA1001'
            ), '14F', 'ECONOMY', 600.00
    ) AS temp
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    price = VALUES(price);

-- MU2002 座位数据
INSERT INTO
    seats (
        flight_id,
        seat_number,
        seat_type,
        price,
        status
    )
SELECT
    id,
    seat_number,
    seat_type,
    price,
    'AVAILABLE'
FROM (
        -- 头等舱 1A-2B (5座)
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ) as id, '1A' as seat_number, 'FIRST' as seat_type, 2500.00 as price
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '1B', 'FIRST', 2500.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '1C', 'FIRST', 2500.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '2A', 'FIRST', 2500.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '2B', 'FIRST', 2500.00
            -- 商务舱 3A-4D (7座)
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '3A', 'BUSINESS', 1200.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '3B', 'BUSINESS', 1200.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '3C', 'BUSINESS', 1200.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '3D', 'BUSINESS', 1200.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '4A', 'BUSINESS', 1200.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '4B', 'BUSINESS', 1200.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '4C', 'BUSINESS', 1200.00
            -- 经济舱 10A-15D (32座)
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '10A', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '10B', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '10C', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '10D', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '10E', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '10F', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '11A', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '11B', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '11C', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '11D', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '11E', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '11F', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '12A', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '12B', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '12C', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '12D', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '12E', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '12F', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '13A', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '13B', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '13C', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '13D', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '13E', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '13F', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '14A', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '14B', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '14C', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '14D', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '15A', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '15B', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '15C', 'ECONOMY', 520.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'MU2002'
            ), '15D', 'ECONOMY', 520.00
    ) AS temp
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    price = VALUES(price);

-- CA3003 座位数据
INSERT INTO
    seats (
        flight_id,
        seat_number,
        seat_type,
        price,
        status
    )
SELECT
    id,
    seat_number,
    seat_type,
    price,
    'AVAILABLE'
FROM (
        -- 头等舱 1A-1B (2座)
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ) as id, '1A' as seat_number, 'FIRST' as seat_type, 3200.00 as price
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '1B', 'FIRST', 3200.00
            -- 商务舱 2A-3C (6座)
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '2A', 'BUSINESS', 1800.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '2B', 'BUSINESS', 1800.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '2C', 'BUSINESS', 1800.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '3A', 'BUSINESS', 1800.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '3B', 'BUSINESS', 1800.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '3C', 'BUSINESS', 1800.00
            -- 经济舱 10A-13E (20座)
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '10A', 'ECONOMY', 650.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '10B', 'ECONOMY', 650.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '10C', 'ECONOMY', 650.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '10D', 'ECONOMY', 650.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '10E', 'ECONOMY', 650.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '11A', 'ECONOMY', 650.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '11B', 'ECONOMY', 650.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '11C', 'ECONOMY', 650.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '11D', 'ECONOMY', 650.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '11E', 'ECONOMY', 650.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '12A', 'ECONOMY', 650.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '12B', 'ECONOMY', 650.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '12C', 'ECONOMY', 650.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '12D', 'ECONOMY', 650.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '12E', 'ECONOMY', 650.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '13A', 'ECONOMY', 650.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '13B', 'ECONOMY', 650.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '13C', 'ECONOMY', 650.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '13D', 'ECONOMY', 650.00
        UNION ALL
        SELECT (
                SELECT id
                FROM flights
                WHERE
                    flight_number = 'CA3003'
            ), '13E', 'ECONOMY', 650.00
    ) AS temp
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    price = VALUES(price);

-- ====================================================================
-- 第五部分：验证与统计
-- ====================================================================

-- 显示所有表
SHOW TABLES;

-- 统计各表数据量
SELECT '用户表' as 表名, COUNT(*) as 记录数
FROM users
UNION ALL
SELECT '航空公司表', COUNT(*)
FROM airlines
UNION ALL
SELECT '机场表', COUNT(*)
FROM airports
UNION ALL
SELECT '航班表', COUNT(*)
FROM flights
UNION ALL
SELECT '座位表', COUNT(*)
FROM seats
UNION ALL
SELECT '订单表', COUNT(*)
FROM orders
UNION ALL
SELECT '订单详情表', COUNT(*)
FROM order_items
UNION ALL
SELECT '消息表', COUNT(*)
FROM messages
UNION ALL
SELECT '公告表', COUNT(*)
FROM announcements
UNION ALL
SELECT '公告阅读记录表', COUNT(*)
FROM announcement_reads;

-- 统计各航班座位情况
SELECT
    f.flight_number as 航班号,
    f.aircraft_type as 机型,
    s.seat_type as 舱位,
    COUNT(*) as 座位总数,
    SUM(
        CASE
            WHEN s.status = 'AVAILABLE' THEN 1
            ELSE 0
        END
    ) as 可用座位,
    SUM(
        CASE
            WHEN s.status = 'OCCUPIED' THEN 1
            ELSE 0
        END
    ) as 已售座位,
    MIN(s.price) as 最低价,
    MAX(s.price) as 最高价
FROM flights f
    LEFT JOIN seats s ON f.id = s.flight_id
WHERE
    f.flight_number IN ('CA1001', 'MU2002', 'CA3003')
GROUP BY
    f.flight_number,
    f.aircraft_type,
    s.seat_type
ORDER BY f.flight_number, FIELD(
        s.seat_type, 'FIRST', 'BUSINESS', 'ECONOMY'
    );

-- 显示用户信息
SELECT
    id,
    username as 用户名,
    full_name as 姓名,
    role as 角色,
    status as 状态,
    created_at as 创建时间
FROM users
ORDER BY role, id;

-- ====================================================================
-- 初始化完成提示
-- ====================================================================
SELECT
    '✅ SEUAirline 数据库初始化完成！' as 状态,
    (
        SELECT COUNT(*)
        FROM users
    ) as 用户数,
    (
        SELECT COUNT(*)
        FROM airlines
    ) as 航空公司数,
    (
        SELECT COUNT(*)
        FROM airports
    ) as 机场数,
    (
        SELECT COUNT(*)
        FROM flights
    ) as 航班数,
    (
        SELECT COUNT(*)
        FROM seats
    ) as 座位数,
    (
        SELECT COUNT(*)
        FROM messages
    ) as 消息数,
    (
        SELECT COUNT(*)
        FROM announcements
    ) as 公告数;