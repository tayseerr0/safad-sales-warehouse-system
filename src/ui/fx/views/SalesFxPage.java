package ui.fx.views;

import dao.ClientDAO;
import dao.InventoryDAO;
import dao.ProductDAO;
import dao.SalesInvoiceDAO;
import dao.WarehouseDAO;
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
import model.Client;
import model.Product;
import model.SalesInvoice;
import model.SalesInvoiceItem;
import model.Warehouse;
import ui.fx.FxTableUtil;
import ui.fx.FxTheme;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SalesFxPage extends VBox {

    private final ClientDAO clientDAO = new ClientDAO();
    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final SalesInvoiceDAO salesInvoiceDAO = new SalesInvoiceDAO();

    private final ObservableList<LineItem> currentItems = FXCollections.observableArrayList();
    private final ObservableList<SalesInvoice> invoices = FXCollections.observableArrayList();
    private final ObservableList<SalesInvoiceItem> selectedInvoiceItems = FXCollections.observableArrayList();
    private final List<SalesInvoiceItem> originalItems = new ArrayList<>();

    private final ComboBox<Client> clientComboBox = new ComboBox<>();
    private final ComboBox<Warehouse> warehouseComboBox = new ComboBox<>();
    private final ComboBox<Product> productComboBox = new ComboBox<>();
    private final DatePicker invoiceDatePicker = new DatePicker(LocalDate.now());
    private final ComboBox<String> paymentTypeComboBox = new ComboBox<>();
    private final TextField paymentField = FxTheme.textField("0.00");
    private final TextField availableStockField = FxTheme.textField("Stock");
    private final TextField quantityField = FxTheme.textField("Quantity");
    private final TextField priceField = FxTheme.textField("Selling price");
    private final TextField invoiceSearchField = FxTheme.textField("Search sales invoices");
    private final DatePicker warrantyDatePicker = new DatePicker();
    private final Label modeLabel = new Label("Mode: New Sales Invoice");
    private final Label itemCardTitle = new Label("Add Item");
    private Button itemActionButton;
    private Button itemRemoveButton;
    private final TableView<LineItem> itemTable = new TableView<>();
    private final TableView<SalesInvoice> invoiceTable = new TableView<>();
    private final TableView<SalesInvoiceItem> previousItemsTable = new TableView<>();

    private int editingInvoiceId = -1;
    private LineItem editingItem;

    public SalesFxPage() {
        styleSelectors();
        getStyleClass().add("ledger-page");
        BorderPane content = createContent();
        VBox.setVgrow(content, Priority.ALWAYS);
        getChildren().add(content);
        loadData();
    }

    private void styleSelectors() {
        FxTheme.styleComboBox(clientComboBox);
        FxTheme.styleComboBox(warehouseComboBox);
        FxTheme.styleComboBox(productComboBox);
        FxTheme.styleComboBox(paymentTypeComboBox);
        clientComboBox.getStyleClass().add("compact-selector");
        warehouseComboBox.getStyleClass().add("compact-selector");
        productComboBox.getStyleClass().add("compact-selector");
        paymentTypeComboBox.getStyleClass().add("compact-selector");
    }

    private BorderPane createContent() {
        availableStockField.setEditable(false);
        paymentTypeComboBox.setItems(FXCollections.observableArrayList("Cash", "Card", "Bank Transfer", "Cheque"));
        paymentTypeComboBox.getSelectionModel().selectFirst();
        modeLabel.getStyleClass().add("card-title");
        itemCardTitle.getStyleClass().add("card-title");
        configureTables();

        productComboBox.setOnAction(e -> updateProductInfo());
        warehouseComboBox.setOnAction(e -> updateProductInfo());

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

        return FxTheme.ledgerWorkspace(createHistoryPane(), FxTheme.ledgerInspector("Sales Inspector", editor));
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
        addRow(form, 1, "Client", clientComboBox);
        addRow(form, 2, "Warehouse", warehouseComboBox);
        addRow(form, 3, "Invoice Date", invoiceDatePicker);
        addRow(form, 4, "Payment Type", paymentTypeComboBox);
        addRow(form, 5, "Payment", paymentField);
        return form;
    }

    private GridPane createItemForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        addRow(form, 0, "Product", productComboBox);
        addRow(form, 1, "Available Stock", availableStockField);
        addRow(form, 2, "Quantity", quantityField);
        addRow(form, 3, "Selling Price", priceField);
        addRow(form, 4, "Warranty End", warrantyDatePicker);

        itemActionButton = FxTheme.primaryButton("Add");
        itemRemoveButton = FxTheme.secondaryButton("Remove");
        Button clear = FxTheme.secondaryButton("Clear");
        itemActionButton.setOnAction(e -> saveCurrentItem());
        itemRemoveButton.setOnAction(e -> removeItem());
        clear.setOnAction(e -> clearItemForm());
        setVisible(itemRemoveButton, false);
        form.add(FxTheme.actionRow(itemActionButton, itemRemoveButton, clear), 0, 5, 2, 1);
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

        pane.setCenter(FxTheme.ledgerSurface("Sales Invoice Ledger", toolbar, split));
        return pane;
    }

    private void addRow(GridPane form, int row, String label, javafx.scene.Node field) {
        form.add(new Label(label), 0, row);
        form.add(field, 1, row);
    }

    private void configureTables() {
        itemTable.getColumns().add(FxTableUtil.column("Product ID", LineItem::getProductId, 90));
        itemTable.getColumns().add(FxTableUtil.column("Product", LineItem::getProductName, 180));
        itemTable.getColumns().add(FxTableUtil.column("Price", LineItem::getSellingPrice, 90));
        itemTable.getColumns().add(FxTableUtil.column("Quantity", LineItem::getQuantity, 80));
        itemTable.getColumns().add(FxTableUtil.column("Warranty", LineItem::getWarrantyEndDate, 110));
        itemTable.getColumns().add(FxTableUtil.column("Line Total", LineItem::getLineTotal, 110));
        itemTable.setItems(currentItems);
        FxTheme.styleTable(itemTable);
        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, item) -> {
            if (item != null) {
                fillItemForm(item);
            }
        });

        invoiceTable.getColumns().add(FxTableUtil.column("Invoice ID", SalesInvoice::getSalesInvoiceId, 90));
        invoiceTable.getColumns().add(FxTableUtil.column("Date", SalesInvoice::getInvoiceDate, 110));
        invoiceTable.getColumns().add(FxTableUtil.column("Client ID", SalesInvoice::getClientId, 90));
        invoiceTable.getColumns().add(FxTableUtil.column("Client", invoice -> clientName(invoice.getClientId()), 160));
        invoiceTable.getColumns().add(FxTableUtil.column("Warehouse ID", SalesInvoice::getWarehouseId, 110));
        invoiceTable.getColumns().add(FxTableUtil.column("Warehouse", invoice -> warehouseName(invoice.getWarehouseId()), 150));
        invoiceTable.getColumns().add(FxTableUtil.column("Payment Type", SalesInvoice::getPaymentType, 120));
        invoiceTable.getColumns().add(FxTableUtil.column("Payment", SalesInvoice::getPayment, 100));
        invoiceTable.getColumns().add(FxTableUtil.column("Amount", SalesInvoice::getAmount, 110));
        FxTableUtil.installSearch(invoiceTable, invoices, invoiceSearchField);
        FxTheme.styleTable(invoiceTable);
        invoiceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, invoice) -> loadInvoiceItems(invoice));
        invoiceTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) loadSelectedInvoiceForEdit();
        });

        previousItemsTable.getColumns().add(FxTableUtil.column("Item ID", SalesInvoiceItem::getSalesItemId, 80));
        previousItemsTable.getColumns().add(FxTableUtil.column("Product ID", SalesInvoiceItem::getProductId, 90));
        previousItemsTable.getColumns().add(FxTableUtil.column("Product", item -> productName(item.getProductId()), 180));
        previousItemsTable.getColumns().add(FxTableUtil.column("Price", SalesInvoiceItem::getSellingPrice, 90));
        previousItemsTable.getColumns().add(FxTableUtil.column("Quantity", SalesInvoiceItem::getQuantity, 80));
        previousItemsTable.getColumns().add(FxTableUtil.column("Warranty", SalesInvoiceItem::getWarrantyEndDate, 110));
        previousItemsTable.getColumns().add(FxTableUtil.column("Line Total", SalesInvoiceItem::getLineTotal, 110));
        previousItemsTable.setItems(selectedInvoiceItems);
        FxTheme.styleTable(previousItemsTable);
    }

    private void loadData() {
        clientComboBox.setItems(FXCollections.observableArrayList(clientDAO.getAllClients()));
        warehouseComboBox.setItems(FXCollections.observableArrayList(warehouseDAO.getAllWarehouses()));
        productComboBox.setItems(FXCollections.observableArrayList(productDAO.getAllProducts()));
        invoices.setAll(salesInvoiceDAO.getAllSalesInvoices());
        if (!clientComboBox.getItems().isEmpty()) clientComboBox.getSelectionModel().selectFirst();
        if (!warehouseComboBox.getItems().isEmpty()) warehouseComboBox.getSelectionModel().selectFirst();
        if (!productComboBox.getItems().isEmpty()) productComboBox.getSelectionModel().selectFirst();
        updateProductInfo();
    }

    private void updateProductInfo() {
        Product product = productComboBox.getValue();
        Warehouse warehouse = warehouseComboBox.getValue();
        if (product == null || warehouse == null) return;
        int stock = inventoryDAO.getAvailableStock(product.getProductId(), warehouse.getWarehouseId());
        availableStockField.setText(String.valueOf(stock + originalQuantity(product.getProductId())));
        priceField.setText(String.valueOf(product.getDefaultSellingPrice()));
    }

    private void saveCurrentItem() {
        Product product = productComboBox.getValue();
        Warehouse warehouse = warehouseComboBox.getValue();
        if (product == null || warehouse == null) return;

        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            BigDecimal price = new BigDecimal(priceField.getText().trim());
            if (quantity <= 0 || price.signum() <= 0) {
                FxTheme.showError("Quantity and selling price must be positive.");
                return;
            }

            int availableAfterReverse = inventoryDAO.getAvailableStock(product.getProductId(), warehouse.getWarehouseId())
                    + originalQuantity(product.getProductId());
            int currentQuantityExcludingEdit = currentQuantity(product.getProductId());
            if (editingItem != null && editingItem.getProductId() == product.getProductId()) {
                currentQuantityExcludingEdit -= editingItem.getQuantity();
            }
            int requestedAfterChange = currentQuantityExcludingEdit + quantity;

            if (requestedAfterChange > availableAfterReverse) {
                FxTheme.showError("Not enough stock. Available: " + availableAfterReverse + ", already in invoice: " + currentQuantityExcludingEdit);
                return;
            }

            if (editingItem == null && !mergeItem(product, price, quantity, warrantyDatePicker.getValue(), null)) {
                currentItems.add(new LineItem(product.getProductId(), product.getProductName(), price, quantity, warrantyDatePicker.getValue()));
            } else if (editingItem != null) {
                updateEditingItem(product, price, quantity, warrantyDatePicker.getValue());
            }

            clearItemForm();
            updatePaymentToTotal();
        } catch (Exception e) {
            FxTheme.showError("Quantity and price must be valid.");
        }
    }

    private boolean mergeItem(Product product, BigDecimal price, int quantity, LocalDate warrantyDate, LineItem excludedItem) {
        for (LineItem item : currentItems) {
            if (item != excludedItem
                    && item.productId == product.getProductId()
                    && item.sellingPrice.compareTo(price) == 0
                    && sameDate(item.warrantyEndDate, warrantyDate)) {
                item.quantity += quantity;
                itemTable.refresh();
                return true;
            }
        }
        return false;
    }

    private void updateEditingItem(Product product, BigDecimal price, int quantity, LocalDate warrantyDate) {
        if (mergeItem(product, price, quantity, warrantyDate, editingItem)) {
            currentItems.remove(editingItem);
        } else {
            editingItem.setProductId(product.getProductId());
            editingItem.setProductName(product.getProductName());
            editingItem.setSellingPrice(price);
            editingItem.setQuantity(quantity);
            editingItem.setWarrantyEndDate(warrantyDate);
            itemTable.refresh();
        }
    }

    private void removeItem() {
        LineItem selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            currentItems.remove(selected);
            clearItemForm();
            updatePaymentToTotal();
        }
    }

    private void saveInvoice() {
        SalesInvoice invoice = buildInvoice(-1);
        if (invoice == null) return;

        if (salesInvoiceDAO.createSalesInvoice(invoice)) {
            FxTheme.showInfo("Sales invoice saved. Inventory decreased.");
            clearInvoice();
            loadData();
        } else {
            FxTheme.showError("Failed to save sales invoice.");
        }
    }

    private void updateInvoice() {
        if (editingInvoiceId <= 0) {
            FxTheme.showError("Load an invoice first.");
            return;
        }

        if (!FxTheme.confirm("Update this sales invoice? Inventory will be adjusted.")) return;

        SalesInvoice invoice = buildInvoice(editingInvoiceId);
        if (invoice == null) return;

        if (salesInvoiceDAO.updateSalesInvoice(invoice)) {
            FxTheme.showInfo("Sales invoice updated.");
            clearInvoice();
            loadData();
        } else {
            FxTheme.showError("Failed to update sales invoice.");
        }
    }

    private void deleteInvoice() {
        SalesInvoice selected = invoiceTable.getSelectionModel().getSelectedItem();
        int invoiceId = editingInvoiceId > 0 ? editingInvoiceId : selected == null ? -1 : selected.getSalesInvoiceId();
        if (invoiceId <= 0) {
            FxTheme.showError("Select an invoice first.");
            return;
        }

        if (!FxTheme.confirm("Delete sales invoice #" + invoiceId + "? Stock will be returned.")) return;

        if (salesInvoiceDAO.deleteSalesInvoice(invoiceId)) {
            FxTheme.showInfo("Sales invoice deleted.");
            clearInvoice();
            loadData();
            selectedInvoiceItems.clear();
        } else {
            FxTheme.showError("Failed to delete sales invoice.");
        }
    }

    private SalesInvoice buildInvoice(int invoiceId) {
        Client client = clientComboBox.getValue();
        Warehouse warehouse = warehouseComboBox.getValue();
        if (client == null || warehouse == null || currentItems.isEmpty()) {
            FxTheme.showError("Select client, warehouse, and add at least one item.");
            return null;
        }

        BigDecimal payment;
        try {
            payment = new BigDecimal(paymentField.getText().trim());
        } catch (Exception e) {
            FxTheme.showError("Payment must be valid.");
            return null;
        }

        ArrayList<SalesInvoiceItem> items = new ArrayList<>();
        for (LineItem item : currentItems) {
            items.add(new SalesInvoiceItem(item.productId, item.sellingPrice, item.quantity, item.warrantyEndDate));
        }

        SalesInvoice invoice = new SalesInvoice(invoiceDatePicker.getValue(), payment, paymentTypeComboBox.getValue(),
                totalAmount(), client.getClientId(), warehouse.getWarehouseId(), items);
        if (invoiceId > 0) invoice.setSalesInvoiceId(invoiceId);
        return invoice;
    }

    private void loadInvoiceItems(SalesInvoice invoice) {
        selectedInvoiceItems.clear();
        if (invoice != null) {
            selectedInvoiceItems.setAll(salesInvoiceDAO.getSalesInvoiceItems(invoice.getSalesInvoiceId()));
        }
    }

    private void loadSelectedInvoiceForEdit() {
        SalesInvoice selected = invoiceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxTheme.showError("Select an invoice first.");
            return;
        }

        SalesInvoice invoice = salesInvoiceDAO.getSalesInvoiceById(selected.getSalesInvoiceId());
        if (invoice == null) {
            FxTheme.showError("Could not load selected invoice.");
            return;
        }

        editingInvoiceId = invoice.getSalesInvoiceId();
        modeLabel.setText("Mode: Editing Sales Invoice #" + editingInvoiceId);
        selectClient(invoice.getClientId());
        selectWarehouse(invoice.getWarehouseId());
        invoiceDatePicker.setValue(invoice.getInvoiceDate());
        paymentField.setText(invoice.getPayment().toString());
        paymentTypeComboBox.setValue(invoice.getPaymentType());

        originalItems.clear();
        originalItems.addAll(invoice.getItems());
        currentItems.clear();

        for (SalesInvoiceItem item : invoice.getItems()) {
            currentItems.add(new LineItem(
                    item.getProductId(),
                    productName(item.getProductId()),
                    item.getSellingPrice(),
                    item.getQuantity(),
                    item.getWarrantyEndDate()
            ));
        }
        clearItemForm();
        updateProductInfo();
        updatePaymentToTotal();
    }

    private BigDecimal totalAmount() {
        return currentItems.stream().map(LineItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void updatePaymentToTotal() {
        paymentField.setText(totalAmount().toString());
    }

    private int originalQuantity(int productId) {
        return originalItems.stream()
                .filter(item -> item.getProductId() == productId)
                .mapToInt(SalesInvoiceItem::getQuantity)
                .sum();
    }

    private int currentQuantity(int productId) {
        return currentItems.stream()
                .filter(item -> item.productId == productId)
                .mapToInt(LineItem::getQuantity)
                .sum();
    }

    private boolean sameDate(LocalDate first, LocalDate second) {
        return first == null ? second == null : first.equals(second);
    }

    private String clientName(int clientId) {
        return clientComboBox.getItems().stream()
                .filter(client -> client.getClientId() == clientId)
                .map(Client::getClientName)
                .findFirst()
                .orElse("Unknown Client");
    }

    private String warehouseName(int warehouseId) {
        return warehouseComboBox.getItems().stream()
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

    private void selectClient(int clientId) {
        clientComboBox.getItems().stream()
                .filter(client -> client.getClientId() == clientId)
                .findFirst()
                .ifPresent(clientComboBox::setValue);
    }

    private void selectWarehouse(int warehouseId) {
        warehouseComboBox.getItems().stream()
                .filter(warehouse -> warehouse.getWarehouseId() == warehouseId)
                .findFirst()
                .ifPresent(warehouseComboBox::setValue);
    }

    private void clearInvoice() {
        editingInvoiceId = -1;
        modeLabel.setText("Mode: New Sales Invoice");
        originalItems.clear();
        invoiceDatePicker.setValue(LocalDate.now());
        paymentTypeComboBox.getSelectionModel().selectFirst();
        currentItems.clear();
        updatePaymentToTotal();
        clearItemForm();
        updateProductInfo();
    }

    private void fillItemForm(LineItem item) {
        editingItem = item;
        selectProduct(item.getProductId());
        quantityField.setText(String.valueOf(item.getQuantity()));
        priceField.setText(item.getSellingPrice().toString());
        warrantyDatePicker.setValue(item.getWarrantyEndDate());
        updateItemEditMode();
    }

    private void clearItemForm() {
        editingItem = null;
        itemTable.getSelectionModel().clearSelection();
        quantityField.clear();
        warrantyDatePicker.setValue(null);
        updateProductInfo();
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

    public static class LineItem {
        private int productId;
        private String productName;
        private BigDecimal sellingPrice;
        private int quantity;
        private LocalDate warrantyEndDate;

        public LineItem(int productId, String productName, BigDecimal sellingPrice, int quantity, LocalDate warrantyEndDate) {
            this.productId = productId;
            this.productName = productName;
            this.sellingPrice = sellingPrice;
            this.quantity = quantity;
            this.warrantyEndDate = warrantyEndDate;
        }

        public int getProductId() { return productId; }
        public String getProductName() { return productName; }
        public BigDecimal getSellingPrice() { return sellingPrice; }
        public int getQuantity() { return quantity; }
        public LocalDate getWarrantyEndDate() { return warrantyEndDate; }
        public BigDecimal getLineTotal() { return sellingPrice.multiply(BigDecimal.valueOf(quantity)); }
        public void setProductId(int productId) { this.productId = productId; }
        public void setProductName(String productName) { this.productName = productName; }
        public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public void setWarrantyEndDate(LocalDate warrantyEndDate) { this.warrantyEndDate = warrantyEndDate; }
    }
}
