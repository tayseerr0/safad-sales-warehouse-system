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
                       sp.supply_price AS 'Supply Price'
                FROM SupplierProduct sp
                JOIN Supplier s ON sp.supplier_id = s.supplier_id
                JOIN Product p ON sp.product_id = p.product_id
                WHERE sp.product_id = ?
                ORDER BY sp.supply_price, s.supplier_name
                """;

        return runQuery(sql, productId);
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
                       COUNT(DISTINCT pi.purchase_invoice_id) AS 'Invoice Count',
                       SUM(pii.quantity) AS 'Total Quantity',
                       ROUND(SUM(pii.quantity * pii.purchase_price), 2) AS 'Total Purchase Amount'
                FROM Supplier s
                JOIN PurchaseInvoice pi ON s.supplier_id = pi.supplier_id
                JOIN PurchaseInvoiceItem pii ON pi.purchase_invoice_id = pii.purchase_invoice_id
                GROUP BY s.supplier_id, s.supplier_name
                ORDER BY SUM(pii.quantity * pii.purchase_price) DESC
                """;

        return runQuery(sql);
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
