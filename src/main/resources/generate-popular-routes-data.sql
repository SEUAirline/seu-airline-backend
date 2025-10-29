-- ====================================================================
-- 热门航线测试数据生成脚本
-- 为前端首页的6条热门航线生成足够的航班数据
-- 每条航线生成 5-8 个航班，覆盖未来7天，确保用户点击后有航班显示
-- ====================================================================

USE seu_airline;

-- ====================================================================
-- 热门航线清单（与前端 PopularFlights.vue 保持一致）
-- 1. 南京 → 北京
-- 2. 南京 → 上海
-- 3. 南京 → 广州
-- 4. 南京 → 成都
-- 5. 南京 → 深圳
-- 6. 南京 → 杭州
-- ====================================================================

-- 删除旧的测试航班数据（保留系统初始航班 CA1001, MU2002, CA3003）
DELETE FROM order_items
WHERE
    seat_id IN (
        SELECT id
        FROM seats
        WHERE
            flight_id IN (
                SELECT id
                FROM flights
                WHERE
                    flight_number LIKE 'TEST%'
            )
    );

DELETE FROM seats
WHERE
    flight_id IN (
        SELECT id
        FROM flights
        WHERE
            flight_number LIKE 'TEST%'
    );

DELETE FROM flights WHERE flight_number LIKE 'TEST%';

-- ====================================================================
-- 航线1: 南京 → 北京 (NKG → PEK)
-- 生成 6 个航班，覆盖未来一周
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
    -- 第1天早班
    (
        'TEST_NJ_BJ_001',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'PEK'
        ),
        DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 7 HOUR,
        DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 9 HOUR + INTERVAL 15 MINUTE,
        'A320',
        'SCHEDULED'
    ),

-- 第1天午班
(
    'TEST_NJ_BJ_002',
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
            code = 'NKG'
    ),
    (
        SELECT id
        FROM airports
        WHERE
            code = 'PEK'
    ),
    DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 13 HOUR + INTERVAL 30 MINUTE,
    DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 15 HOUR + INTERVAL 50 MINUTE,
    'B737',
    'SCHEDULED'
),

-- 第2天早班
(
    'TEST_NJ_BJ_003',
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
            code = 'NKG'
    ),
    (
        SELECT id
        FROM airports
        WHERE
            code = 'PEK'
    ),
    DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 8 HOUR + INTERVAL 20 MINUTE,
    DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 10 HOUR + INTERVAL 35 MINUTE,
    'A321',
    'SCHEDULED'
),

-- 第3天午班
(
    'TEST_NJ_BJ_004',
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
            code = 'NKG'
    ),
    (
        SELECT id
        FROM airports
        WHERE
            code = 'PEK'
    ),
    DATE_ADD(CURDATE(), INTERVAL 3 DAY) + INTERVAL 14 HOUR,
    DATE_ADD(CURDATE(), INTERVAL 3 DAY) + INTERVAL 16 HOUR + INTERVAL 20 MINUTE,
    'A330',
    'SCHEDULED'
),

-- 第4天晚班
(
    'TEST_NJ_BJ_005',
    (
        SELECT id
        FROM airlines
        WHERE
            code = 'HU'
    ),
    (
        SELECT id
        FROM airports
        WHERE
            code = 'NKG'
    ),
    (
        SELECT id
        FROM airports
        WHERE
            code = 'PEK'
    ),
    DATE_ADD(CURDATE(), INTERVAL 4 DAY) + INTERVAL 18 HOUR + INTERVAL 30 MINUTE,
    DATE_ADD(CURDATE(), INTERVAL 4 DAY) + INTERVAL 20 HOUR + INTERVAL 50 MINUTE,
    'B737',
    'SCHEDULED'
),

