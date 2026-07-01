-- v2.3.0: 会员等级体系
CREATE TABLE IF NOT EXISTS member_tier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    min_points BIGINT NOT NULL,
    discount_rate INT DEFAULT 100,
    free_shipping TINYINT(1) DEFAULT 0,
    birthday_gift TINYINT(1) DEFAULT 0,
    priority_support TINYINT(1) DEFAULT 0,
    sort_order INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 默认等级数据
INSERT IGNORE INTO member_tier (name, min_points, discount_rate, free_shipping, sort_order) VALUES
('VIP1', 1000, 98, 0, 1),
('VIP2', 5000, 95, 1, 2),
('VIP3', 20000, 90, 1, 3);