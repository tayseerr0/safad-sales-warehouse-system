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

    private JTextField productIdField, productNameField, descriptionField, priceField;
    private JTextField nameFilterField, minPriceField, maxPriceField;

    private JComboBox<Category> categoryComboBox;
    private JComboBox<Brand> brandComboBox;
    private JComboBox<Object> categoryFilterComboBox;
    private JComboBox<Object> brandFilterComboBox;

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
        return UIStyle.createHeaderPanel(
                "Product Management",
                "Manage products, combine filters, and choose visible columns."
        );
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIStyle.BACKGROUND);
        panel.add(createFormPanel(), BorderLayout.NORTH);
        panel.add(createTablePanel(), BorderLayout.CENTER);
        panel.add(createFilterPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel wrapper = UIStyle.createCardPanel();

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
        JPanel panel = UIStyle.createCardPanel();

        String[] columns = {"ID", "Product Name", "Description", "Price", "Category ID", "Category", "Brand ID", "Brand"};

        productTableModel = TableUtil.createNonEditableTableModel(columns);
        productTable = new JTable(productTableModel);
        TableUtil.setupTable(productTable);

        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromTable();
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setBackground(UIStyle.PANEL_BACKGROUND);
        topPanel.add(TableUtil.createColumnVisibilityButton(productTable, "Columns"));

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFilterPanel() {
        JPanel wrapper = UIStyle.createCardPanel();

        JLabel title = new JLabel("Advanced Filters");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JPanel filterPanel = new JPanel(new GridLayout(2, 5, 10, 10));
        filterPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        nameFilterField = new JTextField();
        minPriceField = new JTextField();
        maxPriceField = new JTextField();
        categoryFilterComboBox = new JComboBox<>();
        brandFilterComboBox = new JComboBox<>();

        UIStyle.styleTextField(nameFilterField);
        UIStyle.styleTextField(minPriceField);
        UIStyle.styleTextField(maxPriceField);
        UIStyle.styleComboBox(categoryFilterComboBox);
        UIStyle.styleComboBox(brandFilterComboBox);

        filterPanel.add(new JLabel("Name/Keyword:"));
        filterPanel.add(new JLabel("Category:"));
        filterPanel.add(new JLabel("Brand:"));
        filterPanel.add(new JLabel("Min Price:"));
        filterPanel.add(new JLabel("Max Price:"));

        filterPanel.add(nameFilterField);
        filterPanel.add(categoryFilterComboBox);
        filterPanel.add(brandFilterComboBox);
        filterPanel.add(minPriceField);
        filterPanel.add(maxPriceField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        JButton applyButton = new JButton("Apply Filters");
        JButton clearButton = new JButton("Clear Filters");
        JButton showAllButton = new JButton("Show All");

        UIStyle.stylePrimaryButton(applyButton);
        UIStyle.stylePrimaryButton(clearButton);
        UIStyle.stylePrimaryButton(showAllButton);

        applyButton.addActionListener(e -> applyAdvancedFilters());
        clearButton.addActionListener(e -> clearFilters());
        showAllButton.addActionListener(e -> loadProducts());

        buttonPanel.add(applyButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(showAllButton);

        wrapper.add(title, BorderLayout.NORTH);
        wrapper.add(filterPanel, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        return wrapper;
    }

    private void loadComboBoxes() {
        categoryComboBox.removeAllItems();
        categoryFilterComboBox.removeAllItems();
        categoryFilterComboBox.addItem("All Categories");

        for (Category category : categoryDAO.getAllCategories()) {
            categoryComboBox.addItem(category);
            categoryFilterComboBox.addItem(category);
        }

        brandComboBox.removeAllItems();
        brandFilterComboBox.removeAllItems();
        brandFilterComboBox.addItem("All Brands");

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
        if (!validateProductInput()) return;

        if (productDAO.addProduct(buildProductFromForm(false))) {
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

        if (!validateProductInput()) return;

        if (productDAO.updateProduct(buildProductFromForm(true))) {
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

        if (!MessageUtil.confirm("Are you sure you want to delete this product?")) return;

        int productId = Integer.parseInt(productIdField.getText());

        if (productDAO.deleteProduct(productId)) {
            MessageUtil.showSuccess("Product deleted successfully.");
            clearForm();
            loadProducts();
        } else {
            MessageUtil.showError("Failed to delete product. It may be used in inventory, invoices, or supplier links.");
        }
    }

    private void applyAdvancedFilters() {
        String keyword = nameFilterField.getText().trim();

        Integer categoryId = null;
        Object categoryObj = categoryFilterComboBox.getSelectedItem();
        if (categoryObj instanceof Category) categoryId = ((Category) categoryObj).getCategoryId();

        Integer brandId = null;
        Object brandObj = brandFilterComboBox.getSelectedItem();
        if (brandObj instanceof Brand) brandId = ((Brand) brandObj).getBrandId();

        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;

        if (!ValidationUtil.isEmpty(minPriceField.getText())) {
            if (!ValidationUtil.isNonNegativeDecimal(minPriceField.getText())) {
                MessageUtil.showError("Min price must be a valid non-negative number.");
                return;
            }
            minPrice = new BigDecimal(minPriceField.getText().trim());
        }

        if (!ValidationUtil.isEmpty(maxPriceField.getText())) {
            if (!ValidationUtil.isNonNegativeDecimal(maxPriceField.getText())) {
                MessageUtil.showError("Max price must be a valid non-negative number.");
                return;
            }
            maxPrice = new BigDecimal(maxPriceField.getText().trim());
        }

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            MessageUtil.showError("Min price cannot be greater than max price.");
            return;
        }

        fillProductTable(productDAO.filterProducts(keyword, categoryId, brandId, minPrice, maxPrice));
    }

    private void clearFilters() {
        nameFilterField.setText("");
        minPriceField.setText("");
        maxPriceField.setText("");

        if (categoryFilterComboBox.getItemCount() > 0) categoryFilterComboBox.setSelectedIndex(0);
        if (brandFilterComboBox.getItemCount() > 0) brandFilterComboBox.setSelectedIndex(0);

        loadProducts();
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

        if (includeId) product.setProductId(Integer.parseInt(productIdField.getText()));
        return product;
    }

    private void fillFormFromTable() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) return;

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
            if (comboBox.getItemAt(i).getCategoryId() == categoryId) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectBrandById(JComboBox<Brand> comboBox, int brandId) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (comboBox.getItemAt(i).getBrandId() == brandId) {
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

        if (categoryComboBox.getItemCount() > 0) categoryComboBox.setSelectedIndex(0);
        if (brandComboBox.getItemCount() > 0) brandComboBox.setSelectedIndex(0);

        productTable.clearSelection();
    }
}
