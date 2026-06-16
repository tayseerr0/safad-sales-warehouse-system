package dao;

import db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventoryDAO {

    // =========================================================
    // Normal standalone methods
    // These open their own connection.
    // Good for simple testing and simple screens.
    // =========================================================

    public int getAvailableStock(int productId, int warehouseId) {
        try (Connection conn = DBConnection.getConnection()) {
            return getAvailableStock(conn, productId, warehouseId);
        } catch (SQLException e) {
            System.out.println("Error getting available stock: " + e.getMessage());
            return 0;
        }
    }

    public boolean productExistsInWarehouse(int productId, int warehouseId) {
        try (Connection conn = DBConnection.getConnection()) {
            return productExistsInWarehouse(conn, productId, warehouseId);
        } catch (SQLException e) {
            System.out.println("Error checking inventory record: " + e.getMessage());
            return false;
        }
    }

    public boolean increaseStock(int productId, int warehouseId, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            return increaseStock(conn, productId, warehouseId, quantity);
        } catch (SQLException e) {
            System.out.println("Error increasing stock: " + e.getMessage());
            return false;
        }
    }

    public boolean decreaseStock(int productId, int warehouseId, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            return decreaseStock(conn, productId, warehouseId, quantity);
        } catch (SQLException e) {
            System.out.println("Error decreasing stock: " + e.getMessage());
            return false;
        }
    }

    public boolean setThreshold(int productId, int warehouseId, int threshold) {
        if (threshold < 0) {
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            return setThreshold(conn, productId, warehouseId, threshold);
        } catch (SQLException e) {
            System.out.println("Error setting threshold: " + e.getMessage());
            return false;
        }
    }

    public int getWarehouseTotalQuantity(int warehouseId) {
        try (Connection conn = DBConnection.getConnection()) {
            return getWarehouseTotalQuantity(conn, warehouseId);
        } catch (SQLException e) {
            System.out.println("Error getting warehouse total quantity: " + e.getMessage());
            return 0;
        }
    }

    // =========================================================
    // Transaction-safe methods
    // These use an existing Connection.
    // Use these inside PurchaseInvoiceDAO, SalesInvoiceDAO,
    // and WarehouseTransferDAO.
    // =========================================================

    public int getAvailableStock(Connection conn, int productId, int warehouseId) throws SQLException {
        String sql = """
                SELECT quantity
                FROM Inventory
                WHERE product_id = ? AND warehouse_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.setInt(2, warehouseId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity");
                }
                return 0;
            }
        }
    }

    public boolean productExistsInWarehouse(Connection conn, int productId, int warehouseId) throws SQLException {
        String sql = """
                SELECT 1
                FROM Inventory
                WHERE product_id = ? AND warehouse_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.setInt(2, warehouseId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean increaseStock(Connection conn, int productId, int warehouseId, int quantity) throws SQLException {
        if (quantity <= 0) {
            return false;
        }

        if (productExistsInWarehouse(conn, productId, warehouseId)) {
            return updateStock(conn, productId, warehouseId, quantity);
        } else {
            return insertStock(conn, productId, warehouseId, quantity);
        }
    }

    public boolean decreaseStock(Connection conn, int productId, int warehouseId, int quantity) throws SQLException {
        if (quantity <= 0) {
            return false;
        }

        int availableStock = getAvailableStock(conn, productId, warehouseId);

        if (availableStock < quantity) {
            return false;
        }

        return updateStock(conn, productId, warehouseId, -quantity);
    }

    public boolean setThreshold(Connection conn, int productId, int warehouseId, int threshold) throws SQLException {
        if (threshold < 0) {
            return false;
        }

        String sql = """
                UPDATE Inventory
                SET threshold = ?
                WHERE product_id = ? AND warehouse_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, threshold);
            stmt.setInt(2, productId);
            stmt.setInt(3, warehouseId);

            return stmt.executeUpdate() > 0;
        }
    }

    public int getWarehouseTotalQuantity(Connection conn, int warehouseId) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(quantity), 0) AS total_quantity
                FROM Inventory
                WHERE warehouse_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, warehouseId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("total_quantity") : 0;
            }
        }
    }

    // =========================================================
    // Private helper methods
    // =========================================================

    private boolean updateStock(Connection conn, int productId, int warehouseId, int quantityChange) throws SQLException {
        String sql = """
                UPDATE Inventory
                SET quantity = quantity + ?
                WHERE product_id = ? AND warehouse_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantityChange);
            stmt.setInt(2, productId);
            stmt.setInt(3, warehouseId);

            return stmt.executeUpdate() > 0;
        }
    }

    private boolean insertStock(Connection conn, int productId, int warehouseId, int quantity) throws SQLException {
        String sql = """
                INSERT INTO Inventory (product_id, warehouse_id, quantity, threshold)
                VALUES (?, ?, ?, 0)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.setInt(2, warehouseId);
            stmt.setInt(3, quantity);

            return stmt.executeUpdate() > 0;
        }
    }
}
