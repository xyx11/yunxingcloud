-- v2.3.0: 社交功能
CREATE TABLE IF NOT EXISTS wishlist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    product_id BIGINT NOT NULL,
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_wl_user ON wishlist(username);

CREATE TABLE IF NOT EXISTS share_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sharer VARCHAR(100) NOT NULL,
    product_id BIGINT NOT NULL,
    channel VARCHAR(20),
    click_count BIGINT DEFAULT 0,
    order_count BIGINT DEFAULT 0,
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;