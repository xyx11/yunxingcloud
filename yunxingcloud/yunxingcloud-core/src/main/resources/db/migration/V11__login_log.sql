CREATE TABLE IF NOT EXISTS sys_logininfor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(50) NOT NULL,
    ipaddr VARCHAR(128),
    login_location VARCHAR(255),
    browser VARCHAR(50),
    os VARCHAR(50),
    status CHAR(1) DEFAULT '0',
    msg VARCHAR(255),
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_logininfor_user ON sys_logininfor(user_name);
CREATE INDEX idx_logininfor_status ON sys_logininfor(status);
CREATE INDEX idx_logininfor_time ON sys_logininfor(login_time);
