package dao;

import db.DBConnection;
import model.SupplierProduct;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierProductDAO {

    public boolean linkSupplierToProduct(int supplierId, int productId, BigDecimal supplyPrice) {
        String sql = """
                INSERT INTO SupplierProduct (supplier_id, product_id, supply_price)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supplierId);
            stmt.setInt(2, productId);
            stmt.setBigDecimal(3, supplyPrice);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error linking supplier to product: " + e.getMessage());
            return false;
        }
    }

    public boolean updateSupplyPrice(int supplierId, int productId, BigDecimal supplyPrice) {
        String sql = """
                UPDATE SupplierProduct
                SET supply_price = ?
                WHERE supplier_id = ? AND product_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, supplyPrice);
            stmt.setInt(2, supplierId);
            stmt.setInt(3, productId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating supply price: " + e.getMessage());
            return false;
        }
    }

    public boolean removeSupplierProduct(int supplierId, int productId) {
        String sql = """
                DELETE FROM SupplierProduct
                WHERE supplier_id = ? AND product_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supplierId);
            stmt.setInt(2, productId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error removing supplier-product link: " + e.getMessage());
            return false;
        }
    }

    public boolean supplierProductExists(int supplierId, int productId) {
        String sql = """
                SELECT 1
                FROM SupplierProduct
                WHERE supplier_id = ? AND product_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supplierId);
            stmt.setInt(2, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.out.println("Error checking supplier-product link: " + e.getMessage());
            return false;
        }
    }

    public List<SupplierProduct> getAllSupplierProducts() {
        List<SupplierProduct> links = new ArrayList<>();

        String sql = """
                SELECT sp.supplier_id,
                       s.supplier_name,
                       sp.product_id,
                       p.product_name,
                       sp.supply_price
                FROM SupplierProduct sp
                JOIN Supplier s ON sp.supplier_id = s.supplier_id
                JOIN Product p ON sp.product_id = p.product_id
                ORDER BY s.supplier_name, p.product_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                links.add(mapJoinedResultSetToSupplierProduct(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error loading supplier-product links: " + e.getMessage());
        }

        return links;
    }

    public List<SupplierProduct> getProductsBySupplier(int supplierId) {
        List<SupplierProduct> links = new ArrayList<>();

        String sql = """
                SELECT sp.supplier_id,
                       s.supplier_name,
                       sp.product_id,
                       p.product_name,
                       sp.supply_price
                FROM SupplierProduct sp
                JOIN Supplier s ON sp.supplier_id = s.supplier_id
                JOIN Product p ON sp.product_id = p.product_id
                WHERE sp.supplier_id = ?
                ORDER BY p.product_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supplierId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    links.add(mapJoinedResultSetToSupplierProduct(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error loading products by supplier: " + e.getMessage());
        }

        return links;
    }

    public List<SupplierProduct> getSuppliersByProduct(int productId) {
        List<SupplierProduct> links = new ArrayList<>();

        String sql = """
                SELECT sp.supplier_id,
                       s.supplier_name,
                       sp.product_id,
                       p.product_name,
                       sp.supply_price
                FROM SupplierProduct sp
                JOIN Supplier s ON sp.supplier_id = s.supplier_id
                JOIN Product p ON sp.product_id = p.product_id
                WHERE sp.product_id = ?
                ORDER BY s.supplier_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    links.add(mapJoinedResultSetToSupplierProduct(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error loading suppliers by product: " + e.getMessage());
        }

        return links;
    }

    private SupplierProduct mapJoinedResultSetToSupplierProduct(ResultSet rs) throws SQLException {
        return new SupplierProduct(
                rs.getInt("supplier_id"),
                rs.getString("supplier_name"),
                rs.getInt("product_id"),
                rs.getString("product_name"),
                rs.getBigDecimal("supply_price")
        );
    }
}