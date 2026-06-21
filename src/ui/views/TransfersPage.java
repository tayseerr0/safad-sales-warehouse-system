package ui.views;

import dao.ProductDAO;
import dao.WarehouseDAO;
import dao.WarehouseTransferDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
import ui.TableUtil;
import ui.Theme;

import java.time.LocalDate;
import java.util.ArrayList;

public class TransfersPage extends VBox {

    private final ProductDAO productDAO = new ProductDAO();
    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    private final WarehouseTransferDAO transferDAO = new WarehouseTransferDAO();

    private final ObservableList<TransferLine> currentItems = FXCollections.observableArrayList();
    private final ObservableList<WarehouseTransfer> transfers = FXCollections.observableArrayList();

    private final ComboBox<Warehouse> fromWarehouseComboBox = new ComboBox<>();
    private final ComboBox<Warehouse> toWarehouseComboBox = new ComboBox<>();
    private final ComboBox<Product> productComboBox = new ComboBox<>();
    private final DatePicker transferDatePicker = new DatePicker(LocalDate.now());
    private final TextField quantityField = Theme.textField("Quantity");
    private final TextField transferSearchField = Theme.textField("Search transfers");
    private final ComboBox<String> transferSearchColumnBox = TableUtil.searchColumnBox();
    private final Label modeLabel = new Label("Mode: New Transfer");
    private final TableView<TransferLine> itemTable = new TableView<>();
    private final TableView<WarehouseTransfer> transferTable = new TableView<>();
    private Button itemActionButton;
    private Button itemRemoveButton;
    private Button transferActionButton;

    private int editingTransferId = -1;

    public TransfersPage() {
        Theme.styleComboBox(fromWarehouseComboBox);
        Theme.styleComboBox(toWarehouseComboBox);
        Theme.styleComboBox(productComboBox);
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

        itemTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox editor = new VBox(9,
                sectionLabel("Transfer"),
                createTransferForm(),
                createSaveButtons(),
                sectionLabel("Line Item"),
                createItemForm()
        );
        editor.getStyleClass().add("workflow-editor");

        return Theme.ledgerWorkspace(createHistoryPane(), Theme.ledgerInspector("Transfer Inspector", scrollEditor(editor)));
    }

    private ScrollPane scrollEditor(VBox editor) {
        ScrollPane scroll = new ScrollPane(editor);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("inspector-scroll");
        return scroll;
    }

