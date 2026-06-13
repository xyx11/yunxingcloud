ALTER TABLE users ADD COLUMN post_id BIGINT AFTER department_id;
CREATE INDEX idx_users_post ON users(post_id);
