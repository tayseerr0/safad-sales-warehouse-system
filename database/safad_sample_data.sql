USE safad_db;

-- =========================================================
-- SAFAD Sales and Warehouse Management System
-- Moderate realistic sample data
-- =========================================================

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE WarehouseTransferItem;
TRUNCATE TABLE WarehouseTransfer;
TRUNCATE TABLE SalesInvoiceItem;
TRUNCATE TABLE SalesPayment;
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
TRUNCATE TABLE AppUser;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- 1. App Users
-- Demo logins:
-- tayseer / admin123
-- ruaa / admin321
-- =========================================================

INSERT INTO AppUser (user_id, username, password_hash, password_salt, active, created_date) VALUES
(1, 'tayseer', '3po+laEjnDeXPisTLtoUGOn3X5Fl9HKLIxz8mmeM+FU=', 'ZWgoKrR+s0h5TrZce96GsQ==', TRUE, CURRENT_DATE),
(2, 'ruaa', 'FykG3XToynOPzl+nU4mOumYSmpUlnSf30tStNSLrk68=', 'SosKJIytpQd5K1DTYv2Kow==', TRUE, CURRENT_DATE);

-- =========================================================
-- 2. Categories
-- =========================================================

INSERT INTO Category (category_id, category_name, description, category_type) VALUES
(1, 'Laptops', 'Portable computers for business and education teams', 'Computers'),
(2, 'Networking Devices', 'Routers, switches, access points, and firewall appliances', 'Network'),
(3, 'Cables and Accessories', 'Cables, adapters, docks, webcams, and desk accessories', 'Accessories'),
(4, 'Power and UPS', 'UPS units, surge protection, and power continuity products', 'Power'),
(5, 'Security and Software', 'Licenses and security subscriptions', 'Software'),
(6, 'Peripherals and Printers', 'Displays, keyboards, webcams, and printers', 'Peripherals');

-- =========================================================
-- 3. Brands
-- =========================================================

INSERT INTO Brand (brand_id, brand_name, description) VALUES
(1, 'HP', 'Business computers, printers, and accessories'),
(2, 'Cisco', 'Enterprise and small business networking'),
(3, 'APC', 'Power protection and UPS systems'),
(4, 'Microsoft', 'Productivity and server software'),
(5, 'Sophos', 'Security and firewall solutions'),
(6, 'R&M', 'Cabling and network infrastructure'),
(7, 'Lenovo', 'Business laptops and workstations'),
(8, 'Dell', 'Business laptops and monitors'),
(9, 'Ubiquiti', 'Wireless and network equipment'),
(10, 'Logitech', 'Video and desktop peripherals');

-- =========================================================
-- 4. Clients
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
(10, 'Jenin Network Shop', '0599001010', 'info@jns.ps', '2026-04-01', 'Jenin', 'Cinema Street', 'Reseller'),
(11, 'Jericho Municipal IT Unit', '0599001011', 'it@jericho-muni.ps', '2026-04-07', 'Jericho', 'City Hall', 'Company'),
(12, 'An-Najah Engineering Lab', '0599001012', 'labs@najah.ps', '2026-04-16', 'Nablus', 'Campus Road', 'Company'),
(13, 'Bethlehem Smart Retail', '0599001013', 'orders@bsr.ps', '2026-04-22', 'Bethlehem', 'Manger Street', 'Reseller'),
(14, 'Lina Mansour', '0599001014', 'lina.mansour@email.com', '2026-05-01', 'Ramallah', 'Al-Tireh', 'Individual'),
(15, 'Qalqilya Office Hub', '0599001015', 'procurement@qoh.ps', '2026-05-09', 'Qalqilya', 'Main Market', 'Company'),
(16, 'Rafah Network Services', '0599001016', 'support@rns.ps', '2026-05-18', 'Rafah', 'Commercial Center', 'Company'),
(17, 'Tubas Computer Point', '0599001017', 'sales@tcp.ps', '2026-05-29', 'Tubas', 'Central Street', 'Reseller'),
(18, 'Omar Darwish', '0599001018', 'omar.darwish@email.com', '2026-06-03', 'Hebron', 'University Area', 'Individual');

-- =========================================================
-- 5. Suppliers
-- =========================================================

