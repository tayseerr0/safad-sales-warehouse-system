package ui.fx.views;

import dao.InventoryDAO;
import dao.WarehouseDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Warehouse;
import ui.fx.FxTableUtil;
import ui.fx.FxTheme;

public class WarehousesFxPage extends VBox {

    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final ObservableList<Warehouse> warehouses = FXCollections.observableArrayList();

    private final TextField idField = FxTheme.textField("ID");
    private final TextField nameField = FxTheme.textField("Warehouse name");
    private final TextField locationField = FxTheme.textField("Location");
    private final TextField capacityField = FxTheme.textField("Capacity");
    private final TextField searchField = FxTheme.textField("Search warehouses");
    private final TableView<Warehouse> table = new TableView<>();

    public WarehousesFxPage() {
        getChildren().add(FxTheme.ledgerPage("Warehouses", "Warehouse capacity and stock usage records.", createContent()));
        loadWarehouses();
    }

    private BorderPane createContent() {
        idField.setEditable(false);
        configureTable();

        return FxTheme.ledgerWorkspace(
                FxTheme.ledgerSurface("Warehouse Ledger", createToolbar(), table),
                FxTheme.ledgerInspector("Warehouse Inspector", createForm())
        );
    }

    private HBox createToolbar() {
        Button searchButton = FxTheme.secondaryButton("Search");
        Button refreshButton = FxTheme.secondaryButton("Refresh");
        searchButton.setOnAction(e -> searchWarehouses());
        refreshButton.setOnAction(e -> loadWarehouses());

        HBox toolbar = FxTheme.ledgerCommandBar(searchField, searchButton, refreshButton);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        return toolbar;
    }

    private GridPane createForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        addRow(form, 0, "ID", idField);
        addRow(form, 1, "Name", nameField);
        addRow(form, 2, "Location", locationField);
        addRow(form, 3, "Capacity", capacityField);

        Button addButton = FxTheme.primaryButton("Add");
        Button updateButton = FxTheme.primaryButton("Update");
        Button deleteButton = FxTheme.dangerButton("Delete");
        Button clearButton = FxTheme.secondaryButton("Clear");

        addButton.setOnAction(e -> addWarehouse());
        updateButton.setOnAction(e -> updateWarehouse());
        deleteButton.setOnAction(e -> deleteWarehouse());
        clearButton.setOnAction(e -> clearForm());

        form.add(FxTheme.actionRow(addButton, updateButton, deleteButton, clearButton), 0, 4, 2, 1);
        return form;
    }

    private void addRow(GridPane form, int row, String label, javafx.scene.Node field) {
        form.add(new javafx.scene.control.Label(label), 0, row);
        form.add(field, 1, row);
    }

    private void configureTable() {
        table.getColumns().add(FxTableUtil.column("ID", Warehouse::getWarehouseId, 70));
        table.getColumns().add(FxTableUtil.column("Warehouse", Warehouse::getWarehouseName, 180));
        table.getColumns().add(FxTableUtil.column("Location", Warehouse::getLocation, 180));
        table.getColumns().add(FxTableUtil.column("Capacity", Warehouse::getCapacity, 100));
        table.getColumns().add(FxTableUtil.column("Current Used", warehouse -> currentUsed(warehouse), 120));
        table.getColumns().add(FxTableUtil.column("Available", warehouse -> availableCapacity(warehouse), 110));
        table.getColumns().add(FxTableUtil.column("Usage %", warehouse -> usagePercent(warehouse), 90));
        FxTableUtil.installSearch(table, warehouses, searchField);
        FxTheme.styleTable(table);

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

    private void searchWarehouses() {
        String keyword = searchField.getText().trim();
        warehouses.setAll(keyword.isEmpty() ? warehouseDAO.getAllWarehouses() : warehouseDAO.searchWarehouses(keyword));
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
            FxTheme.showInfo("Warehouse added successfully.");
            clearForm();
            loadWarehouses();
        }
    }

    private void updateWarehouse() {
        Warehouse warehouse = readForm(true);
        if (warehouse != null && warehouseDAO.updateWarehouse(warehouse)) {
            FxTheme.showInfo("Warehouse updated successfully.");
            clearForm();
            loadWarehouses();
        }
    }

    private void deleteWarehouse() {
        if (idField.getText().isBlank()) {
            FxTheme.showError("Select a warehouse first.");
            return;
        }

        if (FxTheme.confirm("Delete selected warehouse?") && warehouseDAO.deleteWarehouse(Integer.parseInt(idField.getText()))) {
            FxTheme.showInfo("Warehouse deleted successfully.");
            clearForm();
            loadWarehouses();
        }
    }

    private Warehouse readForm(boolean includeId) {
        if (nameField.getText().trim().isEmpty()) {
            FxTheme.showError("Warehouse name is required.");
            return null;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(capacityField.getText().trim());
        } catch (Exception e) {
            FxTheme.showError("Capacity must be a number.");
            return null;
        }

        Warehouse warehouse = new Warehouse(nameField.getText().trim(), locationField.getText().trim(), capacity);

        if (includeId) {
            if (idField.getText().isBlank()) {
                FxTheme.showError("Select a warehouse first.");
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
    }

    private void clearForm() {
        idField.clear();
        nameField.clear();
        locationField.clear();
        capacityField.clear();
        table.getSelectionModel().clearSelection();
    }
}
