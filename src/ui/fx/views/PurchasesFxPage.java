package ui.fx.views;

import dao.ProductDAO;
import dao.PurchaseInvoiceDAO;
import dao.SupplierDAO;
import dao.WarehouseDAO;
import dao.InventoryDAO;
import db.DBConnection;
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
import model.PurchaseInvoice;
import model.PurchaseInvoiceItem;
import model.Supplier;
import model.Warehouse;
import ui.fx.FxTableUtil;
import ui.fx.FxTheme;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

public class PurchasesFxPage extends VBox {

    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final PurchaseInvoiceDAO purchaseInvoiceDAO = new PurchaseInvoiceDAO();

    private final ObservableList<PurchaseInvoiceItem> currentItems = FXCollections.observableArrayList();
    private final ObservableList<PurchaseInvoice> invoices = FXCollections.observableArrayList();
    private final ObservableList<PurchaseInvoiceItem> selectedInvoiceItems = FXCollections.observableArrayList();

    private final ComboBox<Supplier> supplierComboBox = new ComboBox<>();
    private final ComboBox<Warehouse> warehouseComboBox = new ComboBox<>();
    private final DatePicker invoiceDatePicker = new DatePicker(LocalDate.now());
    private final DatePicker arrivalDatePicker = new DatePicker(LocalDate.now().plusDays(5));
    private final TextField paymentField = FxTheme.textField("0.00");
    private final ComboBox<String> paymentTypeComboBox = new ComboBox<>();
    private final ComboBox<Product> productComboBox = new ComboBox<>();
    private final TextField quantityField = FxTheme.textField("Quantity");
    private final TextField priceField = FxTheme.textField("Purchase price");
    private final TextField invoiceSearchField = FxTheme.textField("Search purchase invoices");
    private final Label modeLabel = new Label("Mode: New Purchase Invoice");
    private final Label itemCardTitle = new Label("Add Item");
    private final Label priceHintLabel = new Label(" ");
    private Button itemActionButton;
    private Button itemRemoveButton;
    private final TableView<PurchaseInvoiceItem> itemTable = new TableView<>();
    private final TableView<PurchaseInvoice> invoiceTable = new TableView<>();
    private final TableView<PurchaseInvoiceItem> previousItemsTable = new TableView<>();

    private int editingInvoiceId = -1;
    private PurchaseInvoiceItem editingItem;

    public PurchasesFxPage() {
        styleSelectors();
        getStyleClass().add("ledger-page");
        BorderPane content = createContent();
        VBox.setVgrow(content, Priority.ALWAYS);
        getChildren().add(content);
        loadData();
    }

    private void styleSelectors() {
        FxTheme.styleComboBox(supplierComboBox);
        FxTheme.styleComboBox(warehouseComboBox);
        FxTheme.styleComboBox(paymentTypeComboBox);
        FxTheme.styleComboBox(productComboBox);
        supplierComboBox.getStyleClass().add("compact-selector");
        warehouseComboBox.getStyleClass().add("compact-selector");
        productComboBox.getStyleClass().add("compact-selector");
        paymentTypeComboBox.getStyleClass().add("compact-selector");
    }

    private BorderPane createContent() {
        paymentTypeComboBox.setItems(FXCollections.observableArrayList("Cash", "Card", "Bank Transfer", "Cheque"));
        paymentTypeComboBox.getSelectionModel().selectFirst();
        modeLabel.getStyleClass().add("card-title");
        itemCardTitle.getStyleClass().add("card-title");
        priceHintLabel.getStyleClass().add("muted-label");

        configureTables();

        supplierComboBox.setOnAction(e -> updateDefaultPurchasePrice());
        productComboBox.setOnAction(e -> updateDefaultPurchasePrice());

        itemTable.setMinHeight(118);
        itemTable.setPrefHeight(125);
        itemTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox editor = new VBox(6,
                sectionLabel("Invoice"),
                createInvoiceForm(),
                sectionLabel("Line Item"),
                createItemForm(),
                sectionLabel("Current Items"),
                itemTable,
                createSaveButtons()
        );
        editor.getStyleClass().add("workflow-editor");

        return FxTheme.ledgerWorkspace(createHistoryPane(), FxTheme.ledgerInspector("Purchase Inspector", editor));
    }

