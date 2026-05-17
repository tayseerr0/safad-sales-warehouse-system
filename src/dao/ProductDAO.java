package dao;

import db.DBConnection;
import model.Product;

import java.math.BigDecimal;
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
        String sql = baseProductQuery() + " WHERE p.product_id = ?";

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
        return filterProducts(null, null, null, null, null);
    }

    public List<Product> searchProducts(String keyword) {
        return filterProducts(keyword, null, null, null, null);
    }

    public List<Product> getProductsByCategory(int categoryId) {
        return filterProducts(null, categoryId, null, null, null);
    }

    public List<Product> getProductsByBrand(int brandId) {
        return filterProducts(null, null, brandId, null, null);
    }

    public List<Product> getProductsWithCategoryAndBrand() {
        return getAllProducts();
    }

    public List<Product> filterProducts(String keyword,
                                        Integer categoryId,
                                        Integer brandId,
                                        BigDecimal minPrice,
                                        BigDecimal maxPrice) {
        List<Product> products = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder(baseProductQuery());
        sql.append(" WHERE 1 = 1 ");

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("""
                    AND (
                        p.product_name LIKE ?
                        OR p.description LIKE ?
                        OR b.brand_name LIKE ?
                        OR c.category_name LIKE ?
                    )
                    """);

            String searchValue = "%" + keyword.trim() + "%";
            params.add(searchValue);
            params.add(searchValue);
            params.add(searchValue);
            params.add(searchValue);
        }

        if (categoryId != null) {
            sql.append(" AND p.category_id = ? ");
            params.add(categoryId);
        }

        if (brandId != null) {
            sql.append(" AND p.brand_id = ? ");
            params.add(brandId);
        }

        if (minPrice != null) {
            sql.append(" AND p.default_selling_price >= ? ");
            params.add(minPrice);
        }

        if (maxPrice != null) {
            sql.append(" AND p.default_selling_price <= ? ");
            params.add(maxPrice);
        }

        sql.append(" ORDER BY p.product_name ");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object value = params.get(i);

                if (value instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) value);
                } else if (value instanceof BigDecimal) {
                    stmt.setBigDecimal(i + 1, (BigDecimal) value);
                } else {
                    stmt.setString(i + 1, value.toString());
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapJoinedResultSetToProduct(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error filtering products: " + e.getMessage());
        }

        return products;
    }

    private String baseProductQuery() {
        return """
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
                """;
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
