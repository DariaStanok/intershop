CREATE TABLE IF NOT EXISTS carts ( id SERIAL PRIMARY KEY );

CREATE TABLE IF NOT EXISTS items (
  id SERIAL PRIMARY KEY,
  title VARCHAR(255),
  description TEXT,
  price INTEGER,
  img_path VARCHAR(255),
  views INTEGER
);

CREATE TABLE IF NOT EXISTS orders ( id SERIAL PRIMARY KEY );

CREATE TABLE IF NOT EXISTS order_item (
  id SERIAL PRIMARY KEY,
  order_id BIGINT,
  item_id BIGINT,
  count INTEGER
);

CREATE TABLE IF NOT EXISTS cart_lines (
  id SERIAL PRIMARY KEY,
  quantity INTEGER,
  cart_id BIGINT,
  item_id BIGINT
);
