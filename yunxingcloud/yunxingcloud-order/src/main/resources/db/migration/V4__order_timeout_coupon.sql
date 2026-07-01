-- 订单模块 v4: 优惠券 + 超时 + 实付金额
ALTER TABLE order_head ADD COLUMN IF NOT EXISTS coupon_id BIGINT AFTER total_amount;
ALTER TABLE order_head ADD COLUMN IF NOT EXISTS coupon_amount BIGINT DEFAULT 0 AFTER coupon_id;
ALTER TABLE order_head ADD COLUMN IF NOT EXISTS actual_amount BIGINT AFTER coupon_amount;
ALTER TABLE order_head ADD COLUMN IF NOT EXISTS expire_at DATETIME AFTER remark;