package ui;

import dao.WarehouseDAO;
import model.Warehouse;
import util.MessageUtil;
import util.TableUtil;
import util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class WarehousePanel extends JPanel {

    private final WarehouseDAO warehouseDAO = new WarehouseDAO();

    private JTextField warehouseIdField;
    private JTextField warehouseNameField;
    private JTextField locationField;
    private JTextField capacityField;
    private JTextField searchField;

    private JTable warehouseTable;
    private DefaultTableModel tableModel;

    public WarehousePanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIStyle.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        loadWarehouses();
    }

    private JPanel createHeaderPanel() {
        return UIStyle.createHeaderPanel(
                "Warehouse Management",
                "Add, update, delete, search, and view SAFAD warehouses."
        );
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIStyle.BACKGROUND);

        JPanel formPanel = createFormPanel();
        JPanel tablePanel = createTablePanel();

        formPanel.setMinimumSize(new Dimension(250, 0));
        tablePanel.setMinimumSize(new Dimension(500, 0));

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                formPanel,
                tablePanel
        );

        UIStyle.styleSplitPane(splitPane, 0.30);
        splitPane.setDividerLocation(330);

        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel wrapper = UIStyle.createCardPanel();
        wrapper.setPreferredSize(new Dimension(330, 0));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;

        warehouseIdField = new JTextField();
        warehouseIdField.setEditable(false);

        warehouseNameField = new JTextField();
        locationField = new JTextField();
        capacityField = new JTextField();

        addLabeledField(formPanel, gbc, "Warehouse ID", warehouseIdField);
        addLabeledField(formPanel, gbc, "Warehouse Name *", warehouseNameField);
        addLabeledField(formPanel, gbc, "Location", locationField);
        addLabeledField(formPanel, gbc, "Capacity *", capacityField);

        wrapper.add(formPanel, BorderLayout.CENTER);
        wrapper.add(createButtonPanel(), BorderLayout.SOUTH);

        return wrapper;
    }

    private void addLabeledField(JPanel panel, GridBagConstraints gbc, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(UIStyle.LABEL_FONT);
        label.setForeground(UIStyle.TEXT_DARK);

        UIStyle.styleTextFieldIfPossible(field);

        gbc.gridy++;
        panel.add(label, gbc);

        gbc.gridy++;
        panel.add(field, gbc);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.setBackground(UIStyle.PANEL_BACKGROUND);

        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear");
        JButton refreshButton = new JButton("Refresh");

        UIStyle.stylePrimaryButton(addButton);
        UIStyle.stylePrimaryButton(updateButton);
        UIStyle.stylePrimaryButton(deleteButton);
        UIStyle.stylePrimaryButton(clearButton);
        UIStyle.stylePrimaryButton(refreshButton);

        addButton.addActionListener(e -> addWarehouse());
        updateButton.addActionListener(e -> updateWarehouse());
        deleteButton.addActionListener(e -> deleteWarehouse());
        clearButton.addActionListener(e -> clearForm());
        refreshButton.addActionListener(e -> loadWarehouses());

        panel.add(addButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        panel.add(clearButton);
        panel.add(refreshButton);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = UIStyle.createCardPanel();

        panel.add(createSearchPanel(), BorderLayout.NORTH);

        String[] columns = {"ID", "Warehouse Name", "Location", "Capacity"};

        tableModel = TableUtil.createNonEditableTableModel(columns);
        warehouseTable = new JTable(tableModel);
        TableUtil.setupTable(warehouseTable);

        warehouseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelectedRow();
            }
        });

        JScrollPane scrollPane = new JScrollPane(warehouseTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(UIStyle.PANEL_BACKGROUND);

        searchField = new JTextField(22);
        JButton searchButton = new JButton("Search");

        UIStyle.styleTextField(searchField);
        UIStyle.stylePrimaryButton(searchButton);

        searchButton.addActionListener(e -> searchWarehouses());

        panel.add(new JLabel("Search:"));
        panel.add(searchField);
        panel.add(searchButton);

        return panel;
    }

    private void addWarehouse() {
        try {
            Warehouse warehouse = readWarehouseFromForm(false);

            boolean success = warehouseDAO.addWarehouse(warehouse);

            if (success) {
                MessageUtil.showSuccess("Warehouse added successfully.");
                clearForm();
                loadWarehouses();
            } else {
                MessageUtil.showError("Failed to add warehouse.");
            }

        } catch (Exception e) {
            MessageUtil.showError(e.getMessage());
        }
    }

    private void updateWarehouse() {
        try {
            if (ValidationUtil.isEmpty(warehouseIdField.getText())) {
                MessageUtil.showWarning("Please select a warehouse to update.");
                return;
            }

            Warehouse warehouse = readWarehouseFromForm(true);

            boolean success = warehouseDAO.updateWarehouse(warehouse);

            if (success) {
                MessageUtil.showSuccess("Warehouse updated successfully.");
                clearForm();
                loadWarehouses();
            } else {
                MessageUtil.showError("Failed to update warehouse.");
            }

        } catch (Exception e) {
            MessageUtil.showError(e.getMessage());
        }
    }

    private void deleteWarehouse() {
        if (ValidationUtil.isEmpty(warehouseIdField.getText())) {
            MessageUtil.showWarning("Please select a warehouse to delete.");
            return;
        }

        boolean confirmed = MessageUtil.confirm("Are you sure you want to delete this warehouse?");

        if (!confirmed) {
            return;
        }

        int warehouseId = Integer.parseInt(warehouseIdField.getText());

        boolean success = warehouseDAO.deleteWarehouse(warehouseId);

        if (success) {
            MessageUtil.showSuccess("Warehouse deleted successfully.");
            clearForm();
            loadWarehouses();
        } else {
            MessageUtil.showError("Failed to delete warehouse. This warehouse may be linked to inventory, invoices, or transfers.");
        }
    }

    private Warehouse readWarehouseFromForm(boolean includeId) {
        String warehouseName = warehouseNameField.getText().trim();
        String location = locationField.getText().trim();
        String capacityText = capacityField.getText().trim();

        if (ValidationUtil.isEmpty(warehouseName)) {
            throw new IllegalArgumentException("Warehouse name is required.");
        }

        if (ValidationUtil.isEmpty(capacityText)) {
            throw new IllegalArgumentException("Capacity is required.");
        }

        if (!ValidationUtil.isNonNegativeInteger(capacityText)) {
            throw new IllegalArgumentException("Capacity must be a non-negative integer.");
        }

        int capacity = Integer.parseInt(capacityText);

        Warehouse warehouse = new Warehouse(
                warehouseName,
                location,
                capacity
        );

        if (includeId) {
            warehouse.setWarehouseId(Integer.parseInt(warehouseIdField.getText()));
        }

        return warehouse;
    }

    private void loadWarehouses() {
        List<Warehouse> warehouses = warehouseDAO.getAllWarehouses();
        fillTable(warehouses);
    }

    private void searchWarehouses() {
        String keyword = searchField.getText().trim();

        if (ValidationUtil.isEmpty(keyword)) {
            loadWarehouses();
            return;
        }

        List<Warehouse> warehouses = warehouseDAO.searchWarehouses(keyword);
        fillTable(warehouses);
    }

    private void fillTable(List<Warehouse> warehouses) {
        TableUtil.clearTable(tableModel);

        for (Warehouse warehouse : warehouses) {
            tableModel.addRow(new Object[]{
                    warehouse.getWarehouseId(),
                    warehouse.getWarehouseName(),
                    warehouse.getLocation(),
                    warehouse.getCapacity()
            });
        }
    }

    private void fillFormFromSelectedRow() {
        int selectedRow = warehouseTable.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        int modelRow = warehouseTable.convertRowIndexToModel(selectedRow);

        warehouseIdField.setText(tableModel.getValueAt(modelRow, 0).toString());
        warehouseNameField.setText(valueToString(tableModel.getValueAt(modelRow, 1)));
        locationField.setText(valueToString(tableModel.getValueAt(modelRow, 2)));
        capacityField.setText(valueToString(tableModel.getValueAt(modelRow, 3)));
    }

    private String valueToString(Object value) {
        return value == null ? "" : value.toString();
    }

    private void clearForm() {
        warehouseIdField.setText("");
        warehouseNameField.setText("");
        locationField.setText("");
        capacityField.setText("");
        warehouseTable.clearSelection();
    }
}
