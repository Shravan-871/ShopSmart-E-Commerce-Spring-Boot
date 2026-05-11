-- V1: initial schema

CREATE TABLE product (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    price       DOUBLE       NOT NULL,
    category    VARCHAR(100) NOT NULL,
    stock       INT          NOT NULL DEFAULT 0,
    description VARCHAR(500)
);

CREATE TABLE app_user (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(20)  NOT NULL DEFAULT 'USER'
);
