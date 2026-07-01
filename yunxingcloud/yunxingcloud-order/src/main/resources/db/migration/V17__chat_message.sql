-- v2.3.0: 客服聊天
CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender VARCHAR(100) NOT NULL,
    receiver VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    session_id VARCHAR(50),
    is_read TINYINT(1) DEFAULT 0,
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_chat_session ON chat_message(session_id);
CREATE INDEX idx_chat_users ON chat_message(sender, receiver);