    private Label sectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("ledger-section-label");
        return label;
    }

    private GridPane createInvoiceForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        addRow(form, 0, "Status", modeLabel);
        addRow(form, 1, "Supplier", supplierComboBox);
        addRow(form, 2, "Warehouse", warehouseComboBox);
        addRow(form, 3, "Invoice Date", invoiceDatePicker);
        addRow(form, 4, "Estimated Arrival", arrivalDatePicker);
        addRow(form, 5, "Payment", paymentField);
        addRow(form, 6, "Payment Type", paymentTypeComboBox);
        return form;
    }

    private GridPane createItemForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        addRow(form, 0, "Product", productComboBox);
        addRow(form, 1, "Quantity", quantityField);
        addRow(form, 2, "Purchase Price", priceField);
        form.add(priceHintLabel, 1, 3);

        itemActionButton = FxTheme.primaryButton("Add");
        itemRemoveButton = FxTheme.secondaryButton("Remove");
        Button clear = FxTheme.secondaryButton("Clear");
        itemActionButton.setOnAction(e -> saveCurrentItem());
        itemRemoveButton.setOnAction(e -> removeItem());
        clear.setOnAction(e -> clearItemForm());
        setVisible(itemRemoveButton, false);
        form.add(FxTheme.actionRow(itemActionButton, itemRemoveButton, clear), 0, 4, 2, 1);
        return form;
    }

    private HBox createSaveButtons() {
        Button save = FxTheme.primaryButton("Save");
        Button update = FxTheme.primaryButton("Update");
        Button delete = FxTheme.dangerButton("Delete");
        Button clear = FxTheme.secondaryButton("New");

        save.setOnAction(e -> saveInvoice());
        update.setOnAction(e -> updateInvoice());
        delete.setOnAction(e -> deleteInvoice());
        clear.setOnAction(e -> clearInvoice());

        return FxTheme.toolbar(clear, delete, update, save);
    }

    private BorderPane createHistoryPane() {
        BorderPane pane = new BorderPane();

        HBox toolbar = FxTheme.toolbar(invoiceSearchField);
        HBox.setHgrow(invoiceSearchField, Priority.ALWAYS);

        VBox top = new VBox(10, invoiceTable);
        VBox bottom = new VBox(10,
                FxTheme.toolbar(new Label("Selected Invoice Items")),
                previousItemsTable
        );

        SplitPane split = new SplitPane(top, bottom);
        split.setOrientation(javafx.geometry.Orientation.VERTICAL);
        split.setDividerPositions(0.58);

        pane.setCenter(FxTheme.ledgerSurface("Purchase Invoice Ledger", toolbar, split));
        return pane;
    }

    private void addRow(GridPane form, int row, String label, javafx.scene.Node field) {
        form.add(new Label(label), 0, row);
        form.add(field, 1, row);
    }

    private void configureTables() {
        itemTable.getColumns().add(FxTableUtil.column("Product ID", PurchaseInvoiceItem::getProductId, 90));
        itemTable.getColumns().add(FxTableUtil.column("Product", PurchaseInvoiceItem::getProductName, 190));
        itemTable.getColumns().add(FxTableUtil.column("Quantity", PurchaseInvoiceItem::getQuantity, 90));
        itemTable.getColumns().add(FxTableUtil.column("Price", PurchaseInvoiceItem::getPurchasePrice, 100));
        itemTable.getColumns().add(FxTableUtil.column("Line Total", item -> item.getPurchasePrice().multiply(BigDecimal.valueOf(item.getQuantity())), 120));
        itemTable.setItems(currentItems);
        FxTheme.styleTable(itemTable);
        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, item) -> {
            if (item != null) {
                fillItemForm(item);
            }
        });

        invoiceTable.getColumns().add(FxTableUtil.column("Invoice ID", PurchaseInvoice::getPurchaseInvoiceId, 90));
        invoiceTable.getColumns().add(FxTableUtil.column("Date", PurchaseInvoice::getInvoiceDate, 110));
        invoiceTable.getColumns().add(FxTableUtil.column("Supplier", PurchaseInvoice::getSupplierName, 170));
        invoiceTable.getColumns().add(FxTableUtil.column("Warehouse", PurchaseInvoice::getWarehouseName, 150));
        invoiceTable.getColumns().add(FxTableUtil.column("Payment", PurchaseInvoice::getPayment, 100));
        invoiceTable.getColumns().add(FxTableUtil.column("Payment Type", PurchaseInvoice::getPaymentType, 120));
        invoiceTable.getColumns().add(FxTableUtil.column("Amount", PurchaseInvoice::getAmount, 110));
        FxTableUtil.installSearch(invoiceTable, invoices, invoiceSearchField);
        FxTheme.styleTable(invoiceTable);
        invoiceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, invoice) -> loadInvoiceItems(invoice));
        invoiceTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) loadSelectedInvoiceForEdit();
        });

        previousItemsTable.getColumns().add(FxTableUtil.column("Item ID", PurchaseInvoiceItem::getPurchaseItemId, 80));
        previousItemsTable.getColumns().add(FxTableUtil.column("Product ID", PurchaseInvoiceItem::getProductId, 90));
        previousItemsTable.getColumns().add(FxTableUtil.column("Product", PurchaseInvoiceItem::getProductName, 190));
        previousItemsTable.getColumns().add(FxTableUtil.column("Quantity", PurchaseInvoiceItem::getQuantity, 90));
        previousItemsTable.getColumns().add(FxTableUtil.column("Price", PurchaseInvoiceItem::getPurchasePrice, 100));
        previousItemsTable.getColumns().add(FxTableUtil.column("Line Total", item -> item.getPurchasePrice().multiply(BigDecimal.valueOf(item.getQuantity())), 120));
        previousItemsTable.setItems(selectedInvoiceItems);
        FxTheme.styleTable(previousItemsTable);
    }

    private void loadData() {
        supplierComboBox.setItems(FXCollections.observableArrayList(supplierDAO.getAllSuppliers()));
        productComboBox.setItems(FXCollections.observableArrayList(productDAO.getAllProducts()));
        warehouseComboBox.setItems(FXCollections.observableArrayList(warehouseDAO.getAllWarehouses()));
        invoices.setAll(purchaseInvoiceDAO.getAllPurchaseInvoices());
        if (!supplierComboBox.getItems().isEmpty()) supplierComboBox.getSelectionModel().selectFirst();
        if (!productComboBox.getItems().isEmpty()) productComboBox.getSelectionModel().selectFirst();
        if (!warehouseComboBox.getItems().isEmpty()) warehouseComboBox.getSelectionModel().selectFirst();
        updateDefaultPurchasePrice();
    }

    private void saveCurrentItem() {
        Product product = productComboBox.getValue();
        if (product == null) return;

        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            BigDecimal price = new BigDecimal(priceField.getText().trim());

            if (quantity <= 0 || price.signum() < 0) {
                FxTheme.showError("Quantity must be positive and price cannot be negative.");
                return;
            }

            warnIfUnlinkedSupplier(product);

            if (editingItem == null && !mergeItem(product, price, quantity, null)) {
                PurchaseInvoiceItem item = new PurchaseInvoiceItem(product.getProductId(), price, quantity);
                item.setProductName(product.getProductName());
                currentItems.add(item);
            } else if (editingItem != null) {
                updateEditingItem(product, price, quantity);
            }

            clearItemForm();
            updatePaymentToTotal();
        } catch (Exception e) {
            FxTheme.showError("Quantity and price must be valid numbers.");
        }
    }

    private boolean mergeItem(Product product, BigDecimal price, int quantity, PurchaseInvoiceItem excludedItem) {
        for (PurchaseInvoiceItem item : currentItems) {
            if (item != excludedItem
                    && item.getProductId() == product.getProductId()
                    && item.getPurchasePrice().compareTo(price) == 0) {
                item.setQuantity(item.getQuantity() + quantity);
                itemTable.refresh();
                return true;
            }
        }
        return false;
    }

    private void updateEditingItem(Product product, BigDecimal price, int quantity) {
        if (mergeItem(product, price, quantity, editingItem)) {
            currentItems.remove(editingItem);
        } else {
            editingItem.setProductId(product.getProductId());
            editingItem.setProductName(product.getProductName());
            editingItem.setPurchasePrice(price);
            editingItem.setQuantity(quantity);
            itemTable.refresh();
        }
    }

    private void removeItem() {
        PurchaseInvoiceItem selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            currentItems.remove(selected);
            clearItemForm();
            updatePaymentToTotal();
        }
    }

    private void saveInvoice() {
        PurchaseInvoice invoice = buildInvoice(-1);
        if (invoice == null) return;

        if (purchaseInvoiceDAO.createPurchaseInvoice(invoice)) {
            FxTheme.showInfo("Purchase invoice saved. Inventory increased.");
            clearInvoice();
            loadData();
        } else {
            FxTheme.showError("Failed to save purchase invoice.");
        }
    }

    private void updateInvoice() {
        if (editingInvoiceId <= 0) {
            FxTheme.showError("Load an invoice first.");
            return;
        }

        if (!FxTheme.confirm("Update this purchase invoice? Inventory will be adjusted.")) return;

        PurchaseInvoice invoice = buildInvoice(editingInvoiceId);
        if (invoice == null) return;

        if (purchaseInvoiceDAO.updatePurchaseInvoice(invoice)) {
            FxTheme.showInfo("Purchase invoice updated.");
            clearInvoice();
            loadData();
        } else {
            FxTheme.showError("Failed to update purchase invoice.");
        }
    }

    private void deleteInvoice() {
        PurchaseInvoice selected = invoiceTable.getSelectionModel().getSelectedItem();
        int invoiceId = editingInvoiceId > 0 ? editingInvoiceId : selected == null ? -1 : selected.getPurchaseInvoiceId();

        if (invoiceId <= 0) {
            FxTheme.showError("Select an invoice first.");
            return;
        }

        if (!FxTheme.confirm("Delete purchase invoice #" + invoiceId + "? Purchased stock will be removed if still available.")) return;

        if (purchaseInvoiceDAO.deletePurchaseInvoice(invoiceId)) {
            FxTheme.showInfo("Purchase invoice deleted.");
            clearInvoice();
            loadData();
            selectedInvoiceItems.clear();
        } else {
            FxTheme.showError("Failed to delete purchase invoice.");
        }
    }

    private PurchaseInvoice buildInvoice(int invoiceId) {
        Supplier supplier = supplierComboBox.getValue();
        Warehouse warehouse = warehouseComboBox.getValue();
        if (supplier == null || warehouse == null || currentItems.isEmpty()) {
            FxTheme.showError("Select supplier, warehouse, and add at least one item.");
            return null;
        }

        BigDecimal payment;
        try {
            payment = new BigDecimal(paymentField.getText().trim());
        } catch (Exception e) {
            FxTheme.showError("Payment must be valid.");
            return null;
        }

        BigDecimal total = totalAmount();
        PurchaseInvoice invoice = new PurchaseInvoice(
                invoiceDatePicker.getValue(),
                arrivalDatePicker.getValue(),
                payment,
                paymentTypeComboBox.getValue(),
                total,
                supplier.getSupplierId(),
                warehouse.getWarehouseId(),
                new ArrayList<>(currentItems)
        );

        if (invoiceId > 0) invoice.setPurchaseInvoiceId(invoiceId);
        if (!warehouseHasCapacity(invoice)) return null;
        return invoice;
    }

    private boolean warehouseHasCapacity(PurchaseInvoice invoice) {
        Warehouse warehouse = warehouseComboBox.getValue();
        if (warehouse == null || warehouse.getCapacity() <= 0) return true;

        int currentQuantity = inventoryDAO.getWarehouseTotalQuantity(warehouse.getWarehouseId());
        int incomingQuantity = totalItemQuantity(invoice.getItems());

        if (invoice.getPurchaseInvoiceId() > 0) {
            PurchaseInvoice oldInvoice = purchaseInvoiceDAO.getPurchaseInvoiceById(invoice.getPurchaseInvoiceId());
            if (oldInvoice != null && oldInvoice.getWarehouseId() == warehouse.getWarehouseId()) {
                currentQuantity -= totalItemQuantity(oldInvoice.getItems());
            }
        }

        int finalQuantity = currentQuantity + incomingQuantity;
        if (finalQuantity > warehouse.getCapacity()) {
            FxTheme.showError("Warehouse capacity exceeded. Capacity: " + warehouse.getCapacity()
                    + ", current quantity: " + currentQuantity
                    + ", incoming quantity: " + incomingQuantity + ".");
            return false;
        }

        return true;
    }

    private int totalItemQuantity(java.util.List<PurchaseInvoiceItem> items) {
        int total = 0;
        for (PurchaseInvoiceItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }

    private void loadInvoiceItems(PurchaseInvoice invoice) {
        selectedInvoiceItems.clear();
        if (invoice != null) {
            selectedInvoiceItems.setAll(purchaseInvoiceDAO.getPurchaseInvoiceItems(invoice.getPurchaseInvoiceId()));
        }
    }

    private void loadSelectedInvoiceForEdit() {
        PurchaseInvoice selected = invoiceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxTheme.showError("Select an invoice first.");
            return;
        }

        PurchaseInvoice invoice = purchaseInvoiceDAO.getPurchaseInvoiceById(selected.getPurchaseInvoiceId());
        if (invoice == null) {
            FxTheme.showError("Could not load selected invoice.");
            return;
        }

        editingInvoiceId = invoice.getPurchaseInvoiceId();
        modeLabel.setText("Mode: Editing Purchase Invoice #" + editingInvoiceId);
        selectSupplier(invoice.getSupplierId());
        selectWarehouse(invoice.getWarehouseId());
        invoiceDatePicker.setValue(invoice.getInvoiceDate());
        arrivalDatePicker.setValue(invoice.getEstimatedArrival());
        paymentField.setText(invoice.getPayment().toString());
        paymentTypeComboBox.setValue(invoice.getPaymentType());
        currentItems.setAll(invoice.getItems());
        clearItemForm();
        updatePaymentToTotal();
    }

    private void updateDefaultPurchasePrice() {
        Supplier supplier = supplierComboBox.getValue();
        Product product = productComboBox.getValue();
        if (supplier == null || product == null) return;

        BigDecimal price = supplierPrice(supplier.getSupplierId(), product.getProductId());
        if (price != null) {
            priceField.setText(price.toString());
            priceHintLabel.setText("Default supplier price loaded.");
            priceHintLabel.getStyleClass().setAll("success-label");
        } else {
            priceHintLabel.setText("This supplier is not linked to the selected product.");
            priceHintLabel.getStyleClass().setAll("warning-label");
        }
    }

    private void warnIfUnlinkedSupplier(Product product) {
        Supplier supplier = supplierComboBox.getValue();
        if (supplier != null && supplierPrice(supplier.getSupplierId(), product.getProductId()) == null) {
            FxTheme.showError("Warning: this supplier is not linked to the selected product.");
        }
    }

    private BigDecimal supplierPrice(int supplierId, int productId) {
        String sql = "SELECT supply_price FROM SupplierProduct WHERE supplier_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, supplierId);
            stmt.setInt(2, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("supply_price") : null;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    private BigDecimal totalAmount() {
        return currentItems.stream()
                .map(item -> item.getPurchasePrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void updatePaymentToTotal() {
        paymentField.setText(totalAmount().toString());
    }

    private void selectSupplier(int supplierId) {
        supplierComboBox.getItems().stream()
                .filter(supplier -> supplier.getSupplierId() == supplierId)
                .findFirst()
                .ifPresent(supplierComboBox::setValue);
    }

    private void selectWarehouse(int warehouseId) {
        warehouseComboBox.getItems().stream()
                .filter(warehouse -> warehouse.getWarehouseId() == warehouseId)
                .findFirst()
                .ifPresent(warehouseComboBox::setValue);
    }

    private void clearInvoice() {
        editingInvoiceId = -1;
        modeLabel.setText("Mode: New Purchase Invoice");
        invoiceDatePicker.setValue(LocalDate.now());
        arrivalDatePicker.setValue(LocalDate.now().plusDays(5));
        paymentField.setText("0.00");
        paymentTypeComboBox.getSelectionModel().selectFirst();
        currentItems.clear();
        clearItemForm();
        updateDefaultPurchasePrice();
    }

    private void fillItemForm(PurchaseInvoiceItem item) {
        editingItem = item;
        selectProduct(item.getProductId());
        quantityField.setText(String.valueOf(item.getQuantity()));
        priceField.setText(item.getPurchasePrice().toString());
        updateItemEditMode();
    }

    private void clearItemForm() {
        editingItem = null;
        itemTable.getSelectionModel().clearSelection();
        quantityField.clear();
        updateDefaultPurchasePrice();
        updateItemEditMode();
    }

    private void updateItemEditMode() {
        itemCardTitle.setText(editingItem == null ? "Add Item" : "Edit Item");
        if (itemActionButton != null) {
            itemActionButton.setText(editingItem == null ? "Add" : "Update");
        }
        if (itemRemoveButton != null) {
            setVisible(itemRemoveButton, editingItem != null);
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
}
