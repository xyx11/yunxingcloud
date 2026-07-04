CREATE TABLE IF NOT EXISTS warehouse (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(300),
    status CHAR(1) DEFAULT '0',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200),
    warehouse_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    min_quantity INT DEFAULT 10 COMMENT '库存预警阈值',
    updated_at TIMESTAMP NULL,
    UNIQUE KEY uk_product_warehouse (product_id, warehouse_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_stock_warehouse ON stock(warehouse_id);
CREATE INDEX idx_stock_product ON stock(product_id);

CREATE TABLE IF NOT EXISTS stock_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL COMMENT 'in/out/order_out/order_back',
    quantity INT NOT NULL,
    order_id BIGINT,
    remark VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_log_product ON stock_log(product_id);
CREATE INDEX idx_log_type ON stock_log(type);
