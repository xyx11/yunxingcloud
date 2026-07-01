-- v2.3.0: 供应商
CREATE TABLE IF NOT EXISTS supplier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    contact VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(200),
    address VARCHAR(500),
    remark VARCHAR(500),
    status CHAR(1) DEFAULT '1',
    created_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;