INSERT INTO Supplier (supplier_id, supplier_name, phone, email, starting_date, city, country, address) VALUES
(1, 'HP Middle East Distributor', '022401001', 'orders@hpme.example', '2024-01-10', 'Dubai', 'UAE', 'Business Bay'),
(2, 'Cisco Authorized Partner', '022401002', 'supply@cisco-partner.example', '2024-03-15', 'Amman', 'Jordan', 'Mecca Street'),
(3, 'APC Power Systems Supplier', '022401003', 'sales@apc-supplier.example', '2024-05-20', 'Ramallah', 'Palestine', 'Industrial Zone'),
(4, 'Microsoft Licensing Provider', '022401004', 'licenses@microsoft-provider.example', '2024-07-01', 'Jerusalem', 'Palestine', 'Technology Park'),
(5, 'R&M Cabling Distributor', '022401005', 'orders@rm-cabling.example', '2024-08-10', 'Nablus', 'Palestine', 'Industrial Area'),
(6, 'Levant Business Hardware', '022401006', 'quotes@levant-hardware.example', '2024-09-02', 'Amman', 'Jordan', 'Gardens Street'),
(7, 'Gulf Peripheral Trading', '022401007', 'sales@gulf-peripherals.example', '2024-10-12', 'Dubai', 'UAE', 'Deira'),
(8, 'PalTech Wholesale', '022401008', 'supply@paltech-wholesale.example', '2024-11-03', 'Hebron', 'Palestine', 'Industrial Road');

-- =========================================================
-- 6. Warehouses
-- =========================================================

INSERT INTO Warehouse (warehouse_id, warehouse_name, location, capacity) VALUES
(1, 'Ramallah Central Warehouse', 'Ramallah - Industrial Zone', 5000),
(2, 'Nablus Secondary Warehouse', 'Nablus - Rafidia', 2500),
(3, 'Gaza Secondary Warehouse', 'Gaza - Al-Rimal', 2000);

-- =========================================================
-- 7. Products
-- =========================================================

INSERT INTO Product (product_id, product_name, description, default_selling_price, brand_id, category_id) VALUES
(1, 'HP ProBook 450 G10', 'Business laptop, Intel i5, 16GB RAM, 512GB SSD', 850.00, 1, 1),
(2, 'HP EliteBook 840 G9', 'Premium business laptop, Intel i7, 16GB RAM, 1TB SSD', 1250.00, 1, 1),
(3, 'Lenovo ThinkPad E14', 'Durable business laptop, Ryzen 5, 16GB RAM', 760.00, 7, 1),
(4, 'Dell Latitude 5440', 'Corporate laptop, Intel i5, 16GB RAM, 512GB SSD', 890.00, 8, 1),
(5, 'Cisco ISR Router 1100', 'Enterprise branch router', 150.00, 2, 2),
(6, 'Cisco Catalyst Switch 24-Port', 'Managed 24-port gigabit switch', 120.00, 2, 2),
(7, 'Ubiquiti UniFi Access Point', 'Wi-Fi 6 access point for office deployments', 115.00, 9, 2),
(8, 'Sophos XGS Firewall 87', 'Small business firewall appliance', 350.00, 5, 2),
(9, 'R&M Cat6 Cable Box', '305m Cat6 network cable box', 25.00, 6, 3),
(10, 'R&M Patch Panel 24-Port', 'Network patch panel for racks', 45.00, 6, 3),
(11, 'USB-C Docking Adapter', 'Multi-port USB-C docking adapter', 35.00, 1, 3),
(12, 'Logitech Wireless Keyboard', 'Wireless keyboard and mouse desktop combo', 45.00, 10, 6),
(13, 'Logitech HD Webcam', 'HD webcam for classrooms and meetings', 60.00, 10, 6),
(14, 'APC Smart-UPS 1500VA', 'Power backup unit for servers and network equipment', 1100.00, 3, 4),
(15, 'APC Surge Protector', 'Power surge protection device', 155.00, 3, 4),
(16, 'Microsoft 365 Business License', 'Annual productivity software license', 180.00, 4, 5),
(17, 'Windows Server License', 'Server operating system license', 220.00, 4, 5),
(18, 'HP LaserJet Pro M404', 'Monochrome laser printer for offices', 260.00, 1, 6),
(19, 'Dell 24-inch Monitor', 'Business monitor with HDMI and DisplayPort', 185.00, 8, 6),
(20, 'Cisco Small Business Switch 8-Port', 'Compact managed switch for small branches', 72.00, 2, 2);

