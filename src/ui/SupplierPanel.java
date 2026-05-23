package ui;

import dao.ProductDAO;
import dao.SupplierDAO;
import dao.SupplierProductDAO;
import model.Product;
import model.Supplier;
import model.SupplierProduct;
import util.MessageUtil;
import util.TableUtil;
import util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class SupplierPanel extends JPanel {

    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final SupplierProductDAO supplierProductDAO = new SupplierProductDAO();

    private JTextField supplierIdField;
    private JTextField supplierNameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField startingDateField;
    private JTextField cityField;
    private JTextField addressField;
    private JTextField searchField;

    private JTable supplierTable;
    private DefaultTableModel supplierTableModel;

    private JComboBox<Supplier> supplierComboBox;
    private JComboBox<Product> productComboBox;
    private JTextField supplyPriceField;

    private JTable supplierProductTable;
    private DefaultTableModel supplierProductTableModel;

    public SupplierPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIStyle.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        loadSuppliers();
        loadComboBoxes();
        loadSupplierProductLinks();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setBackground(UIStyle.BACKGROUND);

        panel.add(UIStyle.createTitle("Supplier Management"));
        panel.add(UIStyle.createSubtitle("Manage suppliers and link them to the products they provide."));

        return panel;
    }

    private JSplitPane createMainPanel() {
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                createSupplierManagementPanel(),
                createSupplierProductPanel()
        );

        splitPane.setResizeWeight(0.50);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);

        return splitPane;
    }

    private JPanel createSupplierManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIStyle.PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = UIStyle.createTitle("Suppliers");

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 8, 8));
        formPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        supplierIdField = new JTextField();
        supplierIdField.setEditable(false);

        supplierNameField = new JTextField();
        phoneField = new JTextField();
        emailField = new JTextField();
        startingDateField = new JTextField();
        cityField = new JTextField();
        addressField = new JTextField();

        UIStyle.styleTextField(supplierIdField);
        UIStyle.styleTextField(supplierNameField);
        UIStyle.styleTextField(phoneField);
        UIStyle.styleTextField(emailField);
        UIStyle.styleTextField(startingDateField);
        UIStyle.styleTextField(cityField);
        UIStyle.styleTextField(addressField);

        startingDateField.setToolTipText("Format: YYYY-MM-DD");

        formPanel.add(new JLabel("Supplier ID:"));
        formPanel.add(supplierIdField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(supplierNameField);
        formPanel.add(new JLabel("Phone:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Starting Date:"));
        formPanel.add(startingDateField);
        formPanel.add(new JLabel("City:"));
        formPanel.add(cityField);
        formPanel.add(new JLabel("Address:"));
        formPanel.add(addressField);

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

        addButton.addActionListener(e -> addSupplier());
        updateButton.addActionListener(e -> updateSupplier());
        deleteButton.addActionListener(e -> deleteSupplier());
        clearButton.addActionListener(e -> clearSupplierForm());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        JPanel searchPanel = new JPanel(new BorderLayout(8, 8));
        searchPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        searchField = new JTextField();
        UIStyle.styleTextField(searchField);

        JButton searchButton = new JButton("Search");
        JButton refreshButton = new JButton("Refresh");

        UIStyle.stylePrimaryButton(searchButton);
        UIStyle.stylePrimaryButton(refreshButton);

        searchButton.addActionListener(e -> searchSuppliers());
        refreshButton.addActionListener(e -> {
            loadSuppliers();
            loadComboBoxes();
            clearSupplierForm();
        });

        JPanel searchButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchButtons.setBackground(UIStyle.PANEL_BACKGROUND);
        searchButtons.add(searchButton);
        searchButtons.add(refreshButton);

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButtons, BorderLayout.EAST);

        String[] columns = {"ID", "Name", "Phone", "Email", "Starting Date", "City", "Address"};
        supplierTableModel = TableUtil.createNonEditableTableModel(columns);
        supplierTable = new JTable(supplierTableModel);
        TableUtil.setupTable(supplierTable);

        supplierTable.getSelectionModel().addListSelectionListener(e -> fillSupplierFormFromTable());

        JScrollPane tableScrollPane = new JScrollPane(supplierTable);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(UIStyle.PANEL_BACKGROUND);
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSupplierProductPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIStyle.PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = UIStyle.createTitle("Supplier Products");

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        formPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        supplierComboBox = new JComboBox<>();
        productComboBox = new JComboBox<>();
        supplyPriceField = new JTextField();

        UIStyle.styleComboBox(supplierComboBox);
        UIStyle.styleComboBox(productComboBox);
        UIStyle.styleTextField(supplyPriceField);

        formPanel.add(new JLabel("Supplier:"));
        formPanel.add(supplierComboBox);
        formPanel.add(new JLabel("Product:"));
        formPanel.add(productComboBox);
        formPanel.add(new JLabel("Supply Price:"));
        formPanel.add(supplyPriceField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        JButton linkButton = new JButton("Link");
        JButton updatePriceButton = new JButton("Update Price");
        JButton removeButton = new JButton("Remove Link");
        JButton refreshButton = new JButton("Refresh");

        UIStyle.stylePrimaryButton(linkButton);
        UIStyle.stylePrimaryButton(updatePriceButton);
        UIStyle.stylePrimaryButton(removeButton);
        UIStyle.stylePrimaryButton(refreshButton);

        linkButton.addActionListener(e -> linkSupplierToProduct());
        updatePriceButton.addActionListener(e -> updateSupplyPrice());
        removeButton.addActionListener(e -> removeSupplierProduct());
        refreshButton.addActionListener(e -> {
            loadComboBoxes();
            loadSupplierProductLinks();
            clearSupplierProductForm();
        });

        buttonPanel.add(linkButton);
        buttonPanel.add(updatePriceButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(refreshButton);

        String[] columns = {"Supplier ID", "Supplier", "Product ID", "Product", "Supply Price"};
        supplierProductTableModel = TableUtil.createNonEditableTableModel(columns);
        supplierProductTable = new JTable(supplierProductTableModel);
        TableUtil.setupTable(supplierProductTable);

        supplierProductTable.getSelectionModel().addListSelectionListener(e -> fillSupplierProductFormFromTable());

        JScrollPane tableScrollPane = new JScrollPane(supplierProductTable);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(UIStyle.PANEL_BACKGROUND);
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadSuppliers() {
        fillSupplierTable(supplierDAO.getAllSuppliers());
    }

    private void fillSupplierTable(List<Supplier> suppliers) {
        TableUtil.clearTable(supplierTableModel);

        for (Supplier supplier : suppliers) {
            supplierTableModel.addRow(new Object[]{
                    supplier.getSupplierId(),
                    supplier.getSupplierName(),
                    supplier.getPhone(),
                    supplier.getEmail(),
                    supplier.getStartingDate(),
                    supplier.getCity(),
                    supplier.getAddress()
            });
        }
    }

    private void searchSuppliers() {
        String keyword = searchField.getText().trim();

        if (ValidationUtil.isEmpty(keyword)) {
            loadSuppliers();
        } else {
            fillSupplierTable(supplierDAO.searchSuppliers(keyword));
        }
    }

    private void addSupplier() {
        if (!validateSupplierInput()) {
            return;
        }

        Supplier supplier = buildSupplierFromForm(false);

        if (supplierDAO.addSupplier(supplier)) {
            MessageUtil.showSuccess("Supplier added successfully.");
            clearSupplierForm();
            loadSuppliers();
            loadComboBoxes();
        } else {
            MessageUtil.showError("Failed to add supplier.");
        }
    }

    private void updateSupplier() {
        if (ValidationUtil.isEmpty(supplierIdField.getText())) {
            MessageUtil.showWarning("Select a supplier to update.");
            return;
        }

        if (!validateSupplierInput()) {
            return;
        }

        Supplier supplier = buildSupplierFromForm(true);

        if (supplierDAO.updateSupplier(supplier)) {
            MessageUtil.showSuccess("Supplier updated successfully.");
            clearSupplierForm();
            loadSuppliers();
            loadComboBoxes();
            loadSupplierProductLinks();
        } else {
            MessageUtil.showError("Failed to update supplier.");
        }
    }

    private void deleteSupplier() {
        if (ValidationUtil.isEmpty(supplierIdField.getText())) {
            MessageUtil.showWarning("Select a supplier to delete.");
            return;
        }

        if (!MessageUtil.confirm("Are you sure you want to delete this supplier?")) {
            return;
        }

        int supplierId = Integer.parseInt(supplierIdField.getText());

        if (supplierDAO.deleteSupplier(supplierId)) {
            MessageUtil.showSuccess("Supplier deleted successfully.");
            clearSupplierForm();
            loadSuppliers();
            loadComboBoxes();
            loadSupplierProductLinks();
        } else {
            MessageUtil.showError("Failed to delete supplier. It may be used in supplier-product links or purchase invoices.");
        }
    }

    private boolean validateSupplierInput() {
        if (ValidationUtil.isEmpty(supplierNameField.getText())) {
            MessageUtil.showError("Supplier name is required.");
            return false;
        }

        if (!ValidationUtil.isValidEmail(emailField.getText())) {
            MessageUtil.showError("Invalid email format.");
            return false;
        }

        if (!ValidationUtil.isEmpty(startingDateField.getText())) {
            try {
                LocalDate.parse(startingDateField.getText().trim());
            } catch (DateTimeParseException e) {
                MessageUtil.showError("Starting date must be in YYYY-MM-DD format.");
                return false;
            }
        }

        return true;
    }

    private Supplier buildSupplierFromForm(boolean includeId) {
        LocalDate startingDate = null;

        if (!ValidationUtil.isEmpty(startingDateField.getText())) {
            startingDate = LocalDate.parse(startingDateField.getText().trim());
        }

        Supplier supplier = new Supplier(
                supplierNameField.getText().trim(),
                phoneField.getText().trim(),
                emailField.getText().trim(),
                startingDate,
                cityField.getText().trim(),
                addressField.getText().trim()
        );

        if (includeId) {
            supplier.setSupplierId(Integer.parseInt(supplierIdField.getText()));
        }

        return supplier;
    }

    private void fillSupplierFormFromTable() {
        int selectedRow = supplierTable.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        int modelRow = supplierTable.convertRowIndexToModel(selectedRow);

        supplierIdField.setText(String.valueOf(supplierTableModel.getValueAt(modelRow, 0)));
        supplierNameField.setText(String.valueOf(supplierTableModel.getValueAt(modelRow, 1)));
        phoneField.setText(String.valueOf(supplierTableModel.getValueAt(modelRow, 2)));
        emailField.setText(String.valueOf(supplierTableModel.getValueAt(modelRow, 3)));
        startingDateField.setText(String.valueOf(supplierTableModel.getValueAt(modelRow, 4)));
        cityField.setText(String.valueOf(supplierTableModel.getValueAt(modelRow, 5)));
        addressField.setText(String.valueOf(supplierTableModel.getValueAt(modelRow, 6)));
    }

    private void clearSupplierForm() {
        supplierIdField.setText("");
        supplierNameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        startingDateField.setText("");
        cityField.setText("");
        addressField.setText("");
        supplierTable.clearSelection();
    }

    private void loadComboBoxes() {
        supplierComboBox.removeAllItems();
        for (Supplier supplier : supplierDAO.getAllSuppliers()) {
            supplierComboBox.addItem(supplier);
        }

        productComboBox.removeAllItems();
        for (Product product : productDAO.getAllProducts()) {
            productComboBox.addItem(product);
        }
    }

    private void loadSupplierProductLinks() {
        fillSupplierProductTable(supplierProductDAO.getAllSupplierProducts());
    }

    private void fillSupplierProductTable(List<SupplierProduct> links) {
        TableUtil.clearTable(supplierProductTableModel);

        for (SupplierProduct link : links) {
            supplierProductTableModel.addRow(new Object[]{
                    link.getSupplierId(),
                    link.getSupplierName(),
                    link.getProductId(),
                    link.getProductName(),
                    link.getSupplyPrice()
            });
        }
    }

    private void linkSupplierToProduct() {
        Supplier supplier = (Supplier) supplierComboBox.getSelectedItem();
        Product product = (Product) productComboBox.getSelectedItem();

        if (supplier == null || product == null) {
            MessageUtil.showWarning("Select both supplier and product.");
            return;
        }

        if (!ValidationUtil.isNonNegativeDecimal(supplyPriceField.getText())) {
            MessageUtil.showError("Supply price must be a valid non-negative number.");
            return;
        }

        BigDecimal supplyPrice = new BigDecimal(supplyPriceField.getText().trim());

        if (supplierProductDAO.supplierProductExists(supplier.getSupplierId(), product.getProductId())) {
            MessageUtil.showWarning("This supplier is already linked to this product. Use Update Price instead.");
            return;
        }

        if (supplierProductDAO.linkSupplierToProduct(supplier.getSupplierId(), product.getProductId(), supplyPrice)) {
            MessageUtil.showSuccess("Supplier linked to product successfully.");
            clearSupplierProductForm();
            loadSupplierProductLinks();
        } else {
            MessageUtil.showError("Failed to link supplier to product.");
        }
    }

    private void updateSupplyPrice() {
        Supplier supplier = (Supplier) supplierComboBox.getSelectedItem();
        Product product = (Product) productComboBox.getSelectedItem();

        if (supplier == null || product == null) {
            MessageUtil.showWarning("Select both supplier and product.");
            return;
        }

        if (!ValidationUtil.isNonNegativeDecimal(supplyPriceField.getText())) {
            MessageUtil.showError("Supply price must be a valid non-negative number.");
            return;
        }

        BigDecimal supplyPrice = new BigDecimal(supplyPriceField.getText().trim());

        if (!supplierProductDAO.supplierProductExists(supplier.getSupplierId(), product.getProductId())) {
            MessageUtil.showWarning("This link does not exist. Use Link first.");
            return;
        }

        if (supplierProductDAO.updateSupplyPrice(supplier.getSupplierId(), product.getProductId(), supplyPrice)) {
            MessageUtil.showSuccess("Supply price updated successfully.");
            clearSupplierProductForm();
            loadSupplierProductLinks();
        } else {
            MessageUtil.showError("Failed to update supply price.");
        }
    }

    private void removeSupplierProduct() {
        Supplier supplier = (Supplier) supplierComboBox.getSelectedItem();
        Product product = (Product) productComboBox.getSelectedItem();

        if (supplier == null || product == null) {
            MessageUtil.showWarning("Select both supplier and product.");
            return;
        }

        if (!MessageUtil.confirm("Remove this supplier-product link?")) {
            return;
        }

        if (supplierProductDAO.removeSupplierProduct(supplier.getSupplierId(), product.getProductId())) {
            MessageUtil.showSuccess("Supplier-product link removed successfully.");
            clearSupplierProductForm();
            loadSupplierProductLinks();
        } else {
            MessageUtil.showError("Failed to remove supplier-product link.");
        }
    }

    private void fillSupplierProductFormFromTable() {
        int selectedRow = supplierProductTable.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        int modelRow = supplierProductTable.convertRowIndexToModel(selectedRow);

        int supplierId = Integer.parseInt(String.valueOf(supplierProductTableModel.getValueAt(modelRow, 0)));
        int productId = Integer.parseInt(String.valueOf(supplierProductTableModel.getValueAt(modelRow, 2)));

        selectSupplierById(supplierId);
        selectProductById(productId);

        supplyPriceField.setText(String.valueOf(supplierProductTableModel.getValueAt(modelRow, 4)));
    }

    private void selectSupplierById(int supplierId) {
        for (int i = 0; i < supplierComboBox.getItemCount(); i++) {
            Supplier supplier = supplierComboBox.getItemAt(i);
            if (supplier.getSupplierId() == supplierId) {
                supplierComboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectProductById(int productId) {
        for (int i = 0; i < productComboBox.getItemCount(); i++) {
            Product product = productComboBox.getItemAt(i);
            if (product.getProductId() == productId) {
                productComboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void clearSupplierProductForm() {
        if (supplierComboBox.getItemCount() > 0) {
            supplierComboBox.setSelectedIndex(0);
        }

        if (productComboBox.getItemCount() > 0) {
            productComboBox.setSelectedIndex(0);
        }

        supplyPriceField.setText("");
        supplierProductTable.clearSelection();
    }
}