package dao;

import db.DBConnection;
import model.Client;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    public boolean addClient(Client client) {
        String sql = """
                INSERT INTO Client
                (client_name, phone, email, registration_date, city, address, client_type)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, client.getClientName());
            stmt.setString(2, client.getPhone());
            stmt.setString(3, client.getEmail());
            stmt.setDate(4, Date.valueOf(client.getRegistrationDate()));
            stmt.setString(5, client.getCity());
            stmt.setString(6, client.getAddress());
            stmt.setString(7, client.getClientType());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error adding client: " + e.getMessage());
            return false;
        }
    }

    public boolean updateClient(Client client) {
        String sql = """
                UPDATE Client
                SET client_name = ?,
                    phone = ?,
                    email = ?,
                    registration_date = ?,
                    city = ?,
                    address = ?,
                    client_type = ?
                WHERE client_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, client.getClientName());
            stmt.setString(2, client.getPhone());
            stmt.setString(3, client.getEmail());
            stmt.setDate(4, Date.valueOf(client.getRegistrationDate()));
            stmt.setString(5, client.getCity());
            stmt.setString(6, client.getAddress());
            stmt.setString(7, client.getClientType());
            stmt.setInt(8, client.getClientId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating client: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteClient(int clientId) {
        String sql = """
                DELETE FROM Client
                WHERE client_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clientId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting client: " + e.getMessage());
            return false;
        }
    }

    public Client getClientById(int clientId) {
        String sql = """
                SELECT *
                FROM Client
                WHERE client_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clientId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToClient(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error getting client by ID: " + e.getMessage());
        }

        return null;
    }

    public List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();

        String sql = """
                SELECT *
                FROM Client
                ORDER BY client_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error loading clients: " + e.getMessage());
        }

        return clients;
    }

    public List<Client> searchClients(String keyword) {
        List<Client> clients = new ArrayList<>();

        String sql = """
                SELECT *
                FROM Client
                WHERE client_name LIKE ?
                   OR phone LIKE ?
                   OR email LIKE ?
                   OR city LIKE ?
                   OR client_type LIKE ?
                ORDER BY client_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchValue = "%" + keyword + "%";

            stmt.setString(1, searchValue);
            stmt.setString(2, searchValue);
            stmt.setString(3, searchValue);
            stmt.setString(4, searchValue);
            stmt.setString(5, searchValue);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    clients.add(mapResultSetToClient(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error searching clients: " + e.getMessage());
        }

        return clients;
    }

    public List<Client> getClientsByType(String clientType) {
        List<Client> clients = new ArrayList<>();

        String sql = """
                SELECT *
                FROM Client
                WHERE client_type = ?
                ORDER BY client_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, clientType);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    clients.add(mapResultSetToClient(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error filtering clients by type: " + e.getMessage());
        }

        return clients;
    }

    private Client mapResultSetToClient(ResultSet rs) throws SQLException {
        return new Client(
                rs.getInt("client_id"),
                rs.getString("client_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getDate("registration_date").toLocalDate(),
                rs.getString("city"),
                rs.getString("address"),
                rs.getString("client_type")
        );
    }
}