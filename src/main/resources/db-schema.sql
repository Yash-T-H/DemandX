-- SQLite Schema for Inventory Management & Stock-Out Prediction

CREATE TABLE IF NOT EXISTS products (
    product_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    price REAL NOT NULL CHECK(price >= 0),
    quantity_in_stock INTEGER NOT NULL DEFAULT 0 CHECK(quantity_in_stock >= 0),
    supplier_lead_time_days INTEGER NOT NULL DEFAULT 7 CHECK(supplier_lead_time_days > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sales (
    sale_id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER NOT NULL,
    sale_date TEXT NOT NULL, -- Format: YYYY-MM-DD
    quantity_sold INTEGER NOT NULL CHECK(quantity_sold > 0),
    unit_price REAL NOT NULL CHECK(unit_price >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

-- Indexes for performance on sales queries
CREATE INDEX IF NOT EXISTS idx_sales_product_date ON sales(product_id, sale_date);