    private Label sectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("ledger-section-label");
        return label;
    }

    private GridPane createTransferForm() {
        GridPane form = new GridPane();
        Theme.configureInspectorForm(form);
        addRow(form, 0, "Status", modeLabel);
        addRow(form, 1, "From Warehouse", fromWarehouseComboBox);
        addRow(form, 2, "To Warehouse", toWarehouseComboBox);
        addRow(form, 3, "Date", transferDatePicker);
        return form;
    }

    private GridPane createItemForm() {
        GridPane form = new GridPane();
        Theme.configureInspectorForm(form);
        addRow(form, 0, "Product", productComboBox);
        addRow(form, 1, "Quantity", quantityField);

        itemActionButton = Theme.primaryButton("Add");
        itemRemoveButton = Theme.secondaryButton("Remove");
        Button clear = Theme.secondaryButton("Clear");
        itemActionButton.setOnAction(e -> saveOrUpdateItem());
        itemRemoveButton.setOnAction(e -> removeItem());
        clear.setOnAction(e -> clearItemForm());
        Theme.setVisible(itemRemoveButton, false);
        Theme.addInspectorActions(form, 2, itemActionButton, itemRemoveButton, clear);
        return form;
    }

    private HBox createSaveButtons() {
        transferActionButton = Theme.primaryButton("Save");
        Button clear = Theme.secondaryButton("Clear");
        transferActionButton.setOnAction(e -> saveOrUpdateTransfer());
        clear.setOnAction(e -> clearForm());
        HBox actions = Theme.compactActionRow(transferActionButton, clear);
        actions.getStyleClass().add("transfer-actions-row");
        actions.setMaxWidth(Double.MAX_VALUE);
        return actions;
    }

    private BorderPane createHistoryPane() {
        Button refresh = Theme.refreshButton();
        refresh.setOnAction(e -> {
            transferSearchField.clear();
            loadData();
        });
        HBox toolbar = Theme.ledgerCommandBar(transferSearchColumnBox, transferSearchField, refresh);
        Theme.stretchToolbarField(transferSearchField);

        VBox top = new VBox(10, transferTable);
        VBox bottom = new VBox(10,
                Theme.ledgerCommandBar(new Label("Selected Transfer Items")),
                itemTable
        );
        VBox.setVgrow(transferTable, Priority.ALWAYS);
        VBox.setVgrow(itemTable, Priority.ALWAYS);

        SplitPane split = new SplitPane(top, bottom);
        split.setOrientation(javafx.geometry.Orientation.VERTICAL);
        split.setDividerPositions(0.58);
        split.setMinWidth(0);

        BorderPane pane = new BorderPane();
        pane.setCenter(Theme.ledgerSurface("Transfer Ledger", toolbar, split));
        pane.setMinWidth(0);
        return pane;
    }

    private void addRow(GridPane form, int row, String label, javafx.scene.Node field) {
        Theme.addInspectorRow(form, row, label, field);
    }

    private void configureTables() {
        itemTable.getColumns().add(TableUtil.column("Product ID", TransferLine::getProductId, 90));
        itemTable.getColumns().add(TableUtil.column("Product", TransferLine::getProductName, 180));
        itemTable.getColumns().add(TableUtil.column("Quantity", TransferLine::getQuantity, 90));
        itemTable.setItems(currentItems);
        Theme.styleTable(itemTable);
        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, line) -> {
            if (line != null) fillItemForm(line);
        });

        transferTable.getColumns().add(TableUtil.column("Transfer ID", WarehouseTransfer::getTransferId, 90));
        transferTable.getColumns().add(TableUtil.column("Date", WarehouseTransfer::getTransferDate, 110));
        transferTable.getColumns().add(TableUtil.column("From Warehouse ID", WarehouseTransfer::getFromWarehouseId, 130));
        transferTable.getColumns().add(TableUtil.column("From Warehouse", transfer -> warehouseName(transfer.getFromWarehouseId()), 160));
        transferTable.getColumns().add(TableUtil.column("To Warehouse ID", WarehouseTransfer::getToWarehouseId, 120));
        transferTable.getColumns().add(TableUtil.column("To Warehouse", transfer -> warehouseName(transfer.getToWarehouseId()), 160));
        TableUtil.installSearch(transferTable, transfers, transferSearchField, transferSearchColumnBox);
        Theme.styleTable(transferTable);
        transferTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, transfer) -> {
            if (transfer != null) {
                loadSelectedTransferForEdit();
            }
        });
        transferTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) loadSelectedTransferForEdit();
        });

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
                Theme.showError("Quantity must be positive.");
                return;
            }

            if (!mergeItem(product, quantity)) {
                currentItems.add(new TransferLine(product.getProductId(), product.getProductName(), quantity));
            }
            quantityField.clear();
        } catch (Exception e) {
            Theme.showError("Quantity must be valid.");
        }
    }

    private void saveOrUpdateItem() {
        if (itemTable.getSelectionModel().getSelectedItem() == null) {
            addItem();
        } else {
            updateSelectedItem();
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
            Theme.showError("Select an item and product.");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                Theme.showError("Quantity must be positive.");
                return;
            }
            selected.productId = product.getProductId();
            selected.productName = product.getProductName();
            selected.quantity = quantity;
            itemTable.refresh();
            clearItemForm();
        } catch (Exception e) {
            Theme.showError("Quantity must be valid.");
        }
    }

    private void saveOrUpdateTransfer() {
        if (editingTransferId > 0) {
            updateTransfer();
        } else {
            saveTransfer();
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
            Theme.showInfo("Transfer saved. Inventory updated.");
            clearForm();
            loadData();
        } else {
            Theme.showError("Transfer failed. Check available stock.");
        }
    }

    private void updateTransfer() {
        if (editingTransferId <= 0) {
            Theme.showError("Load an existing transfer first.");
            return;
        }

        if (!Theme.confirm("Update this transfer? Inventory will be adjusted.")) return;

        WarehouseTransfer transfer = buildTransfer(editingTransferId);
        if (transfer == null) return;

        if (transferDAO.updateTransfer(transfer)) {
            Theme.showInfo("Transfer updated.");
            clearForm();
            loadData();
        } else {
            Theme.showError("Transfer update failed. Check stock availability.");
        }
    }

    private WarehouseTransfer buildTransfer(int transferId) {
        Warehouse from = fromWarehouseComboBox.getValue();
        Warehouse to = toWarehouseComboBox.getValue();
        if (from == null || to == null || currentItems.isEmpty()) {
            Theme.showError("Select warehouses and add at least one item.");
            return null;
        }
        if (from.getWarehouseId() == to.getWarehouseId()) {
            Theme.showError("Source and destination warehouses cannot be the same.");
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

    private void loadSelectedTransferForEdit() {
        WarehouseTransfer selected = transferTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Theme.showError("Select a transfer first.");
            return;
        }

        WarehouseTransfer transfer = transferDAO.getTransferById(selected.getTransferId());
        if (transfer == null) {
            Theme.showError("Could not load selected transfer.");
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
        clearItemForm();
        updateTransferEditMode();
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
        if (itemActionButton != null) {
            itemActionButton.setText(itemSelected ? "Update" : "Add");
        }
        if (itemRemoveButton != null) {
            Theme.setVisible(itemRemoveButton, itemSelected);
        }
    }

    private void updateTransferEditMode() {
        if (transferActionButton != null) {
            transferActionButton.setText(editingTransferId > 0 ? "Update" : "Save");
        }
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
        transferTable.getSelectionModel().clearSelection();
        updateTransferEditMode();
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
