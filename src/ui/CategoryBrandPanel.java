package ui;

import dao.BrandDAO;
import dao.CategoryDAO;
import model.Brand;
import model.Category;
import util.MessageUtil;
import util.TableUtil;
import util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CategoryBrandPanel extends JPanel {

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final BrandDAO brandDAO = new BrandDAO();

    private JTextField categoryIdField;
    private JTextField categoryNameField;
    private JTextField categoryDescriptionField;
    private JTextField categoryTypeField;
    private JTextField categorySearchField;
    private JTable categoryTable;
    private DefaultTableModel categoryTableModel;

    private JTextField brandIdField;
    private JTextField brandNameField;
    private JTextField brandDescriptionField;
    private JTextField brandSearchField;
    private JTable brandTable;
    private DefaultTableModel brandTableModel;

    public CategoryBrandPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIStyle.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                createCategoryPanel(),
                createBrandPanel()
        );

        splitPane.setResizeWeight(0.50);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);

        loadCategories();
        loadBrands();
    }

    private JPanel createCategoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIStyle.PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = UIStyle.createTitle("Category Management");

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        formPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        categoryIdField = new JTextField();
        categoryIdField.setEditable(false);

        categoryNameField = new JTextField();
        categoryDescriptionField = new JTextField();
        categoryTypeField = new JTextField();

        UIStyle.styleTextField(categoryIdField);
        UIStyle.styleTextField(categoryNameField);
        UIStyle.styleTextField(categoryDescriptionField);
        UIStyle.styleTextField(categoryTypeField);

        formPanel.add(new JLabel("Category ID:"));
        formPanel.add(categoryIdField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(categoryNameField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(categoryDescriptionField);
        formPanel.add(new JLabel("Type:"));
        formPanel.add(categoryTypeField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear");

        UIStyle.stylePrimaryButton(addButton);
        UIStyle.stylePrimaryButton(updateButton);
        UIStyle.stylePrimaryButton(deleteButton);
        UIStyle.stylePrimaryButton(clearButton);

        addButton.addActionListener(e -> addCategory());
        updateButton.addActionListener(e -> updateCategory());
        deleteButton.addActionListener(e -> deleteCategory());
        clearButton.addActionListener(e -> clearCategoryForm());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        JPanel searchPanel = new JPanel(new BorderLayout(8, 8));
        searchPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        categorySearchField = new JTextField();
        UIStyle.styleTextField(categorySearchField);

        JButton searchButton = new JButton("Search");
        JButton refreshButton = new JButton("Refresh");

        UIStyle.stylePrimaryButton(searchButton);
        UIStyle.stylePrimaryButton(refreshButton);

        searchButton.addActionListener(e -> searchCategories());
        refreshButton.addActionListener(e -> loadCategories());

        JPanel searchButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchButtons.setBackground(UIStyle.PANEL_BACKGROUND);
        searchButtons.add(searchButton);
        searchButtons.add(refreshButton);

        searchPanel.add(categorySearchField, BorderLayout.CENTER);
        searchPanel.add(searchButtons, BorderLayout.EAST);

        String[] columns = {"ID", "Name", "Description", "Type"};
        categoryTableModel = TableUtil.createNonEditableTableModel(columns);
        categoryTable = new JTable(categoryTableModel);
        TableUtil.setupTable(categoryTable);

        categoryTable.getSelectionModel().addListSelectionListener(e -> fillCategoryFormFromTable());

        JScrollPane scrollPane = new JScrollPane(categoryTable);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(UIStyle.PANEL_BACKGROUND);
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBrandPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIStyle.PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = UIStyle.createTitle("Brand Management");

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        formPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        brandIdField = new JTextField();
        brandIdField.setEditable(false);

        brandNameField = new JTextField();
        brandDescriptionField = new JTextField();

        UIStyle.styleTextField(brandIdField);
        UIStyle.styleTextField(brandNameField);
        UIStyle.styleTextField(brandDescriptionField);

        formPanel.add(new JLabel("Brand ID:"));
        formPanel.add(brandIdField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(brandNameField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(brandDescriptionField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear");

        UIStyle.stylePrimaryButton(addButton);
        UIStyle.stylePrimaryButton(updateButton);
        UIStyle.stylePrimaryButton(deleteButton);
        UIStyle.stylePrimaryButton(clearButton);

        addButton.addActionListener(e -> addBrand());
        updateButton.addActionListener(e -> updateBrand());
        deleteButton.addActionListener(e -> deleteBrand());
        clearButton.addActionListener(e -> clearBrandForm());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        JPanel searchPanel = new JPanel(new BorderLayout(8, 8));
        searchPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        brandSearchField = new JTextField();
        UIStyle.styleTextField(brandSearchField);

        JButton searchButton = new JButton("Search");
        JButton refreshButton = new JButton("Refresh");

        UIStyle.stylePrimaryButton(searchButton);
        UIStyle.stylePrimaryButton(refreshButton);

        searchButton.addActionListener(e -> searchBrands());
        refreshButton.addActionListener(e -> loadBrands());

        JPanel searchButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchButtons.setBackground(UIStyle.PANEL_BACKGROUND);
        searchButtons.add(searchButton);
        searchButtons.add(refreshButton);

        searchPanel.add(brandSearchField, BorderLayout.CENTER);
        searchPanel.add(searchButtons, BorderLayout.EAST);

        String[] columns = {"ID", "Name", "Description"};
        brandTableModel = TableUtil.createNonEditableTableModel(columns);
        brandTable = new JTable(brandTableModel);
        TableUtil.setupTable(brandTable);

        brandTable.getSelectionModel().addListSelectionListener(e -> fillBrandFormFromTable());

        JScrollPane scrollPane = new JScrollPane(brandTable);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(UIStyle.PANEL_BACKGROUND);
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addCategory() {
        if (ValidationUtil.isEmpty(categoryNameField.getText())) {
            MessageUtil.showError("Category name is required.");
            return;
        }

        Category category = new Category(
                categoryNameField.getText().trim(),
                categoryDescriptionField.getText().trim(),
                categoryTypeField.getText().trim()
        );

        if (categoryDAO.addCategory(category)) {
            MessageUtil.showSuccess("Category added successfully.");
            clearCategoryForm();
            loadCategories();
        } else {
            MessageUtil.showError("Failed to add category.");
        }
    }

    private void updateCategory() {
        if (ValidationUtil.isEmpty(categoryIdField.getText())) {
            MessageUtil.showWarning("Select a category to update.");
            return;
        }

        if (ValidationUtil.isEmpty(categoryNameField.getText())) {
            MessageUtil.showError("Category name is required.");
            return;
        }

        Category category = new Category(
                Integer.parseInt(categoryIdField.getText()),
                categoryNameField.getText().trim(),
                categoryDescriptionField.getText().trim(),
                categoryTypeField.getText().trim()
        );

        if (categoryDAO.updateCategory(category)) {
            MessageUtil.showSuccess("Category updated successfully.");
            clearCategoryForm();
            loadCategories();
        } else {
            MessageUtil.showError("Failed to update category.");
        }
    }

    private void deleteCategory() {
        if (ValidationUtil.isEmpty(categoryIdField.getText())) {
            MessageUtil.showWarning("Select a category to delete.");
            return;
        }

        if (!MessageUtil.confirm("Are you sure you want to delete this category?")) {
            return;
        }

        int categoryId = Integer.parseInt(categoryIdField.getText());

        if (categoryDAO.deleteCategory(categoryId)) {
            MessageUtil.showSuccess("Category deleted successfully.");
            clearCategoryForm();
            loadCategories();
        } else {
            MessageUtil.showError("Failed to delete category. It may be used by existing products.");
        }
    }

    private void loadCategories() {
        List<Category> categories = categoryDAO.getAllCategories();
        fillCategoryTable(categories);
    }

    private void searchCategories() {
        String keyword = categorySearchField.getText().trim();

        if (ValidationUtil.isEmpty(keyword)) {
            loadCategories();
        } else {
            fillCategoryTable(categoryDAO.searchCategories(keyword));
        }
    }

    private void fillCategoryTable(List<Category> categories) {
        TableUtil.clearTable(categoryTableModel);

        for (Category category : categories) {
            categoryTableModel.addRow(new Object[]{
                    category.getCategoryId(),
                    category.getCategoryName(),
                    category.getDescription(),
                    category.getCategoryType()
            });
        }
    }

    private void fillCategoryFormFromTable() {
        int selectedRow = categoryTable.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        int modelRow = categoryTable.convertRowIndexToModel(selectedRow);

        categoryIdField.setText(categoryTableModel.getValueAt(modelRow, 0).toString());
        categoryNameField.setText(categoryTableModel.getValueAt(modelRow, 1).toString());
        categoryDescriptionField.setText(String.valueOf(categoryTableModel.getValueAt(modelRow, 2)));
        categoryTypeField.setText(String.valueOf(categoryTableModel.getValueAt(modelRow, 3)));
    }

    private void clearCategoryForm() {
        categoryIdField.setText("");
        categoryNameField.setText("");
        categoryDescriptionField.setText("");
        categoryTypeField.setText("");
        categoryTable.clearSelection();
    }

    private void addBrand() {
        if (ValidationUtil.isEmpty(brandNameField.getText())) {
            MessageUtil.showError("Brand name is required.");
            return;
        }

        Brand brand = new Brand(
                brandNameField.getText().trim(),
                brandDescriptionField.getText().trim()
        );

        if (brandDAO.addBrand(brand)) {
            MessageUtil.showSuccess("Brand added successfully.");
            clearBrandForm();
            loadBrands();
        } else {
            MessageUtil.showError("Failed to add brand.");
        }
    }

    private void updateBrand() {
        if (ValidationUtil.isEmpty(brandIdField.getText())) {
            MessageUtil.showWarning("Select a brand to update.");
            return;
        }

        if (ValidationUtil.isEmpty(brandNameField.getText())) {
            MessageUtil.showError("Brand name is required.");
            return;
        }

        Brand brand = new Brand(
                Integer.parseInt(brandIdField.getText()),
                brandNameField.getText().trim(),
                brandDescriptionField.getText().trim()
        );

        if (brandDAO.updateBrand(brand)) {
            MessageUtil.showSuccess("Brand updated successfully.");
            clearBrandForm();
            loadBrands();
        } else {
            MessageUtil.showError("Failed to update brand.");
        }
    }

    private void deleteBrand() {
        if (ValidationUtil.isEmpty(brandIdField.getText())) {
            MessageUtil.showWarning("Select a brand to delete.");
            return;
        }

        if (!MessageUtil.confirm("Are you sure you want to delete this brand?")) {
            return;
        }

        int brandId = Integer.parseInt(brandIdField.getText());

        if (brandDAO.deleteBrand(brandId)) {
            MessageUtil.showSuccess("Brand deleted successfully.");
            clearBrandForm();
            loadBrands();
        } else {
            MessageUtil.showError("Failed to delete brand. It may be used by existing products.");
        }
    }

    private void loadBrands() {
        List<Brand> brands = brandDAO.getAllBrands();
        fillBrandTable(brands);
    }

    private void searchBrands() {
        String keyword = brandSearchField.getText().trim();

        if (ValidationUtil.isEmpty(keyword)) {
            loadBrands();
        } else {
            fillBrandTable(brandDAO.searchBrands(keyword));
        }
    }

    private void fillBrandTable(List<Brand> brands) {
        TableUtil.clearTable(brandTableModel);

        for (Brand brand : brands) {
            brandTableModel.addRow(new Object[]{
                    brand.getBrandId(),
                    brand.getBrandName(),
                    brand.getDescription()
            });
        }
    }

    private void fillBrandFormFromTable() {
        int selectedRow = brandTable.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        int modelRow = brandTable.convertRowIndexToModel(selectedRow);

        brandIdField.setText(brandTableModel.getValueAt(modelRow, 0).toString());
        brandNameField.setText(brandTableModel.getValueAt(modelRow, 1).toString());
        brandDescriptionField.setText(String.valueOf(brandTableModel.getValueAt(modelRow, 2)));
    }

    private void clearBrandForm() {
        brandIdField.setText("");
        brandNameField.setText("");
        brandDescriptionField.setText("");
        brandTable.clearSelection();
    }
}