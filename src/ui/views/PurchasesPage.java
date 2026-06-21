package ui.views;

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
import model.PurchaseInvoice;
import model.PurchaseInvoiceItem;
import model.Supplier;
import model.Warehouse;
import ui.TableUtil;
import ui.Theme;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

public class PurchasesPage extends VBox {

    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final PurchaseInvoiceDAO purchaseInvoiceDAO = new PurchaseInvoiceDAO();

    private final ObservableList<PurchaseInvoiceItem> currentItems = FXCollections.observableArrayList();
    private final ObservableList<PurchaseInvoice> invoices = FXCollections.observableArrayList();

    private final ComboBox<Supplier> supplierComboBox = new ComboBox<>();
    private final ComboBox<Warehouse> warehouseComboBox = new ComboBox<>();
    private final DatePicker invoiceDatePicker = new DatePicker(LocalDate.now());
    private final DatePicker arrivalDatePicker = new DatePicker(LocalDate.now().plusDays(5));
    private final TextField paymentField = Theme.textField("0.00");
    private final ComboBox<String> paymentTypeComboBox = new ComboBox<>();
    private final ComboBox<Product> productComboBox = new ComboBox<>();
    private final TextField quantityField = Theme.textField("Quantity");
    private final TextField priceField = Theme.textField("Purchase price");
    private final TextField invoiceSearchField = Theme.textField("Search purchase invoices");
    private final ComboBox<String> invoiceSearchColumnBox = TableUtil.searchColumnBox();
    private final Label modeLabel = new Label("Mode: New Purchase Invoice");
    private final Label itemCardTitle = new Label("Add Item");
    private final Label priceHintLabel = new Label(" ");
    private Button itemActionButton;
    private Button itemRemoveButton;
    private Button invoiceActionButton;
    private Button invoiceDeleteButton;
    private final TableView<PurchaseInvoiceItem> itemTable = new TableView<>();
    private final TableView<PurchaseInvoice> invoiceTable = new TableView<>();

    private int editingInvoiceId = -1;
    private PurchaseInvoiceItem editingItem;

    public PurchasesPage() {
        styleSelectors();
        getStyleClass().add("ledger-page");
        BorderPane content = createContent();
        VBox.setVgrow(content, Priority.ALWAYS);
        getChildren().add(content);
        loadData();
    }

