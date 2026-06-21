package ui.views;

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
import ui.TableUtil;
import ui.Theme;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SuppliersPage extends VBox {

    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final SupplierProductDAO supplierProductDAO = new SupplierProductDAO();

    private final ObservableList<Supplier> suppliers = FXCollections.observableArrayList();
    private final ObservableList<SupplierProduct> supplierProducts = FXCollections.observableArrayList();

    private final TextField idField = Theme.textField("ID");
    private final TextField nameField = Theme.textField("Name");
    private final TextField phoneField = Theme.textField("Phone");
    private final TextField emailField = Theme.textField("Email");
    private final DatePicker startingDatePicker = new DatePicker(LocalDate.now());
    private final TextField cityField = Theme.textField("City");
    private final TextField countryField = Theme.textField("Country");
    private final TextField addressField = Theme.textField("Address");
    private final TextField searchField = Theme.textField("Search suppliers");
    private final ComboBox<String> searchColumnBox = TableUtil.searchColumnBox();
    private final TableView<Supplier> supplierTable = new TableView<>();

    private final ComboBox<Supplier> linkSupplierComboBox = new ComboBox<>();
    private final ComboBox<Product> productComboBox = new ComboBox<>();
    private final TextField supplyPriceField = Theme.textField("Supply price");
    private final TextField linkSearchField = Theme.textField("Search supplier-product links");
    private final ComboBox<String> linkSearchColumnBox = TableUtil.searchColumnBox();
    private final TableView<SupplierProduct> linkTable = new TableView<>();
    private Button supplierActionButton;
    private Button supplierDeleteButton;
    private Button linkActionButton;
    private Button linkRemoveButton;

    public SuppliersPage() {
        Theme.styleComboBox(linkSupplierComboBox);
        Theme.styleComboBox(productComboBox);
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
        content.setCenter(Theme.ledgerWorkspace(
                Theme.ledgerSurface("Supplier Ledger", createToolbar(), supplierTable),
                Theme.ledgerInspector("Supplier Inspector", createSupplierForm())
        ));
        content.setBottom(createLinkPane());
        BorderPane.setMargin(content.getBottom(), new javafx.geometry.Insets(16, 0, 0, 0));
        return content;
    }

    private HBox createToolbar() {
        Button refreshButton = Theme.refreshButton();
        refreshButton.setOnAction(e -> {
            searchField.clear();
            loadAll();
        });

        HBox toolbar = Theme.ledgerCommandBar(searchColumnBox, searchField, refreshButton);
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
        addRow(form, 6, "Country", countryField);
        addRow(form, 7, "Address", addressField);

        supplierActionButton = Theme.primaryButton("Add");
        supplierDeleteButton = Theme.dangerButton("Delete");
        Button clear = Theme.secondaryButton("Clear");
        supplierActionButton.setMinWidth(78);
        supplierDeleteButton.setMinWidth(78);
        clear.setMinWidth(78);
        supplierActionButton.setOnAction(e -> saveSupplier());
        supplierDeleteButton.setOnAction(e -> deleteSupplier());
        clear.setOnAction(e -> clearSupplierForm());
        Theme.setVisible(supplierDeleteButton, false);
        form.add(Theme.compactActionRow(supplierActionButton, supplierDeleteButton, clear), 0, 8, 2, 1);
        return form;
    }

    private BorderPane createLinkPane() {
        GridPane form = new GridPane();
        configureInspectorForm(form);
        addRow(form, 0, "Supplier", linkSupplierComboBox);
        addRow(form, 1, "Product", productComboBox);
        addRow(form, 2, "Supply Price", supplyPriceField);

        linkActionButton = Theme.primaryButton("Link");
        linkRemoveButton = Theme.dangerButton("Remove");
        Button clear = Theme.secondaryButton("Clear");
        linkActionButton.setOnAction(e -> saveSupplierProductLink());
        linkRemoveButton.setOnAction(e -> removeSupplierProduct());
        clear.setOnAction(e -> clearLinkForm());
        Theme.setVisible(linkRemoveButton, false);
        form.add(Theme.compactActionRow(linkActionButton, linkRemoveButton, clear), 0, 3, 2, 1);

        Button refresh = Theme.refreshButton();
        refresh.setOnAction(e -> {
            linkSearchField.clear();
            loadAll();
        });
        HBox toolbar = Theme.ledgerCommandBar(linkSearchColumnBox, linkSearchField, refresh);
        HBox.setHgrow(linkSearchField, Priority.ALWAYS);

        VBox tableBox = Theme.ledgerSurface("Supplier Product Ledger", toolbar, linkTable);
        VBox.setVgrow(linkTable, Priority.ALWAYS);

        return Theme.ledgerWorkspace(tableBox, Theme.ledgerInspector("Supply Link Inspector", form));
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
        supplierTable.getColumns().add(TableUtil.column("ID", Supplier::getSupplierId, 70));
        supplierTable.getColumns().add(TableUtil.column("Supplier", Supplier::getSupplierName, 170));
        supplierTable.getColumns().add(TableUtil.column("Phone", Supplier::getPhone, 120));
        supplierTable.getColumns().add(TableUtil.column("Email", Supplier::getEmail, 180));
        supplierTable.getColumns().add(TableUtil.column("Starting Date", Supplier::getStartingDate, 120));
        supplierTable.getColumns().add(TableUtil.column("City", Supplier::getCity, 120));
        supplierTable.getColumns().add(TableUtil.column("Country", Supplier::getCountry, 120));
        supplierTable.getColumns().add(TableUtil.column("Address", Supplier::getAddress, 180));
        TableUtil.installSearch(supplierTable, suppliers, searchField, searchColumnBox);
        Theme.styleTable(supplierTable);
        supplierTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, supplier) -> {
            if (supplier != null) fillSupplierForm(supplier);
        });

        linkTable.getColumns().add(TableUtil.column("Supplier ID", SupplierProduct::getSupplierId, 100));
        linkTable.getColumns().add(TableUtil.column("Supplier", SupplierProduct::getSupplierName, 170));
        linkTable.getColumns().add(TableUtil.column("Product ID", SupplierProduct::getProductId, 100));
        linkTable.getColumns().add(TableUtil.column("Product", SupplierProduct::getProductName, 190));
        linkTable.getColumns().add(TableUtil.column("Supply Price", SupplierProduct::getSupplyPrice, 120));
        TableUtil.installSearch(linkTable, supplierProducts, linkSearchField, linkSearchColumnBox);
        Theme.styleTable(linkTable);
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
        updateSupplierFormMode();
        updateLinkFormMode();
    }

    private void addSupplier() {
        Supplier supplier = readSupplierForm(false);
        if (supplier != null && supplierDAO.addSupplier(supplier)) {
            Theme.showInfo("Supplier added.");
            clearSupplierForm();
            loadAll();
        }
    }

    private void saveSupplier() {
        if (idField.getText().isBlank()) {
            addSupplier();
        } else {
            updateSupplier();
        }
    }

    private void updateSupplier() {
        Supplier supplier = readSupplierForm(true);
        if (supplier != null && supplierDAO.updateSupplier(supplier)) {
            Theme.showInfo("Supplier updated.");
            clearSupplierForm();
            loadAll();
        }
    }

    private void deleteSupplier() {
        if (!idField.getText().isBlank() && Theme.confirm("Delete selected supplier?")) {
            supplierDAO.deleteSupplier(Integer.parseInt(idField.getText()));
            clearSupplierForm();
            loadAll();
        }
    }

    private Supplier readSupplierForm(boolean includeId) {
        if (nameField.getText().trim().isEmpty()) {
            Theme.showError("Supplier name is required.");
            return null;
        }

        Supplier supplier = new Supplier(nameField.getText().trim(), phoneField.getText().trim(), emailField.getText().trim(),
                startingDatePicker.getValue(), cityField.getText().trim(), countryField.getText().trim(), addressField.getText().trim());

        if (includeId) {
            if (idField.getText().isBlank()) {
                Theme.showError("Select a supplier first.");
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
            if (supplierProductDAO.supplierProductExists(supplier.getSupplierId(), product.getProductId())) {
                Theme.showWarning("This supplier is already linked to the selected product. Select the existing link to update its price.");
                return;
            }
            supplierProductDAO.linkSupplierToProduct(supplier.getSupplierId(), product.getProductId(), price);
            clearLinkForm();
            loadAll();
        }
    }

    private void updateSupplyPrice() {
        Supplier supplier = linkSupplierComboBox.getValue();
        Product product = productComboBox.getValue();
        BigDecimal price = readPrice();
        if (supplier != null && product != null && price != null) {
            supplierProductDAO.updateSupplyPrice(supplier.getSupplierId(), product.getProductId(), price);
            clearLinkForm();
            loadAll();
        }
    }

    private void saveSupplierProductLink() {
        if (linkTable.getSelectionModel().getSelectedItem() == null) {
            linkSupplierProduct();
        } else {
            updateSupplyPrice();
        }
    }

    private void removeSupplierProduct() {
        Supplier supplier = linkSupplierComboBox.getValue();
        Product product = productComboBox.getValue();
        if (supplier != null && product != null && Theme.confirm("Remove supplier-product link?")) {
            supplierProductDAO.removeSupplierProduct(supplier.getSupplierId(), product.getProductId());
            clearLinkForm();
            loadAll();
        }
    }

    private BigDecimal readPrice() {
        try {
            return new BigDecimal(supplyPriceField.getText().trim());
        } catch (Exception e) {
            Theme.showError("Supply price must be a valid number.");
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
        countryField.setText(supplier.getCountry());
        addressField.setText(supplier.getAddress());
        updateSupplierFormMode();
    }

    private void fillLinkForm(SupplierProduct link) {
        selectSupplier(link.getSupplierId());
        selectProduct(link.getProductId());
        supplyPriceField.setText(String.valueOf(link.getSupplyPrice()));
        updateLinkFormMode();
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
        countryField.clear();
        addressField.clear();
        supplierTable.getSelectionModel().clearSelection();
        updateSupplierFormMode();
    }

    private void clearLinkForm() {
        supplyPriceField.clear();
        if (!linkSupplierComboBox.getItems().isEmpty()) linkSupplierComboBox.getSelectionModel().selectFirst();
        if (!productComboBox.getItems().isEmpty()) productComboBox.getSelectionModel().selectFirst();
        linkTable.getSelectionModel().clearSelection();
        updateLinkFormMode();
    }

    private void updateSupplierFormMode() {
        boolean selected = !idField.getText().isBlank();
        if (supplierActionButton != null) {
            supplierActionButton.setText(selected ? "Update" : "Add");
        }
        if (supplierDeleteButton != null) {
            Theme.setVisible(supplierDeleteButton, selected);
        }
    }

    private void updateLinkFormMode() {
        boolean selected = linkTable.getSelectionModel().getSelectedItem() != null;
        if (linkActionButton != null) {
            linkActionButton.setText(selected ? "Update Price" : "Link");
        }
        if (linkRemoveButton != null) {
            Theme.setVisible(linkRemoveButton, selected);
        }
    }
}
