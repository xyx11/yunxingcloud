CREATE TABLE IF NOT EXISTS sys_job_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL,
    job_group VARCHAR(100),
    invoke_target VARCHAR(500),
    start_time TIMESTAMP NULL,
    end_time TIMESTAMP NULL,
    status CHAR(1) DEFAULT '1',
    result TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_job_log_job ON sys_job_log(job_name);
CREATE INDEX idx_job_log_time ON sys_job_log(created_at DESC);
