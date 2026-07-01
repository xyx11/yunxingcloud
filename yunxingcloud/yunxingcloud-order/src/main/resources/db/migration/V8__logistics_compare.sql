-- v2.3.0: 物流追踪 + 商品对比
CREATE TABLE IF NOT EXISTS logistics_trace (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    carrier VARCHAR(100),
    tracking_no VARCHAR(50),
    status VARCHAR(200),
    location VARCHAR(500),
    description VARCHAR(500),
    trace_time DATETIME,
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_lt_order ON logistics_trace(order_id);
CREATE INDEX idx_lt_tracking ON logistics_trace(tracking_no);

CREATE TABLE IF NOT EXISTS compare_list (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    product_id BIGINT NOT NULL,
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_cl_username ON compare_list(username);