package dao;

import db.DBConnection;
import model.SalesInvoice;
import model.SalesInvoiceItem;
import model.SalesPayment;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalesInvoiceDAO {

    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final SalesPaymentDAO salesPaymentDAO = new SalesPaymentDAO();

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
                invoice.setAmount(calculateInvoiceAmount(invoice.getItems()));

                int salesInvoiceId = insertSalesInvoiceHeader(conn, invoice);
                addInitialPayment(conn, invoice, salesInvoiceId);

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

                salesPaymentDAO.refreshInvoicePaymentSummary(conn, salesInvoiceId);

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

    public boolean updateSalesInvoice(SalesInvoice updatedInvoice) {
        if (updatedInvoice == null || updatedInvoice.getSalesInvoiceId() <= 0) {
            System.out.println("Valid sales invoice ID is required for update.");
            return false;
        }

        if (updatedInvoice.getItems() == null || updatedInvoice.getItems().isEmpty()) {
            System.out.println("Sales invoice must contain at least one item.");
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            try {
                conn.setAutoCommit(false);

                SalesInvoice oldInvoice = getSalesInvoiceById(conn, updatedInvoice.getSalesInvoiceId());
                if (oldInvoice == null) {
                    throw new SQLException("Sales invoice not found.");
                }

                for (SalesInvoiceItem oldItem : oldInvoice.getItems()) {
                    if (!inventoryDAO.increaseStock(conn, oldItem.getProductId(), oldInvoice.getWarehouseId(), oldItem.getQuantity())) {
                        throw new SQLException("Failed to return stock for product ID: " + oldItem.getProductId());
                    }
                }

                updatedInvoice.setAmount(calculateInvoiceAmount(updatedInvoice.getItems()));
                validateStock(conn, updatedInvoice);

                updateSalesInvoiceHeader(conn, updatedInvoice);
                deleteSalesInvoiceItems(conn, updatedInvoice.getSalesInvoiceId());

                for (SalesInvoiceItem item : updatedInvoice.getItems()) {
                    insertSalesInvoiceItem(conn, updatedInvoice.getSalesInvoiceId(), item);

                    if (!inventoryDAO.decreaseStock(conn, item.getProductId(), updatedInvoice.getWarehouseId(), item.getQuantity())) {
                        throw new SQLException("Failed to decrease inventory for product ID: " + item.getProductId());
                    }
                }

                salesPaymentDAO.refreshInvoicePaymentSummary(conn, updatedInvoice.getSalesInvoiceId());

                conn.commit();
                return true;

            } catch (Exception e) {
                conn.rollback();
                System.out.println("Sales invoice update failed. Rolled back changes.");
                System.out.println(e.getMessage());
                return false;

            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Database error while updating sales invoice: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteSalesInvoice(int salesInvoiceId) {
        if (salesInvoiceId <= 0) {
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            try {
                conn.setAutoCommit(false);

                SalesInvoice invoice = getSalesInvoiceById(conn, salesInvoiceId);
                if (invoice == null) {
                    throw new SQLException("Sales invoice not found.");
                }

                for (SalesInvoiceItem item : invoice.getItems()) {
                    if (!inventoryDAO.increaseStock(conn, item.getProductId(), invoice.getWarehouseId(), item.getQuantity())) {
                        throw new SQLException("Failed to return stock for product ID: " + item.getProductId());
                    }
                }

                deleteSalesInvoiceItems(conn, salesInvoiceId);
                deleteSalesInvoiceHeader(conn, salesInvoiceId);

                conn.commit();
                return true;

            } catch (Exception e) {
                conn.rollback();
                System.out.println("Sales invoice delete failed. Rolled back changes.");
                System.out.println(e.getMessage());
                return false;

            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Database error while deleting sales invoice: " + e.getMessage());
            return false;
        }
    }

    private void validateStock(Connection conn, SalesInvoice invoice) throws SQLException {
        if (invoice.getInvoiceDate() == null) {
            throw new IllegalArgumentException("Invoice date is required.");
        }
        if (invoice.getPayment() == null || invoice.getPayment().signum() < 0) {
            throw new IllegalArgumentException("Payment cannot be negative.");
        }
        if (invoice.getPaymentType() == null || invoice.getPaymentType().trim().isEmpty()) {
            throw new IllegalArgumentException("Payment type is required.");
        }
        if (invoice.getClientId() <= 0 || invoice.getWarehouseId() <= 0) {
            throw new IllegalArgumentException("Invalid client or warehouse.");
        }

        Map<Integer, Integer> totalQuantityPerProduct = new HashMap<>();

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

            totalQuantityPerProduct.put(
                    item.getProductId(),
                    totalQuantityPerProduct.getOrDefault(item.getProductId(), 0) + item.getQuantity()
            );
        }

        for (Map.Entry<Integer, Integer> entry : totalQuantityPerProduct.entrySet()) {
            int productId = entry.getKey();
            int requestedQuantity = entry.getValue();

            int availableStock = inventoryDAO.getAvailableStock(
                    conn,
                    productId,
                    invoice.getWarehouseId()
            );

            if (availableStock < requestedQuantity) {
                throw new IllegalArgumentException(
                        "Not enough stock for product ID " + productId +
                                ". Available: " + availableStock +
                                ", requested: " + requestedQuantity
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
            stmt.setBigDecimal(2, BigDecimal.ZERO);
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

    private void updateSalesInvoiceHeader(Connection conn, SalesInvoice invoice) throws SQLException {
        String sql = """
                UPDATE SalesInvoice
                SET invoice_date = ?, payment_type = ?, amount = ?,
                    client_id = ?, warehouse_id = ?
                WHERE sales_invoice_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(invoice.getInvoiceDate()));
            stmt.setString(2, invoice.getPaymentType());
            stmt.setBigDecimal(3, invoice.getAmount());
            stmt.setInt(4, invoice.getClientId());
            stmt.setInt(5, invoice.getWarehouseId());
            stmt.setInt(6, invoice.getSalesInvoiceId());

            if (stmt.executeUpdate() == 0) {
                throw new SQLException("Updating sales invoice failed.");
            }
        }
    }

    private void deleteSalesInvoiceItems(Connection conn, int salesInvoiceId) throws SQLException {
        String sql = "DELETE FROM SalesInvoiceItem WHERE sales_invoice_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, salesInvoiceId);
            stmt.executeUpdate();
        }
    }

    private void deleteSalesInvoiceHeader(Connection conn, int salesInvoiceId) throws SQLException {
        String sql = "DELETE FROM SalesInvoice WHERE sales_invoice_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, salesInvoiceId);

            if (stmt.executeUpdate() == 0) {
                throw new SQLException("Deleting sales invoice failed.");
            }
        }
    }

    private BigDecimal calculateInvoiceAmount(List<SalesInvoiceItem> items) {
        BigDecimal total = BigDecimal.ZERO;

        for (SalesInvoiceItem item : items) {
            total = total.add(item.getSellingPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        return total;
    }

    private void addInitialPayment(Connection conn, SalesInvoice invoice, int salesInvoiceId) throws SQLException {
        if (invoice.getPayment() == null || invoice.getPayment().signum() <= 0) {
            return;
        }

        SalesPayment payment = new SalesPayment(
                salesInvoiceId,
                invoice.getInvoiceDate(),
                invoice.getPayment(),
                invoice.getPaymentType()
        );
        salesPaymentDAO.addPayment(conn, payment);
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

    public SalesInvoice getSalesInvoiceById(int salesInvoiceId) {
        try (Connection conn = DBConnection.getConnection()) {
            return getSalesInvoiceById(conn, salesInvoiceId);
        } catch (SQLException e) {
            System.out.println("Error getting sales invoice by ID: " + e.getMessage());
            return null;
        }
    }

    private SalesInvoice getSalesInvoiceById(Connection conn, int salesInvoiceId) throws SQLException {
        String sql = """
                SELECT sales_invoice_id, invoice_date, payment, payment_type, amount, client_id, warehouse_id
                FROM SalesInvoice
                WHERE sales_invoice_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, salesInvoiceId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    SalesInvoice invoice = mapResultSetToSalesInvoice(rs);
                    invoice.setItems(getSalesInvoiceItems(conn, salesInvoiceId));
                    return invoice;
                }
            }
        }

        return null;
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
        try (Connection conn = DBConnection.getConnection()) {
            return getSalesInvoiceItems(conn, salesInvoiceId);
        } catch (SQLException e) {
            System.out.println("Error loading sales invoice items: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<SalesInvoiceItem> getSalesInvoiceItems(Connection conn, int salesInvoiceId) throws SQLException {
        List<SalesInvoiceItem> items = new ArrayList<>();

        String sql = """
                SELECT sales_item_id, sales_invoice_id, product_id, selling_price, quantity, warranty_end_date
                FROM SalesInvoiceItem
                WHERE sales_invoice_id = ?
                ORDER BY sales_item_id
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, salesInvoiceId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToSalesInvoiceItem(rs));
                }
            }

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
