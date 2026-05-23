USE safad_db;

-- =========================================================
-- SAFAD Sales and Warehouse Management System
-- Sample Data
-- Compatible with the clean schema using:
-- category_id, brand_id, product_id, supplier_id, client_id,
-- warehouse_id, purchase_invoice_id, sales_invoice_id, transfer_id
-- =========================================================

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE WarehouseTransferItem;
TRUNCATE TABLE WarehouseTransfer;
TRUNCATE TABLE SalesInvoiceItem;
TRUNCATE TABLE SalesInvoice;
TRUNCATE TABLE PurchaseInvoiceItem;
TRUNCATE TABLE PurchaseInvoice;
TRUNCATE TABLE Inventory;
TRUNCATE TABLE SupplierProduct;
TRUNCATE TABLE Product;
TRUNCATE TABLE Warehouse;
TRUNCATE TABLE Supplier;
TRUNCATE TABLE Client;
TRUNCATE TABLE Brand;
TRUNCATE TABLE Category;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- 1. Categories
-- =========================================================

INSERT INTO Category (category_id, category_name, description, category_type) VALUES
(1, 'Laptops', 'Portable computers for business and personal use', 'Computers'),
(2, 'Networking Devices', 'Routers, switches, access points, and related devices', 'Network'),
(3, 'Cables and Accessories', 'Cables, adapters, chargers, and accessories', 'Accessories'),
(4, 'Power and UPS', 'UPS units, power protection, and electrical accessories', 'Power');

-- =========================================================
-- 2. Brands
-- =========================================================

INSERT INTO Brand (brand_id, brand_name, description) VALUES
(1, 'HP', 'Computers and laptops'),
(2, 'Cisco', 'Networking devices and enterprise network solutions'),
(3, 'APC', 'Power protection and UPS solutions'),
(4, 'Microsoft', 'Software and productivity solutions'),
(5, 'Sophos', 'Security and firewall solutions'),
(6, 'R&M', 'Cabling and network infrastructure');

-- =========================================================
-- 3. Clients
-- =========================================================

INSERT INTO Client (client_id, client_name, phone, email, registration_date, city, address, client_type) VALUES
(1, 'Al-Quds Technology Store', '0599001001', 'sales@qudstech.ps', '2025-10-10', 'Ramallah', 'Al-Irsal Street', 'Reseller'),
(2, 'Birzeit Training Center', '0599001002', 'it@bztc.ps', '2025-11-05', 'Birzeit', 'Main Street', 'Company'),
(3, 'Nablus Computer House', '0599001003', 'info@nch.ps', '2025-12-01', 'Nablus', 'Rafidia', 'Reseller'),
(4, 'Rawan Haddad', '0599001004', 'rawan.haddad@email.com', '2026-01-08', 'Ramallah', 'Al-Masyoun', 'Individual'),
(5, 'Gaza Smart Solutions', '0599001005', 'contact@gss.ps', '2026-01-15', 'Gaza', 'Al-Rimal', 'Company'),
(6, 'Tulkarem IT Services', '0599001006', 'support@tis.ps', '2026-02-01', 'Tulkarem', 'Downtown', 'Company'),
(7, 'Hebron Digital Market', '0599001007', 'sales@hdm.ps', '2026-02-20', 'Hebron', 'Ein Sara', 'Reseller'),
(8, 'Maya Nasser', '0599001008', 'maya.nasser@email.com', '2026-03-02', 'Nablus', 'University Street', 'Individual'),
(9, 'Elite Office Supplies', '0599001009', 'orders@eliteoffice.ps', '2026-03-16', 'Ramallah', 'Al-Bireh', 'Company'),
(10, 'Jenin Network Shop', '0599001010', 'info@jns.ps', '2026-04-01', 'Jenin', 'Cinema Street', 'Reseller');

-- =========================================================
-- 4. Suppliers
-- =========================================================

