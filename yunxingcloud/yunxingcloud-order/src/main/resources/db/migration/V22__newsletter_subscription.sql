CREATE TABLE IF NOT EXISTS newsletter_subscription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(1) DEFAULT '1',
    subscribed_at DATETIME,
    unsubscribed_at DATETIME
);
