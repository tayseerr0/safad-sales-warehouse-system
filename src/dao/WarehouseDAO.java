package dao;

import db.DBConnection;
import model.Warehouse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WarehouseDAO {

    public boolean addWarehouse(Warehouse warehouse) {
        String sql = """
                INSERT INTO Warehouse
                (warehouse_name, location, capacity)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, warehouse.getWarehouseName());
            stmt.setString(2, warehouse.getLocation());
            stmt.setInt(3, warehouse.getCapacity());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error adding warehouse: " + e.getMessage());
            return false;
        }
    }

    public boolean updateWarehouse(Warehouse warehouse) {
        String sql = """
                UPDATE Warehouse
                SET warehouse_name = ?,
                    location = ?,
                    capacity = ?
                WHERE warehouse_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, warehouse.getWarehouseName());
            stmt.setString(2, warehouse.getLocation());
            stmt.setInt(3, warehouse.getCapacity());
            stmt.setInt(4, warehouse.getWarehouseId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating warehouse: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteWarehouse(int warehouseId) {
        String sql = """
                DELETE FROM Warehouse
                WHERE warehouse_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, warehouseId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting warehouse: " + e.getMessage());
            return false;
        }
    }

    public Warehouse getWarehouseById(int warehouseId) {
        String sql = """
                SELECT *
                FROM Warehouse
                WHERE warehouse_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, warehouseId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToWarehouse(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error getting warehouse by ID: " + e.getMessage());
        }

        return null;
    }

    public List<Warehouse> getAllWarehouses() {
        List<Warehouse> warehouses = new ArrayList<>();

        String sql = """
                SELECT *
                FROM Warehouse
                ORDER BY warehouse_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                warehouses.add(mapResultSetToWarehouse(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error loading warehouses: " + e.getMessage());
        }

        return warehouses;
    }

    public List<Warehouse> searchWarehouses(String keyword) {
        List<Warehouse> warehouses = new ArrayList<>();

        String sql = """
                SELECT *
                FROM Warehouse
                WHERE warehouse_name LIKE ?
                   OR location LIKE ?
                ORDER BY warehouse_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchValue = "%" + keyword + "%";

            stmt.setString(1, searchValue);
            stmt.setString(2, searchValue);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    warehouses.add(mapResultSetToWarehouse(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error searching warehouses: " + e.getMessage());
        }

        return warehouses;
    }

    private Warehouse mapResultSetToWarehouse(ResultSet rs) throws SQLException {
        return new Warehouse(
                rs.getInt("warehouse_id"),
                rs.getString("warehouse_name"),
                rs.getString("location"),
                rs.getInt("capacity")
        );
    }
}