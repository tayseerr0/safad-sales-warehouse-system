package dao;

import db.DBConnection;
import model.PurchaseInvoice;
import model.PurchaseInvoiceItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseInvoiceDAO {

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    public boolean createPurchaseInvoice(PurchaseInvoice invoice) {
        if (invoice == null || invoice.getItems() == null || invoice.getItems().isEmpty()) {
            System.out.println("Purchase invoice must contain at least one item.");
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            try {
                conn.setAutoCommit(false);
                validateInvoice(invoice);
                invoice.setAmount(calculateInvoiceAmount(invoice.getItems()));

                int invoiceId = insertPurchaseInvoiceHeader(conn, invoice);

                for (PurchaseInvoiceItem item : invoice.getItems()) {
                    insertPurchaseInvoiceItem(conn, invoiceId, item);
                    if (!inventoryDAO.increaseStock(conn, item.getProductId(), invoice.getWarehouseId(), item.getQuantity())) {
                        throw new SQLException("Failed to increase inventory for product ID: " + item.getProductId());
                    }
                }

                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                System.out.println("Purchase invoice failed. Rolled back changes.");
                System.out.println(e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Database error while creating purchase invoice: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePurchaseInvoice(PurchaseInvoice updatedInvoice) {
        if (updatedInvoice == null || updatedInvoice.getPurchaseInvoiceId() <= 0) {
            System.out.println("Valid purchase invoice ID is required for update.");
            return false;
        }

        if (updatedInvoice.getItems() == null || updatedInvoice.getItems().isEmpty()) {
            System.out.println("Purchase invoice must contain at least one item.");
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            try {
                conn.setAutoCommit(false);
                validateInvoice(updatedInvoice);

                PurchaseInvoice oldInvoice = getPurchaseInvoiceById(conn, updatedInvoice.getPurchaseInvoiceId());
                if (oldInvoice == null) throw new SQLException("Purchase invoice not found.");

                // Reverse old purchase effect from inventory.
                for (PurchaseInvoiceItem oldItem : oldInvoice.getItems()) {
                    if (!inventoryDAO.decreaseStock(conn, oldItem.getProductId(), oldInvoice.getWarehouseId(), oldItem.getQuantity())) {
                        throw new SQLException("Cannot update invoice because old stock for product ID " + oldItem.getProductId() + " is no longer available.");
                    }
                }

                updatedInvoice.setAmount(calculateInvoiceAmount(updatedInvoice.getItems()));

                deletePurchaseInvoiceItems(conn, updatedInvoice.getPurchaseInvoiceId());
                updatePurchaseInvoiceHeader(conn, updatedInvoice);

                // Apply new purchase effect to inventory.
                for (PurchaseInvoiceItem item : updatedInvoice.getItems()) {
                    insertPurchaseInvoiceItem(conn, updatedInvoice.getPurchaseInvoiceId(), item);
                    if (!inventoryDAO.increaseStock(conn, item.getProductId(), updatedInvoice.getWarehouseId(), item.getQuantity())) {
                        throw new SQLException("Failed to increase inventory for product ID: " + item.getProductId());
                    }
                }

                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                System.out.println("Purchase invoice update failed. Rolled back changes.");
                System.out.println(e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Database error while updating purchase invoice: " + e.getMessage());
            return false;
        }
    }

    private void validateInvoice(PurchaseInvoice invoice) {
        if (invoice.getInvoiceDate() == null) throw new IllegalArgumentException("Invoice date is required.");
        if (invoice.getPayment() == null || invoice.getPayment().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Payment must be non-negative.");
        if (invoice.getPaymentType() == null || invoice.getPaymentType().trim().isEmpty()) throw new IllegalArgumentException("Payment type is required.");
        if (invoice.getSupplierId() <= 0) throw new IllegalArgumentException("Invalid supplier ID.");
        if (invoice.getWarehouseId() <= 0) throw new IllegalArgumentException("Invalid warehouse ID.");

        for (PurchaseInvoiceItem item : invoice.getItems()) {
            if (item.getProductId() <= 0) throw new IllegalArgumentException("Invalid product ID.");
            if (item.getQuantity() <= 0) throw new IllegalArgumentException("Item quantity must be greater than zero.");
            if (item.getPurchasePrice() == null || item.getPurchasePrice().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Purchase price must be non-negative.");
        }
    }

    private BigDecimal calculateInvoiceAmount(List<PurchaseInvoiceItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseInvoiceItem item : items) {
            total = total.add(item.getPurchasePrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return total;
    }

    private int insertPurchaseInvoiceHeader(Connection conn, PurchaseInvoice invoice) throws SQLException {
        String sql = """
                INSERT INTO PurchaseInvoice
                (invoice_date, estimated_arrival, payment, payment_type, amount, supplier_id, warehouse_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDate(1, Date.valueOf(invoice.getInvoiceDate()));
            if (invoice.getEstimatedArrival() != null) stmt.setDate(2, Date.valueOf(invoice.getEstimatedArrival()));
            else stmt.setNull(2, Types.DATE);
            stmt.setBigDecimal(3, invoice.getPayment());
            stmt.setString(4, invoice.getPaymentType());
            stmt.setBigDecimal(5, invoice.getAmount());
            stmt.setInt(6, invoice.getSupplierId());
            stmt.setInt(7, invoice.getWarehouseId());

            if (stmt.executeUpdate() == 0) throw new SQLException("Creating purchase invoice failed.");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("No invoice ID generated.");
            }
        }
    }

    private void updatePurchaseInvoiceHeader(Connection conn, PurchaseInvoice invoice) throws SQLException {
        String sql = """
                UPDATE PurchaseInvoice
                SET invoice_date = ?, estimated_arrival = ?, payment = ?, payment_type = ?,
                    amount = ?, supplier_id = ?, warehouse_id = ?
                WHERE purchase_invoice_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(invoice.getInvoiceDate()));
            if (invoice.getEstimatedArrival() != null) stmt.setDate(2, Date.valueOf(invoice.getEstimatedArrival()));
            else stmt.setNull(2, Types.DATE);
            stmt.setBigDecimal(3, invoice.getPayment());
            stmt.setString(4, invoice.getPaymentType());
            stmt.setBigDecimal(5, invoice.getAmount());
            stmt.setInt(6, invoice.getSupplierId());
            stmt.setInt(7, invoice.getWarehouseId());
            stmt.setInt(8, invoice.getPurchaseInvoiceId());

            if (stmt.executeUpdate() == 0) throw new SQLException("Updating purchase invoice failed.");
        }
    }

    private void insertPurchaseInvoiceItem(Connection conn, int invoiceId, PurchaseInvoiceItem item) throws SQLException {
        String sql = """
                INSERT INTO PurchaseInvoiceItem
                (purchase_invoice_id, product_id, purchase_price, quantity)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, invoiceId);
            stmt.setInt(2, item.getProductId());
            stmt.setBigDecimal(3, item.getPurchasePrice());
            stmt.setInt(4, item.getQuantity());
            stmt.executeUpdate();
        }
    }

    private void deletePurchaseInvoiceItems(Connection conn, int purchaseInvoiceId) throws SQLException {
        String sql = "DELETE FROM PurchaseInvoiceItem WHERE purchase_invoice_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, purchaseInvoiceId);
            stmt.executeUpdate();
        }
    }

    public List<PurchaseInvoice> getAllPurchaseInvoices() {
        List<PurchaseInvoice> invoices = new ArrayList<>();
        String sql = """
                SELECT pi.purchase_invoice_id, pi.invoice_date, pi.estimated_arrival, pi.payment,
                       pi.payment_type, pi.amount, pi.supplier_id, s.supplier_name,
                       pi.warehouse_id, w.warehouse_name
                FROM PurchaseInvoice pi
                JOIN Supplier s ON pi.supplier_id = s.supplier_id
                JOIN Warehouse w ON pi.warehouse_id = w.warehouse_id
                ORDER BY pi.invoice_date DESC, pi.purchase_invoice_id DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) invoices.add(mapJoinedResultSetToPurchaseInvoice(rs));
        } catch (SQLException e) {
            System.out.println("Error loading purchase invoices: " + e.getMessage());
        }
        return invoices;
    }

    public PurchaseInvoice getPurchaseInvoiceById(int purchaseInvoiceId) {
        try (Connection conn = DBConnection.getConnection()) {
            return getPurchaseInvoiceById(conn, purchaseInvoiceId);
        } catch (SQLException e) {
            System.out.println("Error getting purchase invoice by ID: " + e.getMessage());
            return null;
        }
    }

    private PurchaseInvoice getPurchaseInvoiceById(Connection conn, int purchaseInvoiceId) throws SQLException {
        String sql = """
                SELECT pi.purchase_invoice_id, pi.invoice_date, pi.estimated_arrival, pi.payment,
                       pi.payment_type, pi.amount, pi.supplier_id, s.supplier_name,
                       pi.warehouse_id, w.warehouse_name
                FROM PurchaseInvoice pi
                JOIN Supplier s ON pi.supplier_id = s.supplier_id
                JOIN Warehouse w ON pi.warehouse_id = w.warehouse_id
                WHERE pi.purchase_invoice_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, purchaseInvoiceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PurchaseInvoice invoice = mapJoinedResultSetToPurchaseInvoice(rs);
                    invoice.setItems(getPurchaseInvoiceItems(conn, purchaseInvoiceId));
                    return invoice;
                }
            }
        }
        return null;
    }

    public List<PurchaseInvoiceItem> getPurchaseInvoiceItems(int purchaseInvoiceId) {
        try (Connection conn = DBConnection.getConnection()) {
            return getPurchaseInvoiceItems(conn, purchaseInvoiceId);
        } catch (SQLException e) {
            System.out.println("Error loading purchase invoice items: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<PurchaseInvoiceItem> getPurchaseInvoiceItems(Connection conn, int purchaseInvoiceId) throws SQLException {
        List<PurchaseInvoiceItem> items = new ArrayList<>();
        String sql = """
                SELECT pii.purchase_item_id, pii.purchase_invoice_id, pii.product_id,
                       p.product_name, pii.purchase_price, pii.quantity
                FROM PurchaseInvoiceItem pii
                JOIN Product p ON pii.product_id = p.product_id
                WHERE pii.purchase_invoice_id = ?
                ORDER BY pii.purchase_item_id
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, purchaseInvoiceId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new PurchaseInvoiceItem(
                            rs.getInt("purchase_item_id"),
                            rs.getInt("purchase_invoice_id"),
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getBigDecimal("purchase_price"),
                            rs.getInt("quantity")
                    ));
                }
            }
        }
        return items;
    }

    public List<PurchaseInvoice> getPurchaseInvoicesBySupplierAndDate(int supplierId, Date startDate, Date endDate) {
        List<PurchaseInvoice> invoices = new ArrayList<>();
        String sql = """
                SELECT pi.purchase_invoice_id, pi.invoice_date, pi.estimated_arrival, pi.payment,
                       pi.payment_type, pi.amount, pi.supplier_id, s.supplier_name,
                       pi.warehouse_id, w.warehouse_name
                FROM PurchaseInvoice pi
                JOIN Supplier s ON pi.supplier_id = s.supplier_id
                JOIN Warehouse w ON pi.warehouse_id = w.warehouse_id
                WHERE pi.supplier_id = ? AND pi.invoice_date BETWEEN ? AND ?
                ORDER BY pi.invoice_date DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, supplierId);
            stmt.setDate(2, startDate);
            stmt.setDate(3, endDate);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) invoices.add(mapJoinedResultSetToPurchaseInvoice(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error loading purchase invoices by supplier/date: " + e.getMessage());
        }
        return invoices;
    }

    private PurchaseInvoice mapJoinedResultSetToPurchaseInvoice(ResultSet rs) throws SQLException {
        Date estimatedArrival = rs.getDate("estimated_arrival");

        return new PurchaseInvoice(
                rs.getInt("purchase_invoice_id"),
                rs.getDate("invoice_date").toLocalDate(),
                estimatedArrival != null ? estimatedArrival.toLocalDate() : null,
                rs.getBigDecimal("payment"),
                rs.getString("payment_type"),
                rs.getBigDecimal("amount"),
                rs.getInt("supplier_id"),
                rs.getString("supplier_name"),
                rs.getInt("warehouse_id"),
                rs.getString("warehouse_name")
        );
    }
}