INSERT INTO Supplier (supplier_id, supplier_name, phone, email, starting_date, city, address) VALUES
(1, 'HP Middle East Distributor', '022401001', 'orders@hpme.example', '2024-01-10', 'Dubai', 'Business Bay'),
(2, 'Cisco Authorized Partner', '022401002', 'supply@cisco-partner.example', '2024-03-15', 'Amman', 'Mecca Street'),
(3, 'APC Power Systems Supplier', '022401003', 'sales@apc-supplier.example', '2024-05-20', 'Ramallah', 'Industrial Zone'),
(4, 'Microsoft Licensing Provider', '022401004', 'licenses@microsoft-provider.example', '2024-07-01', 'Jerusalem', 'Technology Park'),
(5, 'R&M Cabling Distributor', '022401005', 'orders@rm-cabling.example', '2024-08-10', 'Nablus', 'Industrial Area');

-- =========================================================
-- 5. Warehouses
-- =========================================================

INSERT INTO Warehouse (warehouse_id, warehouse_name, location, capacity) VALUES
(1, 'Ramallah Central Warehouse', 'Ramallah - Industrial Zone', 5000),
(2, 'Nablus Secondary Warehouse', 'Nablus - Rafidia', 2500),
(3, 'Gaza Secondary Warehouse', 'Gaza - Al-Rimal', 2000);

-- =========================================================
-- 6. Products
-- =========================================================

INSERT INTO Product (product_id, product_name, description, default_selling_price, brand_id, category_id) VALUES
(1, 'HP ProBook 450 G10', 'Business laptop, Intel i5, 16GB RAM, 512GB SSD', 850.00, 1, 1),
(2, 'HP EliteBook 840 G9', 'Premium business laptop, Intel i7, 16GB RAM, 1TB SSD', 720.00, 1, 1),
(3, 'Cisco ISR Router 1100', 'Enterprise branch router', 150.00, 2, 2),
(4, 'Cisco Catalyst Switch 24-Port', 'Managed 24-port gigabit switch', 120.00, 2, 2),
(5, 'R&M Cat6 Cable Box', '305m Cat6 network cable box', 25.00, 6, 3),
(6, 'APC Smart-UPS 1500VA', 'Power backup unit for servers and network equipment', 1100.00, 3, 4),
(7, 'Sophos XGS Firewall 87', 'Small business firewall appliance', 350.00, 5, 2),
(8, 'USB-C Docking Adapter', 'Multi-port USB-C docking adapter', 35.00, 1, 3),
(9, 'Microsoft 365 Business License', 'Annual productivity software license', 180.00, 4, 3),
(10, 'Windows Server License', 'Server operating system license', 220.00, 4, 3),
(11, 'APC Surge Protector', 'Power surge protection device', 210.00, 3, 4),
(12, 'R&M Patch Panel 24-Port', 'Network patch panel for racks', 45.00, 6, 3);

-- =========================================================
-- 7. Supplier-Product Relationships
-- =========================================================

INSERT INTO SupplierProduct (supplier_id, product_id, supply_price) VALUES
(1, 1, 650.00),
(1, 2, 520.00),
(1, 8, 18.00),
(2, 3, 95.00),
(2, 4, 75.00),
(2, 5, 12.00),
(3, 6, 850.00),
(3, 7, 240.00),
(3, 11, 140.00),
(4, 9, 110.00),
(4, 10, 135.00),
(5, 5, 13.00),
(5, 8, 18.00),
(5, 12, 28.00);

-- =========================================================
-- 8. Purchase Invoices
-- =========================================================

