package ui.views;

import dao.BrandDAO;
import dao.CategoryDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Brand;
import model.Category;
import ui.TableUtil;
import ui.Theme;

public class CatalogPage extends VBox {

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final BrandDAO brandDAO = new BrandDAO();
    private final ObservableList<Category> categories = FXCollections.observableArrayList();
    private final ObservableList<Brand> brands = FXCollections.observableArrayList();

    private final TextField categoryIdField = Theme.textField("ID");
    private final TextField categoryNameField = Theme.textField("Name");
    private final TextField categoryDescriptionField = Theme.textField("Description");
    private final TextField categoryTypeField = Theme.textField("Type");
    private final TextField categorySearchField = Theme.textField("Search categories");
    private final ComboBox<String> categorySearchColumnBox = TableUtil.searchColumnBox();
    private final TableView<Category> categoryTable = new TableView<>();

    private final TextField brandIdField = Theme.textField("ID");
    private final TextField brandNameField = Theme.textField("Name");
    private final TextField brandDescriptionField = Theme.textField("Description");
    private final TextField brandSearchField = Theme.textField("Search brands");
    private final ComboBox<String> brandSearchColumnBox = TableUtil.searchColumnBox();
    private final TableView<Brand> brandTable = new TableView<>();
    private Button categoryActionButton;
    private Button categoryDeleteButton;
    private Button brandActionButton;
    private Button brandDeleteButton;

    public CatalogPage(ProductsPage productsPage) {
        getStyleClass().add("ledger-page");
        TabPane content = createContent(productsPage);
        VBox.setVgrow(content, javafx.scene.layout.Priority.ALWAYS);
        getChildren().add(content);
        loadData();
    }

    private TabPane createContent(ProductsPage productsPage) {
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
        return Theme.ledgerWorkspace(
                Theme.ledgerSurface("Category Ledger", createCategoryToolbar(), categoryTable),
                Theme.ledgerInspector("Category Inspector", createCategoryForm())
        );
    }

    private BorderPane createBrandPane() {
        return Theme.ledgerWorkspace(
                Theme.ledgerSurface("Brand Ledger", createBrandToolbar(), brandTable),
                Theme.ledgerInspector("Brand Inspector", createBrandForm())
        );
    }

    private HBox createCategoryToolbar() {
        Button refresh = Theme.refreshButton();
        refresh.setOnAction(e -> {
            categorySearchField.clear();
            loadData();
        });
        HBox toolbar = Theme.ledgerCommandBar(categorySearchColumnBox, categorySearchField, refresh);
        Theme.stretchToolbarField(categorySearchField);
        return toolbar;
    }

    private HBox createBrandToolbar() {
        Button refresh = Theme.refreshButton();
        refresh.setOnAction(e -> {
            brandSearchField.clear();
            loadData();
        });
        HBox toolbar = Theme.ledgerCommandBar(brandSearchColumnBox, brandSearchField, refresh);
        Theme.stretchToolbarField(brandSearchField);
        return toolbar;
    }

    private GridPane createCategoryForm() {
        GridPane form = new GridPane();
        Theme.configureInspectorForm(form);
        addRow(form, 0, "ID", categoryIdField);
        addRow(form, 1, "Name", categoryNameField);
        addRow(form, 2, "Description", categoryDescriptionField);
        addRow(form, 3, "Type", categoryTypeField);

        categoryActionButton = Theme.primaryButton("Add");
        categoryDeleteButton = Theme.dangerButton("Delete");
        Button clear = Theme.secondaryButton("Clear");
        categoryActionButton.setOnAction(e -> saveCategory());
        categoryDeleteButton.setOnAction(e -> deleteCategory());
        clear.setOnAction(e -> clearCategoryForm());
        Theme.setVisible(categoryDeleteButton, false);
        Theme.addInspectorActions(form, 4, categoryActionButton, categoryDeleteButton, clear);
        return form;
    }

    private GridPane createBrandForm() {
        GridPane form = new GridPane();
        Theme.configureInspectorForm(form);
        addRow(form, 0, "ID", brandIdField);
        addRow(form, 1, "Name", brandNameField);
        addRow(form, 2, "Description", brandDescriptionField);

        brandActionButton = Theme.primaryButton("Add");
        brandDeleteButton = Theme.dangerButton("Delete");
        Button clear = Theme.secondaryButton("Clear");
        brandActionButton.setOnAction(e -> saveBrand());
        brandDeleteButton.setOnAction(e -> deleteBrand());
        clear.setOnAction(e -> clearBrandForm());
        Theme.setVisible(brandDeleteButton, false);
        Theme.addInspectorActions(form, 3, brandActionButton, brandDeleteButton, clear);
        return form;
    }