-- 第5天早班
(
    'TEST_NJ_BJ_006',
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
            code = 'NKG'
    ),
    (
        SELECT id
        FROM airports
        WHERE
            code = 'PEK'
    ),
    DATE_ADD(CURDATE(), INTERVAL 5 DAY) + INTERVAL 6 HOUR + INTERVAL 45 MINUTE,
    DATE_ADD(CURDATE(), INTERVAL 5 DAY) + INTERVAL 9 HOUR,
    'A320',
    'SCHEDULED'
)
ON DUPLICATE KEY UPDATE
    updated_at = CURRENT_TIMESTAMP;

-- ====================================================================
-- 航线2: 南京 → 上海 (NKG → SHA/PVG)
-- 生成 8 个航班（高频航线）
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
VALUES (
        'TEST_NJ_SH_001',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'SHA'
        ),
        DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 6 HOUR + INTERVAL 30 MINUTE,
        DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 7 HOUR + INTERVAL 30 MINUTE,
        'A320',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_SH_002',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'PVG'
        ),
        DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 9 HOUR,
        DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 10 HOUR,
        'B737',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_SH_003',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'SHA'
        ),
        DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 12 HOUR + INTERVAL 15 MINUTE,
        DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 13 HOUR + INTERVAL 15 MINUTE,
        'A321',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_SH_004',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'PVG'
        ),
        DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 7 HOUR + INTERVAL 45 MINUTE,
        DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 8 HOUR + INTERVAL 45 MINUTE,
        'A320',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_SH_005',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'SHA'
        ),
        DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 15 HOUR + INTERVAL 20 MINUTE,
        DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 16 HOUR + INTERVAL 20 MINUTE,
        'B737',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_SH_006',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'PVG'
        ),
        DATE_ADD(CURDATE(), INTERVAL 3 DAY) + INTERVAL 8 HOUR + INTERVAL 30 MINUTE,
        DATE_ADD(CURDATE(), INTERVAL 3 DAY) + INTERVAL 9 HOUR + INTERVAL 30 MINUTE,
        'A320',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_SH_007',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'SHA'
        ),
        DATE_ADD(CURDATE(), INTERVAL 4 DAY) + INTERVAL 10 HOUR,
        DATE_ADD(CURDATE(), INTERVAL 4 DAY) + INTERVAL 11 HOUR,
        'A321',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_SH_008',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'PVG'
        ),
        DATE_ADD(CURDATE(), INTERVAL 5 DAY) + INTERVAL 17 HOUR + INTERVAL 30 MINUTE,
        DATE_ADD(CURDATE(), INTERVAL 5 DAY) + INTERVAL 18 HOUR + INTERVAL 30 MINUTE,
        'B737',
        'SCHEDULED'
    )
ON DUPLICATE KEY UPDATE
    updated_at = CURRENT_TIMESTAMP;

-- ====================================================================
-- 航线3: 南京 → 广州 (NKG → CAN)
-- 生成 5 个航班
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
VALUES (
        'TEST_NJ_GZ_001',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'CAN'
        ),
        DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 7 HOUR + INTERVAL 15 MINUTE,
        DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 9 HOUR + INTERVAL 45 MINUTE,
        'A330',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_GZ_002',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'CAN'
        ),
        DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 13 HOUR,
        DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 15 HOUR + INTERVAL 30 MINUTE,
        'B737',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_GZ_003',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'CAN'
        ),
        DATE_ADD(CURDATE(), INTERVAL 3 DAY) + INTERVAL 9 HOUR + INTERVAL 20 MINUTE,
        DATE_ADD(CURDATE(), INTERVAL 3 DAY) + INTERVAL 11 HOUR + INTERVAL 50 MINUTE,
        'A321',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_GZ_004',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'CAN'
        ),
        DATE_ADD(CURDATE(), INTERVAL 4 DAY) + INTERVAL 16 HOUR + INTERVAL 45 MINUTE,
        DATE_ADD(CURDATE(), INTERVAL 4 DAY) + INTERVAL 19 HOUR + INTERVAL 15 MINUTE,
        'A320',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_GZ_005',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'CAN'
        ),
        DATE_ADD(CURDATE(), INTERVAL 5 DAY) + INTERVAL 8 HOUR,
        DATE_ADD(CURDATE(), INTERVAL 5 DAY) + INTERVAL 10 HOUR + INTERVAL 30 MINUTE,
        'A330',
        'SCHEDULED'
    )
