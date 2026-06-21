package ui.views;

import dao.InventoryDAO;
import dao.PurchaseReportDAO;
import dao.PurchaseReportDAO.ComboOption;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ui.TableUtil;
import ui.Theme;

import model.ReportTable;
import java.util.Map;

public class InventoryPage extends VBox {

    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final PurchaseReportDAO reportDAO = new PurchaseReportDAO();
    private final ComboBox<String> viewComboBox = new ComboBox<>();
    private final ComboBox<ComboOption> warehouseComboBox = new ComboBox<>();
    private final TextField searchField = Theme.textField("Search inventory");
    private final ComboBox<String> searchColumnBox = TableUtil.searchColumnBox();
    private final Label selectedStockLabel = new Label("Select an inventory row.");
    private final TextField thresholdField = Theme.textField("Threshold");
    private final TableView<Map<String, Object>> table = new TableView<>();
    private ReportTable currentModel;

    public InventoryPage() {
        Theme.styleComboBox(viewComboBox);
        Theme.styleComboBox(warehouseComboBox);
        viewComboBox.getStyleClass().add("compact-selector");
        getStyleClass().add("ledger-page");
        BorderPane content = createContent();
        VBox.setVgrow(content, Priority.ALWAYS);
        getChildren().add(content);
        loadFilters();
        runReport();
    }

