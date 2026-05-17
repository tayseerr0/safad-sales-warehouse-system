package dao;

import db.DBConnection;
import model.WarehouseTransfer;
import model.WarehouseTransferItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WarehouseTransferDAO {

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    public boolean createTransfer(WarehouseTransfer transfer) {
        if (transfer == null) {
            System.out.println("Transfer cannot be null.");
            return false;
        }

        if (transfer.getFromWarehouseId() == transfer.getToWarehouseId()) {
            System.out.println("Source and destination warehouses cannot be the same.");
            return false;
        }

        if (transfer.getItems() == null || transfer.getItems().isEmpty()) {
            System.out.println("Transfer must contain at least one item.");
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            try {
                conn.setAutoCommit(false);

                validateTransferItems(conn, transfer);

                int transferId = insertTransferHeader(conn, transfer);

                for (WarehouseTransferItem item : transfer.getItems()) {
                    insertTransferItem(conn, transferId, item);

                    inventoryDAO.decreaseStock(
                            conn,
                            item.getProductId(),
                            transfer.getFromWarehouseId(),
                            item.getQuantity()
                    );

                    inventoryDAO.increaseStock(
                            conn,
                            item.getProductId(),
                            transfer.getToWarehouseId(),
                            item.getQuantity()
                    );
                }

                conn.commit();
                return true;

            } catch (Exception e) {
                conn.rollback();
                System.out.println("Transfer failed. Rolled back changes.");
                System.out.println(e.getMessage());
                return false;

            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Database error while creating transfer: " + e.getMessage());
            return false;
        }
    }

    private void validateTransferItems(Connection conn, WarehouseTransfer transfer) throws SQLException {
        Map<Integer, Integer> totalQuantityPerProduct = new HashMap<>();

        for (WarehouseTransferItem item : transfer.getItems()) {
            if (item.getProductId() <= 0) {
                throw new IllegalArgumentException("Invalid product ID.");
            }

            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Transfer quantity must be greater than zero.");
            }

            totalQuantityPerProduct.put(
                    item.getProductId(),
                    totalQuantityPerProduct.getOrDefault(item.getProductId(), 0) + item.getQuantity()
            );
        }

        for (Map.Entry<Integer, Integer> entry : totalQuantityPerProduct.entrySet()) {
            int productId = entry.getKey();
            int totalRequestedQuantity = entry.getValue();

            int availableStock = inventoryDAO.getAvailableStock(
                    conn,
                    productId,
                    transfer.getFromWarehouseId()
            );

            if (availableStock < totalRequestedQuantity) {
                throw new IllegalArgumentException(
                        "Not enough stock for product ID " + productId +
                                ". Available: " + availableStock +
                                ", requested: " + totalRequestedQuantity
                );
            }
        }
    }

    private int insertTransferHeader(Connection conn, WarehouseTransfer transfer) throws SQLException {
        String sql = """
                INSERT INTO WarehouseTransfer
                (transfer_date, from_warehouse_id, to_warehouse_id)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDate(1, Date.valueOf(transfer.getTransferDate()));
            stmt.setInt(2, transfer.getFromWarehouseId());
            stmt.setInt(3, transfer.getToWarehouseId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating transfer failed. No rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating transfer failed. No ID obtained.");
                }
            }
        }
    }

    private void insertTransferItem(Connection conn, int transferId, WarehouseTransferItem item) throws SQLException {
        String sql = """
                INSERT INTO WarehouseTransferItem
                (transfer_id, product_id, quantity)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transferId);
            stmt.setInt(2, item.getProductId());
            stmt.setInt(3, item.getQuantity());

            stmt.executeUpdate();
        }
    }

    public List<WarehouseTransfer> getAllTransfers() {
        List<WarehouseTransfer> transfers = new ArrayList<>();

        String sql = """
                SELECT transfer_id, transfer_date, from_warehouse_id, to_warehouse_id
                FROM WarehouseTransfer
                ORDER BY transfer_date DESC, transfer_id DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                WarehouseTransfer transfer = new WarehouseTransfer(
                        rs.getInt("transfer_id"),
                        rs.getDate("transfer_date").toLocalDate(),
                        rs.getInt("from_warehouse_id"),
                        rs.getInt("to_warehouse_id")
                );

                transfers.add(transfer);
            }

        } catch (SQLException e) {
            System.out.println("Error loading transfers: " + e.getMessage());
        }

        return transfers;
    }

    public List<WarehouseTransferItem> getTransferItems(int transferId) {
        List<WarehouseTransferItem> items = new ArrayList<>();

        String sql = """
                SELECT transfer_item_id, transfer_id, product_id, quantity
                FROM WarehouseTransferItem
                WHERE transfer_id = ?
                ORDER BY transfer_item_id
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, transferId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    WarehouseTransferItem item = new WarehouseTransferItem(
                            rs.getInt("transfer_item_id"),
                            rs.getInt("transfer_id"),
                            rs.getInt("product_id"),
                            rs.getInt("quantity")
                    );

                    items.add(item);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error loading transfer items: " + e.getMessage());
        }

        return items;
    }
}