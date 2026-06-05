-- 操作日志查询优化
CREATE INDEX IF NOT EXISTS idx_oper_log_time ON sys_oper_log(oper_time);
CREATE INDEX IF NOT EXISTS idx_oper_log_type ON sys_oper_log(business_type);
CREATE INDEX IF NOT EXISTS idx_oper_log_user ON sys_oper_log(oper_name);

-- 用户查询优化
CREATE INDEX IF NOT EXISTS idx_users_department ON users(department_id);

-- 菜单查询优化
CREATE INDEX IF NOT EXISTS idx_menu_parent ON sys_menu(parent_id);
CREATE INDEX IF NOT EXISTS idx_menu_sort ON sys_menu(sort_order);

-- 角色查询优化
CREATE INDEX IF NOT EXISTS idx_role_code ON role(code);

-- 令牌查询优化
CREATE INDEX IF NOT EXISTS idx_pwd_token ON password_reset_tokens(token);
CREATE INDEX IF NOT EXISTS idx_pwd_expires ON password_reset_tokens(expires_at);
