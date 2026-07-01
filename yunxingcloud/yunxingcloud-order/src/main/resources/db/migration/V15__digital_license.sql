-- v2.3.0: 数字商品+激活码
CREATE TABLE IF NOT EXISTS digital_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    file_url VARCHAR(500),
    file_size BIGINT,
    auto_deliver TINYINT(1) DEFAULT 1,
    total_keys INT,
    used_keys INT DEFAULT 0,
    status CHAR(1) DEFAULT '1',
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS license_key (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    order_id BIGINT,
    key_code VARCHAR(64) NOT NULL UNIQUE,
    status CHAR(1) DEFAULT '0',
    activated_by VARCHAR(100),
    activate_at DATETIME,
    expire_at DATETIME,
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;