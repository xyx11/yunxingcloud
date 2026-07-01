-- v2.3.0: 反馈+降价提醒+组合商品
CREATE TABLE IF NOT EXISTS feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    content VARCHAR(500) NOT NULL,
    images VARCHAR(1000),
    contact VARCHAR(50),
    status CHAR(1) DEFAULT '0',
    reply VARCHAR(500),
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS price_alert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    product_id BIGINT NOT NULL,
    target_price BIGINT NOT NULL,
    notified TINYINT(1) DEFAULT 0,
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_bundle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    bundle_price BIGINT NOT NULL,
    original_price BIGINT,
    product_ids VARCHAR(1000),
    image_url VARCHAR(500),
    status CHAR(1) DEFAULT '1',
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;