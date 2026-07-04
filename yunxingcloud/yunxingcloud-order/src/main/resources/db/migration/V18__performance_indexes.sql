-- V18: Production performance indexes
-- Priority 1: product table (most queried, zero indexes beyond PK)
CREATE INDEX IF NOT EXISTS idx_product_category ON product(category_id);
CREATE INDEX IF NOT EXISTS idx_product_brand ON product(brand_id);
CREATE INDEX IF NOT EXISTS idx_product_status ON product(status);
CREATE INDEX IF NOT EXISTS idx_product_hot ON product(is_hot, status);
CREATE INDEX IF NOT EXISTS idx_product_new ON product(is_new, status);

-- Priority 2: order_head.expire_at (used by scheduled cancel task)
CREATE INDEX IF NOT EXISTS idx_order_status_expire ON order_head(status, expire_at);

-- Priority 3: after_sale (zero indexes, financial table)
CREATE INDEX IF NOT EXISTS idx_as_username ON after_sale(username);
CREATE INDEX IF NOT EXISTS idx_as_status ON after_sale(status);
CREATE INDEX IF NOT EXISTS idx_as_order ON after_sale(order_id);

-- Priority 4: invoice (zero indexes, grows unbounded)
CREATE INDEX IF NOT EXISTS idx_inv_username ON invoice(username);
CREATE INDEX IF NOT EXISTS idx_inv_order ON invoice(order_id);
