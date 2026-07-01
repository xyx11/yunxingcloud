-- v2.2.0: 拼团 + 秒杀 + 售后 + 发票
CREATE TABLE IF NOT EXISTS group_buy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    min_members INT NOT NULL DEFAULT 2,
    group_price BIGINT NOT NULL,
    start_time DATETIME,
    end_time DATETIME,
    status CHAR(1) DEFAULT '0',
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS group_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    group_buy_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    username VARCHAR(100) NOT NULL,
    is_leader TINYINT(1) DEFAULT 0,
    status CHAR(1) DEFAULT '0',
    joined_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_group_id ON group_record(group_id);

CREATE TABLE IF NOT EXISTS flash_sale (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    flash_price BIGINT NOT NULL,
    stock INT NOT NULL,
    sold INT DEFAULT 0,
    limit_per_user INT DEFAULT 1,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status CHAR(1) DEFAULT '0',
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS after_sale (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    order_no VARCHAR(30),
    username VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    reason VARCHAR(500),
    refund_amount BIGINT,
    evidence_urls VARCHAR(2000),
    status CHAR(1) DEFAULT '0',
    remark VARCHAR(500),
    created_at DATETIME,
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS invoice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    order_no VARCHAR(30),
    username VARCHAR(100) NOT NULL,
    type VARCHAR(10) NOT NULL,
    title VARCHAR(200),
    tax_no VARCHAR(30),
    email VARCHAR(200),
    status CHAR(1) DEFAULT '0',
    invoice_no VARCHAR(50),
    invoice_url VARCHAR(500),
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;