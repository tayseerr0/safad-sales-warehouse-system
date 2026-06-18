package ui.fx.views;

import dao.ProductDAO;
import dao.WarehouseDAO;
import dao.WarehouseTransferDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Product;
import model.Warehouse;
import model.WarehouseTransfer;
import model.WarehouseTransferItem;
import ui.fx.FxTableUtil;
import ui.fx.FxTheme;

import java.time.LocalDate;
import java.util.ArrayList;

public class TransfersFxPage extends VBox {

    private final ProductDAO productDAO = new ProductDAO();
    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    private final WarehouseTransferDAO transferDAO = new WarehouseTransferDAO();

    private final ObservableList<TransferLine> currentItems = FXCollections.observableArrayList();
    private final ObservableList<WarehouseTransfer> transfers = FXCollections.observableArrayList();
    private final ObservableList<WarehouseTransferItem> selectedTransferItems = FXCollections.observableArrayList();

    private final ComboBox<Warehouse> fromWarehouseComboBox = new ComboBox<>();
    private final ComboBox<Warehouse> toWarehouseComboBox = new ComboBox<>();
    private final ComboBox<Product> productComboBox = new ComboBox<>();
    private final DatePicker transferDatePicker = new DatePicker(LocalDate.now());
    private final TextField quantityField = FxTheme.textField("Quantity");
    private final TextField transferSearchField = FxTheme.textField("Search transfers");
    private final Label modeLabel = new Label("Mode: New Transfer");
    private final TableView<TransferLine> itemTable = new TableView<>();
    private final TableView<WarehouseTransfer> transferTable = new TableView<>();
    private final TableView<WarehouseTransferItem> transferItemsTable = new TableView<>();
    private Button itemUpdateButton;
    private Button itemRemoveButton;

    private int editingTransferId = -1;

    public TransfersFxPage() {
        FxTheme.styleComboBox(fromWarehouseComboBox);
        FxTheme.styleComboBox(toWarehouseComboBox);
        FxTheme.styleComboBox(productComboBox);
        fromWarehouseComboBox.getStyleClass().add("compact-selector");
        toWarehouseComboBox.getStyleClass().add("compact-selector");
        productComboBox.getStyleClass().add("compact-selector");
        getStyleClass().add("ledger-page");
        BorderPane content = createContent();
        VBox.setVgrow(content, Priority.ALWAYS);
        getChildren().add(content);
        loadData();
    }

    private BorderPane createContent() {
        modeLabel.getStyleClass().add("card-title");
        configureTables();

        itemTable.setPrefHeight(155);

        VBox editor = new VBox(9,
                sectionLabel("Transfer"),
                createForm(),
                sectionLabel("Current Items"),
                itemTable,
                createSaveButtons()
        );
        editor.getStyleClass().add("workflow-editor");

        return FxTheme.ledgerWorkspace(createHistoryPane(), FxTheme.ledgerInspector("Transfer Inspector", editor));
    }