    private void styleSelectors() {
        Theme.styleComboBox(supplierComboBox);
        Theme.styleComboBox(warehouseComboBox);
        Theme.styleComboBox(paymentTypeComboBox);
        Theme.styleComboBox(productComboBox);
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

        itemTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox editor = new VBox(6,
                sectionLabel("Invoice"),
                createInvoiceForm(),
                createSaveButtons(),
                sectionLabel("Line Item"),
                createItemForm()
        );
        editor.getStyleClass().add("workflow-editor");

        return Theme.ledgerWorkspace(createHistoryPane(), Theme.ledgerInspector("Purchase Inspector", scrollEditor(editor)));
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

    private GridPane createInvoiceForm() {
        GridPane form = new GridPane();
        Theme.configureInspectorForm(form);
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
        Theme.configureInspectorForm(form);
        addRow(form, 0, "Product", productComboBox);
        addRow(form, 1, "Quantity", quantityField);
        addRow(form, 2, "Purchase Price", priceField);
        form.add(priceHintLabel, 1, 3);

        itemActionButton = Theme.primaryButton("Add");
        itemRemoveButton = Theme.secondaryButton("Remove");
        Button clear = Theme.secondaryButton("Clear");
        itemActionButton.setOnAction(e -> saveCurrentItem());
        itemRemoveButton.setOnAction(e -> removeItem());
        clear.setOnAction(e -> clearItemForm());
        Theme.setVisible(itemRemoveButton, false);
        Theme.addInspectorActions(form, 4, itemActionButton, itemRemoveButton, clear);
        return form;
    }

    private HBox createSaveButtons() {
        invoiceActionButton = Theme.primaryButton("Save");
        invoiceDeleteButton = Theme.dangerButton("Delete");
        Button clear = Theme.secondaryButton("Clear");

        invoiceActionButton.setOnAction(e -> saveOrUpdateInvoice());
        invoiceDeleteButton.setOnAction(e -> deleteInvoice());
        clear.setOnAction(e -> clearInvoice());
        Theme.setVisible(invoiceDeleteButton, false);

        HBox actions = Theme.compactActionRow(invoiceActionButton, invoiceDeleteButton, clear);
        actions.setMaxWidth(Double.MAX_VALUE);
        return actions;
    }

    private BorderPane createHistoryPane() {
        BorderPane pane = new BorderPane();

        Button refresh = Theme.refreshButton();
        refresh.setOnAction(e -> {
            invoiceSearchField.clear();
            loadData();
        });
        HBox toolbar = Theme.ledgerCommandBar(invoiceSearchColumnBox, invoiceSearchField, refresh);
        Theme.stretchToolbarField(invoiceSearchField);

        VBox top = new VBox(10, invoiceTable);
        VBox bottom = new VBox(10,
                Theme.ledgerCommandBar(new Label("Selected Invoice Items")),
                itemTable
        );
        VBox.setVgrow(invoiceTable, Priority.ALWAYS);
        VBox.setVgrow(itemTable, Priority.ALWAYS);

        SplitPane split = new SplitPane(top, bottom);
        split.setOrientation(javafx.geometry.Orientation.VERTICAL);
        split.setDividerPositions(0.58);
        split.setMinWidth(0);

        pane.setCenter(Theme.ledgerSurface("Purchase Invoice Ledger", toolbar, split));
        pane.setMinWidth(0);
        return pane;
    }

    private void addRow(GridPane form, int row, String label, javafx.scene.Node field) {
        Theme.addInspectorRow(form, row, label, field);
    }

    private void configureTables() {
        itemTable.getColumns().add(TableUtil.column("Product ID", PurchaseInvoiceItem::getProductId, 90));
        itemTable.getColumns().add(TableUtil.column("Product", PurchaseInvoiceItem::getProductName, 190));
        itemTable.getColumns().add(TableUtil.column("Quantity", PurchaseInvoiceItem::getQuantity, 90));
        itemTable.getColumns().add(TableUtil.column("Price", PurchaseInvoiceItem::getPurchasePrice, 100));
        itemTable.getColumns().add(TableUtil.column("Line Total", item -> item.getPurchasePrice().multiply(BigDecimal.valueOf(item.getQuantity())), 120));
        itemTable.setItems(currentItems);
        Theme.styleTable(itemTable);
        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, item) -> {
            if (item != null) {
                fillItemForm(item);
            }
        });

