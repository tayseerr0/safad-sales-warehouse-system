DROP DATABASE IF EXISTS safad_db;
CREATE DATABASE safad_db;
USE safad_db;

-- =========================================================
-- SAFAD Sales and Warehouse Management System
-- Full MySQL Schema
-- =========================================================

-- =========================================================
-- 1. Independent Tables
-- =========================================================

CREATE TABLE Category (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    category_type VARCHAR(50)
);

CREATE TABLE Brand (
    brand_id INT AUTO_INCREMENT PRIMARY KEY,
    brand_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE Client (
    client_id INT AUTO_INCREMENT PRIMARY KEY,
    client_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30),
    email VARCHAR(100),
    registration_date DATE DEFAULT (CURRENT_DATE),
    city VARCHAR(50),
    address VARCHAR(255),
    client_type ENUM('Individual', 'Company', 'Reseller') NOT NULL
);

CREATE TABLE Supplier (
    supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30),
    email VARCHAR(100),
    starting_date DATE,
    city VARCHAR(50),
    country VARCHAR(50),
    address VARCHAR(255)
);

CREATE TABLE Warehouse (
    warehouse_id INT AUTO_INCREMENT PRIMARY KEY,
    warehouse_name VARCHAR(100) NOT NULL,
    location VARCHAR(150),
    capacity INT,

    CHECK (capacity IS NULL OR capacity >= 0)
);

CREATE TABLE AppUser (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    password_salt VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_date DATE DEFAULT (CURRENT_DATE)
);

-- =========================================================
-- 2. Product Tables
-- =========================================================

