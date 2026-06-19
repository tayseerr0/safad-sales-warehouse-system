package dao;

import db.DBConnection;
import model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    public boolean addSupplier(Supplier supplier) {
        String sql = """
                INSERT INTO Supplier
                (supplier_name, phone, email, starting_date, city, country, address)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, supplier.getSupplierName());
            stmt.setString(2, supplier.getPhone());
            stmt.setString(3, supplier.getEmail());

            if (supplier.getStartingDate() != null) {
                stmt.setDate(4, Date.valueOf(supplier.getStartingDate()));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            stmt.setString(5, supplier.getCity());
            stmt.setString(6, supplier.getCountry());
            stmt.setString(7, supplier.getAddress());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error adding supplier: " + e.getMessage());
            return false;
        }
    }

    public boolean updateSupplier(Supplier supplier) {
        String sql = """
                UPDATE Supplier
                SET supplier_name = ?,
                    phone = ?,
                    email = ?,
                    starting_date = ?,
                    city = ?,
                    country = ?,
                    address = ?
                WHERE supplier_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, supplier.getSupplierName());
            stmt.setString(2, supplier.getPhone());
            stmt.setString(3, supplier.getEmail());

            if (supplier.getStartingDate() != null) {
                stmt.setDate(4, Date.valueOf(supplier.getStartingDate()));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            stmt.setString(5, supplier.getCity());
            stmt.setString(6, supplier.getCountry());
            stmt.setString(7, supplier.getAddress());
            stmt.setInt(8, supplier.getSupplierId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating supplier: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteSupplier(int supplierId) {
        String sql = "DELETE FROM Supplier WHERE supplier_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supplierId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting supplier: " + e.getMessage());
            return false;
        }
    }

    public Supplier getSupplierById(int supplierId) {
        String sql = """
                SELECT supplier_id, supplier_name, phone, email, starting_date, city, country, address
                FROM Supplier
                WHERE supplier_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supplierId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSupplier(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error getting supplier by ID: " + e.getMessage());
        }

        return null;
    }

    public List<Supplier> getAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();

        String sql = """
                SELECT supplier_id, supplier_name, phone, email, starting_date, city, country, address
                FROM Supplier
                ORDER BY supplier_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                suppliers.add(mapResultSetToSupplier(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error loading suppliers: " + e.getMessage());
        }

        return suppliers;
    }

    public List<Supplier> searchSuppliers(String keyword) {
        List<Supplier> suppliers = new ArrayList<>();

        String sql = """
                SELECT supplier_id, supplier_name, phone, email, starting_date, city, country, address
                FROM Supplier
                WHERE supplier_name LIKE ?
                   OR phone LIKE ?
                   OR email LIKE ?
                   OR city LIKE ?
                   OR country LIKE ?
                   OR address LIKE ?
                ORDER BY supplier_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchValue = "%" + keyword + "%";

            stmt.setString(1, searchValue);
            stmt.setString(2, searchValue);
            stmt.setString(3, searchValue);
            stmt.setString(4, searchValue);
            stmt.setString(5, searchValue);
            stmt.setString(6, searchValue);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    suppliers.add(mapResultSetToSupplier(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error searching suppliers: " + e.getMessage());
        }

        return suppliers;
    }

    private Supplier mapResultSetToSupplier(ResultSet rs) throws SQLException {
        Date startingDate = rs.getDate("starting_date");

        return new Supplier(
                rs.getInt("supplier_id"),
                rs.getString("supplier_name"),
                rs.getString("phone"),
                rs.getString("email"),
                startingDate != null ? startingDate.toLocalDate() : null,
                rs.getString("city"),
                rs.getString("country"),
                rs.getString("address")
        );
    }
}
