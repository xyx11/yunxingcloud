# Flyway 迁移索引

## yunxingcloud_core (Core) — 18 迁移

| 版本 | 内容 |
|------|------|
| V1 | OAuth2 表 (client/authorization) |
| V2 | 应用表 (user/role/menu/config) |
| V3 | 用户锁定 (failed_attempts/lockout_until) |
| V4 | 性能索引 |
| V5 | 最后登录时间 |
| V6 | 部门表 (sys_dept) |
| V7 | 字典表 (sys_dict_type/sys_dict_data) |
| V8 | 通知公告 (sys_notice) |
| V9 | 岗位表 (sys_post) |
| V10 | 用户关联岗位 |
| V11 | 登录日志 (sys_logininfor) |
| V12 | 定时任务日志 (sys_job_log) |
| V13 | 站内信 (sys_message) |
| V14 | IP黑名单 (ip_blacklist) |
| V15 | 审批流程 (sys_approval) |
| V16 | 用户审核 (user.approved) |
| V17 | 公告发布时间 |
| V18 | 工单表 (sys_ticket) v2.0 |

## yunxingcloud_usercenter — 1 迁移

| 版本 | 内容 |
|------|------|
| V1 | 用户/角色/部门表 |

## yunxingcloud_payment — 1 迁移

| 版本 | 内容 |
|------|------|
| V1 | 支付订单/支付记录表 |

## yunxingcloud_order — 10 迁移

| 版本 | 内容 |
|------|------|
| V1 | 订单基础表 (order_head/line/product/cart...) |
| V2 | 商城增强 (coupon/address/review/sku/banner) |
| V3 | Banner+商品增强 |
| V4 | 优惠券+超时+实付金额 v2.1 |
| V5 | 拼团+秒杀+售后+发票 v2.2 |
| V6 | 积分体系 (points_account/record) v2.3 |
| V7 | CMS+通知 (cms_article/notification) v2.3 |
| V8 | 物流+对比 (logistics_trace/compare_list) v2.3 |
| V9 | 礼品卡 (gift_card) v2.3 |
| V10 | 营销活动 (campaign) v2.3 |

## yunxingcloud_inventory — 2 迁移

| 版本 | 内容 |
|------|------|
| V1 | 仓库/库存/日志表 |
| V2 | 预占库存 (reserved) v2.1 |

## 总计

| 数据库 | 迁移数 | 表数 |
|--------|--------|------|
| core | 18 | 15 |
| usercenter | 1 | 3 |
| payment | 1 | 2 |
| order | 10 | 16 |
| inventory | 2 | 3 |
| **合计** | **32** | **39** |