-- =========================================================
-- 8. Supplier-Product Relationships
-- =========================================================

INSERT INTO SupplierProduct (supplier_id, product_id, supply_price) VALUES
(1, 1, 650.00),
(1, 2, 940.00),
(1, 11, 18.00),
(1, 18, 168.00),
(2, 5, 95.00),
(2, 6, 76.00),
(2, 8, 245.00),
(2, 20, 46.00),
(3, 14, 845.00),
(3, 15, 95.00),
(3, 8, 240.00),
(4, 16, 110.00),
(4, 17, 135.00),
(5, 9, 12.00),
(5, 10, 28.00),
(5, 20, 45.00),
(6, 1, 660.00),
(6, 2, 930.00),
(6, 3, 540.00),
(6, 4, 615.00),
(6, 19, 116.00),
(7, 12, 22.00),
(7, 13, 31.00),
(7, 19, 114.00),
(7, 11, 17.00),
(8, 3, 535.00),
(8, 4, 610.00),
(8, 5, 96.00),
(8, 6, 75.00),
(8, 7, 71.00),
(8, 9, 13.00),
(8, 10, 27.00),
(8, 12, 21.00),
(3, 7, 72.00),
(5, 8, 238.00);

-- =========================================================
-- 9. Purchase Invoices
-- =========================================================

INSERT INTO PurchaseInvoice
(purchase_invoice_id, invoice_date, estimated_arrival, payment, payment_type, amount, supplier_id, warehouse_id) VALUES
(1, '2026-01-04', '2026-01-09', 15770.00, 'Bank Transfer', 15770.00, 1, 1),
(2, '2026-01-09', '2026-01-14', 1500.00, 'Bank Transfer', 2972.00, 2, 1),
(3, '2026-01-16', '2026-01-22', 8000.00, 'Cheque', 10580.00, 3, 2),
(4, '2026-01-25', '2026-01-30', 3980.00, 'Bank Transfer', 3980.00, 4, 1),
(5, '2026-02-03', '2026-02-08', 0.00, 'Cash', 2286.00, 5, 2),
(6, '2026-02-12', '2026-02-18', 5000.00, 'Bank Transfer', 8750.00, 6, 3),
(7, '2026-02-24', '2026-03-02', 7600.00, 'Card', 7600.00, 6, 2),
(8, '2026-03-04', '2026-03-10', 3000.00, 'Bank Transfer', 4740.00, 8, 1),
(9, '2026-03-13', '2026-03-19', 6194.00, 'Cheque', 6194.00, 3, 3),
(10, '2026-03-27', '2026-04-02', 10080.00, 'Bank Transfer', 10080.00, 6, 1),
(11, '2026-04-05', '2026-04-10', 1000.00, 'Cash', 2520.00, 2, 2),
(12, '2026-04-16', '2026-04-22', 3460.00, 'Bank Transfer', 3460.00, 4, 3),
(13, '2026-04-28', '2026-05-03', 2440.00, 'Card', 2440.00, 7, 1),
(14, '2026-05-06', '2026-05-12', 3500.00, 'Bank Transfer', 6853.00, 8, 2),
(15, '2026-05-14', '2026-05-21', 0.00, 'Cheque', 2506.00, 8, 3),
(16, '2026-05-23', '2026-05-28', 5842.00, 'Bank Transfer', 5842.00, 3, 1),
(17, '2026-06-05', '2026-06-11', 7930.00, 'Bank Transfer', 7930.00, 6, 2),
(18, '2026-06-13', '2026-06-18', 1200.00, 'Cash', 2029.00, 8, 3);

-- =========================================================
-- 10. Purchase Invoice Items
-- =========================================================