INSERT INTO PurchaseInvoice
(purchase_invoice_id, invoice_date, estimated_arrival, payment, payment_type, amount, supplier_id, warehouse_id) VALUES
(1, '2026-01-05', '2026-01-10', 21520.00, 'Bank Transfer', 21520.00, 1, 1),
(2, '2026-01-12', '2026-01-18', 5925.00, 'Bank Transfer', 5925.00, 2, 1),
(3, '2026-02-03', '2026-02-10', 22500.00, 'Cheque', 22500.00, 3, 2),
(4, '2026-02-18', '2026-02-25', 4370.00, 'Bank Transfer', 4370.00, 4, 1),
(5, '2026-03-07', '2026-03-12', 3920.00, 'Cash', 3920.00, 5, 2),
(6, '2026-03-20', '2026-03-28', 8760.00, 'Bank Transfer', 8760.00, 1, 3),
(7, '2026-04-05', '2026-04-12', 8450.00, 'Cheque', 8450.00, 2, 2),
(8, '2026-04-22', '2026-04-28', 11260.00, 'Bank Transfer', 11260.00, 3, 1);

-- =========================================================
-- 9. Purchase Invoice Items
-- =========================================================

INSERT INTO PurchaseInvoiceItem
(purchase_item_id, purchase_invoice_id, product_id, purchase_price, quantity) VALUES
(1, 1, 1, 650.00, 20),
(2, 1, 2, 520.00, 15),
(3, 1, 8, 18.00, 40),

(4, 2, 3, 95.00, 30),
(5, 2, 4, 75.00, 25),
(6, 2, 5, 12.00, 100),

(7, 3, 6, 850.00, 18),
(8, 3, 7, 240.00, 30),

(9, 4, 9, 110.00, 25),
(10, 4, 10, 135.00, 12),

(11, 5, 11, 140.00, 20),
(12, 5, 12, 28.00, 40),

(13, 6, 1, 660.00, 10),
(14, 6, 3, 96.00, 15),
(15, 6, 5, 12.00, 60),

(16, 7, 2, 525.00, 12),
(17, 7, 4, 76.00, 20),
(18, 7, 8, 18.00, 35),

(19, 8, 6, 845.00, 8),
(20, 8, 7, 242.00, 15),
(21, 8, 12, 29.00, 30);

-- =========================================================
-- 10. Sales Invoices
-- =========================================================

INSERT INTO SalesInvoice
(sales_invoice_id, invoice_date, payment, payment_type, amount, client_id, warehouse_id) VALUES
(1, '2026-01-20', 2450.00, 'Cash', 2450.00, 1, 1),
(2, '2026-02-10', 2950.00, 'Bank Transfer', 2950.00, 2, 1),
(3, '2026-02-25', 3950.00, 'Card', 3950.00, 3, 2),
(4, '2026-03-05', 1160.00, 'Cash', 1160.00, 4, 1),
(5, '2026-03-18', 1080.00, 'Bank Transfer', 1080.00, 5, 2),
(6, '2026-03-28', 1870.00, 'Cash', 1870.00, 6, 3),
(7, '2026-04-12', 1008.00, 'Card', 1008.00, 7, 1),
(8, '2026-04-28', 1940.00, 'Bank Transfer', 1940.00, 8, 2),
(9, '2026-05-03', 2465.00, 'Cheque', 2465.00, 9, 1),
(10, '2026-05-10', 2464.00, 'Cash', 2464.00, 10, 3);

-- =========================================================
-- 11. Sales Invoice Items
-- =========================================================

INSERT INTO SalesInvoiceItem
(sales_item_id, sales_invoice_id, product_id, selling_price, quantity, warranty_end_date) VALUES
(1, 1, 1, 850.00, 2, '2027-01-20'),
(2, 1, 3, 150.00, 5, '2027-01-20'),

(3, 2, 2, 700.00, 3, '2027-02-10'),
(4, 2, 5, 25.00, 20, NULL),
(5, 2, 8, 35.00, 10, '2026-08-10'),

(6, 3, 6, 1100.00, 2, '2028-02-25'),
(7, 3, 7, 350.00, 5, '2027-02-25'),

(8, 4, 9, 180.00, 4, '2027-03-05'),
(9, 4, 10, 220.00, 2, '2027-03-05'),

(10, 5, 11, 210.00, 3, '2027-03-18'),
(11, 5, 12, 45.00, 10, '2026-04-01'),

(12, 6, 1, 870.00, 1, '2027-03-28'),
(13, 6, 3, 160.00, 4, '2027-03-28'),
(14, 6, 5, 24.00, 15, NULL),

