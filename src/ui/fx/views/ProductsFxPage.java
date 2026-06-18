package ui.fx.views;

import dao.BrandDAO;
import dao.CategoryDAO;
import dao.ProductDAO;
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
import model.Brand;
import model.Category;
import model.Product;
import ui.fx.FxTableUtil;
import ui.fx.FxTheme;

import java.math.BigDecimal;

public class ProductsFxPage extends VBox {

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final BrandDAO brandDAO = new BrandDAO();
    private final ObservableList<Product> products = FXCollections.observableArrayList();

    private final TextField idField = FxTheme.textField("ID");
    private final TextField nameField = FxTheme.textField("Product name");
    private final TextField descriptionField = FxTheme.textField("Description");
    private final TextField priceField = FxTheme.textField("Price");
    private final ComboBox<Category> categoryComboBox = new ComboBox<>();
    private final ComboBox<Brand> brandComboBox = new ComboBox<>();
    private final TextField searchField = FxTheme.textField("Search products");
    private final TableView<Product> table = new TableView<>();

    public ProductsFxPage() {
        FxTheme.styleComboBox(categoryComboBox);
        FxTheme.styleComboBox(brandComboBox);
        categoryComboBox.getStyleClass().add("compact-selector");
        brandComboBox.getStyleClass().add("compact-selector");
        getChildren().add(createContent());
        loadCombos();
        loadProducts();
    }

    private BorderPane createContent() {
        idField.setEditable(false);
        configureTable();

        return FxTheme.ledgerWorkspace(
                FxTheme.ledgerSurface("Product Ledger", createToolbar(), table),
                FxTheme.ledgerInspector("Product Inspector", createForm())
        );
    }

    private HBox createToolbar() {
        Button searchButton = FxTheme.secondaryButton("Search");
        Button refreshButton = FxTheme.secondaryButton("Refresh");
        searchButton.setOnAction(e -> searchProducts());
        refreshButton.setOnAction(e -> {
            loadCombos();
            loadProducts();
        });

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
        addRow(form, 2, "Description", descriptionField);
        addRow(form, 3, "Price", priceField);
        addRow(form, 4, "Category", categoryComboBox);
        addRow(form, 5, "Brand", brandComboBox);

        Button addButton = FxTheme.primaryButton("Add");
        Button updateButton = FxTheme.primaryButton("Update");
        Button deleteButton = FxTheme.dangerButton("Delete");
        Button clearButton = FxTheme.secondaryButton("Clear");

        addButton.setOnAction(e -> addProduct());
        updateButton.setOnAction(e -> updateProduct());
        deleteButton.setOnAction(e -> deleteProduct());
        clearButton.setOnAction(e -> clearForm());

        form.add(FxTheme.actionRow(addButton, updateButton, deleteButton, clearButton), 0, 6, 2, 1);
        return form;
    }

    private void addRow(GridPane form, int row, String label, javafx.scene.Node field) {
        form.add(new javafx.scene.control.Label(label), 0, row);
        form.add(field, 1, row);
    }

    private void configureTable() {
        table.getColumns().add(FxTableUtil.column("ID", Product::getProductId, 70));
        table.getColumns().add(FxTableUtil.column("Product", Product::getProductName, 180));
        table.getColumns().add(FxTableUtil.column("Description", Product::getDescription, 220));
        table.getColumns().add(FxTableUtil.column("Price", Product::getDefaultSellingPrice, 100));
        table.getColumns().add(FxTableUtil.column("Category", Product::getCategoryName, 140));
        table.getColumns().add(FxTableUtil.column("Brand", Product::getBrandName, 140));
        FxTableUtil.installSearch(table, products, searchField);
        FxTheme.styleTable(table);
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, product) -> {
            if (product != null) fillForm(product);
        });
    }

    private void loadCombos() {
        categoryComboBox.setItems(FXCollections.observableArrayList(categoryDAO.getAllCategories()));
        brandComboBox.setItems(FXCollections.observableArrayList(brandDAO.getAllBrands()));
        if (!categoryComboBox.getItems().isEmpty()) categoryComboBox.getSelectionModel().selectFirst();
        if (!brandComboBox.getItems().isEmpty()) brandComboBox.getSelectionModel().selectFirst();
    }

    private void loadProducts() {
        products.setAll(productDAO.getAllProducts());
    }

    private void searchProducts() {
        String keyword = searchField.getText().trim();
        products.setAll(keyword.isEmpty() ? productDAO.getAllProducts() : productDAO.searchProducts(keyword));
    }

    private void addProduct() {
        Product product = readForm(false);
        if (product != null && productDAO.addProduct(product)) {
            FxTheme.showInfo("Product added successfully.");
            clearForm();
            loadProducts();
        }
    }

    private void updateProduct() {
        Product product = readForm(true);
        if (product != null && productDAO.updateProduct(product)) {
            FxTheme.showInfo("Product updated successfully.");
            clearForm();
            loadProducts();
        }
    }

    private void deleteProduct() {
        if (!idField.getText().isBlank() && FxTheme.confirm("Delete selected product?")) {
            productDAO.deleteProduct(Integer.parseInt(idField.getText()));
            clearForm();
            loadProducts();
        }
    }

    private Product readForm(boolean includeId) {
        Category category = categoryComboBox.getValue();
        Brand brand = brandComboBox.getValue();

        if (nameField.getText().trim().isEmpty() || category == null || brand == null) {
            FxTheme.showError("Product name, category, and brand are required.");
            return null;
        }

        BigDecimal price;
        try {
            price = new BigDecimal(priceField.getText().trim());
        } catch (Exception e) {
            FxTheme.showError("Price must be a valid number.");
            return null;
        }

        Product product = new Product(
                nameField.getText().trim(),
                descriptionField.getText().trim(),
                price,
                brand.getBrandId(),
                category.getCategoryId()
        );

        if (includeId) {
            if (idField.getText().isBlank()) {
                FxTheme.showError("Select a product first.");
                return null;
            }
            product.setProductId(Integer.parseInt(idField.getText()));
        }

        return product;
    }

    private void fillForm(Product product) {
        idField.setText(String.valueOf(product.getProductId()));
        nameField.setText(product.getProductName());
        descriptionField.setText(product.getDescription());
        priceField.setText(String.valueOf(product.getDefaultSellingPrice()));
        selectCategory(product.getCategoryId());
        selectBrand(product.getBrandId());
    }

    private void selectCategory(int categoryId) {
        for (Category category : categoryComboBox.getItems()) {
            if (category.getCategoryId() == categoryId) {
                categoryComboBox.setValue(category);
                return;
            }
        }
    }

    private void selectBrand(int brandId) {
        for (Brand brand : brandComboBox.getItems()) {
            if (brand.getBrandId() == brandId) {
                brandComboBox.setValue(brand);
                return;
            }
        }
    }

    private void clearForm() {
        idField.clear();
        nameField.clear();
        descriptionField.clear();
        priceField.clear();
        if (!categoryComboBox.getItems().isEmpty()) categoryComboBox.getSelectionModel().selectFirst();
        if (!brandComboBox.getItems().isEmpty()) brandComboBox.getSelectionModel().selectFirst();
        table.getSelectionModel().clearSelection();
    }
}
