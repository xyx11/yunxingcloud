CREATE TABLE IF NOT EXISTS payment_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(30) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    amount BIGINT NOT NULL COMMENT '金额(分)',
    channel VARCHAR(20) NOT NULL COMMENT '支付渠道: wechat, alipay',
    status CHAR(1) DEFAULT '0' COMMENT '0=待支付 1=支付中 2=已支付 3=已退款 4=已关闭',
    trade_no VARCHAR(100) COMMENT '第三方交易号',
    buyer VARCHAR(100) COMMENT '付款人',
    paid_at TIMESTAMP NULL,
    refund_amount BIGINT DEFAULT 0 COMMENT '退款金额(分)',
    refund_reason VARCHAR(500),
    refund_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_payment_order_no ON payment_order(order_no);
CREATE INDEX idx_payment_order_status ON payment_order(status);
CREATE INDEX idx_payment_order_channel ON payment_order(channel);

CREATE TABLE IF NOT EXISTS payment_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL COMMENT 'pay, refund, callback',
    channel VARCHAR(20) NOT NULL,
    request TEXT COMMENT '请求参数(JSON)',
    response TEXT COMMENT '响应/回调数据(JSON)',
    success BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_payment_record_order ON payment_record(order_id);