(15, 7, 4, 120.00, 5, '2027-04-12'),
(16, 7, 8, 34.00, 12, '2026-10-12'),

(17, 8, 2, 720.00, 2, '2027-04-28'),
(18, 8, 4, 125.00, 4, '2027-04-28'),

(19, 9, 6, 1150.00, 1, '2028-05-03'),
(20, 9, 7, 360.00, 3, '2027-05-03'),
(21, 9, 12, 47.00, 5, '2026-11-03'),

(22, 10, 1, 875.00, 2, '2027-05-10'),
(23, 10, 3, 158.00, 3, '2027-05-10'),
(24, 10, 5, 24.00, 10, NULL);

-- =========================================================
-- 12. Warehouse Transfers
-- =========================================================

INSERT INTO WarehouseTransfer
(transfer_id, transfer_date, from_warehouse_id, to_warehouse_id) VALUES
(1, '2026-04-15', 1, 2),
(2, '2026-05-05', 2, 3);

INSERT INTO WarehouseTransferItem
(transfer_item_id, transfer_id, product_id, quantity) VALUES
(1, 1, 1, 3),
(2, 1, 8, 5),
(3, 2, 12, 6),
(4, 2, 7, 4);

-- =========================================================
-- 13. Final Inventory
-- This represents the current stock after purchases, sales, and transfers.
-- =========================================================

INSERT INTO Inventory (product_id, warehouse_id, quantity, threshold) VALUES
-- Ramallah Central Warehouse
(1, 1, 15, 5),
(2, 1, 12, 5),
(3, 1, 25, 8),
(4, 1, 20, 6),
(5, 1, 80, 20),
(6, 1, 7, 3),
(7, 1, 12, 4),
(8, 1, 13, 10),
(9, 1, 21, 5),
(10, 1, 10, 3),
(12, 1, 25, 8),

-- Nablus Secondary Warehouse
(1, 2, 3, 5),
(2, 2, 10, 5),
(4, 2, 16, 6),
(6, 2, 16, 3),
(7, 2, 21, 4),
(8, 2, 40, 10),
(11, 2, 17, 5),
(12, 2, 24, 8),

-- Gaza Secondary Warehouse
(1, 3, 7, 5),
(3, 3, 8, 8),
(5, 3, 35, 20),
(7, 3, 4, 4),
(12, 3, 6, 8);

-- =========================================================
-- 14. Quick Test Queries
-- =========================================================

SELECT 'Sample data inserted successfully' AS message;

SELECT COUNT(*) AS total_categories FROM Category;
SELECT COUNT(*) AS total_brands FROM Brand;
SELECT COUNT(*) AS total_products FROM Product;
SELECT COUNT(*) AS total_suppliers FROM Supplier;
SELECT COUNT(*) AS total_clients FROM Client;
SELECT COUNT(*) AS total_warehouses FROM Warehouse;
SELECT COUNT(*) AS total_purchase_invoices FROM PurchaseInvoice;
SELECT COUNT(*) AS total_sales_invoices FROM SalesInvoice;
SELECT COUNT(*) AS total_inventory_records FROM Inventory;

-- Current stock report
SELECT
    w.warehouse_name,
    p.product_name,
    i.quantity,
    i.threshold
FROM Inventory i
JOIN Warehouse w ON i.warehouse_id = w.warehouse_id
JOIN Product p ON i.product_id = p.product_id
ORDER BY w.warehouse_name, p.product_name;

-- Total sales by month
SELECT
    YEAR(si.invoice_date) AS sales_year,
    MONTH(si.invoice_date) AS sales_month,
    SUM(sii.quantity * sii.selling_price) AS total_sales
FROM SalesInvoice si
JOIN SalesInvoiceItem sii ON si.sales_invoice_id = sii.sales_invoice_id
GROUP BY YEAR(si.invoice_date), MONTH(si.invoice_date)
ORDER BY sales_year, sales_month;