ON DUPLICATE KEY UPDATE
    updated_at = CURRENT_TIMESTAMP;

-- ====================================================================
-- 航线4: 南京 → 成都 (NKG → CTU)
-- 生成 5 个航班
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
VALUES (
        'TEST_NJ_CD_001',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'CTU'
        ),
        DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 8 HOUR + INTERVAL 30 MINUTE,
        DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 11 HOUR + INTERVAL 20 MINUTE,
        'A320',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_CD_002',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'CTU'
        ),
        DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 12 HOUR + INTERVAL 15 MINUTE,
        DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 15 HOUR + INTERVAL 5 MINUTE,
        'B737',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_CD_003',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'CTU'
        ),
        DATE_ADD(CURDATE(), INTERVAL 3 DAY) + INTERVAL 7 HOUR,
        DATE_ADD(CURDATE(), INTERVAL 3 DAY) + INTERVAL 9 HOUR + INTERVAL 50 MINUTE,
        'A321',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_CD_004',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'CTU'
        ),
        DATE_ADD(CURDATE(), INTERVAL 4 DAY) + INTERVAL 14 HOUR + INTERVAL 30 MINUTE,
        DATE_ADD(CURDATE(), INTERVAL 4 DAY) + INTERVAL 17 HOUR + INTERVAL 20 MINUTE,
        'A330',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_CD_005',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'CTU'
        ),
        DATE_ADD(CURDATE(), INTERVAL 6 DAY) + INTERVAL 9 HOUR + INTERVAL 45 MINUTE,
        DATE_ADD(CURDATE(), INTERVAL 6 DAY) + INTERVAL 12 HOUR + INTERVAL 35 MINUTE,
        'A320',
        'SCHEDULED'
    )
ON DUPLICATE KEY UPDATE
    updated_at = CURRENT_TIMESTAMP;

-- ====================================================================
-- 航线5: 南京 → 深圳 (NKG → SZX)
-- 生成 6 个航班
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
VALUES (
        'TEST_NJ_SZ_001',
        (
            SELECT id
            FROM airlines
            WHERE
                code = 'ZH'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'SZX'
        ),
        DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 6 HOUR + INTERVAL 50 MINUTE,
        DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 9 HOUR + INTERVAL 25 MINUTE,
        'A320',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_SZ_002',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'SZX'
        ),
        DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 13 HOUR + INTERVAL 20 MINUTE,
        DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 15 HOUR + INTERVAL 55 MINUTE,
        'B737',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_SZ_003',
        (
            SELECT id
            FROM airlines
            WHERE
                code = 'ZH'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'SZX'
        ),
        DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 8 HOUR + INTERVAL 15 MINUTE,
        DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 10 HOUR + INTERVAL 50 MINUTE,
        'A321',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_SZ_004',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'SZX'
        ),
        DATE_ADD(CURDATE(), INTERVAL 3 DAY) + INTERVAL 15 HOUR + INTERVAL 30 MINUTE,
        DATE_ADD(CURDATE(), INTERVAL 3 DAY) + INTERVAL 18 HOUR + INTERVAL 5 MINUTE,
        'A330',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_SZ_005',
        (
            SELECT id
            FROM airlines
            WHERE
                code = 'ZH'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'SZX'
        ),
        DATE_ADD(CURDATE(), INTERVAL 4 DAY) + INTERVAL 10 HOUR,
        DATE_ADD(CURDATE(), INTERVAL 4 DAY) + INTERVAL 12 HOUR + INTERVAL 35 MINUTE,
        'A320',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_SZ_006',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'SZX'
        ),
        DATE_ADD(CURDATE(), INTERVAL 5 DAY) + INTERVAL 16 HOUR + INTERVAL 45 MINUTE,
        DATE_ADD(CURDATE(), INTERVAL 5 DAY) + INTERVAL 19 HOUR + INTERVAL 20 MINUTE,
        'B737',
        'SCHEDULED'
    )
