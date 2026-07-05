-- V21: 补全预售/直播/商家/盘点表
-- 这些表在代码中已通过 JPA 实体定义，生产环境通过此迁移确保 DDL 一致

CREATE TABLE IF NOT EXISTS presale (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200),
    deposit_price BIGINT NOT NULL COMMENT '定金价(分)',
    full_price BIGINT NOT NULL COMMENT '全价(分)',
    min_deposit BIGINT NOT NULL DEFAULT 0 COMMENT '最低定金(分)',
    start_time DATETIME,
    end_time DATETIME COMMENT '定金阶段结束',
    final_pay_start DATETIME COMMENT '尾款开始时间',
    final_pay_end DATETIME,
    status CHAR(1) DEFAULT '0' COMMENT '0-预告/1-定金阶段/2-尾款阶段/3-结束',
    stock INT DEFAULT 0,
    sold INT DEFAULT 0,
    image_url VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预售活动';

CREATE TABLE IF NOT EXISTS live_room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    cover_url VARCHAR(500),
    stream_url VARCHAR(500),
    status CHAR(1) DEFAULT '0' COMMENT '0-预告/1-直播中/2-已结束',
    start_time DATETIME,
    end_time DATETIME,
    anchor_name VARCHAR(100),
    viewer_count INT DEFAULT 0,
    product_ids TEXT COMMENT '关联商品ID(JSON数组)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='直播间';

CREATE TABLE IF NOT EXISTS merchant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    store_name VARCHAR(200) NOT NULL,
    store_logo VARCHAR(500),
    store_desc TEXT,
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    status CHAR(1) DEFAULT '0' COMMENT '0-待审核/1-已通过/2-已拒绝',
    audit_remark VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家入驻';

CREATE TABLE IF NOT EXISTS stock_take (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    expected_qty INT NOT NULL,
    actual_qty INT,
    difference INT,
    status CHAR(1) DEFAULT '0' COMMENT '0-盘点中/1-已完成',
    remark VARCHAR(500),
    created_by VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点';