-- flash_sale: queried by status AND start_time
CREATE INDEX IF NOT EXISTS idx_flash_sale_status_start ON flash_sale(status, start_time);

-- group_buy: filtered by product_id
CREATE INDEX IF NOT EXISTS idx_group_buy_product ON group_buy(product_id);

-- group_record: looked up by group_buy_id, order_id, username
CREATE INDEX IF NOT EXISTS idx_group_record_group ON group_record(group_buy_id);
CREATE INDEX IF NOT EXISTS idx_group_record_order ON group_record(order_id);
CREATE INDEX IF NOT EXISTS idx_group_record_user ON group_record(username);

-- wishlist: filtered by username
CREATE INDEX IF NOT EXISTS idx_wishlist_user ON wishlist(username);

-- compare_list: filtered by username
CREATE INDEX IF NOT EXISTS idx_compare_user ON compare_list(username);

-- feedback: filtered by username, status
CREATE INDEX IF NOT EXISTS idx_feedback_user ON feedback(username);
CREATE INDEX IF NOT EXISTS idx_feedback_status ON feedback(status);

-- price_alert: looked up by username and product_id
CREATE INDEX IF NOT EXISTS idx_price_alert_user_product ON price_alert(username, product_id);

-- product: add composite index for findByStatus ORDER BY createdAt DESC
CREATE INDEX IF NOT EXISTS idx_product_status_created ON product(status, created_at);

-- payment_order: filtered by channel ORDER BY createdAt
CREATE INDEX IF NOT EXISTS idx_payment_channel_created ON payment_order(channel, created_at);