        invoiceTable.getColumns().add(TableUtil.column("Invoice ID", PurchaseInvoice::getPurchaseInvoiceId, 90));
        invoiceTable.getColumns().add(TableUtil.column("Date", PurchaseInvoice::getInvoiceDate, 110));
        invoiceTable.getColumns().add(TableUtil.column("Supplier", PurchaseInvoice::getSupplierName, 170));
        invoiceTable.getColumns().add(TableUtil.column("Warehouse", PurchaseInvoice::getWarehouseName, 150));
        invoiceTable.getColumns().add(TableUtil.column("Payment", PurchaseInvoice::getPayment, 100));
        invoiceTable.getColumns().add(TableUtil.column("Payment Type", PurchaseInvoice::getPaymentType, 120));
        invoiceTable.getColumns().add(TableUtil.column("Amount", PurchaseInvoice::getAmount, 110));
        TableUtil.installSearch(invoiceTable, invoices, invoiceSearchField, invoiceSearchColumnBox);
        Theme.styleTable(invoiceTable);
        invoiceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, invoice) -> {
            if (invoice != null) {
                loadSelectedInvoiceForEdit();
            }
        });
        invoiceTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) loadSelectedInvoiceForEdit();
        });

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
                Theme.showError("Quantity must be positive and price cannot be negative.");
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
            Theme.showError("Quantity and price must be valid numbers.");
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
            Theme.showInfo("Purchase invoice saved. Inventory increased.");
            clearInvoice();
            loadData();
        } else {
            Theme.showError("Failed to save purchase invoice.");
        }
    }

    private void updateInvoice() {
        if (editingInvoiceId <= 0) {
            Theme.showError("Load an invoice first.");
            return;
        }

        if (!Theme.confirm("Update this purchase invoice? Inventory will be adjusted.")) return;

        PurchaseInvoice invoice = buildInvoice(editingInvoiceId);
        if (invoice == null) return;

        if (purchaseInvoiceDAO.updatePurchaseInvoice(invoice)) {
            Theme.showInfo("Purchase invoice updated.");
            clearInvoice();
            loadData();
        } else {
            Theme.showError("Failed to update purchase invoice.");
        }
    }

    private void saveOrUpdateInvoice() {
        if (editingInvoiceId > 0) {
            updateInvoice();
        } else {
            saveInvoice();
        }
    }

    private void deleteInvoice() {
        PurchaseInvoice selected = invoiceTable.getSelectionModel().getSelectedItem();
        int invoiceId = editingInvoiceId > 0 ? editingInvoiceId : selected == null ? -1 : selected.getPurchaseInvoiceId();

        if (invoiceId <= 0) {
            Theme.showError("Select an invoice first.");
            return;
        }

        if (!Theme.confirm("Delete purchase invoice #" + invoiceId + "? Purchased stock will be removed if still available.")) return;

        if (purchaseInvoiceDAO.deletePurchaseInvoice(invoiceId)) {
            Theme.showInfo("Purchase invoice deleted.");
            clearInvoice();
            loadData();
        } else {
            Theme.showError("Failed to delete purchase invoice.");
        }
    }

    private PurchaseInvoice buildInvoice(int invoiceId) {
        Supplier supplier = supplierComboBox.getValue();
        Warehouse warehouse = warehouseComboBox.getValue();
        if (supplier == null || warehouse == null || currentItems.isEmpty()) {
            Theme.showError("Select supplier, warehouse, and add at least one item.");
            return null;
        }

        BigDecimal payment;
        try {
            payment = new BigDecimal(paymentField.getText().trim());
        } catch (Exception e) {
            Theme.showError("Payment must be valid.");
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
            Theme.showError("Warehouse capacity exceeded. Capacity: " + warehouse.getCapacity()
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

    private void loadSelectedInvoiceForEdit() {
        PurchaseInvoice selected = invoiceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Theme.showError("Select an invoice first.");
            return;
        }

        PurchaseInvoice invoice = purchaseInvoiceDAO.getPurchaseInvoiceById(selected.getPurchaseInvoiceId());
        if (invoice == null) {
            Theme.showError("Could not load selected invoice.");
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
        updateInvoiceEditMode();
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
            Theme.showError("Warning: this supplier is not linked to the selected product.");
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
        String paymentText = paymentField.getText();
        if (paymentText == null || paymentText.trim().isEmpty()) {
            paymentField.setText(totalAmount().toString());
        }
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
        invoiceTable.getSelectionModel().clearSelection();
        updateDefaultPurchasePrice();
        updateInvoiceEditMode();
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
            Theme.setVisible(itemRemoveButton, editingItem != null);
        }
    }

    private void updateInvoiceEditMode() {
        boolean editing = editingInvoiceId > 0;
        if (invoiceActionButton != null) {
            invoiceActionButton.setText(editing ? "Update" : "Save");
        }
        if (invoiceDeleteButton != null) {
            Theme.setVisible(invoiceDeleteButton, editing);
        }
    }

    private void selectProduct(int productId) {
        productComboBox.getItems().stream()
                .filter(product -> product.getProductId() == productId)
                .findFirst()
                .ifPresent(productComboBox::setValue);
    }
}
