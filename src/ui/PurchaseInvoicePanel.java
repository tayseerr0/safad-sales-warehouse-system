package ui;

import dao.ProductDAO;
import dao.PurchaseInvoiceDAO;
import dao.SupplierDAO;
import db.DBConnection;
import model.Product;
import model.PurchaseInvoice;
import model.PurchaseInvoiceItem;
import model.Supplier;
import util.MessageUtil;
import util.TableUtil;
import util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class PurchaseInvoicePanel extends JPanel {

    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final PurchaseInvoiceDAO purchaseInvoiceDAO = new PurchaseInvoiceDAO();

    private int editingInvoiceId = -1;

    private JComboBox<Supplier> supplierComboBox;
    private JComboBox<WarehouseOption> warehouseComboBox;
    private JTextField invoiceDateField;
    private JTextField estimatedArrivalField;
    private JTextField paymentField;
    private JComboBox<String> paymentTypeComboBox;

    private JComboBox<Product> productComboBox;
    private JTextField quantityField;
    private JTextField purchasePriceField;

    private JTable itemTable;
    private DefaultTableModel itemTableModel;

    private JTable invoiceTable;
    private DefaultTableModel invoiceTableModel;

    private JTable previousItemsTable;
    private DefaultTableModel previousItemsTableModel;

    private JLabel totalAmountLabel;
    private JLabel modeLabel;

    private final List<PurchaseInvoiceItem> currentItems = new ArrayList<>();

    public PurchaseInvoicePanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIStyle.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        loadComboBoxes();
        loadPurchaseInvoices();
        updateTotalAmount();
    }

    private JPanel createHeaderPanel() {
        return UIStyle.createHeaderPanel(
                "Purchase Invoices",
                "Create, view, and update supplier purchase invoices."
        );
    }

    private JSplitPane createMainPanel() {
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                createInvoiceCreationPanel(),
                createInvoiceHistoryPanel()
        );

        UIStyle.styleSplitPane(splitPane, 0.52);

        return splitPane;
    }

    private JPanel createInvoiceCreationPanel() {
        JPanel panel = UIStyle.createCardPanel();

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(UIStyle.PANEL_BACKGROUND);
        topPanel.add(createInvoiceHeaderForm(), BorderLayout.NORTH);
        topPanel.add(createItemForm(), BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(createCurrentItemsTablePanel(), BorderLayout.CENTER);
        panel.add(createSavePanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createInvoiceHeaderForm() {
        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        wrapper.setBackground(UIStyle.PANEL_BACKGROUND);

        modeLabel = new JLabel("Mode: New Purchase Invoice");
        modeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        modeLabel.setForeground(UIStyle.TEXT_DARK);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 8, 8));
        formPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        supplierComboBox = new JComboBox<>();
        warehouseComboBox = new JComboBox<>();

        invoiceDateField = new JTextField(LocalDate.now().toString());
        estimatedArrivalField = new JTextField(LocalDate.now().plusDays(5).toString());
        paymentField = new JTextField("0.00");
        paymentTypeComboBox = new JComboBox<>(new String[]{"Cash", "Card", "Bank Transfer", "Cheque"});

        UIStyle.styleComboBox(supplierComboBox);
        UIStyle.styleComboBox(warehouseComboBox);
        UIStyle.styleTextField(invoiceDateField);
        UIStyle.styleTextField(estimatedArrivalField);
        UIStyle.styleTextField(paymentField);
        UIStyle.styleComboBox(paymentTypeComboBox);

        formPanel.add(new JLabel("Supplier:"));
        formPanel.add(supplierComboBox);
        formPanel.add(new JLabel("Warehouse:"));
        formPanel.add(warehouseComboBox);
        formPanel.add(new JLabel("Invoice Date:"));
        formPanel.add(invoiceDateField);
        formPanel.add(new JLabel("Estimated Arrival:"));
        formPanel.add(estimatedArrivalField);
        formPanel.add(new JLabel("Payment:"));
        formPanel.add(paymentField);
        formPanel.add(new JLabel("Payment Type:"));
        formPanel.add(paymentTypeComboBox);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIStyle.PANEL_BACKGROUND);
        headerPanel.add(UIStyle.createSectionHeader("Invoice Details"), BorderLayout.WEST);
        headerPanel.add(modeLabel, BorderLayout.EAST);

        wrapper.add(headerPanel, BorderLayout.NORTH);
        wrapper.add(formPanel, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel createItemForm() {
        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        wrapper.setBackground(UIStyle.PANEL_BACKGROUND);
        wrapper.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        productComboBox = new JComboBox<>();
        quantityField = new JTextField();
        purchasePriceField = new JTextField();

        UIStyle.styleComboBox(productComboBox);
        UIStyle.styleTextField(quantityField);
        UIStyle.styleTextField(purchasePriceField);

        formPanel.add(new JLabel("Product:"));
        formPanel.add(productComboBox);
        formPanel.add(new JLabel("Quantity:"));
        formPanel.add(quantityField);
        formPanel.add(new JLabel("Purchase Price:"));
        formPanel.add(purchasePriceField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        JButton addItemButton = new JButton("Add Item");
        JButton removeItemButton = new JButton("Remove Item");
        JButton clearItemsButton = new JButton("Clear Items");

        UIStyle.stylePrimaryButton(addItemButton);
        UIStyle.styleSecondaryButton(removeItemButton);
        UIStyle.styleSecondaryButton(clearItemsButton);

        addItemButton.addActionListener(e -> addItem());
        removeItemButton.addActionListener(e -> removeSelectedItem());
        clearItemsButton.addActionListener(e -> clearItems());

        buttonPanel.add(addItemButton);
        buttonPanel.add(removeItemButton);
        buttonPanel.add(clearItemsButton);

        wrapper.add(UIStyle.createSectionHeader("Invoice Items"), BorderLayout.NORTH);
        wrapper.add(formPanel, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        return wrapper;
    }

    private JPanel createCurrentItemsTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UIStyle.PANEL_BACKGROUND);

        String[] columns = {"Product ID", "Product", "Quantity", "Purchase Price", "Line Total"};
        itemTableModel = TableUtil.createNonEditableTableModel(columns);
        itemTable = new JTable(itemTableModel);
        TableUtil.setupTable(itemTable);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setBackground(UIStyle.PANEL_BACKGROUND);
        topPanel.add(TableUtil.createColumnVisibilityButton(itemTable, "Columns"));

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(UIStyle.createTableScrollPane(itemTable), BorderLayout.CENTER);

        totalAmountLabel = new JLabel("Total Amount: 0.00");
        totalAmountLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        panel.add(totalAmountLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSavePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(UIStyle.PANEL_BACKGROUND);

        JButton saveButton = new JButton("Save New");
        JButton updateButton = new JButton("Update Existing");
        JButton newButton = new JButton("New Invoice");
        JButton refreshButton = new JButton("Refresh");

        UIStyle.stylePrimaryButton(saveButton);
        UIStyle.stylePrimaryButton(updateButton);
        UIStyle.styleSecondaryButton(newButton);
        UIStyle.styleSecondaryButton(refreshButton);

        saveButton.addActionListener(e -> saveNewInvoice());
        updateButton.addActionListener(e -> updateExistingInvoice());
        newButton.addActionListener(e -> clearForm());
        refreshButton.addActionListener(e -> {
            loadComboBoxes();
            loadPurchaseInvoices();
            loadSelectedInvoiceItems();
        });

        panel.add(saveButton);
        panel.add(updateButton);
        panel.add(newButton);
        panel.add(refreshButton);

        return panel;
    }

    private JPanel createInvoiceHistoryPanel() {
        JPanel panel = UIStyle.createCardPanel();

        JPanel title = UIStyle.createSectionHeader("Purchase Invoice History");

        String[] invoiceColumns = {"Invoice ID", "Date", "Estimated Arrival", "Supplier", "Warehouse", "Payment", "Payment Type", "Amount"};
        invoiceTableModel = TableUtil.createNonEditableTableModel(invoiceColumns);
        invoiceTable = new JTable(invoiceTableModel);
        TableUtil.setupTable(invoiceTable);

        invoiceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedInvoiceItems();
            }
        });

        String[] itemColumns = {"Item ID", "Product ID", "Product", "Quantity", "Purchase Price", "Line Total"};
        previousItemsTableModel = TableUtil.createNonEditableTableModel(itemColumns);
        previousItemsTable = new JTable(previousItemsTableModel);
        TableUtil.setupTable(previousItemsTable);

        JButton loadForEditButton = new JButton("Load Selected for Edit");
        UIStyle.stylePrimaryButton(loadForEditButton);
        loadForEditButton.addActionListener(e -> loadSelectedInvoiceForEdit());

        JPanel invoiceButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        invoiceButtons.setBackground(UIStyle.PANEL_BACKGROUND);
        invoiceButtons.add(TableUtil.createColumnVisibilityButton(invoiceTable, "Invoice Columns"));
        invoiceButtons.add(loadForEditButton);

        JPanel invoicePanel = new JPanel(new BorderLayout(8, 8));
        invoicePanel.setBackground(UIStyle.PANEL_BACKGROUND);
        invoicePanel.add(invoiceButtons, BorderLayout.NORTH);
        invoicePanel.add(UIStyle.createTableScrollPane(invoiceTable), BorderLayout.CENTER);

        JPanel itemButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        itemButtons.setBackground(UIStyle.PANEL_BACKGROUND);
        itemButtons.add(UIStyle.createSectionHeader("Selected Invoice Items"));
        itemButtons.add(TableUtil.createColumnVisibilityButton(previousItemsTable, "Item Columns"));

        JPanel itemPanel = new JPanel(new BorderLayout(8, 8));
        itemPanel.setBackground(UIStyle.PANEL_BACKGROUND);
        itemPanel.add(itemButtons, BorderLayout.NORTH);
        itemPanel.add(UIStyle.createTableScrollPane(previousItemsTable), BorderLayout.CENTER);

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, invoicePanel, itemPanel);
        UIStyle.styleSplitPane(verticalSplit, 0.55);

        panel.add(title, BorderLayout.NORTH);
        panel.add(verticalSplit, BorderLayout.CENTER);

        return panel;
    }

    private void loadComboBoxes() {
        supplierComboBox.removeAllItems();
        for (Supplier supplier : supplierDAO.getAllSuppliers()) supplierComboBox.addItem(supplier);

        productComboBox.removeAllItems();
        for (Product product : productDAO.getAllProducts()) productComboBox.addItem(product);

        warehouseComboBox.removeAllItems();
        for (WarehouseOption warehouse : loadWarehouses()) warehouseComboBox.addItem(warehouse);
    }

    private List<WarehouseOption> loadWarehouses() {
        List<WarehouseOption> warehouses = new ArrayList<>();
        String sql = "SELECT warehouse_id, warehouse_name FROM Warehouse ORDER BY warehouse_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) warehouses.add(new WarehouseOption(rs.getInt("warehouse_id"), rs.getString("warehouse_name")));

        } catch (SQLException e) {
            System.out.println("Error loading warehouses: " + e.getMessage());
        }

        return warehouses;
    }

    private void loadPurchaseInvoices() {
        TableUtil.clearTable(invoiceTableModel);

        for (PurchaseInvoice invoice : purchaseInvoiceDAO.getAllPurchaseInvoices()) {
            invoiceTableModel.addRow(new Object[]{
                    invoice.getPurchaseInvoiceId(),
                    invoice.getInvoiceDate(),
                    invoice.getEstimatedArrival(),
                    invoice.getSupplierName(),
                    invoice.getWarehouseName(),
                    invoice.getPayment(),
                    invoice.getPaymentType(),
                    invoice.getAmount()
            });
        }
    }

    private void loadSelectedInvoiceItems() {
        if (previousItemsTableModel == null || invoiceTable == null) return;

        int selectedRow = invoiceTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = invoiceTable.convertRowIndexToModel(selectedRow);
        int invoiceId = Integer.parseInt(invoiceTableModel.getValueAt(modelRow, 0).toString());

        TableUtil.clearTable(previousItemsTableModel);

        for (PurchaseInvoiceItem item : purchaseInvoiceDAO.getPurchaseInvoiceItems(invoiceId)) {
            BigDecimal lineTotal = item.getPurchasePrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            previousItemsTableModel.addRow(new Object[]{
                    item.getPurchaseItemId(),
                    item.getProductId(),
                    item.getProductName(),
                    item.getQuantity(),
                    item.getPurchasePrice(),
                    lineTotal
            });
        }
    }

    private void loadSelectedInvoiceForEdit() {
        int selectedRow = invoiceTable.getSelectedRow();

        if (selectedRow == -1) {
            MessageUtil.showWarning("Select an invoice first.");
            return;
        }

        int modelRow = invoiceTable.convertRowIndexToModel(selectedRow);
        int invoiceId = Integer.parseInt(invoiceTableModel.getValueAt(modelRow, 0).toString());

        PurchaseInvoice invoice = purchaseInvoiceDAO.getPurchaseInvoiceById(invoiceId);

        if (invoice == null) {
            MessageUtil.showError("Could not load selected invoice.");
            return;
        }

        editingInvoiceId = invoice.getPurchaseInvoiceId();
        modeLabel.setText("Mode: Editing Purchase Invoice #" + editingInvoiceId);

        selectSupplierById(invoice.getSupplierId());
        selectWarehouseById(invoice.getWarehouseId());

        invoiceDateField.setText(invoice.getInvoiceDate().toString());
        estimatedArrivalField.setText(invoice.getEstimatedArrival() != null ? invoice.getEstimatedArrival().toString() : "");
        paymentField.setText(invoice.getPayment().toString());
        paymentTypeComboBox.setSelectedItem(invoice.getPaymentType());

        currentItems.clear();
        TableUtil.clearTable(itemTableModel);

        for (PurchaseInvoiceItem item : invoice.getItems()) {
            PurchaseInvoiceItem copy = new PurchaseInvoiceItem(item.getProductId(), item.getPurchasePrice(), item.getQuantity());
            copy.setProductName(item.getProductName());
            currentItems.add(copy);

            BigDecimal lineTotal = item.getPurchasePrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            itemTableModel.addRow(new Object[]{
                    item.getProductId(),
                    item.getProductName(),
                    item.getQuantity(),
                    item.getPurchasePrice(),
                    lineTotal
            });
        }

        updateTotalAmount();
    }

    private void addItem() {
        Product product = (Product) productComboBox.getSelectedItem();

        if (product == null) {
            MessageUtil.showWarning("Select a product.");
            return;
        }

        if (!ValidationUtil.isPositiveInteger(quantityField.getText())) {
            MessageUtil.showError("Quantity must be a positive integer.");
            return;
        }

        if (!ValidationUtil.isNonNegativeDecimal(purchasePriceField.getText())) {
            MessageUtil.showError("Purchase price must be a valid non-negative number.");
            return;
        }

        int quantity = Integer.parseInt(quantityField.getText().trim());
        BigDecimal purchasePrice = new BigDecimal(purchasePriceField.getText().trim());

        PurchaseInvoiceItem item = new PurchaseInvoiceItem(product.getProductId(), purchasePrice, quantity);
        item.setProductName(product.getProductName());
        currentItems.add(item);

        itemTableModel.addRow(new Object[]{
                product.getProductId(),
                product.getProductName(),
                quantity,
                purchasePrice,
                purchasePrice.multiply(BigDecimal.valueOf(quantity))
        });

        quantityField.setText("");
        purchasePriceField.setText("");
        updateTotalAmount();
    }

    private void removeSelectedItem() {
        int selectedRow = itemTable.getSelectedRow();

        if (selectedRow == -1) {
            MessageUtil.showWarning("Select an item to remove.");
            return;
        }

        int modelRow = itemTable.convertRowIndexToModel(selectedRow);
        currentItems.remove(modelRow);
        itemTableModel.removeRow(modelRow);
        updateTotalAmount();
    }

    private void clearItems() {
        currentItems.clear();
        TableUtil.clearTable(itemTableModel);
        updateTotalAmount();
    }

    private void saveNewInvoice() {
        PurchaseInvoice invoice = buildInvoiceFromForm(-1);
        if (invoice == null) return;

        if (purchaseInvoiceDAO.createPurchaseInvoice(invoice)) {
            MessageUtil.showSuccess("Purchase invoice saved successfully. Inventory increased.");
            clearForm();
            loadPurchaseInvoices();
        } else {
            MessageUtil.showError("Failed to save purchase invoice.");
        }
    }

    private void updateExistingInvoice() {
        if (editingInvoiceId <= 0) {
            MessageUtil.showWarning("Load an existing invoice first.");
            return;
        }

        if (!MessageUtil.confirm("Update this invoice? Inventory will be adjusted based on the changes.")) return;

        PurchaseInvoice invoice = buildInvoiceFromForm(editingInvoiceId);
        if (invoice == null) return;

        if (purchaseInvoiceDAO.updatePurchaseInvoice(invoice)) {
            MessageUtil.showSuccess("Purchase invoice updated successfully. Inventory adjusted.");
            clearForm();
            loadPurchaseInvoices();
            TableUtil.clearTable(previousItemsTableModel);
        } else {
            MessageUtil.showError("Failed to update invoice. Old purchased stock may already have been used by sales/transfers.");
        }
    }

    private PurchaseInvoice buildInvoiceFromForm(int invoiceId) {
        Supplier supplier = (Supplier) supplierComboBox.getSelectedItem();
        WarehouseOption warehouse = (WarehouseOption) warehouseComboBox.getSelectedItem();

        if (supplier == null) {
            MessageUtil.showError("Select a supplier.");
            return null;
        }

        if (warehouse == null) {
            MessageUtil.showError("Select a warehouse.");
            return null;
        }

        if (currentItems.isEmpty()) {
            MessageUtil.showError("Add at least one item.");
            return null;
        }

        LocalDate invoiceDate;
        LocalDate estimatedArrival = null;

        try {
            invoiceDate = LocalDate.parse(invoiceDateField.getText().trim());
            if (!ValidationUtil.isEmpty(estimatedArrivalField.getText())) {
                estimatedArrival = LocalDate.parse(estimatedArrivalField.getText().trim());
            }
        } catch (DateTimeParseException e) {
            MessageUtil.showError("Dates must be in YYYY-MM-DD format.");
            return null;
        }

        if (!ValidationUtil.isNonNegativeDecimal(paymentField.getText())) {
            MessageUtil.showError("Payment must be a valid non-negative number.");
            return null;
        }

        BigDecimal payment = new BigDecimal(paymentField.getText().trim());
        BigDecimal amount = calculateTotalAmount();

        PurchaseInvoice invoice = new PurchaseInvoice(
                invoiceDate,
                estimatedArrival,
                payment,
                paymentTypeComboBox.getSelectedItem().toString(),
                amount,
                supplier.getSupplierId(),
                warehouse.getWarehouseId(),
                new ArrayList<>(currentItems)
        );

        if (invoiceId > 0) invoice.setPurchaseInvoiceId(invoiceId);

        return invoice;
    }

    private BigDecimal calculateTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseInvoiceItem item : currentItems) {
            total = total.add(item.getPurchasePrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return total;
    }

    private void updateTotalAmount() {
        totalAmountLabel.setText("Total Amount: " + calculateTotalAmount());
    }

    private void clearForm() {
        editingInvoiceId = -1;
        modeLabel.setText("Mode: New Purchase Invoice");

        if (supplierComboBox.getItemCount() > 0) supplierComboBox.setSelectedIndex(0);
        if (warehouseComboBox.getItemCount() > 0) warehouseComboBox.setSelectedIndex(0);
        if (productComboBox.getItemCount() > 0) productComboBox.setSelectedIndex(0);

        invoiceDateField.setText(LocalDate.now().toString());
        estimatedArrivalField.setText(LocalDate.now().plusDays(5).toString());
        paymentField.setText("0.00");
        paymentTypeComboBox.setSelectedIndex(0);

        quantityField.setText("");
        purchasePriceField.setText("");
        clearItems();
    }

    private void selectSupplierById(int supplierId) {
        for (int i = 0; i < supplierComboBox.getItemCount(); i++) {
            if (supplierComboBox.getItemAt(i).getSupplierId() == supplierId) {
                supplierComboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectWarehouseById(int warehouseId) {
        for (int i = 0; i < warehouseComboBox.getItemCount(); i++) {
            if (warehouseComboBox.getItemAt(i).getWarehouseId() == warehouseId) {
                warehouseComboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private static class WarehouseOption {
        private final int warehouseId;
        private final String warehouseName;

        public WarehouseOption(int warehouseId, String warehouseName) {
            this.warehouseId = warehouseId;
            this.warehouseName = warehouseName;
        }

        public int getWarehouseId() {
            return warehouseId;
        }

        @Override
        public String toString() {
            return warehouseName;
        }
    }
}
