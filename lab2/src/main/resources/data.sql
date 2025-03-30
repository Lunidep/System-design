INSERT INTO users (id, status, password, username) VALUES (1, 'ACTIVE', '$2a$12$BXeONFjoaZQfI7WH/1L0JOmrJ5oV2Q2zcVIBrLh1K3ABZG73Z9nZ6', 'admin');
INSERT INTO users (id, status, password, username) VALUES (2, 'ACTIVE', '$2a$12$BXeONFjoaZQfI7WH/1L0JOmrJ5oV2Q2zcVIBrLh1K3ABZG73Z9nZ6', 'user');


INSERT INTO roles (id, name) VALUES (1, 'ROLE_ADMIN');
INSERT INTO roles (id, name) VALUES (2, 'ROLE_USER');

INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (1, 2);
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2);

INSERT INTO products (name, price, description)
VALUES ('Ноутбук ASUS', 899.99, '15.6", Core i7, 16GB RAM'),
       ('Смартфон Xiaomi', 299.50, '6.5" AMOLED, 128GB'),
       ('Наушники Sony', 199.99, 'Беспроводные, шумоподавление'),
       ('Фотоаппарат Canon', 1200.00, 'Зеркальный, 24.1MP'),
       ('Монитор Samsung', 450.75, '27", 4K UHD');