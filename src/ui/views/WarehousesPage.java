package ui.views;

import dao.InventoryDAO;
import dao.WarehouseDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Warehouse;
import ui.TableUtil;
import ui.Theme;

public class WarehousesPage extends VBox {

    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final ObservableList<Warehouse> warehouses = FXCollections.observableArrayList();

    private final TextField idField = Theme.textField("ID");
    private final TextField nameField = Theme.textField("Warehouse name");
    private final TextField locationField = Theme.textField("Location");
    private final TextField capacityField = Theme.textField("Capacity");
    private final TextField searchField = Theme.textField("Search warehouses");
    private final ComboBox<String> searchColumnBox = TableUtil.searchColumnBox();
    private final TableView<Warehouse> table = new TableView<>();
    private Button warehouseActionButton;
    private Button deleteButton;

    public WarehousesPage() {
        getStyleClass().add("ledger-page");
        BorderPane content = createContent();
        VBox.setVgrow(content, Priority.ALWAYS);
        getChildren().add(content);
        loadWarehouses();
    }

    private BorderPane createContent() {
        idField.setEditable(false);
        configureTable();

        return Theme.ledgerWorkspace(
                Theme.ledgerSurface("Warehouse Ledger", createToolbar(), table),
                Theme.ledgerInspector("Warehouse Inspector", createForm())
        );
    }

    private HBox createToolbar() {
        Button refreshButton = Theme.refreshButton();
        refreshButton.setOnAction(e -> {
            searchField.clear();
            loadWarehouses();
        });

        HBox toolbar = Theme.ledgerCommandBar(searchColumnBox, searchField, refreshButton);
        Theme.stretchToolbarField(searchField);
        return toolbar;
    }

    private GridPane createForm() {
        GridPane form = new GridPane();
        Theme.configureInspectorForm(form);

        addRow(form, 0, "ID", idField);
        addRow(form, 1, "Name", nameField);
        addRow(form, 2, "Location", locationField);
        addRow(form, 3, "Capacity", capacityField);

        warehouseActionButton = Theme.primaryButton("Add");
        deleteButton = Theme.dangerButton("Delete");
        Button clearButton = Theme.secondaryButton("Clear");

        warehouseActionButton.setOnAction(e -> saveWarehouse());
        deleteButton.setOnAction(e -> deleteWarehouse());
        clearButton.setOnAction(e -> clearForm());
        Theme.setVisible(deleteButton, false);

        Theme.addInspectorActions(form, 4, warehouseActionButton, deleteButton, clearButton);
        return form;
    }

    private void addRow(GridPane form, int row, String label, javafx.scene.Node field) {
        Theme.addInspectorRow(form, row, label, field);
    }

    private void configureTable() {
        table.getColumns().add(TableUtil.column("ID", Warehouse::getWarehouseId, 70));
        table.getColumns().add(TableUtil.column("Warehouse", Warehouse::getWarehouseName, 180));
        table.getColumns().add(TableUtil.column("Location", Warehouse::getLocation, 180));
        table.getColumns().add(TableUtil.column("Capacity", Warehouse::getCapacity, 100));
        table.getColumns().add(TableUtil.column("Current Used", warehouse -> currentUsed(warehouse), 120));
        table.getColumns().add(TableUtil.column("Available", warehouse -> availableCapacity(warehouse), 110));
        table.getColumns().add(TableUtil.column("Usage %", warehouse -> usagePercent(warehouse), 90));
        TableUtil.installSearch(table, warehouses, searchField, searchColumnBox);
        Theme.styleTable(table);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, warehouse) -> {
            if (warehouse != null) {
                fillForm(warehouse);
            }
        });
    }

    private void loadWarehouses() {
        warehouses.setAll(warehouseDAO.getAllWarehouses());
        table.refresh();
    }

    private int currentUsed(Warehouse warehouse) {
        return inventoryDAO.getWarehouseTotalQuantity(warehouse.getWarehouseId());
    }

    private int availableCapacity(Warehouse warehouse) {
        return Math.max(warehouse.getCapacity() - currentUsed(warehouse), 0);
    }

    private String usagePercent(Warehouse warehouse) {
        if (warehouse.getCapacity() <= 0) {
            return "0%";
        }

        int percent = (int) Math.round((currentUsed(warehouse) * 100.0) / warehouse.getCapacity());
        return percent + "%";
    }

    private void addWarehouse() {
        Warehouse warehouse = readForm(false);
        if (warehouse != null && warehouseDAO.addWarehouse(warehouse)) {
            Theme.showInfo("Warehouse added successfully.");
            clearForm();
            loadWarehouses();
        }
    }

    private void saveWarehouse() {
        if (idField.getText().isBlank()) {
            addWarehouse();
        } else {
            updateWarehouse();
        }
    }

    private void updateWarehouse() {
        Warehouse warehouse = readForm(true);
        if (warehouse != null && warehouseDAO.updateWarehouse(warehouse)) {
            Theme.showInfo("Warehouse updated successfully.");
            clearForm();
            loadWarehouses();
        }
    }

    private void deleteWarehouse() {
        if (idField.getText().isBlank()) {
            Theme.showError("Select a warehouse first.");
            return;
        }

        if (Theme.confirm("Delete selected warehouse?") && warehouseDAO.deleteWarehouse(Integer.parseInt(idField.getText()))) {
            Theme.showInfo("Warehouse deleted successfully.");
            clearForm();
            loadWarehouses();
        }
    }

    private Warehouse readForm(boolean includeId) {
        if (nameField.getText().trim().isEmpty()) {
            Theme.showError("Warehouse name is required.");
            return null;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(capacityField.getText().trim());
        } catch (Exception e) {
            Theme.showError("Capacity must be a number.");
            return null;
        }

        Warehouse warehouse = new Warehouse(nameField.getText().trim(), locationField.getText().trim(), capacity);

        if (includeId) {
            if (idField.getText().isBlank()) {
                Theme.showError("Select a warehouse first.");
                return null;
            }
            warehouse.setWarehouseId(Integer.parseInt(idField.getText()));
        }

        return warehouse;
    }

    private void fillForm(Warehouse warehouse) {
        idField.setText(String.valueOf(warehouse.getWarehouseId()));
        nameField.setText(warehouse.getWarehouseName());
        locationField.setText(warehouse.getLocation());
        capacityField.setText(String.valueOf(warehouse.getCapacity()));
        updateFormMode();
    }

    private void clearForm() {
        idField.clear();
        nameField.clear();
        locationField.clear();
        capacityField.clear();
        table.getSelectionModel().clearSelection();
        updateFormMode();
    }

    private void updateFormMode() {
        boolean selected = !idField.getText().isBlank();
        if (warehouseActionButton != null) {
            warehouseActionButton.setText(selected ? "Update" : "Add");
        }
        if (deleteButton != null) {
            Theme.setVisible(deleteButton, selected);
        }
    }
}