INSERT INTO PurchaseInvoiceItem
(purchase_item_id, purchase_invoice_id, product_id, purchase_price, quantity) VALUES
(1, 1, 1, 650.00, 12),
(2, 1, 2, 940.00, 8),
(3, 1, 11, 18.00, 25),
(4, 2, 5, 95.00, 16),
(5, 2, 6, 76.00, 12),
(6, 2, 9, 12.00, 45),
(7, 3, 14, 845.00, 8),
(8, 3, 15, 95.00, 20),
(9, 3, 8, 240.00, 8),
(10, 4, 16, 110.00, 22),
(11, 4, 17, 135.00, 8),
(12, 4, 13, 32.00, 15),
(13, 5, 9, 13.00, 60),
(14, 5, 10, 29.00, 24),
(15, 5, 20, 45.00, 18),
(16, 6, 1, 665.00, 6),
(17, 6, 3, 540.00, 8),
(18, 6, 12, 22.00, 20),
(19, 7, 4, 620.00, 7),
(20, 7, 19, 115.00, 18),
(21, 7, 18, 170.00, 7),
(22, 8, 7, 72.00, 20),
(23, 8, 8, 238.00, 10),
(24, 8, 20, 46.00, 20),
(25, 9, 14, 850.00, 5),
(26, 9, 15, 96.00, 15),
(27, 9, 10, 28.00, 18),
(28, 10, 2, 930.00, 5),
(29, 10, 4, 615.00, 6),
(30, 10, 19, 116.00, 15),
(31, 11, 5, 94.00, 10),
(32, 11, 6, 74.00, 10),
(33, 11, 7, 70.00, 12),
(34, 12, 16, 108.00, 18),
(35, 12, 17, 133.00, 6),
(36, 12, 11, 17.00, 20),
(37, 12, 12, 21.00, 18),
(38, 13, 18, 168.00, 8),
(39, 13, 13, 31.00, 16),
(40, 13, 9, 12.00, 50),
(41, 14, 1, 655.00, 6),
(42, 14, 3, 535.00, 5),
(43, 14, 13, 31.00, 8),
(44, 15, 5, 96.00, 8),
(45, 15, 8, 242.00, 5),
(46, 15, 20, 44.00, 12),
(47, 16, 14, 842.00, 4),
(48, 16, 15, 94.00, 20),
(49, 16, 10, 27.00, 22),
(50, 17, 2, 935.00, 4),
(51, 17, 4, 610.00, 5),
(52, 17, 19, 114.00, 10),
(53, 18, 6, 75.00, 8),
(54, 18, 7, 71.00, 10),
(55, 18, 9, 13.00, 35),
(56, 18, 12, 22.00, 12);

-- =========================================================
-- 11. Sales Invoices
-- =========================================================

INSERT INTO SalesInvoice
(sales_invoice_id, invoice_date, payment, payment_type, amount, client_id, warehouse_id) VALUES
(1, '2026-01-14', 1875.00, 'Cash', 1875.00, 1, 1),
(2, '2026-01-21', 500.00, 'Bank Transfer', 950.00, 2, 1),
(3, '2026-01-29', 1720.00, 'Card', 1720.00, 3, 2),
(4, '2026-02-04', 0.00, 'Cheque', 1300.00, 4, 1),
(5, '2026-02-11', 1700.00, 'Bank Transfer', 1700.00, 5, 3),
(6, '2026-02-18', 1000.00, 'Bank Transfer', 1338.00, 6, 2),
(7, '2026-02-27', 1805.00, 'Card', 1805.00, 7, 1),
(8, '2026-03-03', 600.00, 'Cash', 1378.00, 8, 3),
(9, '2026-03-10', 2040.00, 'Bank Transfer', 2040.00, 9, 2),
(10, '2026-03-18', 1019.00, 'Cash', 1019.00, 10, 1),
(11, '2026-03-24', 700.00, 'Cheque', 1345.00, 11, 3),
(12, '2026-04-02', 2460.00, 'Bank Transfer', 2460.00, 12, 2),
(13, '2026-04-08', 1500.00, 'Card', 2290.00, 13, 1),
(14, '2026-04-13', 780.00, 'Cash', 780.00, 14, 3),
(15, '2026-04-19', 0.00, 'Bank Transfer', 2444.00, 15, 2),
(16, '2026-04-26', 1000.00, 'Cheque', 1533.00, 16, 1),
(17, '2026-05-04', 958.00, 'Cash', 958.00, 17, 3),
(18, '2026-05-09', 1950.00, 'Bank Transfer', 1950.00, 18, 2),
(19, '2026-05-15', 1200.00, 'Card', 1800.00, 1, 1),
(20, '2026-05-21', 1907.00, 'Bank Transfer', 1907.00, 5, 3),
(21, '2026-05-29', 504.00, 'Cash', 1004.00, 9, 2),
(22, '2026-06-04', 1735.00, 'Bank Transfer', 1735.00, 2, 1),
(23, '2026-06-11', 1000.00, 'Cheque', 1565.00, 7, 3),
(24, '2026-06-17', 1319.00, 'Card', 1319.00, 12, 2);

