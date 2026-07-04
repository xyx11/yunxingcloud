-- V19: Admin workflow performance indexes

-- Approval table (zero indexes, heavily queried by admin)
CREATE INDEX IF NOT EXISTS idx_appr_applicant ON sys_approval(applicant);
CREATE INDEX IF NOT EXISTS idx_appr_approver ON sys_approval(approver);
CREATE INDEX IF NOT EXISTS idx_appr_status ON sys_approval(status);

-- Ticket table (zero indexes, same pattern as approval)
CREATE INDEX IF NOT EXISTS idx_ticket_applicant ON sys_ticket(applicant);
CREATE INDEX IF NOT EXISTS idx_ticket_assignee ON sys_ticket(assignee);
CREATE INDEX IF NOT EXISTS idx_ticket_status ON sys_ticket(status);

-- Message inbox (queried by to_user + is_read)
CREATE INDEX IF NOT EXISTS idx_msg_touser_read ON sys_message(to_user, is_read);

-- Login log (filtered by user_name, deleted by login_time)
CREATE INDEX IF NOT EXISTS idx_login_user ON sys_logininfor(user_name);

-- Users email (used for login/register dedup)
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
