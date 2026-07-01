-- v2.3.0: CMS + 通知 + 数据看板
CREATE TABLE IF NOT EXISTS cms_article (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    category VARCHAR(100),
    cover_url VARCHAR(500),
    tags VARCHAR(500),
    view_count BIGINT DEFAULT 0,
    status CHAR(1) DEFAULT '0',
    publish_at DATETIME,
    created_at DATETIME,
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    type VARCHAR(20) DEFAULT 'system',
    is_read TINYINT(1) DEFAULT 0,
    read_at DATETIME,
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_notif_user ON notification(username);