INSERT INTO SalesPayment
(sales_payment_id, sales_invoice_id, payment_date, amount, payment_type) VALUES
(1, 1, '2026-01-14', 1875.00, 'Cash'),
(2, 2, '2026-01-21', 500.00, 'Bank Transfer'),
(3, 3, '2026-01-29', 1720.00, 'Card'),
(4, 5, '2026-02-11', 900.00, 'Bank Transfer'),
(5, 5, '2026-02-25', 800.00, 'Bank Transfer'),
(6, 6, '2026-02-18', 1000.00, 'Bank Transfer'),
(7, 7, '2026-02-27', 1805.00, 'Card'),
(8, 8, '2026-03-03', 600.00, 'Cash'),
(9, 9, '2026-03-10', 2040.00, 'Bank Transfer'),
(10, 10, '2026-03-18', 1019.00, 'Cash'),
(11, 11, '2026-03-24', 700.00, 'Cheque'),
(12, 12, '2026-04-02', 1200.00, 'Bank Transfer'),
(13, 12, '2026-04-17', 1260.00, 'Bank Transfer'),
(14, 13, '2026-04-08', 1500.00, 'Card'),
(15, 14, '2026-04-13', 780.00, 'Cash'),
(16, 16, '2026-04-26', 1000.00, 'Cheque'),
(17, 17, '2026-05-04', 958.00, 'Cash'),
(18, 18, '2026-05-09', 1950.00, 'Bank Transfer'),
(19, 19, '2026-05-15', 1200.00, 'Card'),
(20, 20, '2026-05-21', 900.00, 'Bank Transfer'),
(21, 20, '2026-06-01', 1007.00, 'Bank Transfer'),
(22, 21, '2026-05-29', 504.00, 'Cash'),
(23, 22, '2026-06-04', 1735.00, 'Bank Transfer'),
(24, 23, '2026-06-11', 1000.00, 'Cheque'),
(25, 24, '2026-06-17', 1319.00, 'Card');

-- =========================================================
-- 12. Sales Invoice Items
-- =========================================================

INSERT INTO SalesInvoiceItem
(sales_item_id, sales_invoice_id, product_id, selling_price, quantity, warranty_end_date) VALUES
(1, 1, 1, 850.00, 2, '2027-01-14'),
(2, 1, 11, 35.00, 5, '2026-07-14'),
(3, 2, 5, 150.00, 3, '2027-01-21'),
(4, 2, 6, 125.00, 2, '2027-01-21'),
(5, 2, 9, 25.00, 10, NULL),
(6, 3, 14, 1100.00, 1, '2028-01-29'),
(7, 3, 15, 155.00, 4, '2027-01-29'),
(8, 4, 16, 180.00, 6, '2027-02-04'),
(9, 4, 17, 220.00, 1, '2027-02-04'),
(10, 5, 3, 760.00, 2, '2027-02-11'),
(11, 5, 12, 45.00, 4, '2026-08-11'),
(12, 6, 8, 350.00, 3, '2027-02-18'),
(13, 6, 20, 72.00, 4, '2027-02-18'),
(14, 7, 2, 1250.00, 1, '2027-02-27'),
(15, 7, 19, 185.00, 3, '2027-02-27'),
(16, 8, 1, 870.00, 1, '2027-03-03'),
(17, 8, 5, 158.00, 2, '2027-03-03'),
(18, 8, 9, 24.00, 8, NULL),
(19, 9, 4, 890.00, 2, '2027-03-10'),
(20, 9, 18, 260.00, 1, '2027-03-10'),
(21, 10, 7, 115.00, 5, '2027-03-18'),
(22, 10, 13, 60.00, 4, '2026-09-18'),
(23, 10, 11, 34.00, 6, '2026-09-18'),
(24, 11, 14, 1120.00, 1, '2028-03-24'),
(25, 11, 10, 45.00, 5, NULL),
(26, 12, 3, 745.00, 2, '2027-04-02'),
(27, 12, 15, 150.00, 5, '2027-04-02'),
(28, 12, 12, 44.00, 5, '2026-10-02'),
(29, 13, 18, 255.00, 2, '2027-04-08'),
(30, 13, 16, 178.00, 10, '2027-04-08'),
(31, 14, 6, 120.00, 3, '2027-04-13'),
(32, 14, 20, 70.00, 6, '2027-04-13'),
(33, 15, 1, 860.00, 2, '2027-04-19'),
(34, 15, 7, 112.00, 4, '2027-04-19'),
(35, 15, 9, 23.00, 12, NULL),
(36, 16, 4, 880.00, 1, '2027-04-26'),
(37, 16, 5, 155.00, 3, '2027-04-26'),
(38, 16, 10, 47.00, 4, NULL),
(39, 17, 17, 218.00, 2, '2027-05-04'),
(40, 17, 11, 33.00, 8, '2026-11-04'),
(41, 17, 12, 43.00, 6, '2026-11-04'),
(42, 18, 2, 1230.00, 1, '2027-05-09'),
(43, 18, 19, 180.00, 4, '2027-05-09'),
(44, 19, 8, 345.00, 2, '2027-05-15'),
(45, 19, 14, 1110.00, 1, '2028-05-15'),
(46, 20, 3, 755.00, 1, '2027-05-21'),
(47, 20, 15, 152.00, 6, '2027-05-21'),
(48, 20, 9, 24.00, 10, NULL),
(49, 21, 6, 122.00, 3, '2027-05-29'),
(50, 21, 7, 116.00, 3, '2027-05-29'),
(51, 21, 13, 58.00, 5, '2026-11-29'),
(52, 22, 1, 855.00, 1, '2027-06-04'),
(53, 22, 18, 258.00, 2, '2027-06-04'),
(54, 22, 19, 182.00, 2, '2027-06-04'),
(55, 23, 5, 160.00, 2, '2027-06-11'),
(56, 23, 20, 74.00, 5, '2027-06-11'),
(57, 23, 16, 175.00, 5, '2027-06-11'),
(58, 24, 4, 895.00, 1, '2027-06-17'),
(59, 24, 10, 48.00, 6, NULL),
(60, 24, 11, 34.00, 4, '2026-12-17');

