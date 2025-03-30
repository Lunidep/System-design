DROP TABLE IF EXISTS products CASCADE;
CREATE TABLE products
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(255)   NOT NULL,
    price       DECIMAL(10, 2) NOT NULL,
    description VARCHAR(500)
);