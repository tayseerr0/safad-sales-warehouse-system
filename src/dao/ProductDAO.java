package dao;

import db.DBConnection;
import model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public boolean addProduct(Product product) {
        String sql = """
                INSERT INTO Product
                (product_name, description, default_selling_price, brand_id, category_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getDescription());
            stmt.setBigDecimal(3, product.getDefaultSellingPrice());
            stmt.setInt(4, product.getBrandId());
            stmt.setInt(5, product.getCategoryId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error adding product: " + e.getMessage());
            return false;
        }
    }

    public boolean updateProduct(Product product) {
        String sql = """
                UPDATE Product
                SET product_name = ?,
                    description = ?,
                    default_selling_price = ?,
                    brand_id = ?,
                    category_id = ?
                WHERE product_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getDescription());
            stmt.setBigDecimal(3, product.getDefaultSellingPrice());
            stmt.setInt(4, product.getBrandId());
            stmt.setInt(5, product.getCategoryId());
            stmt.setInt(6, product.getProductId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating product: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteProduct(int productId) {
        String sql = "DELETE FROM Product WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting product: " + e.getMessage());
            return false;
        }
    }

    public Product getProductById(int productId) {
        String sql = """
                SELECT p.product_id,
                       p.product_name,
                       p.description,
                       p.default_selling_price,
                       p.brand_id,
                       b.brand_name,
                       p.category_id,
                       c.category_name
                FROM Product p
                JOIN Brand b ON p.brand_id = b.brand_id
                JOIN Category c ON p.category_id = c.category_id
                WHERE p.product_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapJoinedResultSetToProduct(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error getting product by ID: " + e.getMessage());
        }

        return null;
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();

        String sql = """
                SELECT p.product_id,
                       p.product_name,
                       p.description,
                       p.default_selling_price,
                       p.brand_id,
                       b.brand_name,
                       p.category_id,
                       c.category_name
                FROM Product p
                JOIN Brand b ON p.brand_id = b.brand_id
                JOIN Category c ON p.category_id = c.category_id
                ORDER BY p.product_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapJoinedResultSetToProduct(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error loading products: " + e.getMessage());
        }

        return products;
    }

    public List<Product> searchProducts(String keyword) {
        List<Product> products = new ArrayList<>();

        String sql = """
                SELECT p.product_id,
                       p.product_name,
                       p.description,
                       p.default_selling_price,
                       p.brand_id,
                       b.brand_name,
                       p.category_id,
                       c.category_name
                FROM Product p
                JOIN Brand b ON p.brand_id = b.brand_id
                JOIN Category c ON p.category_id = c.category_id
                WHERE p.product_name LIKE ?
                   OR p.description LIKE ?
                   OR b.brand_name LIKE ?
                   OR c.category_name LIKE ?
                ORDER BY p.product_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchValue = "%" + keyword + "%";

            stmt.setString(1, searchValue);
            stmt.setString(2, searchValue);
            stmt.setString(3, searchValue);
            stmt.setString(4, searchValue);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapJoinedResultSetToProduct(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error searching products: " + e.getMessage());
        }

        return products;
    }

    public List<Product> getProductsByCategory(int categoryId) {
        List<Product> products = new ArrayList<>();

        String sql = """
                SELECT p.product_id,
                       p.product_name,
                       p.description,
                       p.default_selling_price,
                       p.brand_id,
                       b.brand_name,
                       p.category_id,
                       c.category_name
                FROM Product p
                JOIN Brand b ON p.brand_id = b.brand_id
                JOIN Category c ON p.category_id = c.category_id
                WHERE p.category_id = ?
                ORDER BY p.product_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapJoinedResultSetToProduct(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error filtering products by category: " + e.getMessage());
        }

        return products;
    }

    public List<Product> getProductsByBrand(int brandId) {
        List<Product> products = new ArrayList<>();

        String sql = """
                SELECT p.product_id,
                       p.product_name,
                       p.description,
                       p.default_selling_price,
                       p.brand_id,
                       b.brand_name,
                       p.category_id,
                       c.category_name
                FROM Product p
                JOIN Brand b ON p.brand_id = b.brand_id
                JOIN Category c ON p.category_id = c.category_id
                WHERE p.brand_id = ?
                ORDER BY p.product_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, brandId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapJoinedResultSetToProduct(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error filtering products by brand: " + e.getMessage());
        }

        return products;
    }

    public List<Product> getProductsWithCategoryAndBrand() {
        return getAllProducts();
    }

    private Product mapJoinedResultSetToProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("product_id"),
                rs.getString("product_name"),
                rs.getString("description"),
                rs.getBigDecimal("default_selling_price"),
                rs.getInt("brand_id"),
                rs.getString("brand_name"),
                rs.getInt("category_id"),
                rs.getString("category_name")
        );
    }
}