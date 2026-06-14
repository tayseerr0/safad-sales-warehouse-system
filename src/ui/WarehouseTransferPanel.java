package ui;

import dao.ProductDAO;
import dao.WarehouseTransferDAO;
import db.DBConnection;
import model.Product;
import model.WarehouseTransfer;
import model.WarehouseTransferItem;
import util.MessageUtil;
import util.TableUtil;
import util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WarehouseTransferPanel extends JPanel {

    private final ProductDAO productDAO = new ProductDAO();
    private final WarehouseTransferDAO transferDAO = new WarehouseTransferDAO();

    private JComboBox<WarehouseOption> fromWarehouseComboBox;
    private JComboBox<WarehouseOption> toWarehouseComboBox;
    private JComboBox<Product> productComboBox;
    private JTextField transferDateField;
    private JTextField quantityField;

    private JTable itemTable;
    private DefaultTableModel itemTableModel;

    private JTable transferTable;
    private DefaultTableModel transferTableModel;

    private JTable transferItemsTable;
    private DefaultTableModel transferItemsTableModel;

    private final List<WarehouseTransferItem> currentItems = new ArrayList<>();
    private final Map<Integer, String> warehouseNames = new HashMap<>();
    private final Map<Integer, String> productNames = new HashMap<>();

    public WarehouseTransferPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIStyle.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        loadComboBoxes();
        loadTransfers();
    }

    private JPanel createHeaderPanel() {
        return UIStyle.createHeaderPanel(
                "Warehouse Transfers",
                "Move product quantities between warehouses and review previous transfer records."
        );
    }

    private JSplitPane createMainPanel() {
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                createTransferFormPanel(),
                createTransferHistoryPanel()
        );

        UIStyle.styleSplitPane(splitPane, 0.50);

        return splitPane;
    }

    private JPanel createTransferFormPanel() {
        JPanel panel = UIStyle.createCardPanel();

        panel.add(createTransferHeaderForm(), BorderLayout.NORTH);
        panel.add(createCurrentItemsPanel(), BorderLayout.CENTER);
        panel.add(createSavePanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTransferHeaderForm() {
        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        wrapper.setBackground(UIStyle.PANEL_BACKGROUND);

        JLabel title = new JLabel("New Transfer");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 8));
        formPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        fromWarehouseComboBox = new JComboBox<>();
        toWarehouseComboBox = new JComboBox<>();
        transferDateField = new JTextField(LocalDate.now().toString());
        productComboBox = new JComboBox<>();
        quantityField = new JTextField();

        UIStyle.styleComboBox(fromWarehouseComboBox);
        UIStyle.styleComboBox(toWarehouseComboBox);
        UIStyle.styleTextField(transferDateField);
        UIStyle.styleComboBox(productComboBox);
        UIStyle.styleTextField(quantityField);

        formPanel.add(new JLabel("From Warehouse:"));
        formPanel.add(fromWarehouseComboBox);

        formPanel.add(new JLabel("To Warehouse:"));
        formPanel.add(toWarehouseComboBox);

        formPanel.add(new JLabel("Transfer Date:"));
        formPanel.add(transferDateField);

        formPanel.add(new JLabel("Product:"));
        formPanel.add(productComboBox);

        formPanel.add(new JLabel("Quantity:"));
        formPanel.add(quantityField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        JButton addItemButton = new JButton("Add Item");
        JButton removeItemButton = new JButton("Remove Item");
        JButton clearItemsButton = new JButton("Clear Items");

        UIStyle.stylePrimaryButton(addItemButton);
        UIStyle.stylePrimaryButton(removeItemButton);
        UIStyle.stylePrimaryButton(clearItemsButton);

        addItemButton.addActionListener(e -> addItem());
        removeItemButton.addActionListener(e -> removeSelectedItem());
        clearItemsButton.addActionListener(e -> clearItems());

        buttonPanel.add(addItemButton);
        buttonPanel.add(removeItemButton);
        buttonPanel.add(clearItemsButton);

        wrapper.add(title, BorderLayout.NORTH);
        wrapper.add(formPanel, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        return wrapper;
    }

    private JPanel createCurrentItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UIStyle.PANEL_BACKGROUND);

        String[] columns = {"Product ID", "Product", "Quantity"};
        itemTableModel = TableUtil.createNonEditableTableModel(columns);
        itemTable = new JTable(itemTableModel);
        TableUtil.setupTable(itemTable);

        panel.add(new JLabel("Current Transfer Items"), BorderLayout.NORTH);
        panel.add(new JScrollPane(itemTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSavePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(UIStyle.PANEL_BACKGROUND);

        JButton saveButton = new JButton("Save Transfer");
        JButton clearButton = new JButton("Clear Form");

        UIStyle.stylePrimaryButton(saveButton);
        UIStyle.stylePrimaryButton(clearButton);

        saveButton.addActionListener(e -> saveTransfer());
        clearButton.addActionListener(e -> clearForm());

        panel.add(clearButton);
        panel.add(saveButton);

        return panel;
    }

    private JPanel createTransferHistoryPanel() {
        JPanel panel = UIStyle.createCardPanel();

        JLabel title = new JLabel("Previous Transfers");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));

        String[] transferColumns = {"Transfer ID", "Date", "From Warehouse", "To Warehouse"};
        transferTableModel = TableUtil.createNonEditableTableModel(transferColumns);
        transferTable = new JTable(transferTableModel);
        TableUtil.setupTable(transferTable);

        transferTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedTransferItems();
            }
        });

        String[] itemColumns = {"Item ID", "Transfer ID", "Product", "Quantity"};
        transferItemsTableModel = TableUtil.createNonEditableTableModel(itemColumns);
        transferItemsTable = new JTable(transferItemsTableModel);
        TableUtil.setupTable(transferItemsTable);

        JSplitPane verticalSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(transferTable),
                new JScrollPane(transferItemsTable)
        );

        UIStyle.styleSplitPane(verticalSplit, 0.60);

        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topButtons.setBackground(UIStyle.PANEL_BACKGROUND);

        JButton refreshButton = new JButton("Refresh");
        UIStyle.stylePrimaryButton(refreshButton);
        refreshButton.addActionListener(e -> loadTransfers());

        topButtons.add(refreshButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIStyle.PANEL_BACKGROUND);
        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(topButtons, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(verticalSplit, BorderLayout.CENTER);

        return panel;
    }

    private void loadComboBoxes() {
        fromWarehouseComboBox.removeAllItems();
        toWarehouseComboBox.removeAllItems();
        productComboBox.removeAllItems();

        warehouseNames.clear();
        productNames.clear();

        for (WarehouseOption warehouse : loadWarehouses()) {
            warehouseNames.put(warehouse.getWarehouseId(), warehouse.toString());

            fromWarehouseComboBox.addItem(warehouse);
            toWarehouseComboBox.addItem(
                    new WarehouseOption(warehouse.getWarehouseId(), warehouse.toString())
            );
        }

        if (toWarehouseComboBox.getItemCount() > 1) {
            toWarehouseComboBox.setSelectedIndex(1);
        }

        for (Product product : productDAO.getAllProducts()) {
            productNames.put(product.getProductId(), product.getProductName());
            productComboBox.addItem(product);
        }
    }

    private List<WarehouseOption> loadWarehouses() {
        List<WarehouseOption> warehouses = new ArrayList<>();

        String sql = "SELECT warehouse_id, warehouse_name FROM Warehouse ORDER BY warehouse_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                warehouses.add(new WarehouseOption(
                        rs.getInt("warehouse_id"),
                        rs.getString("warehouse_name")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Error loading warehouses: " + e.getMessage());
        }

        return warehouses;
    }

    private void addItem() {
        Product product = (Product) productComboBox.getSelectedItem();

        if (product == null) {
            MessageUtil.showWarning("Select a product.");
            return;
        }

        if (!ValidationUtil.isPositiveInteger(quantityField.getText())) {
            MessageUtil.showError("Quantity must be a positive integer.");
            return;
        }

        int quantity = Integer.parseInt(quantityField.getText().trim());

        WarehouseTransferItem item = new WarehouseTransferItem(product.getProductId(), quantity);

        currentItems.add(item);

        itemTableModel.addRow(new Object[]{
                product.getProductId(),
                product.getProductName(),
                quantity
        });

        quantityField.setText("");
    }

    private void removeSelectedItem() {
        int selectedRow = itemTable.getSelectedRow();

        if (selectedRow == -1) {
            MessageUtil.showWarning("Select an item to remove.");
            return;
        }

        int modelRow = itemTable.convertRowIndexToModel(selectedRow);
        currentItems.remove(modelRow);
        itemTableModel.removeRow(modelRow);
    }

    private void clearItems() {
        currentItems.clear();
        TableUtil.clearTable(itemTableModel);
    }

    private void saveTransfer() {
        WarehouseOption fromWarehouse = (WarehouseOption) fromWarehouseComboBox.getSelectedItem();
        WarehouseOption toWarehouse = (WarehouseOption) toWarehouseComboBox.getSelectedItem();

        if (fromWarehouse == null || toWarehouse == null) {
            MessageUtil.showError("Select source and destination warehouses.");
            return;
        }

        if (fromWarehouse.getWarehouseId() == toWarehouse.getWarehouseId()) {
            MessageUtil.showError("Source and destination warehouses cannot be the same.");
            return;
        }

        if (currentItems.isEmpty()) {
            MessageUtil.showError("Add at least one transfer item.");
            return;
        }

        LocalDate transferDate;

        try {
            transferDate = LocalDate.parse(transferDateField.getText().trim());
        } catch (DateTimeParseException e) {
            MessageUtil.showError("Date must be in YYYY-MM-DD format.");
            return;
        }

        WarehouseTransfer transfer = new WarehouseTransfer(
                transferDate,
                fromWarehouse.getWarehouseId(),
                toWarehouse.getWarehouseId(),
                new ArrayList<>(currentItems)
        );

        if (transferDAO.createTransfer(transfer)) {
            MessageUtil.showSuccess("Transfer saved successfully. Inventory updated.");
            clearForm();
            loadTransfers();
        } else {
            MessageUtil.showError("Transfer failed. Check available stock.");
        }
    }

    private void loadTransfers() {
        TableUtil.clearTable(transferTableModel);
        TableUtil.clearTable(transferItemsTableModel);

        for (WarehouseTransfer transfer : transferDAO.getAllTransfers()) {
            transferTableModel.addRow(new Object[]{
                    transfer.getTransferId(),
                    transfer.getTransferDate(),
                    getWarehouseDisplay(transfer.getFromWarehouseId()),
                    getWarehouseDisplay(transfer.getToWarehouseId())
            });
        }
    }

    private void loadSelectedTransferItems() {
        int selectedRow = transferTable.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        int modelRow = transferTable.convertRowIndexToModel(selectedRow);
        int transferId = Integer.parseInt(transferTableModel.getValueAt(modelRow, 0).toString());

        TableUtil.clearTable(transferItemsTableModel);

        for (WarehouseTransferItem item : transferDAO.getTransferItems(transferId)) {
            transferItemsTableModel.addRow(new Object[]{
                    item.getTransferItemId(),
                    item.getTransferId(),
                    getProductDisplay(item.getProductId()),
                    item.getQuantity()
            });
        }
    }

    private void clearForm() {
        if (fromWarehouseComboBox.getItemCount() > 0) {
            fromWarehouseComboBox.setSelectedIndex(0);
        }

        if (toWarehouseComboBox.getItemCount() > 1) {
            toWarehouseComboBox.setSelectedIndex(1);
        }

        if (productComboBox.getItemCount() > 0) {
            productComboBox.setSelectedIndex(0);
        }

        transferDateField.setText(LocalDate.now().toString());
        quantityField.setText("");
        clearItems();
    }

    private String getWarehouseDisplay(int warehouseId) {
        return warehouseId + " - " + warehouseNames.getOrDefault(warehouseId, "Unknown Warehouse");
    }

    private String getProductDisplay(int productId) {
        return productId + " - " + productNames.getOrDefault(productId, "Unknown Product");
    }

    private static class WarehouseOption {
        private final int warehouseId;
        private final String warehouseName;

        public WarehouseOption(int warehouseId, String warehouseName) {
            this.warehouseId = warehouseId;
            this.warehouseName = warehouseName;
        }

        public int getWarehouseId() {
            return warehouseId;
        }

        @Override
        public String toString() {
            return warehouseId + " - " + warehouseName;
        }
    }
}