ON DUPLICATE KEY UPDATE
    updated_at = CURRENT_TIMESTAMP;

-- ====================================================================
-- 航线6: 南京 → 杭州 (NKG → HGH)
-- 生成 7 个航班（短途高频航线）
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
VALUES (
        'TEST_NJ_HZ_001',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'HGH'
        ),
        DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 7 HOUR,
        DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 8 HOUR,
        'A320',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_HZ_002',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'HGH'
        ),
        DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 11 HOUR + INTERVAL 30 MINUTE,
        DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 12 HOUR + INTERVAL 30 MINUTE,
        'B737',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_HZ_003',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'HGH'
        ),
        DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 9 HOUR + INTERVAL 15 MINUTE,
        DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 10 HOUR + INTERVAL 15 MINUTE,
        'A321',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_HZ_004',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'HGH'
        ),
        DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 14 HOUR,
        DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 15 HOUR,
        'A320',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_HZ_005',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'HGH'
        ),
        DATE_ADD(CURDATE(), INTERVAL 3 DAY) + INTERVAL 8 HOUR + INTERVAL 30 MINUTE,
        DATE_ADD(CURDATE(), INTERVAL 3 DAY) + INTERVAL 9 HOUR + INTERVAL 30 MINUTE,
        'B737',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_HZ_006',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'HGH'
        ),
        DATE_ADD(CURDATE(), INTERVAL 4 DAY) + INTERVAL 16 HOUR + INTERVAL 20 MINUTE,
        DATE_ADD(CURDATE(), INTERVAL 4 DAY) + INTERVAL 17 HOUR + INTERVAL 20 MINUTE,
        'A320',
        'SCHEDULED'
    ),
    (
        'TEST_NJ_HZ_007',
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
                code = 'NKG'
        ),
        (
            SELECT id
            FROM airports
            WHERE
                code = 'HGH'
        ),
        DATE_ADD(CURDATE(), INTERVAL 5 DAY) + INTERVAL 10 HOUR + INTERVAL 45 MINUTE,
        DATE_ADD(CURDATE(), INTERVAL 5 DAY) + INTERVAL 11 HOUR + INTERVAL 45 MINUTE,
        'A321',
        'SCHEDULED'
    )
ON DUPLICATE KEY UPDATE
    updated_at = CURRENT_TIMESTAMP;

-- ====================================================================
-- 为所有新增航班生成座位数据
-- 使用通用模板：经济舱20座、商务舱8座、头等舱4座
-- ====================================================================

-- 为所有 TEST_ 开头的航班批量生成座位
INSERT INTO
    seats (
        flight_id,
        seat_number,
        seat_type,
        price,
        status
    )
SELECT
    f.id,
    CONCAT(seat_row, seat_col) as seat_number,
    CASE
        WHEN seat_row <= 2 THEN 'FIRST'
        WHEN seat_row <= 5 THEN 'BUSINESS'
        ELSE 'ECONOMY'
    END as seat_type,
    CASE
        WHEN seat_row <= 2 THEN 2500.00
        WHEN seat_row <= 5 THEN 1200.00
        ELSE 450.00
    END as price,
    'AVAILABLE' as status