-- =========================================================
-- 13. Warehouse Transfers
-- =========================================================

INSERT INTO WarehouseTransfer
(transfer_id, transfer_date, from_warehouse_id, to_warehouse_id) VALUES
(1, '2026-02-20', 1, 2),
(2, '2026-03-15', 2, 3),
(3, '2026-04-09', 1, 3),
(4, '2026-05-01', 3, 2),
(5, '2026-05-20', 2, 1),
(6, '2026-06-08', 1, 2);

INSERT INTO WarehouseTransferItem
(transfer_item_id, transfer_id, product_id, quantity) VALUES
(1, 1, 1, 3),
(2, 1, 11, 6),
(3, 1, 7, 4),
(4, 2, 14, 2),
(5, 2, 15, 5),
(6, 3, 5, 4),
(7, 3, 6, 3),
(8, 3, 9, 10),
(9, 4, 3, 2),
(10, 4, 12, 5),
(11, 5, 4, 2),
(12, 5, 19, 4),
(13, 5, 20, 3),
(14, 6, 16, 5),
(15, 6, 18, 2);

-- =========================================================
-- 14. Final Inventory
-- Current stock after purchases, sales, and transfers.
-- =========================================================

INSERT INTO Inventory (product_id, warehouse_id, quantity, threshold) VALUES
-- Ramallah Central Warehouse
(1, 1, 6, 5),
(2, 1, 12, 4),
(4, 1, 7, 4),
(5, 1, 6, 8),
(6, 1, 7, 5),
(7, 1, 11, 5),
(8, 1, 8, 3),
(9, 1, 75, 20),
(10, 1, 18, 8),
(11, 1, 8, 6),
(13, 1, 27, 5),
(14, 1, 3, 4),
(15, 1, 20, 8),
(16, 1, 1, 3),
(17, 1, 7, 3),
(18, 1, 2, 4),
(19, 1, 14, 5),
(20, 1, 23, 4),

-- Nablus Secondary Warehouse
(1, 2, 7, 5),
(2, 2, 3, 4),
(3, 2, 5, 4),
(4, 2, 7, 4),
(5, 2, 10, 8),
(6, 2, 7, 5),
(7, 2, 9, 5),
(8, 2, 5, 3),
(9, 2, 48, 20),
(10, 2, 18, 8),
(11, 2, 2, 6),
(13, 2, 3, 5),
(14, 2, 5, 4),
(15, 2, 6, 8),
(16, 2, 5, 3),
(18, 2, 8, 4),
(19, 2, 20, 5),
(20, 2, 11, 4),

