package ui.fx.views;

import dao.ProductDAO;
import dao.SupplierDAO;
import dao.SupplierProductDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Product;
import model.Supplier;
import model.SupplierProduct;
import ui.fx.FxTableUtil;
import ui.fx.FxTheme;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SuppliersFxPage extends VBox {

    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final SupplierProductDAO supplierProductDAO = new SupplierProductDAO();

    private final ObservableList<Supplier> suppliers = FXCollections.observableArrayList();
    private final ObservableList<SupplierProduct> supplierProducts = FXCollections.observableArrayList();

    private final TextField idField = FxTheme.textField("ID");
    private final TextField nameField = FxTheme.textField("Name");
    private final TextField phoneField = FxTheme.textField("Phone");
    private final TextField emailField = FxTheme.textField("Email");
    private final DatePicker startingDatePicker = new DatePicker(LocalDate.now());
    private final TextField cityField = FxTheme.textField("City");
    private final TextField addressField = FxTheme.textField("Address");
    private final TextField searchField = FxTheme.textField("Search suppliers");
    private final TableView<Supplier> supplierTable = new TableView<>();

    private final ComboBox<Supplier> linkSupplierComboBox = new ComboBox<>();
    private final ComboBox<Product> productComboBox = new ComboBox<>();
    private final TextField supplyPriceField = FxTheme.textField("Supply price");
    private final TextField linkSearchField = FxTheme.textField("Search supplier-product links");
    private final TableView<SupplierProduct> linkTable = new TableView<>();

    public SuppliersFxPage() {
        FxTheme.styleComboBox(linkSupplierComboBox);
        FxTheme.styleComboBox(productComboBox);
        linkSupplierComboBox.getStyleClass().add("compact-selector");
        productComboBox.getStyleClass().add("compact-selector");
        getStyleClass().add("ledger-page");
        BorderPane content = createContent();
        VBox.setVgrow(content, Priority.ALWAYS);
        getChildren().add(content);
        loadAll();
    }

    private BorderPane createContent() {
        idField.setEditable(false);
        configureTables();

        BorderPane content = new BorderPane();
        content.getStyleClass().add("ledger-stacked-workspace");
        content.setCenter(FxTheme.ledgerWorkspace(
                FxTheme.ledgerSurface("Supplier Ledger", createToolbar(), supplierTable),
                FxTheme.ledgerInspector("Supplier Inspector", createSupplierForm())
        ));
        content.setBottom(createLinkPane());
        BorderPane.setMargin(content.getBottom(), new javafx.geometry.Insets(16, 0, 0, 0));
        return content;
    }

    private HBox createToolbar() {
        Button searchButton = FxTheme.secondaryButton("Search");
        Button refreshButton = FxTheme.secondaryButton("Refresh");
        searchButton.setOnAction(e -> searchSuppliers());
        refreshButton.setOnAction(e -> loadAll());

        HBox toolbar = FxTheme.ledgerCommandBar(searchField, searchButton, refreshButton);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        return toolbar;
    }

    private GridPane createSupplierForm() {
        GridPane form = new GridPane();
        configureInspectorForm(form);
        addRow(form, 0, "ID", idField);
        addRow(form, 1, "Name", nameField);
        addRow(form, 2, "Phone", phoneField);
        addRow(form, 3, "Email", emailField);
        addRow(form, 4, "Starting Date", startingDatePicker);
        addRow(form, 5, "City", cityField);
        addRow(form, 6, "Address", addressField);

        Button add = FxTheme.primaryButton("Add");
        Button update = FxTheme.primaryButton("Update");
        Button delete = FxTheme.dangerButton("Delete");
        Button clear = FxTheme.secondaryButton("Clear");
        add.setMinWidth(78);
        update.setMinWidth(78);
        delete.setMinWidth(78);
        clear.setMinWidth(78);
        add.setOnAction(e -> addSupplier());
        update.setOnAction(e -> updateSupplier());
        delete.setOnAction(e -> deleteSupplier());
        clear.setOnAction(e -> clearSupplierForm());
        form.add(FxTheme.actionRow(add, update, delete, clear), 0, 7, 2, 1);
        return form;
    }

    private BorderPane createLinkPane() {
        GridPane form = new GridPane();
        configureInspectorForm(form);
        addRow(form, 0, "Supplier", linkSupplierComboBox);
        addRow(form, 1, "Product", productComboBox);
        addRow(form, 2, "Supply Price", supplyPriceField);

        Button link = FxTheme.primaryButton("Link");
        Button update = FxTheme.primaryButton("Update Price");
        Button remove = FxTheme.dangerButton("Remove");
        link.setOnAction(e -> linkSupplierProduct());
        update.setOnAction(e -> updateSupplyPrice());
        remove.setOnAction(e -> removeSupplierProduct());
        form.add(FxTheme.actionRow(link, update, remove), 0, 3, 2, 1);

        HBox toolbar = FxTheme.ledgerCommandBar(linkSearchField);
        HBox.setHgrow(linkSearchField, Priority.ALWAYS);

        VBox tableBox = FxTheme.ledgerSurface("Supplier Product Ledger", toolbar, linkTable);
        VBox.setVgrow(linkTable, Priority.ALWAYS);

        return FxTheme.ledgerWorkspace(tableBox, FxTheme.ledgerInspector("Supply Link Inspector", form));
    }

    private void configureInspectorForm(GridPane form) {
        form.setHgap(8);
        form.setVgap(8);

        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(92);
        labelColumn.setPrefWidth(98);
        labelColumn.setHgrow(Priority.NEVER);

        ColumnConstraints fieldColumn = new ColumnConstraints();
        fieldColumn.setMinWidth(0);
        fieldColumn.setHgrow(Priority.ALWAYS);
        fieldColumn.setFillWidth(true);

        form.getColumnConstraints().setAll(labelColumn, fieldColumn);
    }

    private void addRow(GridPane form, int row, String label, javafx.scene.Node field) {
        Label labelNode = new Label(label);
        labelNode.setMinWidth(92);
        labelNode.setMaxWidth(Double.MAX_VALUE);
        if (field instanceof Region) {
            ((Region) field).setMaxWidth(Double.MAX_VALUE);
        }
        GridPane.setHgrow(field, Priority.ALWAYS);
        form.add(labelNode, 0, row);
        form.add(field, 1, row);
    }

    private void configureTables() {
        supplierTable.getColumns().add(FxTableUtil.column("ID", Supplier::getSupplierId, 70));
        supplierTable.getColumns().add(FxTableUtil.column("Supplier", Supplier::getSupplierName, 170));
        supplierTable.getColumns().add(FxTableUtil.column("Phone", Supplier::getPhone, 120));
        supplierTable.getColumns().add(FxTableUtil.column("Email", Supplier::getEmail, 180));
        supplierTable.getColumns().add(FxTableUtil.column("Address", Supplier::getAddress, 180));
        supplierTable.getColumns().add(FxTableUtil.column("Starting Date", Supplier::getStartingDate, 120));
        supplierTable.getColumns().add(FxTableUtil.column("City", Supplier::getCity, 120));
        FxTableUtil.installSearch(supplierTable, suppliers, searchField);
        FxTheme.styleTable(supplierTable);
        supplierTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, supplier) -> {
            if (supplier != null) fillSupplierForm(supplier);
        });

        linkTable.getColumns().add(FxTableUtil.column("Supplier ID", SupplierProduct::getSupplierId, 100));
        linkTable.getColumns().add(FxTableUtil.column("Supplier", SupplierProduct::getSupplierName, 170));
        linkTable.getColumns().add(FxTableUtil.column("Product ID", SupplierProduct::getProductId, 100));
        linkTable.getColumns().add(FxTableUtil.column("Product", SupplierProduct::getProductName, 190));
        linkTable.getColumns().add(FxTableUtil.column("Supply Price", SupplierProduct::getSupplyPrice, 120));
        FxTableUtil.installSearch(linkTable, supplierProducts, linkSearchField);
        FxTheme.styleTable(linkTable);
        linkTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, link) -> {
            if (link != null) fillLinkForm(link);
        });
    }

    private void loadAll() {
        suppliers.setAll(supplierDAO.getAllSuppliers());
        supplierProducts.setAll(supplierProductDAO.getAllSupplierProducts());
        linkSupplierComboBox.setItems(FXCollections.observableArrayList(supplierDAO.getAllSuppliers()));
        productComboBox.setItems(FXCollections.observableArrayList(productDAO.getAllProducts()));
        if (!linkSupplierComboBox.getItems().isEmpty()) linkSupplierComboBox.getSelectionModel().selectFirst();
        if (!productComboBox.getItems().isEmpty()) productComboBox.getSelectionModel().selectFirst();
    }

    private void searchSuppliers() {
        String keyword = searchField.getText().trim();
        suppliers.setAll(keyword.isEmpty() ? supplierDAO.getAllSuppliers() : supplierDAO.searchSuppliers(keyword));
    }

    private void addSupplier() {
        Supplier supplier = readSupplierForm(false);
        if (supplier != null && supplierDAO.addSupplier(supplier)) {
            FxTheme.showInfo("Supplier added.");
            clearSupplierForm();
            loadAll();
        }
    }

    private void updateSupplier() {
        Supplier supplier = readSupplierForm(true);
        if (supplier != null && supplierDAO.updateSupplier(supplier)) {
            FxTheme.showInfo("Supplier updated.");
            clearSupplierForm();
            loadAll();
        }
    }

    private void deleteSupplier() {
        if (!idField.getText().isBlank() && FxTheme.confirm("Delete selected supplier?")) {
            supplierDAO.deleteSupplier(Integer.parseInt(idField.getText()));
            clearSupplierForm();
            loadAll();
        }
    }

    private Supplier readSupplierForm(boolean includeId) {
        if (nameField.getText().trim().isEmpty()) {
            FxTheme.showError("Supplier name is required.");
            return null;
        }

        Supplier supplier = new Supplier(nameField.getText().trim(), phoneField.getText().trim(), emailField.getText().trim(),
                startingDatePicker.getValue(), cityField.getText().trim(), addressField.getText().trim());

        if (includeId) {
            if (idField.getText().isBlank()) {
                FxTheme.showError("Select a supplier first.");
                return null;
            }
            supplier.setSupplierId(Integer.parseInt(idField.getText()));
        }

        return supplier;
    }

    private void linkSupplierProduct() {
        Supplier supplier = linkSupplierComboBox.getValue();
        Product product = productComboBox.getValue();
        BigDecimal price = readPrice();
        if (supplier != null && product != null && price != null) {
            supplierProductDAO.linkSupplierToProduct(supplier.getSupplierId(), product.getProductId(), price);
            loadAll();
        }
    }

    private void updateSupplyPrice() {
        Supplier supplier = linkSupplierComboBox.getValue();
        Product product = productComboBox.getValue();
        BigDecimal price = readPrice();
        if (supplier != null && product != null && price != null) {
            supplierProductDAO.updateSupplyPrice(supplier.getSupplierId(), product.getProductId(), price);
            loadAll();
        }
    }

    private void removeSupplierProduct() {
        Supplier supplier = linkSupplierComboBox.getValue();
        Product product = productComboBox.getValue();
        if (supplier != null && product != null && FxTheme.confirm("Remove supplier-product link?")) {
            supplierProductDAO.removeSupplierProduct(supplier.getSupplierId(), product.getProductId());
            loadAll();
        }
    }

    private BigDecimal readPrice() {
        try {
            return new BigDecimal(supplyPriceField.getText().trim());
        } catch (Exception e) {
            FxTheme.showError("Supply price must be a valid number.");
            return null;
        }
    }

    private void fillSupplierForm(Supplier supplier) {
        idField.setText(String.valueOf(supplier.getSupplierId()));
        nameField.setText(supplier.getSupplierName());
        phoneField.setText(supplier.getPhone());
        emailField.setText(supplier.getEmail());
        startingDatePicker.setValue(supplier.getStartingDate());
        cityField.setText(supplier.getCity());
        addressField.setText(supplier.getAddress());
    }

    private void fillLinkForm(SupplierProduct link) {
        selectSupplier(link.getSupplierId());
        selectProduct(link.getProductId());
        supplyPriceField.setText(String.valueOf(link.getSupplyPrice()));
    }

    private void selectSupplier(int supplierId) {
        linkSupplierComboBox.getItems().stream()
                .filter(supplier -> supplier.getSupplierId() == supplierId)
                .findFirst()
                .ifPresent(linkSupplierComboBox::setValue);
    }

    private void selectProduct(int productId) {
        productComboBox.getItems().stream()
                .filter(product -> product.getProductId() == productId)
                .findFirst()
                .ifPresent(productComboBox::setValue);
    }

    private void clearSupplierForm() {
        idField.clear();
        nameField.clear();
        phoneField.clear();
        emailField.clear();
        startingDatePicker.setValue(LocalDate.now());
        cityField.clear();
        addressField.clear();
        supplierTable.getSelectionModel().clearSelection();
    }
}
