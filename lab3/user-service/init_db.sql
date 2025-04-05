CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(100),
    hashed_password VARCHAR(100) NOT NULL,
    disabled BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS products (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    category VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS cart_items (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    product_id VARCHAR(50) REFERENCES products(id),
    quantity INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_full_name ON users(full_name);
CREATE INDEX idx_products_category ON products(category);

-- Master user
INSERT INTO users (username, full_name, email, hashed_password, disabled)
VALUES ('admin', 'Admin User', 'admin@example.com', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', FALSE)
ON CONFLICT (username) DO NOTHING;

-- Sample products
INSERT INTO products (id, name, description, price, category)
VALUES
    ('prod_1', 'Smartphone', 'Latest model smartphone', 599.99, 'Electronics'),
    ('prod_2', 'Laptop', 'High performance laptop', 1299.99, 'Electronics'),
    ('prod_3', 'Book', 'Bestselling novel', 19.99, 'Books')
ON CONFLICT (id) DO NOTHING;