-- Gaza Secondary Warehouse
(1, 3, 5, 5),
(3, 3, 3, 4),
(5, 3, 8, 8),
(6, 3, 8, 5),
(7, 3, 10, 5),
(8, 3, 5, 3),
(9, 3, 27, 20),
(10, 3, 13, 8),
(11, 3, 12, 6),
(12, 3, 35, 8),
(14, 3, 6, 4),
(15, 3, 14, 8),
(16, 3, 13, 3),
(17, 3, 4, 3),
(20, 3, 1, 4);

-- =========================================================
-- 15. Quick Test Queries
-- =========================================================

SELECT 'Sample data inserted successfully' AS message;

SELECT COUNT(*) AS total_categories FROM Category;
SELECT COUNT(*) AS total_app_users FROM AppUser;
SELECT COUNT(*) AS total_brands FROM Brand;
SELECT COUNT(*) AS total_products FROM Product;
SELECT COUNT(*) AS total_suppliers FROM Supplier;
SELECT COUNT(*) AS total_clients FROM Client;
SELECT COUNT(*) AS total_warehouses FROM Warehouse;
SELECT COUNT(*) AS total_purchase_invoices FROM PurchaseInvoice;
SELECT COUNT(*) AS total_purchase_items FROM PurchaseInvoiceItem;
SELECT COUNT(*) AS total_sales_invoices FROM SalesInvoice;
SELECT COUNT(*) AS total_sales_items FROM SalesInvoiceItem;
SELECT COUNT(*) AS total_sales_payments FROM SalesPayment;
SELECT COUNT(*) AS total_transfers FROM WarehouseTransfer;
SELECT COUNT(*) AS total_transfer_items FROM WarehouseTransferItem;
SELECT COUNT(*) AS total_inventory_records FROM Inventory;

-- Purchase invoice totals must match their line totals.
SELECT
    pi.purchase_invoice_id,
    pi.amount AS invoice_amount,
    ROUND(SUM(pii.quantity * pii.purchase_price), 2) AS line_amount
FROM PurchaseInvoice pi
JOIN PurchaseInvoiceItem pii ON pi.purchase_invoice_id = pii.purchase_invoice_id
GROUP BY pi.purchase_invoice_id, pi.amount
HAVING pi.amount <> ROUND(SUM(pii.quantity * pii.purchase_price), 2);

-- Sales invoice totals must match their line totals.
SELECT
    si.sales_invoice_id,
    si.amount AS invoice_amount,
    ROUND(SUM(sii.quantity * sii.selling_price), 2) AS line_amount
FROM SalesInvoice si
JOIN SalesInvoiceItem sii ON si.sales_invoice_id = sii.sales_invoice_id
GROUP BY si.sales_invoice_id, si.amount
HAVING si.amount <> ROUND(SUM(sii.quantity * sii.selling_price), 2);

-- Sales invoice payment field must match inserted payment rows.
SELECT
    si.sales_invoice_id,
    si.payment AS invoice_payment,
    COALESCE(ROUND(SUM(sp.amount), 2), 0) AS payment_rows
FROM SalesInvoice si
LEFT JOIN SalesPayment sp ON si.sales_invoice_id = sp.sales_invoice_id
GROUP BY si.sales_invoice_id, si.payment
HAVING si.payment <> COALESCE(ROUND(SUM(sp.amount), 2), 0);

-- Inventory must never be negative.
SELECT *
FROM Inventory
WHERE quantity < 0;

-- Current low stock report.
SELECT
    w.warehouse_name,
    p.product_name,
    i.quantity,
    i.threshold
FROM Inventory i
JOIN Warehouse w ON i.warehouse_id = w.warehouse_id
JOIN Product p ON i.product_id = p.product_id
WHERE i.quantity < i.threshold
ORDER BY w.warehouse_name, p.product_name;

-- Total sales by month.
SELECT
    YEAR(si.invoice_date) AS sales_year,
    MONTH(si.invoice_date) AS sales_month,
    SUM(sii.quantity * sii.selling_price) AS total_sales
FROM SalesInvoice si
JOIN SalesInvoiceItem sii ON si.sales_invoice_id = sii.sales_invoice_id
GROUP BY YEAR(si.invoice_date), MONTH(si.invoice_date)
ORDER BY sales_year, sales_month;
