package dao;

import db.DBConnection;
import model.SalesInvoice;
import model.SalesInvoiceItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesInvoiceDAO {

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    public boolean createSalesInvoice(SalesInvoice invoice) {
        if (invoice == null) {
            System.out.println("Invoice cannot be null.");
            return false;
        }

        if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
            System.out.println("Sales invoice must contain at least one item.");
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            try {
                conn.setAutoCommit(false);

                validateStock(conn, invoice);

                int salesInvoiceId = insertSalesInvoiceHeader(conn, invoice);

                for (SalesInvoiceItem item : invoice.getItems()) {
                    insertSalesInvoiceItem(conn, salesInvoiceId, item);

                    boolean decreased = inventoryDAO.decreaseStock(
                            conn,
                            item.getProductId(),
                            invoice.getWarehouseId(),
                            item.getQuantity()
                    );

                    if (!decreased) {
                        throw new SQLException("Failed to decrease inventory for product ID: " + item.getProductId());
                    }
                }

                conn.commit();
                return true;

            } catch (Exception e) {
                conn.rollback();
                System.out.println("Sales invoice failed. Rolled back changes.");
                System.out.println(e.getMessage());
                return false;

            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Database error while creating sales invoice: " + e.getMessage());
            return false;
        }
    }

    private void validateStock(Connection conn, SalesInvoice invoice) throws SQLException {
        for (SalesInvoiceItem item : invoice.getItems()) {
            if (item.getProductId() <= 0) {
                throw new IllegalArgumentException("Invalid product ID.");
            }

            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero.");
            }

            if (item.getSellingPrice() == null || item.getSellingPrice().signum() < 0) {
                throw new IllegalArgumentException("Selling price cannot be negative.");
            }

            int availableStock = inventoryDAO.getAvailableStock(
                    conn,
                    item.getProductId(),
                    invoice.getWarehouseId()
            );

            if (availableStock < item.getQuantity()) {
                throw new IllegalArgumentException(
                        "Not enough stock for product ID " + item.getProductId() +
                                ". Available: " + availableStock +
                                ", requested: " + item.getQuantity()
                );
            }
        }
    }

    private int insertSalesInvoiceHeader(Connection conn, SalesInvoice invoice) throws SQLException {
        String sql = """
                INSERT INTO SalesInvoice
                (invoice_date, payment, payment_type, amount, client_id, warehouse_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDate(1, Date.valueOf(invoice.getInvoiceDate()));
            stmt.setBigDecimal(2, invoice.getPayment());
            stmt.setString(3, invoice.getPaymentType());
            stmt.setBigDecimal(4, invoice.getAmount());
            stmt.setInt(5, invoice.getClientId());
            stmt.setInt(6, invoice.getWarehouseId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating sales invoice failed. No rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating sales invoice failed. No ID obtained.");
                }
            }
        }
    }

    private void insertSalesInvoiceItem(Connection conn, int salesInvoiceId, SalesInvoiceItem item) throws SQLException {
        String sql = """
                INSERT INTO SalesInvoiceItem
                (sales_invoice_id, product_id, selling_price, quantity, warranty_end_date)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, salesInvoiceId);
            stmt.setInt(2, item.getProductId());
            stmt.setBigDecimal(3, item.getSellingPrice());
            stmt.setInt(4, item.getQuantity());

            if (item.getWarrantyEndDate() == null) {
                stmt.setNull(5, Types.DATE);
            } else {
                stmt.setDate(5, Date.valueOf(item.getWarrantyEndDate()));
            }

            stmt.executeUpdate();
        }
    }

    public List<SalesInvoice> getAllSalesInvoices() {
        List<SalesInvoice> invoices = new ArrayList<>();

        String sql = """
                SELECT sales_invoice_id, invoice_date, payment, payment_type, amount, client_id, warehouse_id
                FROM SalesInvoice
                ORDER BY invoice_date DESC, sales_invoice_id DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                invoices.add(mapResultSetToSalesInvoice(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error loading sales invoices: " + e.getMessage());
        }

        return invoices;
    }

    public List<SalesInvoice> getSalesInvoicesByClient(int clientId) {
        List<SalesInvoice> invoices = new ArrayList<>();

        String sql = """
                SELECT sales_invoice_id, invoice_date, payment, payment_type, amount, client_id, warehouse_id
                FROM SalesInvoice
                WHERE client_id = ?
                ORDER BY invoice_date DESC, sales_invoice_id DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    invoices.add(mapResultSetToSalesInvoice(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error loading sales invoices by client: " + e.getMessage());
        }

        return invoices;
    }

    public List<SalesInvoice> getSalesInvoicesByDateRange(Date startDate, Date endDate) {
        List<SalesInvoice> invoices = new ArrayList<>();

        String sql = """
                SELECT sales_invoice_id, invoice_date, payment, payment_type, amount, client_id, warehouse_id
                FROM SalesInvoice
                WHERE invoice_date BETWEEN ? AND ?
                ORDER BY invoice_date DESC, sales_invoice_id DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    invoices.add(mapResultSetToSalesInvoice(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error loading sales invoices by date range: " + e.getMessage());
        }

        return invoices;
    }

    public List<SalesInvoiceItem> getSalesInvoiceItems(int salesInvoiceId) {
        List<SalesInvoiceItem> items = new ArrayList<>();

        String sql = """
                SELECT sales_item_id, sales_invoice_id, product_id, selling_price, quantity, warranty_end_date
                FROM SalesInvoiceItem
                WHERE sales_invoice_id = ?
                ORDER BY sales_item_id
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, salesInvoiceId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToSalesInvoiceItem(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error loading sales invoice items: " + e.getMessage());
        }

        return items;
    }

    private SalesInvoice mapResultSetToSalesInvoice(ResultSet rs) throws SQLException {
        return new SalesInvoice(
                rs.getInt("sales_invoice_id"),
                rs.getDate("invoice_date").toLocalDate(),
                rs.getBigDecimal("payment"),
                rs.getString("payment_type"),
                rs.getBigDecimal("amount"),
                rs.getInt("client_id"),
                rs.getInt("warehouse_id")
        );
    }

    private SalesInvoiceItem mapResultSetToSalesInvoiceItem(ResultSet rs) throws SQLException {
        Date warrantyDate = rs.getDate("warranty_end_date");

        return new SalesInvoiceItem(
                rs.getInt("sales_item_id"),
                rs.getInt("sales_invoice_id"),
                rs.getInt("product_id"),
                rs.getBigDecimal("selling_price"),
                rs.getInt("quantity"),
                warrantyDate == null ? null : warrantyDate.toLocalDate()
        );
    }
}