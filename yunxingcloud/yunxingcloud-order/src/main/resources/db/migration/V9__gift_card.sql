-- v2.3.0: 礼品卡
CREATE TABLE IF NOT EXISTS gift_card (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_no VARCHAR(30) NOT NULL UNIQUE,
    amount BIGINT NOT NULL,
    balance BIGINT NOT NULL,
    status CHAR(1) DEFAULT '0',
    activate_at DATETIME,
    expire_at DATETIME,
    owner VARCHAR(100),
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;