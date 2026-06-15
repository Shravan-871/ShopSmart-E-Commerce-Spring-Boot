CREATE TABLE coupon (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    code          VARCHAR(50)  NOT NULL UNIQUE,
    discount_type VARCHAR(10)  NOT NULL,
    discount_value DOUBLE      NOT NULL,
    expiry_date   DATE         NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE wishlist_item (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(100) NOT NULL,
    product_id BIGINT       NOT NULL,
    added_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (username, product_id)
);

INSERT INTO coupon (code, discount_type, discount_value, expiry_date, active)
VALUES ('SAVE10', 'PERCENT', 10, DATE '2099-12-31', TRUE),
       ('FLAT500', 'FLAT', 500, DATE '2099-12-31', TRUE);