    private BorderPane createContent() {
        viewComboBox.getItems().addAll("All Stock", "Warehouse Stock", "Low Stock");
        viewComboBox.getSelectionModel().selectFirst();

        Button run = Theme.primaryButton("Run");
        Button refresh = Theme.refreshButton();
        run.setOnAction(e -> runReport());
        refresh.setOnAction(e -> {
            searchField.clear();
            loadFilters();
            runReport();
        });

        viewComboBox.setOnAction(e -> {
            updateWarehouseVisibility();
            runReport();
        });
        warehouseComboBox.setOnAction(e -> runReport());
        searchField.textProperty().addListener((obs, oldText, newText) -> applySearch());
        searchColumnBox.valueProperty().addListener((obs, oldValue, newValue) -> applySearch());

        HBox filters = Theme.toolbar(
                viewComboBox,
                warehouseComboBox,
                searchColumnBox,
                searchField,
                refresh,
                run
        );
        Theme.stretchToolbarField(searchField);

        selectedStockLabel.getStyleClass().add("muted-label");
        selectedStockLabel.setWrapText(true);
        thresholdField.getStyleClass().add("compact-input");
        Button updateThreshold = Theme.primaryButton("Update Threshold");
        updateThreshold.setOnAction(e -> updateThreshold());

        GridPane thresholdEditor = new GridPane();
        Theme.configureInspectorForm(thresholdEditor);
        Theme.addInspectorRow(thresholdEditor, 0, "Selected", selectedStockLabel);
        Theme.addInspectorRow(thresholdEditor, 1, "Threshold", thresholdField);
        Theme.addInspectorActions(thresholdEditor, 2, updateThreshold);

        Theme.styleTable(table);
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldRow, row) -> fillThresholdEditor(row));

        return Theme.ledgerWorkspace(
                Theme.ledgerSurface("Inventory Ledger", filters, table),
                Theme.ledgerInspector("Threshold Inspector", thresholdEditor)
        );
    }

    private void loadFilters() {
        warehouseComboBox.getItems().setAll(reportDAO.getWarehouses());
        if (!warehouseComboBox.getItems().isEmpty()) warehouseComboBox.getSelectionModel().selectFirst();
        updateWarehouseVisibility();
    }

    private void runReport() {
        String view = viewComboBox.getValue();
        ReportTable model;

        if ("Warehouse Stock".equals(view)) {
            ComboOption warehouse = warehouseComboBox.getValue();
            if (warehouse == null) return;
            model = reportDAO.getProductsInWarehouse(warehouse.getId());
        } else if ("Low Stock".equals(view)) {
            ComboOption warehouse = warehouseComboBox.getValue();
            if (warehouse == null) return;
            model = reportDAO.getLowStockProducts(warehouse.getId());
        } else {
            model = reportDAO.getCurrentStockByWarehouse();
        }

        currentModel = model;
        TableUtil.populateSearchColumns(searchColumnBox, model);
        TableUtil.fillFromModel(table, model);
        applySearch();
        clearThresholdEditor();
    }

    private void updateWarehouseVisibility() {
        String view = viewComboBox.getValue();
        boolean needsWarehouse = "Warehouse Stock".equals(view) || "Low Stock".equals(view);
        Theme.setVisible(warehouseComboBox, needsWarehouse);
    }

    private void applySearch() {
        if (currentModel == null) {
            return;
        }

        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        ReportTable filteredModel = new ReportTable();

        for (int col = 0; col < currentModel.getColumnCount(); col++) {
            filteredModel.addColumn(currentModel.getColumnName(col));
        }

        for (int row = 0; row < currentModel.getRowCount(); row++) {
            if (keyword.isEmpty() || rowMatches(row, keyword, searchColumnBox.getValue())) {
                Object[] values = new Object[currentModel.getColumnCount()];
                for (int col = 0; col < currentModel.getColumnCount(); col++) {
                    values[col] = currentModel.getValueAt(row, col);
                }
                filteredModel.addRow(values);
            }
        }

        TableUtil.fillFromModel(table, filteredModel);
        clearThresholdEditor();
    }

    private boolean rowMatches(int row, String keyword, String selectedColumn) {
        for (int col = 0; col < currentModel.getColumnCount(); col++) {
            if (selectedColumn != null
                    && !TableUtil.ALL_SEARCH_COLUMNS.equals(selectedColumn)
                    && !currentModel.getColumnName(col).equals(selectedColumn)) {
                continue;
            }

            Object value = currentModel.getValueAt(row, col);
            if (value != null && value.toString().toLowerCase().contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private void fillThresholdEditor(Map<String, Object> row) {
        if (row == null) {
            clearThresholdEditor();
            return;
        }

        String warehouse = text(row.get("Warehouse"));
        String product = text(row.get("Product"));
        String quantity = text(firstValue(row, "Current Quantity", "Quantity"));
        String threshold = text(row.get("Threshold"));

        selectedStockLabel.setText(warehouse + " | " + product + " | Qty: " + quantity);
        thresholdField.setText(threshold);
    }

    private void updateThreshold() {
        Map<String, Object> row = table.getSelectionModel().getSelectedItem();
        if (row == null) {
            Theme.showError("Select an inventory row first.");
            return;
        }

        int productId = intValue(row.get("Product ID"), -1);
        int warehouseId = warehouseIdForRow(row);
        int threshold = intValue(thresholdField.getText(), -1);

        if (productId <= 0 || warehouseId <= 0) {
            Theme.showError("Could not identify the selected product and warehouse.");
            return;
        }

        if (threshold < 0) {
            Theme.showError("Threshold must be a non-negative whole number.");
            return;
        }

        if (inventoryDAO.setThreshold(productId, warehouseId, threshold)) {
            Theme.showInfo("Threshold updated.");
            runReport();
        } else {
            Theme.showError("Could not update threshold.");
        }
    }

    private int warehouseIdForRow(Map<String, Object> row) {
        String view = viewComboBox.getValue();
        ComboOption selectedWarehouse = warehouseComboBox.getValue();
        if (selectedWarehouse != null && ("Warehouse Stock".equals(view) || "Low Stock".equals(view))) {
            return selectedWarehouse.getId();
        }

        String warehouseName = text(row.get("Warehouse"));
        for (ComboOption option : reportDAO.getWarehouses()) {
            if (warehouseName.equals(optionName(option))) {
                return option.getId();
            }
        }
        return -1;
    }

    private String optionName(ComboOption option) {
        String text = option.toString();
        int separator = text.indexOf(" - ");
        return separator >= 0 ? text.substring(separator + 3) : text;
    }

    private Object firstValue(Map<String, Object> row, String firstColumn, String secondColumn) {
        Object value = row.get(firstColumn);
        return value == null ? row.get(secondColumn) : value;
    }

    private int intValue(Object value, int fallback) {
        try {
            return Integer.parseInt(text(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    private String text(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private void clearThresholdEditor() {
        selectedStockLabel.setText("Select an inventory row.");
        thresholdField.clear();
        table.getSelectionModel().clearSelection();
    }
}