FROM flights f
    CROSS JOIN (
        SELECT 1 as seat_row, 'A' as seat_col
        UNION ALL
        SELECT 1, 'B'
        UNION ALL
        SELECT 1, 'C'
        UNION ALL
        SELECT 1, 'D'
        UNION ALL
        SELECT 2, 'A'
        UNION ALL
        SELECT 2, 'B'
        UNION ALL
        SELECT 2, 'C'
        UNION ALL
        SELECT 2, 'D'
        UNION ALL
        SELECT 3, 'A'
        UNION ALL
        SELECT 3, 'B'
        UNION ALL
        SELECT 3, 'C'
        UNION ALL
        SELECT 3, 'D'
        UNION ALL
        SELECT 4, 'A'
        UNION ALL
        SELECT 4, 'B'
        UNION ALL
        SELECT 4, 'C'
        UNION ALL
        SELECT 4, 'D'
        UNION ALL
        SELECT 5, 'A'
        UNION ALL
        SELECT 5, 'B'
        UNION ALL
        SELECT 5, 'C'
        UNION ALL
        SELECT 5, 'D'
        UNION ALL
        SELECT 10, 'A'
        UNION ALL
        SELECT 10, 'B'
        UNION ALL
        SELECT 10, 'C'
        UNION ALL
        SELECT 10, 'D'
        UNION ALL
        SELECT 10, 'E'
        UNION ALL
        SELECT 10, 'F'
        UNION ALL
        SELECT 11, 'A'
        UNION ALL
        SELECT 11, 'B'
        UNION ALL
        SELECT 11, 'C'
        UNION ALL
        SELECT 11, 'D'
        UNION ALL
        SELECT 11, 'E'
        UNION ALL
        SELECT 11, 'F'
        UNION ALL
        SELECT 12, 'A'
        UNION ALL
        SELECT 12, 'B'
        UNION ALL
        SELECT 12, 'C'
        UNION ALL
        SELECT 12, 'D'
        UNION ALL
        SELECT 12, 'E'
        UNION ALL
        SELECT 12, 'F'
        UNION ALL
        SELECT 13, 'A'
        UNION ALL
        SELECT 13, 'B'
    ) seat_positions
WHERE
    f.flight_number LIKE 'TEST_%'
    AND NOT EXISTS (
        SELECT 1
        FROM seats s
        WHERE
            s.flight_id = f.id
            AND s.seat_number = CONCAT(seat_row, seat_col)
    )
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    price = VALUES(price);

-- ====================================================================
-- 数据验证与统计
-- ====================================================================

-- 统计各热门航线的航班数量
SELECT
    CONCAT(
        (
            SELECT city
            FROM airports
            WHERE
                id = f.departure_airport_id
        ),
        ' → ',
        (
            SELECT city
            FROM airports
            WHERE
                id = f.arrival_airport_id
        )
    ) as 航线,
    COUNT(*) as 航班数量,
    MIN(DATE(f.departure_time)) as 最早日期,
    MAX(DATE(f.departure_time)) as 最晚日期,
    SUM(
        (
            SELECT COUNT(*)
            FROM seats
            WHERE
                flight_id = f.id
        )
    ) as 总座位数
FROM flights f
WHERE
    f.flight_number LIKE 'TEST_%'
GROUP BY
    f.departure_airport_id,
    f.arrival_airport_id
ORDER BY 航班数量 DESC;

-- 显示所有测试航班详情
SELECT
    f.flight_number as 航班号,
    a.name as 航空公司,
    CONCAT(
        (
            SELECT city
            FROM airports
            WHERE
                id = f.departure_airport_id
        ),
        ' → ',
        (
            SELECT city
            FROM airports
            WHERE
                id = f.arrival_airport_id
        )
    ) as 航线,
    DATE_FORMAT(
        f.departure_time,
        '%Y-%m-%d %H:%i'
    ) as 出发时间,
    DATE_FORMAT(
        f.arrival_time,
        '%Y-%m-%d %H:%i'
    ) as 到达时间,
    f.aircraft_type as 机型,
    (
        SELECT COUNT(*)
        FROM seats
        WHERE
            flight_id = f.id
    ) as 座位数
FROM flights f
    JOIN airlines a ON f.airline_id = a.id
WHERE
    f.flight_number LIKE 'TEST_%'
ORDER BY f.departure_time;

-- ====================================================================
-- 完成提示
-- ====================================================================
SELECT '✅ 热门航线测试数据生成完成！' as 状态, (
        SELECT COUNT(*)
        FROM flights
        WHERE
            flight_number LIKE 'TEST_%'
    ) as 新增航班数, (
        SELECT COUNT(*)
        FROM seats
        WHERE
            flight_id IN (
                SELECT id
                FROM flights
                WHERE
                    flight_number LIKE 'TEST_%'
            )
    ) as 新增座位数;