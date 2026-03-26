CREATE DATABASE IF NOT EXISTS shopdb;
USE shopdb;

CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    customer_email VARCHAR(200) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id)
) ENGINE=InnoDB;

CREATE TABLE product_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    product_name VARCHAR(200) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0
) ENGINE=InnoDB;

INSERT INTO product_inventory (product_id, product_name, stock_quantity) VALUES
(1001, '맥북 프로 14인치', 50),
(1002, '에어팟 프로', 200),
(1003, '아이패드 에어', 80);

CREATE USER IF NOT EXISTS 'debezium'@'%' IDENTIFIED BY 'dbzpass';
GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'debezium'@'%';
FLUSH PRIVILEGES;
