package dao;

import db.DBConnection;
import model.SalesPayment;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalesPaymentDAO {

    public boolean addPayment(SalesPayment payment) {
        try (Connection conn = DBConnection.getConnection()) {
            try {
                conn.setAutoCommit(false);
                addPayment(conn, payment);
                refreshInvoicePaymentSummary(conn, payment.getSalesInvoiceId());
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                System.out.println("Error adding sales payment: " + e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Database error adding sales payment: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePayment(SalesPayment payment) {
        if (payment == null || payment.getSalesPaymentId() <= 0) {
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            try {
                conn.setAutoCommit(false);
                updatePayment(conn, payment);
                refreshInvoicePaymentSummary(conn, payment.getSalesInvoiceId());
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                System.out.println("Error updating sales payment: " + e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Database error updating sales payment: " + e.getMessage());
            return false;
        }
    }

    public boolean deletePayment(SalesPayment payment) {
        if (payment == null || payment.getSalesPaymentId() <= 0) {
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            try {
                conn.setAutoCommit(false);
                deletePayment(conn, payment.getSalesPaymentId());
                refreshInvoicePaymentSummary(conn, payment.getSalesInvoiceId());
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                System.out.println("Error deleting sales payment: " + e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Database error deleting sales payment: " + e.getMessage());
            return false;
        }
    }

    public List<SalesPayment> getPaymentsForInvoice(int salesInvoiceId) {
        List<SalesPayment> payments = new ArrayList<>();
        String sql = """
                SELECT sales_payment_id, sales_invoice_id, payment_date, amount, payment_type
                FROM SalesPayment
                WHERE sales_invoice_id = ?
                ORDER BY payment_date, sales_payment_id
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, salesInvoiceId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapPayment(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading sales payments: " + e.getMessage());
        }

        return payments;
    }

    public BigDecimal getTotalPaid(int salesInvoiceId) {
        try (Connection conn = DBConnection.getConnection()) {
            return getTotalPaid(conn, salesInvoiceId);
        } catch (SQLException e) {
            System.out.println("Error loading total paid: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getCustomerCredit(int clientId) {
        String sql = """
                SELECT COALESCE(SUM(
                    CASE
                        WHEN si.payment > si.amount THEN si.payment - si.amount
                        ELSE 0
                    END
                ), 0) AS credit
                FROM SalesInvoice si
                WHERE si.client_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clientId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal credit = rs.getBigDecimal("credit");
                    return credit == null ? BigDecimal.ZERO : credit;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading customer credit: " + e.getMessage());
        }

        return BigDecimal.ZERO;
    }

    public void addPayment(Connection conn, SalesPayment payment) throws SQLException {
        validatePayment(payment);

        String sql = """
                INSERT INTO SalesPayment
                (sales_invoice_id, payment_date, amount, payment_type)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, payment.getSalesInvoiceId());
            stmt.setDate(2, Date.valueOf(payment.getPaymentDate()));
            stmt.setBigDecimal(3, payment.getAmount());
            stmt.setString(4, payment.getPaymentType());
            stmt.executeUpdate();
        }
    }

    public void refreshInvoicePaymentSummary(Connection conn, int salesInvoiceId) throws SQLException {
        BigDecimal totalPaid = getTotalPaid(conn, salesInvoiceId);
        String latestType = getLatestPaymentType(conn, salesInvoiceId);

        if (latestType == null || latestType.isBlank()) {
            latestType = getCurrentInvoicePaymentType(conn, salesInvoiceId);
        }

        String sql = """
                UPDATE SalesInvoice
                SET payment = ?,
                    payment_type = ?
                WHERE sales_invoice_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, totalPaid);
            stmt.setString(2, latestType);
            stmt.setInt(3, salesInvoiceId);
            stmt.executeUpdate();
        }
    }

    private void updatePayment(Connection conn, SalesPayment payment) throws SQLException {
        validatePayment(payment);

        String sql = """
                UPDATE SalesPayment
                SET payment_date = ?,
                    amount = ?,
                    payment_type = ?
                WHERE sales_payment_id = ?
                  AND sales_invoice_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(payment.getPaymentDate()));
            stmt.setBigDecimal(2, payment.getAmount());
            stmt.setString(3, payment.getPaymentType());
            stmt.setInt(4, payment.getSalesPaymentId());
            stmt.setInt(5, payment.getSalesInvoiceId());
            stmt.executeUpdate();
        }
    }

    private void deletePayment(Connection conn, int salesPaymentId) throws SQLException {
        String sql = "DELETE FROM SalesPayment WHERE sales_payment_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, salesPaymentId);
            stmt.executeUpdate();
        }
    }

    private BigDecimal getTotalPaid(Connection conn, int salesInvoiceId) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(amount), 0) AS total_paid
                FROM SalesPayment
                WHERE sales_invoice_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, salesInvoiceId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal paid = rs.getBigDecimal("total_paid");
                    return paid == null ? BigDecimal.ZERO : paid;
                }
            }
        }

        return BigDecimal.ZERO;
    }

    private String getLatestPaymentType(Connection conn, int salesInvoiceId) throws SQLException {
        String sql = """
                SELECT payment_type
                FROM SalesPayment
                WHERE sales_invoice_id = ?
                ORDER BY payment_date DESC, sales_payment_id DESC
                LIMIT 1
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, salesInvoiceId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("payment_type") : null;
            }
        }
    }

    private String getCurrentInvoicePaymentType(Connection conn, int salesInvoiceId) throws SQLException {
        String sql = "SELECT payment_type FROM SalesInvoice WHERE sales_invoice_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, salesInvoiceId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("payment_type") : "Cash";
            }
        }
    }

    private void validatePayment(SalesPayment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment is required.");
        }
        if (payment.getSalesInvoiceId() <= 0) {
            throw new IllegalArgumentException("Select a sales invoice first.");
        }
        if (payment.getPaymentDate() == null) {
            throw new IllegalArgumentException("Payment date is required.");
        }
        if (payment.getAmount() == null || payment.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive.");
        }
        if (payment.getPaymentType() == null || payment.getPaymentType().trim().isEmpty()) {
            throw new IllegalArgumentException("Payment type is required.");
        }
    }

    private SalesPayment mapPayment(ResultSet rs) throws SQLException {
        return new SalesPayment(
                rs.getInt("sales_payment_id"),
                rs.getInt("sales_invoice_id"),
                rs.getDate("payment_date").toLocalDate(),
                rs.getBigDecimal("amount"),
                rs.getString("payment_type")
        );
    }
}
