CREATE TABLE IF NOT EXISTS gift_card_transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id BIGINT NOT NULL,
    card_no VARCHAR(32),
    type VARCHAR(20) NOT NULL,
    amount BIGINT NOT NULL,
    remark VARCHAR(500),
    created_at DATETIME
);
