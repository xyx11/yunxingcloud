CREATE TABLE IF NOT EXISTS sys_notice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notice_title VARCHAR(200) NOT NULL,
    notice_type CHAR(1) DEFAULT '1',
    notice_content TEXT,
    status CHAR(1) DEFAULT '0',
    remark VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_notice_type ON sys_notice(notice_type);
CREATE INDEX idx_notice_status ON sys_notice(status);
