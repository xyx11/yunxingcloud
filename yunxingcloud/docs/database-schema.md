
# yunxingcloud 数据库 Schema

## 概览

| 数据库 | 表数 | 迁移数 | 服务 |
|--------|------|--------|------|
| yunxingcloud_core | 15 | V1-V18 | core |
| yunxingcloud_usercenter | 3 | V1 | usercenter |
| yunxingcloud_payment | 2 | V1 | payment |
| yunxingcloud_order | 14 | V1-V4 | order |
| yunxingcloud_inventory | 3 | V1-V2 | inventory |
| yunxingcloud_core | - | - | core(备用) |

## yunxingcloud_core (认证 + 系统管理)

### sys_user
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| username | VARCHAR(100) UNIQUE | 用户名 |
| password | VARCHAR(255) | BCrypt 密码 |
| nickname | VARCHAR(100) | 昵称 |
| email | VARCHAR(200) | 邮箱 |
| phone | VARCHAR(20) | 手机号 |
| avatar | VARCHAR(500) | 头像 URL |
| enabled | TINYINT(1) | 启用状态 |
| dept_id | BIGINT FK | 部门 ID |
| source | VARCHAR(20) | 注册来源 |
| last_login | DATETIME | 最后登录 |
| lockout_until | DATETIME | 锁定到 |
| failed_attempts | INT | 失败次数 |
| created_at/updated_at | DATETIME | 时间戳 |

### sys_role
id, name, code UNIQUE, description, permissions TEXT, enabled

### sys_menu
id, name, type(DIR/MENU/BTN), path, component, icon, parent_id, sort, visible

### sys_dept
id, name, parent_id, sort, enabled

### sys_ticket (v2.0)
id, ticket_no UNIQUE, title, content TEXT, type, priority, status, applicant, assignee, created_at, updated_at

### 其他表
sys_config, sys_notice, sys_post, sys_dict_type, sys_dict_data, sys_oper_log, sys_login_info, sys_job, sys_job_log, sys_message, ip_blacklist, sys_approval, oauth2_client, oauth2_authorization

---

## yunxingcloud_usercenter (用户注册)

### users
id, username UNIQUE, password, email, enabled, approved, created_at

### roles
id, name, code UNIQUE, description, permissions

### departments
id, name, parent_id, sort, enabled

---

## yunxingcloud_payment (支付)

### payment_order
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| order_no | VARCHAR(30) UNIQUE | PAY+时间戳 |
| title | VARCHAR(200) | |
| amount | BIGINT | 金额(分) |
| channel | VARCHAR(20) | wechat/alipay |
| status | CHAR(1) | 0待付/1支付中/2已付/3退款/4关闭 |
| trade_no | VARCHAR(64) | 第三方交易号 |
| paid_at | DATETIME | 支付时间 |
| refund_amount | BIGINT | 已退金额 |
| refund_reason | VARCHAR(500) | |
| refund_at | DATETIME | |
| created_at/updated_at | DATETIME | |

### payment_record
id, order_id FK, type(pay/callback/refund), channel, request TEXT, response TEXT, success, created_at

---

## yunxingcloud_order (订单 + 商城)

### order_head
id, order_no UNIQUE(ORD+时间戳), username, total_amount, coupon_id, coupon_amount, actual_amount, status(0-4), payment_order_id, receiver_name/phone/address, remark, expire_at, created_at, updated_at

### order_line
id, order_id FK, product_id, product_name, price, quantity

### product
id, name, description, price, stock, image_url, status(0上架/1下架), is_hot, is_new, sales, category_id, brand_id, created_at

### product_category
id, name, description, icon, sort, parent_id

### product_brand
id, name, description, logo_url

### product_sku
id, product_id, name, price, stock, specs

### product_review
id, product_id, username, content, rating(1-5), created_at

### cart_item
id, username, product_id, product_name, price, quantity, image_url

### user_address
id, username, name, phone, province, city, district, detail, is_default

### coupon
id, name, type, threshold, amount, total_qty, used_qty, start_time, end_time, status

### coupon_user
id, username, coupon_id, status(0未用/1已用), created_at

### user_favorite
id, username, product_id, created_at

### banner
id, title, image_url, link_url, sort, status, created_at

### order_shipment
id, order_id, carrier, tracking_no, status, created_at

---

## yunxingcloud_inventory (库存)

### warehouse
id, name, address, created_at

### stock
id, product_id, product_name, warehouse_id, quantity, reserved (v2.1), min_quantity, updated_at

### stock_log
id, product_id, warehouse_id, type(in/out/order_out/order_back), quantity, order_id, remark, created_at
