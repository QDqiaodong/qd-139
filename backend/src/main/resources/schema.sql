SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS equipment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_no VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    cold_resistance_spec VARCHAR(100) NOT NULL,
    age_group VARCHAR(20) NOT NULL,
    status INT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_no VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    child_ratio INT DEFAULT 0,
    adult_ratio INT DEFAULT 100,
    elderly_ratio INT DEFAULT 0,
    status INT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS session_equipment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_session_equipment (session_id, equipment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS change_log_child (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    old_age_group VARCHAR(20),
    new_age_group VARCHAR(20) NOT NULL,
    change_reason VARCHAR(500),
    operator VARCHAR(50),
    created_at DATETIME NOT NULL,
    age_group VARCHAR(20) DEFAULT 'CHILD'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS change_log_adult (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    old_age_group VARCHAR(20),
    new_age_group VARCHAR(20) NOT NULL,
    change_reason VARCHAR(500),
    operator VARCHAR(50),
    created_at DATETIME NOT NULL,
    age_group VARCHAR(20) DEFAULT 'ADULT'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS change_log_elderly (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    old_age_group VARCHAR(20),
    new_age_group VARCHAR(20) NOT NULL,
    change_reason VARCHAR(500),
    operator VARCHAR(50),
    created_at DATETIME NOT NULL,
    age_group VARCHAR(20) DEFAULT 'ELDERLY'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO equipment (equipment_no, name, cold_resistance_spec, age_group, status, created_at) VALUES
('EQ-001', '儿童防寒座椅', '-40°C至-10°C', 'CHILD', 1, NOW()),
('EQ-002', '成人防寒座椅', '-60°C至-20°C', 'ADULT', 1, NOW()),
('EQ-003', '老年防寒座椅', '-30°C至0°C', 'ELDERLY', 1, NOW()),
('EQ-004', '儿童低温操作扶手', '-35°C至-5°C', 'CHILD', 1, NOW()),
('EQ-005', '成人低温操作扶手', '-55°C至-15°C', 'ADULT', 1, NOW()),
('EQ-006', '老年低温操作扶手', '-25°C至5°C', 'ELDERLY', 1, NOW()),
('EQ-007', '儿童保暖手套', '-45°C至-15°C', 'CHILD', 1, NOW()),
('EQ-008', '成人保暖手套', '-65°C至-25°C', 'ADULT', 1, NOW()),
('EQ-009', '老年保暖手套', '-35°C至-5°C', 'ELDERLY', 1, NOW());

INSERT INTO session (session_no, name, start_time, end_time, child_ratio, adult_ratio, elderly_ratio, status, created_at) VALUES
('SESS-001', '上午场-家庭体验', '2026-07-09 09:00:00', '2026-07-09 12:00:00', 30, 50, 20, 1, NOW()),
('SESS-002', '下午场-成人专场', '2026-07-09 14:00:00', '2026-07-09 17:00:00', 0, 100, 0, 1, NOW()),
('SESS-003', '晚间场-老年体验', '2026-07-09 18:00:00', '2026-07-09 21:00:00', 0, 30, 70, 1, NOW());