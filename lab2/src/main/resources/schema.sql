drop table if exists users CASCADE;
create table users
(
    id       bigserial primary key,
    status   varchar(10),
    password varchar(255),
    username varchar(255) unique
);

drop table if exists roles CASCADE;
create table roles
(
    id   bigserial primary key,
    name varchar(50) not null
);
drop table if exists user_roles CASCADE;
create table user_roles
(
    user_id bigint not null references users,
    role_id bigint not null references roles,
        unique (user_id, role_id)
);

DROP TABLE IF EXISTS products CASCADE;
CREATE TABLE products
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(255)   NOT NULL,
    price       DECIMAL(10, 2) NOT NULL,
    description VARCHAR(500)
);