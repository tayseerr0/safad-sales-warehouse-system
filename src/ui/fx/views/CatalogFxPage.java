package ui.fx.views;

import dao.BrandDAO;
import dao.CategoryDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Brand;
import model.Category;
import ui.fx.FxTableUtil;
import ui.fx.FxTheme;

public class CatalogFxPage extends VBox {

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final BrandDAO brandDAO = new BrandDAO();
    private final ObservableList<Category> categories = FXCollections.observableArrayList();
    private final ObservableList<Brand> brands = FXCollections.observableArrayList();

    private final TextField categoryIdField = FxTheme.textField("ID");
    private final TextField categoryNameField = FxTheme.textField("Name");
    private final TextField categoryDescriptionField = FxTheme.textField("Description");
    private final TextField categoryTypeField = FxTheme.textField("Type");
    private final TextField categorySearchField = FxTheme.textField("Search categories");
    private final TableView<Category> categoryTable = new TableView<>();

    private final TextField brandIdField = FxTheme.textField("ID");
    private final TextField brandNameField = FxTheme.textField("Name");
    private final TextField brandDescriptionField = FxTheme.textField("Description");
    private final TextField brandSearchField = FxTheme.textField("Search brands");
    private final TableView<Brand> brandTable = new TableView<>();

    public CatalogFxPage(ProductsFxPage productsPage) {
        getChildren().add(FxTheme.page("Products / Catalog", "Manage products, categories, and brands.", createContent(productsPage)));
        loadData();
    }

    private TabPane createContent(ProductsFxPage productsPage) {
        categoryIdField.setEditable(false);
        brandIdField.setEditable(false);

        configureCategoryTable();
        configureBrandTable();

        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("clean-tabs");
        tabs.getTabs().add(new Tab("Products", productsPage));
        tabs.getTabs().add(new Tab("Categories", createCategoryPane()));
        tabs.getTabs().add(new Tab("Brands", createBrandPane()));
        tabs.getTabs().forEach(tab -> tab.setClosable(false));
        return tabs;
    }

    private BorderPane createCategoryPane() {
        BorderPane pane = new BorderPane();
        pane.setLeft(FxTheme.card("Category Form", createCategoryForm()));
        pane.setCenter(FxTheme.card("Categories", new VBox(10,
                FxTheme.toolbar(categorySearchField),
                categoryTable
        )));
        BorderPane.setMargin(pane.getLeft(), new javafx.geometry.Insets(0, 16, 0, 0));
        return pane;
    }

    private BorderPane createBrandPane() {
        BorderPane pane = new BorderPane();
        pane.setLeft(FxTheme.card("Brand Form", createBrandForm()));
        pane.setCenter(FxTheme.card("Brands", new VBox(10,
                FxTheme.toolbar(brandSearchField),
                brandTable
        )));
        BorderPane.setMargin(pane.getLeft(), new javafx.geometry.Insets(0, 16, 0, 0));
        return pane;
    }

    private GridPane createCategoryForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        addRow(form, 0, "ID", categoryIdField);
        addRow(form, 1, "Name", categoryNameField);
        addRow(form, 2, "Description", categoryDescriptionField);
        addRow(form, 3, "Type", categoryTypeField);

