-- v2.3.0: 营销活动
CREATE TABLE IF NOT EXISTS campaign (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    rule_json TEXT,
    threshold BIGINT,
    discount BIGINT,
    max_discount BIGINT,
    gift_product_id BIGINT,
    total_stock INT,
    used_count INT DEFAULT 0,
    limit_per_user INT DEFAULT 1,
    start_time DATETIME,
    end_time DATETIME,
    status CHAR(1) DEFAULT '0',
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;