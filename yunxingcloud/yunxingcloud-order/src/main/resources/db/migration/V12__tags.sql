-- v2.3.0: 商品标签 + 价格历史
CREATE TABLE IF NOT EXISTS product_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(20),
    sort_order INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_tag_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_ptr_pid ON product_tag_relation(product_id);
CREATE INDEX idx_ptr_tid ON product_tag_relation(tag_id);

CREATE TABLE IF NOT EXISTS price_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    old_price BIGINT NOT NULL,
    new_price BIGINT NOT NULL,
    changed_by VARCHAR(100),
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;