        Button add = FxTheme.primaryButton("Add");
        Button update = FxTheme.primaryButton("Update");
        Button delete = FxTheme.dangerButton("Delete");
        Button clear = FxTheme.secondaryButton("Clear");
        add.setOnAction(e -> addCategory());
        update.setOnAction(e -> updateCategory());
        delete.setOnAction(e -> deleteCategory());
        clear.setOnAction(e -> clearCategoryForm());
        form.add(FxTheme.actionRow(add, update, delete, clear), 0, 4, 2, 1);
        return form;
    }

    private GridPane createBrandForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        addRow(form, 0, "ID", brandIdField);
        addRow(form, 1, "Name", brandNameField);
        addRow(form, 2, "Description", brandDescriptionField);

        Button add = FxTheme.primaryButton("Add");
        Button update = FxTheme.primaryButton("Update");
        Button delete = FxTheme.dangerButton("Delete");
        Button clear = FxTheme.secondaryButton("Clear");
        add.setOnAction(e -> addBrand());
        update.setOnAction(e -> updateBrand());
        delete.setOnAction(e -> deleteBrand());
        clear.setOnAction(e -> clearBrandForm());
        form.add(FxTheme.actionRow(add, update, delete, clear), 0, 3, 2, 1);
        return form;
    }

    private void addRow(GridPane form, int row, String label, javafx.scene.Node field) {
        form.add(new javafx.scene.control.Label(label), 0, row);
        form.add(field, 1, row);
    }

    private void configureCategoryTable() {
        categoryTable.getColumns().add(FxTableUtil.column("ID", Category::getCategoryId, 70));
        categoryTable.getColumns().add(FxTableUtil.column("Name", Category::getCategoryName, 160));
        categoryTable.getColumns().add(FxTableUtil.column("Description", Category::getDescription, 220));
        categoryTable.getColumns().add(FxTableUtil.column("Type", Category::getCategoryType, 120));
        FxTableUtil.installSearch(categoryTable, categories, categorySearchField);
        FxTheme.styleTable(categoryTable);
        categoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, category) -> {
            if (category != null) fillCategoryForm(category);
        });
    }

    private void configureBrandTable() {
        brandTable.getColumns().add(FxTableUtil.column("ID", Brand::getBrandId, 70));
        brandTable.getColumns().add(FxTableUtil.column("Name", Brand::getBrandName, 160));
        brandTable.getColumns().add(FxTableUtil.column("Description", Brand::getDescription, 240));
        FxTableUtil.installSearch(brandTable, brands, brandSearchField);
        FxTheme.styleTable(brandTable);
        brandTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, brand) -> {
            if (brand != null) fillBrandForm(brand);
        });
    }

    private void loadData() {
        categories.setAll(categoryDAO.getAllCategories());
        brands.setAll(brandDAO.getAllBrands());
    }

    private void addCategory() {
        if (categoryDAO.addCategory(new Category(categoryNameField.getText().trim(), categoryDescriptionField.getText().trim(), categoryTypeField.getText().trim()))) {
            FxTheme.showInfo("Category added.");
            clearCategoryForm();
            loadData();
        }
    }

    private void updateCategory() {
        if (categoryIdField.getText().isBlank()) return;
        if (categoryDAO.updateCategory(new Category(Integer.parseInt(categoryIdField.getText()), categoryNameField.getText().trim(), categoryDescriptionField.getText().trim(), categoryTypeField.getText().trim()))) {
            FxTheme.showInfo("Category updated.");
            clearCategoryForm();
            loadData();
        }
    }

    private void deleteCategory() {
        if (!categoryIdField.getText().isBlank() && FxTheme.confirm("Delete selected category?")) {
            categoryDAO.deleteCategory(Integer.parseInt(categoryIdField.getText()));
            clearCategoryForm();
            loadData();
        }
    }

    private void addBrand() {
        if (brandDAO.addBrand(new Brand(brandNameField.getText().trim(), brandDescriptionField.getText().trim()))) {
            FxTheme.showInfo("Brand added.");
            clearBrandForm();
            loadData();
        }
    }

    private void updateBrand() {
        if (brandIdField.getText().isBlank()) return;
        if (brandDAO.updateBrand(new Brand(Integer.parseInt(brandIdField.getText()), brandNameField.getText().trim(), brandDescriptionField.getText().trim()))) {
            FxTheme.showInfo("Brand updated.");
            clearBrandForm();
            loadData();
        }
    }

    private void deleteBrand() {
        if (!brandIdField.getText().isBlank() && FxTheme.confirm("Delete selected brand?")) {
            brandDAO.deleteBrand(Integer.parseInt(brandIdField.getText()));
            clearBrandForm();
            loadData();
        }
    }

    private void fillCategoryForm(Category category) {
        categoryIdField.setText(String.valueOf(category.getCategoryId()));
        categoryNameField.setText(category.getCategoryName());
        categoryDescriptionField.setText(category.getDescription());
        categoryTypeField.setText(category.getCategoryType());
    }

    private void fillBrandForm(Brand brand) {
        brandIdField.setText(String.valueOf(brand.getBrandId()));
        brandNameField.setText(brand.getBrandName());
        brandDescriptionField.setText(brand.getDescription());
    }

    private void clearCategoryForm() {
        categoryIdField.clear();
        categoryNameField.clear();
        categoryDescriptionField.clear();
        categoryTypeField.clear();
        categoryTable.getSelectionModel().clearSelection();
    }

    private void clearBrandForm() {
        brandIdField.clear();
        brandNameField.clear();
        brandDescriptionField.clear();
        brandTable.getSelectionModel().clearSelection();
    }
}
