-- 库存模块 v2: 预占库存
ALTER TABLE stock ADD COLUMN IF NOT EXISTS reserved INT DEFAULT 0 AFTER quantity;