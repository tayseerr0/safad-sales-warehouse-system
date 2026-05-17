package ui;

import dao.BrandDAO;
import dao.CategoryDAO;
import dao.ProductDAO;
import model.Brand;
import model.Category;
import model.Product;
import util.MessageUtil;
import util.TableUtil;
import util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class ProductPanel extends JPanel {

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final BrandDAO brandDAO = new BrandDAO();

    private JTextField productIdField;
    private JTextField productNameField;
    private JTextField descriptionField;
    private JTextField priceField;
    private JTextField searchField;

    private JComboBox<Category> categoryComboBox;
    private JComboBox<Brand> brandComboBox;
    private JComboBox<Category> categoryFilterComboBox;
    private JComboBox<Brand> brandFilterComboBox;

    private JTable productTable;
    private DefaultTableModel productTableModel;

    public ProductPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIStyle.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        loadComboBoxes();
        loadProducts();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setBackground(UIStyle.BACKGROUND);

        panel.add(UIStyle.createTitle("Product Management"));
        panel.add(UIStyle.createSubtitle("Manage SAFAD products and connect them to categories and brands."));

        return panel;
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIStyle.BACKGROUND);

        panel.add(createFormPanel(), BorderLayout.NORTH);
        panel.add(createTablePanel(), BorderLayout.CENTER);
        panel.add(createSearchFilterPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.setBackground(UIStyle.PANEL_BACKGROUND);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JPanel formPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        formPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        productIdField = new JTextField();
        productIdField.setEditable(false);

        productNameField = new JTextField();
        descriptionField = new JTextField();
        priceField = new JTextField();

        categoryComboBox = new JComboBox<>();
        brandComboBox = new JComboBox<>();

        UIStyle.styleTextField(productIdField);
        UIStyle.styleTextField(productNameField);
        UIStyle.styleTextField(descriptionField);
        UIStyle.styleTextField(priceField);
        UIStyle.styleComboBox(categoryComboBox);
        UIStyle.styleComboBox(brandComboBox);

        formPanel.add(new JLabel("Product ID:"));
        formPanel.add(productIdField);
        formPanel.add(new JLabel("Product Name:"));
        formPanel.add(productNameField);

        formPanel.add(new JLabel("Description:"));
        formPanel.add(descriptionField);
        formPanel.add(new JLabel("Default Selling Price:"));
        formPanel.add(priceField);

        formPanel.add(new JLabel("Category:"));
        formPanel.add(categoryComboBox);
        formPanel.add(new JLabel("Brand:"));
        formPanel.add(brandComboBox);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear");
        JButton refreshButton = new JButton("Refresh");

        UIStyle.stylePrimaryButton(addButton);
        UIStyle.stylePrimaryButton(updateButton);
        UIStyle.stylePrimaryButton(deleteButton);
        UIStyle.stylePrimaryButton(clearButton);
        UIStyle.stylePrimaryButton(refreshButton);

        addButton.addActionListener(e -> addProduct());
        updateButton.addActionListener(e -> updateProduct());
        deleteButton.addActionListener(e -> deleteProduct());
        clearButton.addActionListener(e -> clearForm());
        refreshButton.addActionListener(e -> {
            loadComboBoxes();
            loadProducts();
            clearForm();
        });

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        wrapper.add(formPanel, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        return wrapper;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIStyle.PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        String[] columns = {
                "ID",
                "Product Name",
                "Description",
                "Price",
                "Category ID",
                "Category",
                "Brand ID",
                "Brand"
        };

        productTableModel = TableUtil.createNonEditableTableModel(columns);
        productTable = new JTable(productTableModel);
        TableUtil.setupTable(productTable);

        productTable.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());

        JScrollPane scrollPane = new JScrollPane(productTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSearchFilterPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 8, 8));
        panel.setBackground(UIStyle.PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JPanel searchPanel = new JPanel(new BorderLayout(8, 8));
        searchPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        searchField = new JTextField();
        UIStyle.styleTextField(searchField);

        JButton searchButton = new JButton("Search");
        JButton showAllButton = new JButton("Show All");

        UIStyle.stylePrimaryButton(searchButton);
        UIStyle.stylePrimaryButton(showAllButton);

        searchButton.addActionListener(e -> searchProducts());
        showAllButton.addActionListener(e -> loadProducts());

        JPanel searchButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchButtons.setBackground(UIStyle.PANEL_BACKGROUND);
        searchButtons.add(searchButton);
        searchButtons.add(showAllButton);

        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButtons, BorderLayout.EAST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        categoryFilterComboBox = new JComboBox<>();
        brandFilterComboBox = new JComboBox<>();

        UIStyle.styleComboBox(categoryFilterComboBox);
        UIStyle.styleComboBox(brandFilterComboBox);

        JButton filterCategoryButton = new JButton("Filter Category");
        JButton filterBrandButton = new JButton("Filter Brand");

        UIStyle.stylePrimaryButton(filterCategoryButton);
        UIStyle.stylePrimaryButton(filterBrandButton);

        filterCategoryButton.addActionListener(e -> filterByCategory());
        filterBrandButton.addActionListener(e -> filterByBrand());

        filterPanel.add(new JLabel("Category:"));
        filterPanel.add(categoryFilterComboBox);
        filterPanel.add(filterCategoryButton);

        filterPanel.add(Box.createHorizontalStrut(20));

        filterPanel.add(new JLabel("Brand:"));
        filterPanel.add(brandFilterComboBox);
        filterPanel.add(filterBrandButton);

        panel.add(searchPanel);
        panel.add(filterPanel);

        return panel;
    }

    private void loadComboBoxes() {
        categoryComboBox.removeAllItems();
        categoryFilterComboBox.removeAllItems();

        for (Category category : categoryDAO.getAllCategories()) {
            categoryComboBox.addItem(category);
            categoryFilterComboBox.addItem(category);
        }

        brandComboBox.removeAllItems();
        brandFilterComboBox.removeAllItems();

        for (Brand brand : brandDAO.getAllBrands()) {
            brandComboBox.addItem(brand);
            brandFilterComboBox.addItem(brand);
        }
    }

    private void loadProducts() {
        fillProductTable(productDAO.getAllProducts());
    }

    private void fillProductTable(List<Product> products) {
        TableUtil.clearTable(productTableModel);

        for (Product product : products) {
            productTableModel.addRow(new Object[]{
                    product.getProductId(),
                    product.getProductName(),
                    product.getDescription(),
                    product.getDefaultSellingPrice(),
                    product.getCategoryId(),
                    product.getCategoryName(),
                    product.getBrandId(),
                    product.getBrandName()
            });
        }
    }

    private void addProduct() {
        if (!validateProductInput()) {
            return;
        }

        Product product = buildProductFromForm(false);

        if (productDAO.addProduct(product)) {
            MessageUtil.showSuccess("Product added successfully.");
            clearForm();
            loadProducts();
        } else {
            MessageUtil.showError("Failed to add product.");
        }
    }

    private void updateProduct() {
        if (ValidationUtil.isEmpty(productIdField.getText())) {
            MessageUtil.showWarning("Select a product to update.");
            return;
        }

        if (!validateProductInput()) {
            return;
        }

        Product product = buildProductFromForm(true);

        if (productDAO.updateProduct(product)) {
            MessageUtil.showSuccess("Product updated successfully.");
            clearForm();
            loadProducts();
        } else {
            MessageUtil.showError("Failed to update product.");
        }
    }

    private void deleteProduct() {
        if (ValidationUtil.isEmpty(productIdField.getText())) {
            MessageUtil.showWarning("Select a product to delete.");
            return;
        }

        if (!MessageUtil.confirm("Are you sure you want to delete this product?")) {
            return;
        }

        int productId = Integer.parseInt(productIdField.getText());

        if (productDAO.deleteProduct(productId)) {
            MessageUtil.showSuccess("Product deleted successfully.");
            clearForm();
            loadProducts();
        } else {
            MessageUtil.showError("Failed to delete product. It may be used in inventory, invoices, or supplier links.");
        }
    }

    private void searchProducts() {
        String keyword = searchField.getText().trim();

        if (ValidationUtil.isEmpty(keyword)) {
            loadProducts();
        } else {
            fillProductTable(productDAO.searchProducts(keyword));
        }
    }

    private void filterByCategory() {
        Category category = (Category) categoryFilterComboBox.getSelectedItem();

        if (category == null) {
            MessageUtil.showWarning("Select a category first.");
            return;
        }

        fillProductTable(productDAO.getProductsByCategory(category.getCategoryId()));
    }

    private void filterByBrand() {
        Brand brand = (Brand) brandFilterComboBox.getSelectedItem();

        if (brand == null) {
            MessageUtil.showWarning("Select a brand first.");
            return;
        }

        fillProductTable(productDAO.getProductsByBrand(brand.getBrandId()));
    }

    private boolean validateProductInput() {
        if (ValidationUtil.isEmpty(productNameField.getText())) {
            MessageUtil.showError("Product name is required.");
            return false;
        }

        if (!ValidationUtil.isNonNegativeDecimal(priceField.getText())) {
            MessageUtil.showError("Default selling price must be a valid non-negative number.");
            return false;
        }

        if (categoryComboBox.getSelectedItem() == null) {
            MessageUtil.showError("Please select a category.");
            return false;
        }

        if (brandComboBox.getSelectedItem() == null) {
            MessageUtil.showError("Please select a brand.");
            return false;
        }

        return true;
    }

    private Product buildProductFromForm(boolean includeId) {
        Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
        Brand selectedBrand = (Brand) brandComboBox.getSelectedItem();

        Product product = new Product(
                productNameField.getText().trim(),
                descriptionField.getText().trim(),
                new BigDecimal(priceField.getText().trim()),
                selectedBrand.getBrandId(),
                selectedCategory.getCategoryId()
        );

        if (includeId) {
            product.setProductId(Integer.parseInt(productIdField.getText()));
        }

        return product;
    }

    private void fillFormFromTable() {
        int selectedRow = productTable.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        int modelRow = productTable.convertRowIndexToModel(selectedRow);

        productIdField.setText(productTableModel.getValueAt(modelRow, 0).toString());
        productNameField.setText(String.valueOf(productTableModel.getValueAt(modelRow, 1)));
        descriptionField.setText(String.valueOf(productTableModel.getValueAt(modelRow, 2)));
        priceField.setText(String.valueOf(productTableModel.getValueAt(modelRow, 3)));

        int categoryId = Integer.parseInt(productTableModel.getValueAt(modelRow, 4).toString());
        int brandId = Integer.parseInt(productTableModel.getValueAt(modelRow, 6).toString());

        selectCategoryById(categoryComboBox, categoryId);
        selectBrandById(brandComboBox, brandId);
    }

    private void selectCategoryById(JComboBox<Category> comboBox, int categoryId) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            Category category = comboBox.getItemAt(i);
            if (category.getCategoryId() == categoryId) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectBrandById(JComboBox<Brand> comboBox, int brandId) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            Brand brand = comboBox.getItemAt(i);
            if (brand.getBrandId() == brandId) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void clearForm() {
        productIdField.setText("");
        productNameField.setText("");
        descriptionField.setText("");
        priceField.setText("");

        if (categoryComboBox.getItemCount() > 0) {
            categoryComboBox.setSelectedIndex(0);
        }

        if (brandComboBox.getItemCount() > 0) {
            brandComboBox.setSelectedIndex(0);
        }

        productTable.clearSelection();
    }
}