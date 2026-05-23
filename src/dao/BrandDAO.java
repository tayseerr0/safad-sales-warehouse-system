package dao;

import db.DBConnection;
import model.Brand;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BrandDAO {

    public boolean addBrand(Brand brand) {
        String sql = """
                INSERT INTO Brand (brand_name, description)
                VALUES (?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, brand.getBrandName());
            stmt.setString(2, brand.getDescription());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error adding brand: " + e.getMessage());
            return false;
        }
    }

    public boolean updateBrand(Brand brand) {
        String sql = """
                UPDATE Brand
                SET brand_name = ?, description = ?
                WHERE brand_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, brand.getBrandName());
            stmt.setString(2, brand.getDescription());
            stmt.setInt(3, brand.getBrandId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating brand: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteBrand(int brandId) {
        String sql = "DELETE FROM Brand WHERE brand_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, brandId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting brand: " + e.getMessage());
            return false;
        }
    }

    public Brand getBrandById(int brandId) {
        String sql = """
                SELECT brand_id, brand_name, description
                FROM Brand
                WHERE brand_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, brandId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBrand(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error getting brand by ID: " + e.getMessage());
        }

        return null;
    }

    public List<Brand> getAllBrands() {
        List<Brand> brands = new ArrayList<>();

        String sql = """
                SELECT brand_id, brand_name, description
                FROM Brand
                ORDER BY brand_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                brands.add(mapResultSetToBrand(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error loading brands: " + e.getMessage());
        }

        return brands;
    }

    public List<Brand> searchBrands(String keyword) {
        List<Brand> brands = new ArrayList<>();

        String sql = """
                SELECT brand_id, brand_name, description
                FROM Brand
                WHERE brand_name LIKE ?
                   OR description LIKE ?
                ORDER BY brand_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchValue = "%" + keyword + "%";

            stmt.setString(1, searchValue);
            stmt.setString(2, searchValue);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    brands.add(mapResultSetToBrand(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error searching brands: " + e.getMessage());
        }

        return brands;
    }

    private Brand mapResultSetToBrand(ResultSet rs) throws SQLException {
        return new Brand(
                rs.getInt("brand_id"),
                rs.getString("brand_name"),
                rs.getString("description")
        );
    }
}