CREATE TABLE IF NOT EXISTS sys_ticket (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_no VARCHAR(30) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    type VARCHAR(20) DEFAULT 'other',
    priority VARCHAR(10) DEFAULT 'medium',
    status CHAR(1) DEFAULT '0',
    applicant VARCHAR(100),
    assignee VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_ticket_applicant ON sys_ticket(applicant);
CREATE INDEX idx_ticket_assignee ON sys_ticket(assignee);
CREATE INDEX idx_ticket_status ON sys_ticket(status);
