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
        if (invoice == null) {
            System.out.println("Purchase invoice cannot be null.");
            return false;
        }

        if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
            System.out.println("Purchase invoice must contain at least one item.");
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            try {
                conn.setAutoCommit(false);

                validateInvoice(invoice);

                BigDecimal calculatedAmount = calculateInvoiceAmount(invoice.getItems());
                invoice.setAmount(calculatedAmount);

                int invoiceId = insertPurchaseInvoiceHeader(conn, invoice);

                for (PurchaseInvoiceItem item : invoice.getItems()) {
                    insertPurchaseInvoiceItem(conn, invoiceId, item);

                    boolean stockIncreased = inventoryDAO.increaseStock(
                            conn,
                            item.getProductId(),
                            invoice.getWarehouseId(),
                            item.getQuantity()
                    );

                    if (!stockIncreased) {
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

    private void validateInvoice(PurchaseInvoice invoice) {
        if (invoice.getInvoiceDate() == null) {
            throw new IllegalArgumentException("Invoice date is required.");
        }

        if (invoice.getPayment() == null || invoice.getPayment().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Payment must be non-negative.");
        }

        if (invoice.getPaymentType() == null || invoice.getPaymentType().trim().isEmpty()) {
            throw new IllegalArgumentException("Payment type is required.");
        }

        if (invoice.getSupplierId() <= 0) {
            throw new IllegalArgumentException("Invalid supplier ID.");
        }

        if (invoice.getWarehouseId() <= 0) {
            throw new IllegalArgumentException("Invalid warehouse ID.");
        }

        for (PurchaseInvoiceItem item : invoice.getItems()) {
            if (item.getProductId() <= 0) {
                throw new IllegalArgumentException("Invalid product ID.");
            }

            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Item quantity must be greater than zero.");
            }

            if (item.getPurchasePrice() == null || item.getPurchasePrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Purchase price must be non-negative.");
            }
        }
    }

    private BigDecimal calculateInvoiceAmount(List<PurchaseInvoiceItem> items) {
        BigDecimal total = BigDecimal.ZERO;

        for (PurchaseInvoiceItem item : items) {
            BigDecimal lineTotal = item.getPurchasePrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            total = total.add(lineTotal);
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

            if (invoice.getEstimatedArrival() != null) {
                stmt.setDate(2, Date.valueOf(invoice.getEstimatedArrival()));
            } else {
                stmt.setNull(2, Types.DATE);
            }

            stmt.setBigDecimal(3, invoice.getPayment());
            stmt.setString(4, invoice.getPaymentType());
            stmt.setBigDecimal(5, invoice.getAmount());
            stmt.setInt(6, invoice.getSupplierId());
            stmt.setInt(7, invoice.getWarehouseId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating purchase invoice failed. No rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating purchase invoice failed. No ID obtained.");
                }
            }
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

    public List<PurchaseInvoice> getAllPurchaseInvoices() {
        List<PurchaseInvoice> invoices = new ArrayList<>();

        String sql = """
                SELECT pi.purchase_invoice_id,
                       pi.invoice_date,
                       pi.estimated_arrival,
                       pi.payment,
                       pi.payment_type,
                       pi.amount,
                       pi.supplier_id,
                       s.supplier_name,
                       pi.warehouse_id,
                       w.warehouse_name
                FROM PurchaseInvoice pi
                JOIN Supplier s ON pi.supplier_id = s.supplier_id
                JOIN Warehouse w ON pi.warehouse_id = w.warehouse_id
                ORDER BY pi.invoice_date DESC, pi.purchase_invoice_id DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                invoices.add(mapJoinedResultSetToPurchaseInvoice(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error loading purchase invoices: " + e.getMessage());
        }

        return invoices;
    }

    public PurchaseInvoice getPurchaseInvoiceById(int purchaseInvoiceId) {
        String sql = """
                SELECT pi.purchase_invoice_id,
                       pi.invoice_date,
                       pi.estimated_arrival,
                       pi.payment,
                       pi.payment_type,
                       pi.amount,
                       pi.supplier_id,
                       s.supplier_name,
                       pi.warehouse_id,
                       w.warehouse_name
                FROM PurchaseInvoice pi
                JOIN Supplier s ON pi.supplier_id = s.supplier_id
                JOIN Warehouse w ON pi.warehouse_id = w.warehouse_id
                WHERE pi.purchase_invoice_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, purchaseInvoiceId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PurchaseInvoice invoice = mapJoinedResultSetToPurchaseInvoice(rs);
                    invoice.setItems(getPurchaseInvoiceItems(purchaseInvoiceId));
                    return invoice;
                }
            }

        } catch (SQLException e) {
            System.out.println("Error getting purchase invoice by ID: " + e.getMessage());
        }

        return null;
    }

    public List<PurchaseInvoiceItem> getPurchaseInvoiceItems(int purchaseInvoiceId) {
        List<PurchaseInvoiceItem> items = new ArrayList<>();

        String sql = """
                SELECT pii.purchase_item_id,
                       pii.purchase_invoice_id,
                       pii.product_id,
                       p.product_name,
                       pii.purchase_price,
                       pii.quantity
                FROM PurchaseInvoiceItem pii
                JOIN Product p ON pii.product_id = p.product_id
                WHERE pii.purchase_invoice_id = ?
                ORDER BY pii.purchase_item_id
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

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

        } catch (SQLException e) {
            System.out.println("Error loading purchase invoice items: " + e.getMessage());
        }

        return items;
    }

    public List<PurchaseInvoice> getPurchaseInvoicesBySupplierAndDate(int supplierId, Date startDate, Date endDate) {
        List<PurchaseInvoice> invoices = new ArrayList<>();

        String sql = """
                SELECT pi.purchase_invoice_id,
                       pi.invoice_date,
                       pi.estimated_arrival,
                       pi.payment,
                       pi.payment_type,
                       pi.amount,
                       pi.supplier_id,
                       s.supplier_name,
                       pi.warehouse_id,
                       w.warehouse_name
                FROM PurchaseInvoice pi
                JOIN Supplier s ON pi.supplier_id = s.supplier_id
                JOIN Warehouse w ON pi.warehouse_id = w.warehouse_id
                WHERE pi.supplier_id = ?
                  AND pi.invoice_date BETWEEN ? AND ?
                ORDER BY pi.invoice_date DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supplierId);
            stmt.setDate(2, startDate);
            stmt.setDate(3, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    invoices.add(mapJoinedResultSetToPurchaseInvoice(rs));
                }
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