CREATE TABLE Product (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    default_selling_price DECIMAL(10,2) NOT NULL,
    brand_id INT NOT NULL,
    category_id INT NOT NULL,

    CHECK (default_selling_price >= 0),

    CONSTRAINT fk_product_brand
        FOREIGN KEY (brand_id)
        REFERENCES Brand(brand_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id)
        REFERENCES Category(category_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE SupplierProduct (
    supplier_id INT NOT NULL,
    product_id INT NOT NULL,
    supply_price DECIMAL(10,2) NOT NULL,

    PRIMARY KEY (supplier_id, product_id),

    CHECK (supply_price >= 0),

    CONSTRAINT fk_supplierproduct_supplier
        FOREIGN KEY (supplier_id)
        REFERENCES Supplier(supplier_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_supplierproduct_product
        FOREIGN KEY (product_id)
        REFERENCES Product(product_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- =========================================================
-- 3. Inventory
-- =========================================================

CREATE TABLE Inventory (
    product_id INT NOT NULL,
    warehouse_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    threshold INT NOT NULL DEFAULT 0,

    PRIMARY KEY (product_id, warehouse_id),

    CHECK (quantity >= 0),
    CHECK (threshold >= 0),

    CONSTRAINT fk_inventory_product
        FOREIGN KEY (product_id)
        REFERENCES Product(product_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_inventory_warehouse
        FOREIGN KEY (warehouse_id)
        REFERENCES Warehouse(warehouse_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- =========================================================
-- 4. Purchase Invoices
-- =========================================================

CREATE TABLE PurchaseInvoice (
    purchase_invoice_id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_date DATE NOT NULL,
    estimated_arrival DATE,
    payment DECIMAL(10,2) DEFAULT 0,
    payment_type ENUM('Cash', 'Card', 'Bank Transfer', 'Cheque') NOT NULL,
    amount DECIMAL(10,2) DEFAULT 0,
    supplier_id INT NOT NULL,
    warehouse_id INT NOT NULL,

    CHECK (payment >= 0),
    CHECK (amount >= 0),

    CONSTRAINT fk_purchaseinvoice_supplier
        FOREIGN KEY (supplier_id)
        REFERENCES Supplier(supplier_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_purchaseinvoice_warehouse
        FOREIGN KEY (warehouse_id)
        REFERENCES Warehouse(warehouse_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE PurchaseInvoiceItem (
    purchase_item_id INT AUTO_INCREMENT PRIMARY KEY,
    purchase_invoice_id INT NOT NULL,
    product_id INT NOT NULL,
    purchase_price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,

    CHECK (purchase_price >= 0),
    CHECK (quantity > 0),

    CONSTRAINT fk_purchaseitem_invoice
        FOREIGN KEY (purchase_invoice_id)
        REFERENCES PurchaseInvoice(purchase_invoice_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_purchaseitem_product
        FOREIGN KEY (product_id)
        REFERENCES Product(product_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- =========================================================
-- 5. Sales Invoices
-- =========================================================

CREATE TABLE SalesInvoice (
    sales_invoice_id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_date DATE NOT NULL,
    payment DECIMAL(10,2) DEFAULT 0,
    payment_type ENUM('Cash', 'Card', 'Bank Transfer', 'Cheque') NOT NULL,
    amount DECIMAL(10,2) DEFAULT 0,
    client_id INT NOT NULL,
    warehouse_id INT NOT NULL,

    CHECK (payment >= 0),
    CHECK (amount >= 0),

    CONSTRAINT fk_salesinvoice_client
        FOREIGN KEY (client_id)
        REFERENCES Client(client_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_salesinvoice_warehouse
        FOREIGN KEY (warehouse_id)
        REFERENCES Warehouse(warehouse_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE SalesPayment (
    sales_payment_id INT AUTO_INCREMENT PRIMARY KEY,
    sales_invoice_id INT NOT NULL,
    payment_date DATE NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_type ENUM('Cash', 'Card', 'Bank Transfer', 'Cheque') NOT NULL,

    CHECK (amount > 0),

    CONSTRAINT fk_salespayment_invoice
        FOREIGN KEY (sales_invoice_id)
        REFERENCES SalesInvoice(sales_invoice_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE SalesInvoiceItem (
    sales_item_id INT AUTO_INCREMENT PRIMARY KEY,
    sales_invoice_id INT NOT NULL,
    product_id INT NOT NULL,
    selling_price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    warranty_end_date DATE,

    CHECK (selling_price >= 0),
    CHECK (quantity > 0),

    CONSTRAINT fk_salesitem_invoice
        FOREIGN KEY (sales_invoice_id)
        REFERENCES SalesInvoice(sales_invoice_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_salesitem_product
        FOREIGN KEY (product_id)
        REFERENCES Product(product_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- =========================================================
-- 6. Warehouse Transfers
-- =========================================================

CREATE TABLE WarehouseTransfer (
    transfer_id INT AUTO_INCREMENT PRIMARY KEY,
    transfer_date DATE NOT NULL,
    from_warehouse_id INT NOT NULL,
    to_warehouse_id INT NOT NULL,

    CONSTRAINT fk_transfer_from_warehouse
        FOREIGN KEY (from_warehouse_id)
        REFERENCES Warehouse(warehouse_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_transfer_to_warehouse
        FOREIGN KEY (to_warehouse_id)
        REFERENCES Warehouse(warehouse_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE WarehouseTransferItem (
    transfer_item_id INT AUTO_INCREMENT PRIMARY KEY,
    transfer_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,

    CHECK (quantity > 0),

    CONSTRAINT fk_transferitem_transfer
        FOREIGN KEY (transfer_id)
        REFERENCES WarehouseTransfer(transfer_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_transferitem_product
        FOREIGN KEY (product_id)
        REFERENCES Product(product_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- =========================================================
-- 7. Useful Indexes
-- =========================================================

CREATE INDEX idx_product_name ON Product(product_name);
CREATE INDEX idx_client_name ON Client(client_name);
CREATE INDEX idx_supplier_name ON Supplier(supplier_name);
CREATE INDEX idx_purchase_date ON PurchaseInvoice(invoice_date);
CREATE INDEX idx_sales_date ON SalesInvoice(invoice_date);
CREATE INDEX idx_salespayment_invoice ON SalesPayment(sales_invoice_id);
CREATE INDEX idx_salespayment_date ON SalesPayment(payment_date);
CREATE INDEX idx_inventory_warehouse ON Inventory(warehouse_id);
CREATE INDEX idx_inventory_product ON Inventory(product_id);

-- =========================================================
-- 8. Quick Check
-- =========================================================

SHOW TABLES;