    private void addRow(GridPane form, int row, String label, javafx.scene.Node field) {
        Theme.addInspectorRow(form, row, label, field);
    }

    private void configureCategoryTable() {
        categoryTable.getColumns().add(TableUtil.column("ID", Category::getCategoryId, 70));
        categoryTable.getColumns().add(TableUtil.column("Name", Category::getCategoryName, 160));
        categoryTable.getColumns().add(TableUtil.column("Description", Category::getDescription, 220));
        categoryTable.getColumns().add(TableUtil.column("Type", Category::getCategoryType, 120));
        TableUtil.installSearch(categoryTable, categories, categorySearchField, categorySearchColumnBox);
        Theme.styleTable(categoryTable);
        categoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, category) -> {
            if (category != null) fillCategoryForm(category);
        });
    }

    private void configureBrandTable() {
        brandTable.getColumns().add(TableUtil.column("ID", Brand::getBrandId, 70));
        brandTable.getColumns().add(TableUtil.column("Name", Brand::getBrandName, 160));
        brandTable.getColumns().add(TableUtil.column("Description", Brand::getDescription, 240));
        TableUtil.installSearch(brandTable, brands, brandSearchField, brandSearchColumnBox);
        Theme.styleTable(brandTable);
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
            Theme.showInfo("Category added.");
            clearCategoryForm();
            loadData();
        }
    }

    private void updateCategory() {
        if (categoryIdField.getText().isBlank()) return;
        if (categoryDAO.updateCategory(new Category(Integer.parseInt(categoryIdField.getText()), categoryNameField.getText().trim(), categoryDescriptionField.getText().trim(), categoryTypeField.getText().trim()))) {
            Theme.showInfo("Category updated.");
            clearCategoryForm();
            loadData();
        }
    }

    private void saveCategory() {
        if (categoryIdField.getText().isBlank()) {
            addCategory();
        } else {
            updateCategory();
        }
    }

    private void deleteCategory() {
        if (!categoryIdField.getText().isBlank() && Theme.confirm("Delete selected category?")) {
            categoryDAO.deleteCategory(Integer.parseInt(categoryIdField.getText()));
            clearCategoryForm();
            loadData();
        }
    }

    private void addBrand() {
        if (brandDAO.addBrand(new Brand(brandNameField.getText().trim(), brandDescriptionField.getText().trim()))) {
            Theme.showInfo("Brand added.");
            clearBrandForm();
            loadData();
        }
    }

    private void updateBrand() {
        if (brandIdField.getText().isBlank()) return;
        if (brandDAO.updateBrand(new Brand(Integer.parseInt(brandIdField.getText()), brandNameField.getText().trim(), brandDescriptionField.getText().trim()))) {
            Theme.showInfo("Brand updated.");
            clearBrandForm();
            loadData();
        }
    }

    private void saveBrand() {
        if (brandIdField.getText().isBlank()) {
            addBrand();
        } else {
            updateBrand();
        }
    }

    private void deleteBrand() {
        if (!brandIdField.getText().isBlank() && Theme.confirm("Delete selected brand?")) {
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
        updateCategoryFormMode();
    }

    private void fillBrandForm(Brand brand) {
        brandIdField.setText(String.valueOf(brand.getBrandId()));
        brandNameField.setText(brand.getBrandName());
        brandDescriptionField.setText(brand.getDescription());
        updateBrandFormMode();
    }

    private void clearCategoryForm() {
        categoryIdField.clear();
        categoryNameField.clear();
        categoryDescriptionField.clear();
        categoryTypeField.clear();
        categoryTable.getSelectionModel().clearSelection();
        updateCategoryFormMode();
    }

    private void clearBrandForm() {
        brandIdField.clear();
        brandNameField.clear();
        brandDescriptionField.clear();
        brandTable.getSelectionModel().clearSelection();
        updateBrandFormMode();
    }

    private void updateCategoryFormMode() {
        boolean selected = !categoryIdField.getText().isBlank();
        if (categoryActionButton != null) {
            categoryActionButton.setText(selected ? "Update" : "Add");
        }
        if (categoryDeleteButton != null) {
            Theme.setVisible(categoryDeleteButton, selected);
        }
    }

    private void updateBrandFormMode() {
        boolean selected = !brandIdField.getText().isBlank();
        if (brandActionButton != null) {
            brandActionButton.setText(selected ? "Update" : "Add");
        }
        if (brandDeleteButton != null) {
            Theme.setVisible(brandDeleteButton, selected);
        }
    }
}