    private Label sectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("ledger-section-label");
        return label;
    }

    private GridPane createForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        addRow(form, 0, "Status", modeLabel);
        addRow(form, 1, "From Warehouse", fromWarehouseComboBox);
        addRow(form, 2, "To Warehouse", toWarehouseComboBox);
        addRow(form, 3, "Date", transferDatePicker);
        addRow(form, 4, "Product", productComboBox);
        addRow(form, 5, "Quantity", quantityField);

        Button add = FxTheme.primaryButton("Add");
        itemUpdateButton = FxTheme.secondaryButton("Update");
        itemRemoveButton = FxTheme.secondaryButton("Remove");
        Button clear = FxTheme.secondaryButton("Clear");
        add.setOnAction(e -> addItem());
        itemUpdateButton.setOnAction(e -> updateSelectedItem());
        itemRemoveButton.setOnAction(e -> removeItem());
        clear.setOnAction(e -> {
            currentItems.clear();
            clearItemForm();
        });
        setVisible(itemUpdateButton, false);
        setVisible(itemRemoveButton, false);
        form.add(FxTheme.actionRow(add, itemUpdateButton, itemRemoveButton, clear), 0, 6, 2, 1);
        return form;
    }

    private HBox createSaveButtons() {
        Button save = FxTheme.primaryButton("Save New");
        Button update = FxTheme.primaryButton("Update Existing");
        Button clear = FxTheme.secondaryButton("New Transfer");
        save.setOnAction(e -> saveTransfer());
        update.setOnAction(e -> updateTransfer());
        clear.setOnAction(e -> clearForm());
        return FxTheme.toolbar(clear, update, save);
    }

    private BorderPane createHistoryPane() {
        HBox toolbar = FxTheme.toolbar(transferSearchField);
        HBox.setHgrow(transferSearchField, Priority.ALWAYS);

        VBox top = new VBox(10, transferTable);
        VBox bottom = new VBox(10,
                FxTheme.toolbar(new Label("Selected Transfer Items")),
                transferItemsTable
        );

        SplitPane split = new SplitPane(top, bottom);
        split.setOrientation(javafx.geometry.Orientation.VERTICAL);
        split.setDividerPositions(0.58);

        BorderPane pane = new BorderPane();
        pane.setCenter(FxTheme.ledgerSurface("Transfer Ledger", toolbar, split));
        return pane;
    }

    private void addRow(GridPane form, int row, String label, javafx.scene.Node field) {
        form.add(new Label(label), 0, row);
        form.add(field, 1, row);
    }

    private void configureTables() {
        itemTable.getColumns().add(FxTableUtil.column("Product ID", TransferLine::getProductId, 90));
        itemTable.getColumns().add(FxTableUtil.column("Product", TransferLine::getProductName, 180));
        itemTable.getColumns().add(FxTableUtil.column("Quantity", TransferLine::getQuantity, 90));
        itemTable.setItems(currentItems);
        FxTheme.styleTable(itemTable);
        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, line) -> {
            if (line != null) fillItemForm(line);
        });

        transferTable.getColumns().add(FxTableUtil.column("Transfer ID", WarehouseTransfer::getTransferId, 90));
        transferTable.getColumns().add(FxTableUtil.column("Date", WarehouseTransfer::getTransferDate, 110));
        transferTable.getColumns().add(FxTableUtil.column("From Warehouse ID", WarehouseTransfer::getFromWarehouseId, 130));
        transferTable.getColumns().add(FxTableUtil.column("From Warehouse", transfer -> warehouseName(transfer.getFromWarehouseId()), 160));
        transferTable.getColumns().add(FxTableUtil.column("To Warehouse ID", WarehouseTransfer::getToWarehouseId, 120));
        transferTable.getColumns().add(FxTableUtil.column("To Warehouse", transfer -> warehouseName(transfer.getToWarehouseId()), 160));
        FxTableUtil.installSearch(transferTable, transfers, transferSearchField);
        FxTheme.styleTable(transferTable);
        transferTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, transfer) -> loadTransferItems(transfer));
        transferTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) loadSelectedTransferForEdit();
        });

        transferItemsTable.getColumns().add(FxTableUtil.column("Item ID", WarehouseTransferItem::getTransferItemId, 80));
        transferItemsTable.getColumns().add(FxTableUtil.column("Transfer ID", WarehouseTransferItem::getTransferId, 90));
        transferItemsTable.getColumns().add(FxTableUtil.column("Product ID", WarehouseTransferItem::getProductId, 90));
        transferItemsTable.getColumns().add(FxTableUtil.column("Product", item -> productName(item.getProductId()), 180));
        transferItemsTable.getColumns().add(FxTableUtil.column("Quantity", WarehouseTransferItem::getQuantity, 90));
        transferItemsTable.setItems(selectedTransferItems);
        FxTheme.styleTable(transferItemsTable);
    }

    private void loadData() {
        fromWarehouseComboBox.setItems(FXCollections.observableArrayList(warehouseDAO.getAllWarehouses()));
        toWarehouseComboBox.setItems(FXCollections.observableArrayList(warehouseDAO.getAllWarehouses()));
        productComboBox.setItems(FXCollections.observableArrayList(productDAO.getAllProducts()));
        transfers.setAll(transferDAO.getAllTransfers());
        if (!fromWarehouseComboBox.getItems().isEmpty()) fromWarehouseComboBox.getSelectionModel().selectFirst();
        if (toWarehouseComboBox.getItems().size() > 1) toWarehouseComboBox.getSelectionModel().select(1);
        else if (!toWarehouseComboBox.getItems().isEmpty()) toWarehouseComboBox.getSelectionModel().selectFirst();
        if (!productComboBox.getItems().isEmpty()) productComboBox.getSelectionModel().selectFirst();
    }

    private void addItem() {
        Product product = productComboBox.getValue();
        if (product == null) return;

        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                FxTheme.showError("Quantity must be positive.");
                return;
            }

            if (!mergeItem(product, quantity)) {
                currentItems.add(new TransferLine(product.getProductId(), product.getProductName(), quantity));
            }
            quantityField.clear();
        } catch (Exception e) {
            FxTheme.showError("Quantity must be valid.");
        }
    }

    private boolean mergeItem(Product product, int quantity) {
        for (TransferLine line : currentItems) {
            if (line.productId == product.getProductId()) {
                line.quantity += quantity;
                itemTable.refresh();
                return true;
            }
        }
        return false;
    }

    private void updateSelectedItem() {
        TransferLine selected = itemTable.getSelectionModel().getSelectedItem();
        Product product = productComboBox.getValue();
        if (selected == null || product == null) {
            FxTheme.showError("Select an item and product.");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                FxTheme.showError("Quantity must be positive.");
                return;
            }
            selected.productId = product.getProductId();
            selected.productName = product.getProductName();
            selected.quantity = quantity;
            itemTable.refresh();
            clearItemForm();
        } catch (Exception e) {
            FxTheme.showError("Quantity must be valid.");
        }
    }

    private void removeItem() {
        TransferLine selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            currentItems.remove(selected);
            clearItemForm();
        }
    }

    private void saveTransfer() {
        WarehouseTransfer transfer = buildTransfer(-1);
        if (transfer == null) return;

        if (transferDAO.createTransfer(transfer)) {
            FxTheme.showInfo("Transfer saved. Inventory updated.");
            clearForm();
            loadData();
        } else {
            FxTheme.showError("Transfer failed. Check available stock.");
        }
    }

    private void updateTransfer() {
        if (editingTransferId <= 0) {
            FxTheme.showError("Load an existing transfer first.");
            return;
        }

        if (!FxTheme.confirm("Update this transfer? Inventory will be adjusted.")) return;

        WarehouseTransfer transfer = buildTransfer(editingTransferId);
        if (transfer == null) return;

        if (transferDAO.updateTransfer(transfer)) {
            FxTheme.showInfo("Transfer updated.");
            clearForm();
            loadData();
        } else {
            FxTheme.showError("Transfer update failed. Check stock availability.");
        }
    }

    private WarehouseTransfer buildTransfer(int transferId) {
        Warehouse from = fromWarehouseComboBox.getValue();
        Warehouse to = toWarehouseComboBox.getValue();
        if (from == null || to == null || currentItems.isEmpty()) {
            FxTheme.showError("Select warehouses and add at least one item.");
            return null;
        }
        if (from.getWarehouseId() == to.getWarehouseId()) {
            FxTheme.showError("Source and destination warehouses cannot be the same.");
            return null;
        }

        ArrayList<WarehouseTransferItem> items = new ArrayList<>();
        for (TransferLine line : currentItems) {
            items.add(new WarehouseTransferItem(line.productId, line.quantity));
        }

        WarehouseTransfer transfer = new WarehouseTransfer(transferDatePicker.getValue(), from.getWarehouseId(), to.getWarehouseId(), items);
        if (transferId > 0) transfer.setTransferId(transferId);
        return transfer;
    }

    private void loadTransferItems(WarehouseTransfer transfer) {
        selectedTransferItems.clear();
        if (transfer != null) {
            selectedTransferItems.setAll(transferDAO.getTransferItems(transfer.getTransferId()));
        }
    }

    private void loadSelectedTransferForEdit() {
        WarehouseTransfer selected = transferTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxTheme.showError("Select a transfer first.");
            return;
        }

        WarehouseTransfer transfer = transferDAO.getTransferById(selected.getTransferId());
        if (transfer == null) {
            FxTheme.showError("Could not load selected transfer.");
            return;
        }

        editingTransferId = transfer.getTransferId();
        modeLabel.setText("Mode: Editing Transfer #" + editingTransferId);
        selectWarehouse(fromWarehouseComboBox, transfer.getFromWarehouseId());
        selectWarehouse(toWarehouseComboBox, transfer.getToWarehouseId());
        transferDatePicker.setValue(transfer.getTransferDate());
        currentItems.clear();

        for (WarehouseTransferItem item : transfer.getItems()) {
            currentItems.add(new TransferLine(item.getProductId(), productName(item.getProductId()), item.getQuantity()));
        }
    }

    private void fillItemForm(TransferLine line) {
        selectProduct(line.productId);
        quantityField.setText(String.valueOf(line.quantity));
        updateItemButtons(true);
    }

    private void clearItemForm() {
        itemTable.getSelectionModel().clearSelection();
        quantityField.clear();
        updateItemButtons(false);
    }

    private void updateItemButtons(boolean itemSelected) {
        if (itemUpdateButton != null) {
            setVisible(itemUpdateButton, itemSelected);
        }
        if (itemRemoveButton != null) {
            setVisible(itemRemoveButton, itemSelected);
        }
    }

    private void setVisible(javafx.scene.Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void selectProduct(int productId) {
        productComboBox.getItems().stream()
                .filter(product -> product.getProductId() == productId)
                .findFirst()
                .ifPresent(productComboBox::setValue);
    }

    private void selectWarehouse(ComboBox<Warehouse> comboBox, int warehouseId) {
        comboBox.getItems().stream()
                .filter(warehouse -> warehouse.getWarehouseId() == warehouseId)
                .findFirst()
                .ifPresent(comboBox::setValue);
    }

    private String warehouseName(int warehouseId) {
        return fromWarehouseComboBox.getItems().stream()
                .filter(warehouse -> warehouse.getWarehouseId() == warehouseId)
                .map(Warehouse::getWarehouseName)
                .findFirst()
                .orElse("Unknown Warehouse");
    }

    private String productName(int productId) {
        return productComboBox.getItems().stream()
                .filter(product -> product.getProductId() == productId)
                .map(Product::getProductName)
                .findFirst()
                .orElse("Unknown Product");
    }

    private void clearForm() {
        editingTransferId = -1;
        modeLabel.setText("Mode: New Transfer");
        transferDatePicker.setValue(LocalDate.now());
        currentItems.clear();
        clearItemForm();
    }

    public static class TransferLine {
        private int productId;
        private String productName;
        private int quantity;

        public TransferLine(int productId, String productName, int quantity) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
        }

        public int getProductId() { return productId; }
        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
    }
}
