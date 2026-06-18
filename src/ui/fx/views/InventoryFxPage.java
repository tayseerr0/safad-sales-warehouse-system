package ui.fx.views;

import dao.InventoryDAO;
import dao.PurchaseReportDAO;
import dao.PurchaseReportDAO.ComboOption;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ui.fx.FxTableUtil;
import ui.fx.FxTheme;

import javax.swing.table.DefaultTableModel;
import java.util.Map;

public class InventoryFxPage extends VBox {

    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final PurchaseReportDAO reportDAO = new PurchaseReportDAO();
    private final ComboBox<String> viewComboBox = new ComboBox<>();
    private final ComboBox<ComboOption> warehouseComboBox = new ComboBox<>();
    private final TextField searchField = FxTheme.textField("Search inventory");
    private final TextField selectedStockField = FxTheme.textField("Select inventory row");
    private final TextField thresholdField = FxTheme.textField("Threshold");
    private final TableView<Map<String, Object>> table = new TableView<>();
    private DefaultTableModel currentModel;

    public InventoryFxPage() {
        FxTheme.styleComboBox(viewComboBox);
        FxTheme.styleComboBox(warehouseComboBox);
        viewComboBox.getStyleClass().add("compact-selector");
        getChildren().add(FxTheme.page("Inventory", "View current stock, warehouse stock, and saved-threshold low stock.", createContent()));
        loadFilters();
        runReport();
    }

    private BorderPane createContent() {
        viewComboBox.getItems().addAll("All Stock", "Warehouse Stock", "Low Stock");
        viewComboBox.getSelectionModel().selectFirst();

        Button run = FxTheme.primaryButton("Run");
        Button refresh = FxTheme.secondaryButton("Refresh");
        run.setOnAction(e -> runReport());
        refresh.setOnAction(e -> {
            loadFilters();
            runReport();
        });

        viewComboBox.setOnAction(e -> {
            updateWarehouseVisibility();
            runReport();
        });
        warehouseComboBox.setOnAction(e -> runReport());
        searchField.textProperty().addListener((obs, oldText, newText) -> applySearch());

        HBox filters = FxTheme.toolbar(
                viewComboBox,
                warehouseComboBox,
                searchField,
                refresh,
                run
        );
        HBox.setHgrow(searchField, Priority.ALWAYS);

        selectedStockField.setEditable(false);
        thresholdField.getStyleClass().add("compact-input");
        Button updateThreshold = FxTheme.primaryButton("Update Threshold");
        updateThreshold.setOnAction(e -> updateThreshold());

        HBox thresholdEditor = FxTheme.toolbar(
                new Label("Selected"),
                selectedStockField,
                new Label("Threshold"),
                thresholdField,
                updateThreshold
        );
        HBox.setHgrow(selectedStockField, Priority.ALWAYS);

        FxTheme.styleTable(table);
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldRow, row) -> fillThresholdEditor(row));

        BorderPane content = new BorderPane();
        FxTheme.styleWorkbench(content);
        VBox controlStack = new VBox(8, filters, thresholdEditor);
        controlStack.getStyleClass().add("inventory-control-stack");
        VBox tableCard = FxTheme.card("Inventory Ledger", table);
        FxTheme.styleTableCard(tableCard);
        content.setTop(controlStack);
        content.setCenter(tableCard);
        BorderPane.setMargin(content.getTop(), new javafx.geometry.Insets(0, 0, 12, 0));
        return content;
    }

    private void loadFilters() {
        warehouseComboBox.getItems().setAll(reportDAO.getWarehouses());
        if (!warehouseComboBox.getItems().isEmpty()) warehouseComboBox.getSelectionModel().selectFirst();
        updateWarehouseVisibility();
    }

    private void runReport() {
        String view = viewComboBox.getValue();
        DefaultTableModel model;

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
        FxTableUtil.fillFromModel(table, model);
        applySearch();
        clearThresholdEditor();
    }

    private void updateWarehouseVisibility() {
        String view = viewComboBox.getValue();
        boolean needsWarehouse = "Warehouse Stock".equals(view) || "Low Stock".equals(view);
        warehouseComboBox.setVisible(needsWarehouse);
        warehouseComboBox.setManaged(needsWarehouse);
    }

    private void applySearch() {
        if (currentModel == null) {
            return;
        }

        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        DefaultTableModel filteredModel = new DefaultTableModel();

        for (int col = 0; col < currentModel.getColumnCount(); col++) {
            filteredModel.addColumn(currentModel.getColumnName(col));
        }

        for (int row = 0; row < currentModel.getRowCount(); row++) {
            if (keyword.isEmpty() || rowMatches(row, keyword)) {
                Object[] values = new Object[currentModel.getColumnCount()];
                for (int col = 0; col < currentModel.getColumnCount(); col++) {
                    values[col] = currentModel.getValueAt(row, col);
                }
                filteredModel.addRow(values);
            }
        }

        FxTableUtil.fillFromModel(table, filteredModel);
        clearThresholdEditor();
    }

    private boolean rowMatches(int row, String keyword) {
        for (int col = 0; col < currentModel.getColumnCount(); col++) {
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

        selectedStockField.setText(warehouse + " | " + product + " | Qty: " + quantity);
        thresholdField.setText(threshold);
    }

    private void updateThreshold() {
        Map<String, Object> row = table.getSelectionModel().getSelectedItem();
        if (row == null) {
            FxTheme.showError("Select an inventory row first.");
            return;
        }

        int productId = intValue(row.get("Product ID"), -1);
        int warehouseId = warehouseIdForRow(row);
        int threshold = intValue(thresholdField.getText(), -1);

        if (productId <= 0 || warehouseId <= 0) {
            FxTheme.showError("Could not identify the selected product and warehouse.");
            return;
        }

        if (threshold < 0) {
            FxTheme.showError("Threshold must be a non-negative whole number.");
            return;
        }

        if (inventoryDAO.setThreshold(productId, warehouseId, threshold)) {
            FxTheme.showInfo("Threshold updated.");
            runReport();
        } else {
            FxTheme.showError("Could not update threshold.");
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
        selectedStockField.clear();
        thresholdField.clear();
        table.getSelectionModel().clearSelection();
    }
}
