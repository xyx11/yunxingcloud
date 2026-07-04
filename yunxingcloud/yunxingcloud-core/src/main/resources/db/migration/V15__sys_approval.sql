CREATE TABLE IF NOT EXISTS sys_approval (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    applicant VARCHAR(100),
    approver VARCHAR(100),
    type VARCHAR(50),
    title VARCHAR(200),
    content TEXT,
    status CHAR(1) DEFAULT '0',
    remark VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_approval_applicant ON sys_approval(applicant);
CREATE INDEX idx_approval_approver ON sys_approval(approver);
