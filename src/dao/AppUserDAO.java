package dao;

import db.DBConnection;
import model.AppUser;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AppUserDAO {

    public AppUser getActiveUserByUsername(String username) {
        String sql = """
                SELECT *
                FROM AppUser
                WHERE username = ?
                  AND active = TRUE
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAppUser(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error loading app user: " + e.getMessage());
        }

        return null;
    }

    private AppUser mapResultSetToAppUser(ResultSet rs) throws SQLException {
        Date createdDate = rs.getDate("created_date");

        return new AppUser(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("password_salt"),
                rs.getBoolean("active"),
                createdDate == null ? null : createdDate.toLocalDate()
        );
    }
}
