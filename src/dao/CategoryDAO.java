package dao;

import db.DBConnection;
import model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public boolean addCategory(Category category) {
        String sql = """
                INSERT INTO Category (category_name, description, category_type)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.getCategoryName());
            stmt.setString(2, category.getDescription());
            stmt.setString(3, category.getCategoryType());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error adding category: " + e.getMessage());
            return false;
        }
    }

    public boolean updateCategory(Category category) {
        String sql = """
                UPDATE Category
                SET category_name = ?, description = ?, category_type = ?
                WHERE category_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.getCategoryName());
            stmt.setString(2, category.getDescription());
            stmt.setString(3, category.getCategoryType());
            stmt.setInt(4, category.getCategoryId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating category: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteCategory(int categoryId) {
        String sql = "DELETE FROM Category WHERE category_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting category: " + e.getMessage());
            return false;
        }
    }

    public Category getCategoryById(int categoryId) {
        String sql = """
                SELECT category_id, category_name, description, category_type
                FROM Category
                WHERE category_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategory(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error getting category by ID: " + e.getMessage());
        }

        return null;
    }

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();

        String sql = """
                SELECT category_id, category_name, description, category_type
                FROM Category
                ORDER BY category_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error loading categories: " + e.getMessage());
        }

        return categories;
    }

    public List<Category> searchCategories(String keyword) {
        List<Category> categories = new ArrayList<>();

        String sql = """
                SELECT category_id, category_name, description, category_type
                FROM Category
                WHERE category_name LIKE ?
                   OR description LIKE ?
                   OR category_type LIKE ?
                ORDER BY category_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchValue = "%" + keyword + "%";

            stmt.setString(1, searchValue);
            stmt.setString(2, searchValue);
            stmt.setString(3, searchValue);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapResultSetToCategory(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error searching categories: " + e.getMessage());
        }

        return categories;
    }

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        return new Category(
                rs.getInt("category_id"),
                rs.getString("category_name"),
                rs.getString("description"),
                rs.getString("category_type")
        );
    }
}