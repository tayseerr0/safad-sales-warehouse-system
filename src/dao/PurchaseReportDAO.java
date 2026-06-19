package dao;

import db.DBConnection;

import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PurchaseReportDAO {

    public DefaultTableModel getProductsWithCategoryAndBrand() {
        String sql = """
                SELECT p.product_id AS 'Product ID',
                       p.product_name AS 'Product',
                       c.category_name AS 'Category',
                       b.brand_name AS 'Brand',
                       p.default_selling_price AS 'Default Selling Price'
                FROM Product p
                JOIN Category c ON p.category_id = c.category_id
                JOIN Brand b ON p.brand_id = b.brand_id
                ORDER BY c.category_name, b.brand_name, p.product_name
                """;

        return runQuery(sql);
    }

    public DefaultTableModel getProductsInWarehouse(int warehouseId) {
        String sql = """
                SELECT w.warehouse_name AS 'Warehouse',
                       p.product_id AS 'Product ID',
                       p.product_name AS 'Product',
                       c.category_name AS 'Category',
                       b.brand_name AS 'Brand',
                       i.quantity AS 'Quantity',
                       i.threshold AS 'Threshold'
                FROM Inventory i
                JOIN Product p ON i.product_id = p.product_id
                JOIN Warehouse w ON i.warehouse_id = w.warehouse_id
                JOIN Category c ON p.category_id = c.category_id
                JOIN Brand b ON p.brand_id = b.brand_id
                WHERE i.warehouse_id = ?
                ORDER BY p.product_name
                """;

        return runQuery(sql, warehouseId);
    }

    public DefaultTableModel getSuppliersForProduct(int productId) {
        String sql = """
                SELECT p.product_name AS 'Product',
                       s.supplier_id AS 'Supplier ID',
                       s.supplier_name AS 'Supplier',
                       s.phone AS 'Phone',
                       s.email AS 'Email',
                       s.country AS 'Country',
                       sp.supply_price AS 'Supply Price'
                FROM SupplierProduct sp
                JOIN Supplier s ON sp.supplier_id = s.supplier_id
                JOIN Product p ON sp.product_id = p.product_id
                WHERE sp.product_id = ?
                ORDER BY sp.supply_price, s.supplier_name
                """;

        return runQuery(sql, productId);
    }

    public DefaultTableModel getCheapestSupplierForEachProduct() {
        String sql = """
                SELECT p.product_id AS 'Product ID',
                       p.product_name AS 'Product',
                       s.supplier_id AS 'Supplier ID',
                       s.supplier_name AS 'Cheapest Supplier',
                       s.country AS 'Country',
                       sp.supply_price AS 'Cheapest Supply Price',
                       ROUND(price_summary.average_supply_price, 2) AS 'Average Supplier Price',
                       ROUND(price_summary.average_supply_price - sp.supply_price, 2) AS 'Savings vs Average'
                FROM SupplierProduct sp
                JOIN Product p ON sp.product_id = p.product_id
                JOIN Supplier s ON sp.supplier_id = s.supplier_id
                JOIN (
                    SELECT product_id,
                           MIN(supply_price) AS cheapest_supply_price,
                           AVG(supply_price) AS average_supply_price
                    FROM SupplierProduct
                    GROUP BY product_id
                ) price_summary ON sp.product_id = price_summary.product_id
                                AND sp.supply_price = price_summary.cheapest_supply_price
                ORDER BY p.product_name, s.supplier_name
                """;

        return runQuery(sql);
    }

    public DefaultTableModel getPurchaseInvoicesBySupplierAndDate(int supplierId,
                                                                  LocalDate fromDate,
                                                                  LocalDate toDate) {
        String sql = """
                SELECT pi.purchase_invoice_id AS 'Invoice ID',
                       pi.invoice_date AS 'Invoice Date',
                       pi.estimated_arrival AS 'Estimated Arrival',
                       s.supplier_name AS 'Supplier',
                       w.warehouse_name AS 'Warehouse',
                       pi.payment_type AS 'Payment Type',
                       pi.payment AS 'Payment',
                       pi.amount AS 'Amount'
                FROM PurchaseInvoice pi
                JOIN Supplier s ON pi.supplier_id = s.supplier_id
                JOIN Warehouse w ON pi.warehouse_id = w.warehouse_id
                WHERE pi.supplier_id = ?
                  AND pi.invoice_date BETWEEN ? AND ?
                ORDER BY pi.invoice_date DESC, pi.purchase_invoice_id DESC
                """;

        return runQuery(sql, supplierId, Date.valueOf(fromDate), Date.valueOf(toDate));
    }

    public DefaultTableModel getPurchaseInvoiceDetails(int invoiceId) {
        String sql = """
                SELECT pi.purchase_invoice_id AS 'Invoice ID',
                       pi.invoice_date AS 'Invoice Date',
                       s.supplier_name AS 'Supplier',
                       w.warehouse_name AS 'Warehouse',
                       p.product_name AS 'Product',
                       pii.quantity AS 'Quantity',
                       pii.purchase_price AS 'Purchase Price',
                       (pii.quantity * pii.purchase_price) AS 'Line Total'
                FROM PurchaseInvoiceItem pii
                JOIN PurchaseInvoice pi ON pii.purchase_invoice_id = pi.purchase_invoice_id
                JOIN Supplier s ON pi.supplier_id = s.supplier_id
                JOIN Warehouse w ON pi.warehouse_id = w.warehouse_id
                JOIN Product p ON pii.product_id = p.product_id
                WHERE pi.purchase_invoice_id = ?
                ORDER BY pii.purchase_item_id
                """;

        return runQuery(sql, invoiceId);
    }

    public DefaultTableModel getTotalQuantityPurchasedPerProduct() {
        String sql = """
                SELECT p.product_id AS 'Product ID',
                       p.product_name AS 'Product',
                       c.category_name AS 'Category',
                       b.brand_name AS 'Brand',
                       SUM(pii.quantity) AS 'Total Quantity Purchased'
                FROM PurchaseInvoiceItem pii
                JOIN Product p ON pii.product_id = p.product_id
                JOIN Category c ON p.category_id = c.category_id
                JOIN Brand b ON p.brand_id = b.brand_id
                GROUP BY p.product_id, p.product_name, c.category_name, b.brand_name
                ORDER BY SUM(pii.quantity) DESC
                """;

        return runQuery(sql);
    }

    public DefaultTableModel getTotalPurchaseAmountPerProduct() {
        String sql = """
                SELECT p.product_id AS 'Product ID',
                       p.product_name AS 'Product',
                       c.category_name AS 'Category',
                       b.brand_name AS 'Brand',
                       SUM(pii.quantity) AS 'Total Quantity Purchased',
                       ROUND(SUM(pii.quantity * pii.purchase_price), 2) AS 'Total Purchase Amount'
                FROM PurchaseInvoiceItem pii
                JOIN Product p ON pii.product_id = p.product_id
                JOIN Category c ON p.category_id = c.category_id
                JOIN Brand b ON p.brand_id = b.brand_id
                GROUP BY p.product_id, p.product_name, c.category_name, b.brand_name
                ORDER BY SUM(pii.quantity * pii.purchase_price) DESC
                """;

        return runQuery(sql);
    }

    public DefaultTableModel getCurrentStockByWarehouse() {
        String sql = """
                SELECT w.warehouse_name AS 'Warehouse',
                       p.product_id AS 'Product ID',
                       p.product_name AS 'Product',
                       c.category_name AS 'Category',
                       b.brand_name AS 'Brand',
                       i.quantity AS 'Current Quantity',
                       i.threshold AS 'Threshold'
                FROM Inventory i
                JOIN Warehouse w ON i.warehouse_id = w.warehouse_id
                JOIN Product p ON i.product_id = p.product_id
                JOIN Category c ON p.category_id = c.category_id
                JOIN Brand b ON p.brand_id = b.brand_id
                ORDER BY w.warehouse_name, p.product_name
                """;

        return runQuery(sql);
    }

    public DefaultTableModel getLowStockProducts(int warehouseId) {
        String sql = """
                SELECT w.warehouse_name AS 'Warehouse',
                       p.product_id AS 'Product ID',
                       p.product_name AS 'Product',
                       c.category_name AS 'Category',
                       b.brand_name AS 'Brand',
                       i.quantity AS 'Current Quantity',
                       i.threshold AS 'Threshold'
                FROM Inventory i
                JOIN Warehouse w ON i.warehouse_id = w.warehouse_id
                JOIN Product p ON i.product_id = p.product_id
                JOIN Category c ON p.category_id = c.category_id
                JOIN Brand b ON p.brand_id = b.brand_id
                WHERE i.warehouse_id = ?
                  AND i.quantity < i.threshold
                ORDER BY i.quantity, p.product_name
                """;

        return runQuery(sql, warehouseId);
    }

    public DefaultTableModel getTotalPurchaseAmountPerSupplier() {
        String sql = """
                SELECT s.supplier_id AS 'Supplier ID',
                       s.supplier_name AS 'Supplier',
                       s.country AS 'Country',
                       COUNT(DISTINCT pi.purchase_invoice_id) AS 'Invoice Count',
                       SUM(pii.quantity) AS 'Total Quantity',
                       ROUND(SUM(pii.quantity * pii.purchase_price), 2) AS 'Total Purchase Amount'
                FROM Supplier s
                JOIN PurchaseInvoice pi ON s.supplier_id = pi.supplier_id
                JOIN PurchaseInvoiceItem pii ON pi.purchase_invoice_id = pii.purchase_invoice_id
                GROUP BY s.supplier_id, s.supplier_name, s.country
                ORDER BY SUM(pii.quantity * pii.purchase_price) DESC
                """;

        return runQuery(sql);
    }

    public DefaultTableModel getTotalPurchaseAmountPerSupplierBetweenDates(LocalDate fromDate, LocalDate toDate) {
        String sql = """
                SELECT s.supplier_id AS 'Supplier ID',
                       s.supplier_name AS 'Supplier',
                       s.city AS 'City',
                       s.country AS 'Country',
                       COUNT(DISTINCT pi.purchase_invoice_id) AS 'Invoice Count',
                       SUM(pii.quantity) AS 'Total Quantity',
                       ROUND(SUM(pii.quantity * pii.purchase_price), 2) AS 'Total Purchase Amount'
                FROM Supplier s
                JOIN PurchaseInvoice pi ON s.supplier_id = pi.supplier_id
                JOIN PurchaseInvoiceItem pii ON pi.purchase_invoice_id = pii.purchase_invoice_id
                WHERE pi.invoice_date BETWEEN ? AND ?
                GROUP BY s.supplier_id, s.supplier_name, s.city, s.country
                ORDER BY SUM(pii.quantity * pii.purchase_price) DESC
                """;

        return runQuery(sql, Date.valueOf(fromDate), Date.valueOf(toDate));
    }

    public DefaultTableModel getPurchaseAmountByMonth() {
        String sql = """
                SELECT DATE_FORMAT(pi.invoice_date, '%Y-%m') AS 'Month',
                       COUNT(DISTINCT pi.purchase_invoice_id) AS 'Invoice Count',
                       SUM(pii.quantity) AS 'Total Quantity',
                       ROUND(SUM(pii.quantity * pii.purchase_price), 2) AS 'Total Purchase Amount'
                FROM PurchaseInvoice pi
                JOIN PurchaseInvoiceItem pii ON pi.purchase_invoice_id = pii.purchase_invoice_id
                GROUP BY DATE_FORMAT(pi.invoice_date, '%Y-%m')
                ORDER BY DATE_FORMAT(pi.invoice_date, '%Y-%m')
                """;

        return runQuery(sql);
    }

    public DefaultTableModel getHighestDemandAndSupplyProducts() {
        String sql = """
                SELECT p.product_id AS 'Product ID',
                       p.product_name AS 'Product',
                       COALESCE(sales.total_sold, 0) AS 'Total Sold',
                       COALESCE(purchases.total_bought, 0) AS 'Total Bought',
                       CASE
                           WHEN COALESCE(purchases.total_bought, 0) > 0
                           THEN ROUND((COALESCE(sales.total_sold, 0) / COALESCE(purchases.total_bought, 0)) * 100, 2)
                           ELSE 0
                       END AS 'Demand / Supply %'
                FROM Product p
                LEFT JOIN (
                    SELECT product_id, SUM(quantity) AS total_sold
                    FROM SalesInvoiceItem
                    GROUP BY product_id
                ) sales ON p.product_id = sales.product_id
                LEFT JOIN (
                    SELECT product_id, SUM(quantity) AS total_bought
                    FROM PurchaseInvoiceItem
                    GROUP BY product_id
                ) purchases ON p.product_id = purchases.product_id
                ORDER BY `Demand / Supply %` DESC,
                         COALESCE(sales.total_sold, 0) DESC,
                         p.product_name
                """;

        return runQuery(sql);
    }

    public DefaultTableModel getAverageSellingPriceAndProfitPerProduct() {
        String sql = """
                SELECT p.product_id AS 'Product ID',
                       p.product_name AS 'Product',
                       ROUND(COALESCE(sales.avg_selling_price, 0), 2) AS 'Average Selling Price',
                       ROUND(COALESCE(purchases.avg_buying_price, 0), 2) AS 'Average Buying Price',
                       ROUND(COALESCE(sales.avg_selling_price, 0) - COALESCE(purchases.avg_buying_price, 0), 2) AS 'Average Profit'
                FROM Product p
                LEFT JOIN (
                    SELECT product_id, AVG(selling_price) AS avg_selling_price
                    FROM SalesInvoiceItem
                    GROUP BY product_id
                ) sales ON p.product_id = sales.product_id
                LEFT JOIN (
                    SELECT product_id, AVG(purchase_price) AS avg_buying_price
                    FROM PurchaseInvoiceItem
                    GROUP BY product_id
                ) purchases ON p.product_id = purchases.product_id
                ORDER BY `Average Profit` DESC, p.product_name
                """;

        return runQuery(sql);
    }

    public DefaultTableModel getProfitPerProduct() {
        String sql = """
                SELECT p.product_id AS 'Product ID',
                       p.product_name AS 'Product',
                       COALESCE(sales.quantity_sold, 0) AS 'Quantity Sold',
                       ROUND(COALESCE(sales.revenue, 0), 2) AS 'Revenue',
                       ROUND(COALESCE(sales.quantity_sold, 0) * COALESCE(costs.average_purchase_price, 0), 2) AS 'Estimated Cost',
                       ROUND(COALESCE(sales.revenue, 0)
                             - (COALESCE(sales.quantity_sold, 0) * COALESCE(costs.average_purchase_price, 0)), 2) AS 'Profit',
                       CASE
                           WHEN COALESCE(sales.revenue, 0) > 0
                           THEN ROUND(((COALESCE(sales.revenue, 0)
                                  - (COALESCE(sales.quantity_sold, 0) * COALESCE(costs.average_purchase_price, 0)))
                                  / COALESCE(sales.revenue, 0)) * 100, 2)
                           ELSE 0
                       END AS 'Profit Margin %'
                FROM Product p
                LEFT JOIN (
                    SELECT product_id,
                           SUM(quantity) AS quantity_sold,
                           SUM(quantity * selling_price) AS revenue
                    FROM SalesInvoiceItem
                    GROUP BY product_id
                ) sales ON p.product_id = sales.product_id
                LEFT JOIN (
                    SELECT product_id,
                           AVG(purchase_price) AS average_purchase_price
                    FROM PurchaseInvoiceItem
                    GROUP BY product_id
                ) costs ON p.product_id = costs.product_id
                WHERE COALESCE(sales.quantity_sold, 0) > 0
                ORDER BY `Profit` DESC, p.product_name
                """;

        return runQuery(sql);
    }

    public List<ComboOption> getSuppliers() {
        String sql = """
                SELECT supplier_id AS id, supplier_name AS name
                FROM Supplier
                ORDER BY supplier_name
                """;

        return loadOptions(sql);
    }

    public List<ComboOption> getWarehouses() {
        String sql = """
                SELECT warehouse_id AS id, warehouse_name AS name
                FROM Warehouse
                ORDER BY warehouse_name
                """;

        return loadOptions(sql);
    }

    public List<ComboOption> getProducts() {
        String sql = """
                SELECT product_id AS id, product_name AS name
                FROM Product
                ORDER BY product_name
                """;

        return loadOptions(sql);
    }

    public List<ComboOption> getPurchaseInvoices() {
        String sql = """
                SELECT pi.purchase_invoice_id AS id,
                       CONCAT('Invoice ', pi.purchase_invoice_id, ' - ', s.supplier_name, ' - ', pi.invoice_date) AS name
                FROM PurchaseInvoice pi
                JOIN Supplier s ON pi.supplier_id = s.supplier_id
                ORDER BY pi.invoice_date DESC, pi.purchase_invoice_id DESC
                """;

        return loadOptions(sql);
    }

    private List<ComboOption> loadOptions(String sql) {
        List<ComboOption> options = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                options.add(new ComboOption(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Error loading report options: " + e.getMessage());
        }

        return options;
    }

    private DefaultTableModel runQuery(String sql, Object... params) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return buildTableModel(rs);
            }

        } catch (SQLException e) {
            System.out.println("Report query error: " + e.getMessage());
            return new DefaultTableModel(new String[]{"Error"}, 0);
        }
    }

    private DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        String[] columns = new String[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            columns[i - 1] = metaData.getColumnLabel(i);
        }

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        while (rs.next()) {
            Object[] row = new Object[columnCount];

            for (int i = 1; i <= columnCount; i++) {
                row[i - 1] = rs.getObject(i);
            }

            model.addRow(row);
        }

        return model;
    }

    public static class ComboOption {
        private final int id;
        private final String name;

        public ComboOption(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return id + " - " + name;
        }
    }

}
