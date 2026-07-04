CREATE TABLE IF NOT EXISTS product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    price BIGINT NOT NULL COMMENT '价格(分)',
    stock INT DEFAULT 0,
    image_url VARCHAR(500),
    status CHAR(1) DEFAULT '0' COMMENT '0=上架 1=下架',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cart_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200),
    price BIGINT NOT NULL COMMENT '加入时价格(分)',
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_cart_username ON cart_item(username);

CREATE TABLE IF NOT EXISTS order_head (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(30) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL,
    total_amount BIGINT NOT NULL COMMENT '总金额(分)',
    status CHAR(1) DEFAULT '0' COMMENT '0=待支付 1=已支付 2=已发货 3=已完成 4=已取消',
    payment_order_id BIGINT COMMENT '关联 payment_order.id',
    receiver_name VARCHAR(50),
    receiver_phone VARCHAR(20),
    receiver_address VARCHAR(500),
    remark VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_order_username ON order_head(username);
CREATE INDEX idx_order_status ON order_head(status);

CREATE TABLE IF NOT EXISTS order_line (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200),
    price BIGINT NOT NULL COMMENT '单价(分)',
    quantity INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_order_line_order ON order_line(order_id);
