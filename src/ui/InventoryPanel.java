package ui;

import dao.PurchaseReportDAO;
import dao.PurchaseReportDAO.ComboOption;
import util.MessageUtil;
import util.TableUtil;
import util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;

public class InventoryPanel extends JPanel {

    private final PurchaseReportDAO reportDAO = new PurchaseReportDAO();

    private JComboBox<String> viewComboBox;
    private JComboBox<ComboOption> warehouseComboBox;
    private JTextField searchField;
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JLabel statusLabel;

    public InventoryPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIStyle.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(UIStyle.createHeaderPanel(
                "Inventory",
                "View current stock, warehouse quantities, and low-stock products."
        ), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        loadWarehouses();
        runSelectedView();
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIStyle.BACKGROUND);

        panel.add(createFilterPanel(), BorderLayout.NORTH);
        panel.add(createTablePanel(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFilterPanel() {
        JPanel wrapper = UIStyle.createCardPanel();

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        viewComboBox = new JComboBox<>(new String[]{
                "All Stock",
                "Warehouse Stock",
                "Low Stock"
        });
        warehouseComboBox = new JComboBox<>();
        searchField = new JTextField();

        UIStyle.styleComboBox(viewComboBox);
        UIStyle.styleComboBox(warehouseComboBox);
        UIStyle.styleTextField(searchField);

        addField(formPanel, gbc, 0, "View", viewComboBox);
        addField(formPanel, gbc, 1, "Warehouse", warehouseComboBox);
        addField(formPanel, gbc, 2, "Search", searchField);

        viewComboBox.addActionListener(e -> {
            updateWarehouseFilterVisibility();
            runSelectedView();
        });
        warehouseComboBox.addActionListener(e -> runSelectedView());

        JPanel buttonPanel = UIStyle.createButtonPanel(FlowLayout.RIGHT);

        JButton runButton = new JButton("Run");
        JButton searchButton = new JButton("Search");
        JButton clearButton = new JButton("Clear");
        JButton refreshButton = new JButton("Refresh");

        UIStyle.stylePrimaryButton(runButton);
        UIStyle.styleSecondaryButton(searchButton);
        UIStyle.styleSecondaryButton(clearButton);
        UIStyle.styleSecondaryButton(refreshButton);

        runButton.addActionListener(e -> runSelectedView());
        searchButton.addActionListener(e -> applySearch());
        clearButton.addActionListener(e -> clearFilters());
        refreshButton.addActionListener(e -> {
            loadWarehouses();
            runSelectedView();
        });

        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(runButton);

        wrapper.add(formPanel, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        updateWarehouseFilterVisibility();

        return wrapper;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int x, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        UIStyle.styleLabel(label);

        gbc.gridx = x;
        gbc.gridy = 0;
        panel.add(label, gbc);

        gbc.gridy = 1;
        panel.add(field, gbc);
    }

    private JPanel createTablePanel() {
        JPanel panel = UIStyle.createCardPanel();

        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(UIStyle.SECTION_TITLE_FONT);
        statusLabel.setForeground(UIStyle.TEXT_DARK);

        tableModel = TableUtil.createNonEditableTableModel(new String[]{"Result"});
        inventoryTable = new JTable(tableModel);
        TableUtil.setupTable(inventoryTable);
        if (searchField != null) {
            TableUtil.installLiveSearch(searchField, inventoryTable);
        }

        topPanel.add(statusLabel, BorderLayout.WEST);
        topPanel.add(TableUtil.createColumnVisibilityButton(inventoryTable, "Columns"), BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(UIStyle.createTableScrollPane(inventoryTable), BorderLayout.CENTER);

        return panel;
    }

    private void updateWarehouseFilterVisibility() {
        if (warehouseComboBox == null || viewComboBox == null) {
            return;
        }

        String selectedView = String.valueOf(viewComboBox.getSelectedItem());
        warehouseComboBox.setEnabled("Warehouse Stock".equals(selectedView) || "Low Stock".equals(selectedView));
    }

    private void loadWarehouses() {
        warehouseComboBox.removeAllItems();

        List<ComboOption> warehouses = reportDAO.getWarehouses();
        for (ComboOption warehouse : warehouses) {
            warehouseComboBox.addItem(warehouse);
        }
    }

    private void runSelectedView() {
        String selectedView = (String) viewComboBox.getSelectedItem();
        if (selectedView == null) {
            return;
        }

        switch (selectedView) {
            case "All Stock" -> showReport(
                    reportDAO.getCurrentStockByWarehouse(),
                    "All stock by warehouse"
            );
            case "Warehouse Stock" -> showWarehouseStock();
            case "Low Stock" -> showLowStock();
            default -> MessageUtil.showWarning("Select a valid inventory view.");
        }
    }

    private void showWarehouseStock() {
        ComboOption warehouse = getSelectedWarehouse();
        if (warehouse == null) {
            return;
        }

        showReport(
                reportDAO.getProductsInWarehouse(warehouse.getId()),
                "Stock for " + warehouse
        );
    }

    private void showLowStock() {
        ComboOption warehouse = getSelectedWarehouse();
        if (warehouse == null) {
            return;
        }

        showReport(
                reportDAO.getLowStockProducts(warehouse.getId()),
                "Low stock for " + warehouse
        );
    }

    private ComboOption getSelectedWarehouse() {
        ComboOption warehouse = (ComboOption) warehouseComboBox.getSelectedItem();

        if (warehouse == null) {
            MessageUtil.showWarning("Select a warehouse.");
            return null;
        }

        return warehouse;
    }

    private void showReport(DefaultTableModel model, String title) {
        tableModel = model;
        inventoryTable.setModel(tableModel);
        TableUtil.setupTable(inventoryTable);

        rowSorter = new TableRowSorter<>(tableModel);
        inventoryTable.setRowSorter(rowSorter);

        statusLabel.setText(title + " | Rows: " + tableModel.getRowCount());
        applySearch();
    }

    private void applySearch() {
        if (rowSorter == null) {
            return;
        }

        String keyword = searchField.getText().trim();

        if (ValidationUtil.isEmpty(keyword)) {
            rowSorter.setRowFilter(null);
            return;
        }

        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(keyword)));
    }

    private void clearFilters() {
        searchField.setText("");

        if (viewComboBox.getItemCount() > 0) {
            viewComboBox.setSelectedIndex(0);
        }

        runSelectedView();
    }
}
