-- V3: shopping cart and orders schema

CREATE TABLE cart (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE cart_item (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id    BIGINT NOT NULL REFERENCES cart(id),
    product_id BIGINT NOT NULL REFERENCES product(id),
    quantity   INT    NOT NULL DEFAULT 1
);

CREATE TABLE orders (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(100) NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total      DOUBLE       NOT NULL DEFAULT 0
);

CREATE TABLE order_item (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id         BIGINT       NOT NULL REFERENCES orders(id),
    product_name     VARCHAR(255) NOT NULL,
    product_price    DOUBLE       NOT NULL,
    product_category VARCHAR(100) NOT NULL,
    quantity         INT          NOT NULL DEFAULT 1
);
