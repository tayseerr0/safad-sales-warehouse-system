package dao;

import db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesReportDAO {

    public List<Object[]> getTotalSalesBetweenDates(Date startDate, Date endDate) {
        List<Object[]> rows = new ArrayList<>();

        String sql = """
                SELECT 
                    ? AS start_date,
                    ? AS end_date,
                    COALESCE(SUM(sii.quantity * sii.selling_price), 0) AS total_sales
                FROM SalesInvoice si
                JOIN SalesInvoiceItem sii ON si.sales_invoice_id = sii.sales_invoice_id
                WHERE si.invoice_date BETWEEN ? AND ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            stmt.setDate(3, startDate);
            stmt.setDate(4, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    rows.add(new Object[]{
                            rs.getDate("start_date"),
                            rs.getDate("end_date"),
                            rs.getBigDecimal("total_sales")
                    });
                }
            }

        } catch (SQLException e) {
            System.out.println("Error getting total sales: " + e.getMessage());
        }

        return rows;
    }

    public List<Object[]> getSalesInvoicesBetweenDates(Date startDate, Date endDate) {
        List<Object[]> rows = new ArrayList<>();

        String sql = """
                SELECT
                    si.sales_invoice_id,
                    si.invoice_date,
                    c.client_id,
                    c.client_name,
                    w.warehouse_id,
                    w.warehouse_name,
                    si.payment_type,
                    si.payment,
                    si.amount
                FROM SalesInvoice si
                JOIN Client c ON si.client_id = c.client_id
                JOIN Warehouse w ON si.warehouse_id = w.warehouse_id
                WHERE si.invoice_date BETWEEN ? AND ?
                ORDER BY si.invoice_date DESC, si.sales_invoice_id DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getInt("sales_invoice_id"),
                            rs.getDate("invoice_date"),
                            rs.getInt("client_id"),
                            rs.getString("client_name"),
                            rs.getInt("warehouse_id"),
                            rs.getString("warehouse_name"),
                            rs.getString("payment_type"),
                            rs.getBigDecimal("payment"),
                            rs.getBigDecimal("amount")
                    });
                }
            }

        } catch (SQLException e) {
            System.out.println("Error getting sales invoices between dates: " + e.getMessage());
        }

        return rows;
    }

    public List<Object[]> getMonthlySales(int year) {
        List<Object[]> rows = new ArrayList<>();

        String sql = """
                SELECT 
                    YEAR(si.invoice_date) AS sales_year,
                    MONTH(si.invoice_date) AS sales_month,
                    COALESCE(SUM(sii.quantity * sii.selling_price), 0) AS total_sales
                FROM SalesInvoice si
                JOIN SalesInvoiceItem sii ON si.sales_invoice_id = sii.sales_invoice_id
                WHERE YEAR(si.invoice_date) = ?
                GROUP BY YEAR(si.invoice_date), MONTH(si.invoice_date)
                ORDER BY sales_month
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, year);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getInt("sales_year"),
                            rs.getInt("sales_month"),
                            rs.getBigDecimal("total_sales")
                    });
                }
            }

        } catch (SQLException e) {
            System.out.println("Error getting monthly sales: " + e.getMessage());
        }

        return rows;
    }

    public List<Object[]> getTopCustomers(Date startDate, Date endDate) {
        List<Object[]> rows = new ArrayList<>();

        String sql = """
                SELECT 
                    c.client_id,
                    c.client_name,
                    c.client_type,
                    COALESCE(SUM(sii.quantity * sii.selling_price), 0) AS total_spent
                FROM Client c
                JOIN SalesInvoice si ON c.client_id = si.client_id
                JOIN SalesInvoiceItem sii ON si.sales_invoice_id = sii.sales_invoice_id
                WHERE si.invoice_date BETWEEN ? AND ?
                GROUP BY c.client_id, c.client_name, c.client_type
                ORDER BY total_spent DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getInt("client_id"),
                            rs.getString("client_name"),
                            rs.getString("client_type"),
                            rs.getBigDecimal("total_spent")
                    });
                }
            }

        } catch (SQLException e) {
            System.out.println("Error getting top customers: " + e.getMessage());
        }

        return rows;
    }

    public List<Object[]> getMostSoldProducts(Date startDate, Date endDate) {
        List<Object[]> rows = new ArrayList<>();

        String sql = """
                SELECT 
                    p.product_id,
                    p.product_name,
                    SUM(sii.quantity) AS total_quantity_sold,
                    COALESCE(SUM(sii.quantity * sii.selling_price), 0) AS total_sales
                FROM Product p
                JOIN SalesInvoiceItem sii ON p.product_id = sii.product_id
                JOIN SalesInvoice si ON sii.sales_invoice_id = si.sales_invoice_id
                WHERE si.invoice_date BETWEEN ? AND ?
                GROUP BY p.product_id, p.product_name
                ORDER BY total_quantity_sold DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getInt("total_quantity_sold"),
                            rs.getBigDecimal("total_sales")
                    });
                }
            }

        } catch (SQLException e) {
            System.out.println("Error getting most sold products: " + e.getMessage());
        }

        return rows;
    }

    public List<Object[]> getSalesByWarehouse(Date startDate, Date endDate) {
        List<Object[]> rows = new ArrayList<>();

        String sql = """
                SELECT 
                    w.warehouse_id,
                    w.warehouse_name,
                    COALESCE(SUM(sii.quantity * sii.selling_price), 0) AS total_sales
                FROM Warehouse w
                JOIN SalesInvoice si ON w.warehouse_id = si.warehouse_id
                JOIN SalesInvoiceItem sii ON si.sales_invoice_id = sii.sales_invoice_id
                WHERE si.invoice_date BETWEEN ? AND ?
                GROUP BY w.warehouse_id, w.warehouse_name
                ORDER BY total_sales DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getInt("warehouse_id"),
                            rs.getString("warehouse_name"),
                            rs.getBigDecimal("total_sales")
                    });
                }
            }

        } catch (SQLException e) {
            System.out.println("Error getting sales by warehouse: " + e.getMessage());
        }

        return rows;
    }

    public List<Object[]> getSalesInvoicesByWarehouse(Date startDate, Date endDate) {
        List<Object[]> rows = new ArrayList<>();

        String sql = """
                SELECT
                    w.warehouse_id,
                    w.warehouse_name,
                    si.sales_invoice_id,
                    si.invoice_date,
                    c.client_name,
                    si.amount AS invoice_amount,
                    totals.warehouse_total
                FROM SalesInvoice si
                JOIN Warehouse w ON si.warehouse_id = w.warehouse_id
                JOIN Client c ON si.client_id = c.client_id
                JOIN (
                    SELECT warehouse_id, COALESCE(SUM(amount), 0) AS warehouse_total
                    FROM SalesInvoice
                    WHERE invoice_date BETWEEN ? AND ?
                    GROUP BY warehouse_id
                ) totals ON totals.warehouse_id = si.warehouse_id
                WHERE si.invoice_date BETWEEN ? AND ?
                ORDER BY w.warehouse_name, si.invoice_date DESC, si.sales_invoice_id DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            stmt.setDate(3, startDate);
            stmt.setDate(4, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getInt("warehouse_id"),
                            rs.getString("warehouse_name"),
                            rs.getInt("sales_invoice_id"),
                            rs.getDate("invoice_date"),
                            rs.getString("client_name"),
                            rs.getBigDecimal("invoice_amount"),
                            rs.getBigDecimal("warehouse_total")
                    });
                }
            }

        } catch (SQLException e) {
            System.out.println("Error getting sales invoices by warehouse: " + e.getMessage());
        }

        return rows;
    }

    public List<Object[]> getValidWarrantyItems() {
        List<Object[]> rows = new ArrayList<>();

        String sql = """
                SELECT 
                    si.sales_invoice_id,
                    c.client_name,
                    p.product_name,
                    sii.quantity,
                    sii.warranty_end_date
                FROM SalesInvoiceItem sii
                JOIN SalesInvoice si ON sii.sales_invoice_id = si.sales_invoice_id
                JOIN Client c ON si.client_id = c.client_id
                JOIN Product p ON sii.product_id = p.product_id
                WHERE sii.warranty_end_date >= CURRENT_DATE
                ORDER BY sii.warranty_end_date
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                rows.add(new Object[]{
                        rs.getInt("sales_invoice_id"),
                        rs.getString("client_name"),
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getDate("warranty_end_date")
                });
            }

        } catch (SQLException e) {
            System.out.println("Error getting valid warranty items: " + e.getMessage());
        }

        return rows;
    }

    public List<Object[]> getSalesByClientType(Date startDate, Date endDate) {
        List<Object[]> rows = new ArrayList<>();

        String sql = """
                SELECT 
                    c.client_type,
                    COALESCE(SUM(sii.quantity * sii.selling_price), 0) AS total_sales
                FROM Client c
                JOIN SalesInvoice si ON c.client_id = si.client_id
                JOIN SalesInvoiceItem sii ON si.sales_invoice_id = sii.sales_invoice_id
                WHERE si.invoice_date BETWEEN ? AND ?
                GROUP BY c.client_type
                ORDER BY total_sales DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getString("client_type"),
                            rs.getBigDecimal("total_sales")
                    });
                }
            }

        } catch (SQLException e) {
            System.out.println("Error getting sales by client type: " + e.getMessage());
        }

        return rows;
    }

    public List<Object[]> getMostSoldCategories(Date startDate, Date endDate) {
        List<Object[]> rows = new ArrayList<>();

        String sql = """
                SELECT 
                    c.category_id,
                    c.category_name,
                    SUM(sii.quantity) AS total_quantity_sold,
                    COALESCE(SUM(sii.quantity * sii.selling_price), 0) AS total_sales
                FROM Category c
                JOIN Product p ON c.category_id = p.category_id
                JOIN SalesInvoiceItem sii ON p.product_id = sii.product_id
                JOIN SalesInvoice si ON sii.sales_invoice_id = si.sales_invoice_id
                WHERE si.invoice_date BETWEEN ? AND ?
                GROUP BY c.category_id, c.category_name
                ORDER BY total_quantity_sold DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getInt("category_id"),
                            rs.getString("category_name"),
                            rs.getInt("total_quantity_sold"),
                            rs.getBigDecimal("total_sales")
                    });
                }
            }

        } catch (SQLException e) {
            System.out.println("Error getting most sold categories: " + e.getMessage());
        }

        return rows;
    }

    public List<Object[]> getSalesByClientCity(Date startDate, Date endDate) {
        List<Object[]> rows = new ArrayList<>();

        String sql = """
                SELECT 
                    c.city,
                    COALESCE(SUM(sii.quantity * sii.selling_price), 0) AS total_sales
                FROM Client c
                JOIN SalesInvoice si ON c.client_id = si.client_id
                JOIN SalesInvoiceItem sii ON si.sales_invoice_id = sii.sales_invoice_id
                WHERE si.invoice_date BETWEEN ? AND ?
                GROUP BY c.city
                ORDER BY total_sales DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getString("city"),
                            rs.getBigDecimal("total_sales")
                    });
                }
            }

        } catch (SQLException e) {
            System.out.println("Error getting sales by city: " + e.getMessage());
        }

        return rows;
    }
}
