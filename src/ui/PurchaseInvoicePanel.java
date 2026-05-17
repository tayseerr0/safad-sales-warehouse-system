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

    private JLabel totalAmountLabel;

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
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setBackground(UIStyle.BACKGROUND);

        panel.add(UIStyle.createTitle("Purchase Invoices"));
        panel.add(UIStyle.createSubtitle("Create supplier purchase invoices and automatically increase warehouse inventory."));

        return panel;
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 15));
        panel.setBackground(UIStyle.BACKGROUND);

        panel.add(createInvoiceCreationPanel());
        panel.add(createInvoiceListPanel());

        return panel;
    }

    private JPanel createInvoiceCreationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIStyle.PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        topPanel.add(createInvoiceHeaderForm(), BorderLayout.NORTH);
        topPanel.add(createItemForm(), BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(createItemsTablePanel(), BorderLayout.CENTER);
        panel.add(createSavePanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createInvoiceHeaderForm() {
        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        wrapper.setBackground(UIStyle.PANEL_BACKGROUND);

        JLabel title = new JLabel("Invoice Information");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 8, 8));
        formPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        supplierComboBox = new JComboBox<>();
        warehouseComboBox = new JComboBox<>();

        invoiceDateField = new JTextField(LocalDate.now().toString());
        estimatedArrivalField = new JTextField(LocalDate.now().plusDays(5).toString());
        paymentField = new JTextField("0.00");

        paymentTypeComboBox = new JComboBox<>(new String[]{
                "Cash",
                "Card",
                "Bank Transfer",
                "Cheque"
        });

        UIStyle.styleComboBox(supplierComboBox);
        UIStyle.styleComboBox(warehouseComboBox);
        UIStyle.styleTextField(invoiceDateField);
        UIStyle.styleTextField(estimatedArrivalField);
        UIStyle.styleTextField(paymentField);
        UIStyle.styleComboBox(paymentTypeComboBox);

        invoiceDateField.setToolTipText("Format: YYYY-MM-DD");
        estimatedArrivalField.setToolTipText("Format: YYYY-MM-DD");

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

        wrapper.add(title, BorderLayout.NORTH);
        wrapper.add(formPanel, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel createItemForm() {
        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        wrapper.setBackground(UIStyle.PANEL_BACKGROUND);
        wrapper.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JLabel title = new JLabel("Add Purchase Items");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));

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
        UIStyle.stylePrimaryButton(removeItemButton);
        UIStyle.stylePrimaryButton(clearItemsButton);

        addItemButton.addActionListener(e -> addItem());
        removeItemButton.addActionListener(e -> removeSelectedItem());
        clearItemsButton.addActionListener(e -> clearItems());

        buttonPanel.add(addItemButton);
        buttonPanel.add(removeItemButton);
        buttonPanel.add(clearItemsButton);

        wrapper.add(title, BorderLayout.NORTH);
        wrapper.add(formPanel, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        return wrapper;
    }

    private JPanel createItemsTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UIStyle.PANEL_BACKGROUND);

        String[] columns = {"Product ID", "Product", "Quantity", "Purchase Price", "Line Total"};
        itemTableModel = TableUtil.createNonEditableTableModel(columns);
        itemTable = new JTable(itemTableModel);
        TableUtil.setupTable(itemTable);

        panel.add(new JScrollPane(itemTable), BorderLayout.CENTER);

        totalAmountLabel = new JLabel("Total Amount: 0.00");
        totalAmountLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        panel.add(totalAmountLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSavePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(UIStyle.PANEL_BACKGROUND);

        JButton saveButton = new JButton("Save Invoice");
        JButton clearButton = new JButton("Clear Form");
        JButton refreshButton = new JButton("Refresh");

        UIStyle.stylePrimaryButton(saveButton);
        UIStyle.stylePrimaryButton(clearButton);
        UIStyle.stylePrimaryButton(refreshButton);

        saveButton.addActionListener(e -> saveInvoice());
        clearButton.addActionListener(e -> clearForm());
        refreshButton.addActionListener(e -> {
            loadComboBoxes();
            loadPurchaseInvoices();
        });

        panel.add(saveButton);
        panel.add(clearButton);
        panel.add(refreshButton);

        return panel;
    }

    private JPanel createInvoiceListPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIStyle.PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = UIStyle.createTitle("Purchase Invoice History");

        String[] columns = {
                "Invoice ID",
                "Date",
                "Estimated Arrival",
                "Supplier",
                "Warehouse",
                "Payment",
                "Payment Type",
                "Amount"
        };

        invoiceTableModel = TableUtil.createNonEditableTableModel(columns);
        invoiceTable = new JTable(invoiceTableModel);
        TableUtil.setupTable(invoiceTable);

        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(invoiceTable), BorderLayout.CENTER);

        return panel;
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

        warehouseComboBox.removeAllItems();
        for (WarehouseOption warehouse : loadWarehouses()) {
            warehouseComboBox.addItem(warehouse);
        }
    }

    private List<WarehouseOption> loadWarehouses() {
        List<WarehouseOption> warehouses = new ArrayList<>();

        String sql = """
                SELECT warehouse_id, warehouse_name
                FROM Warehouse
                ORDER BY warehouse_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                warehouses.add(new WarehouseOption(
                        rs.getInt("warehouse_id"),
                        rs.getString("warehouse_name")
                ));
            }

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

        PurchaseInvoiceItem item = new PurchaseInvoiceItem(
                product.getProductId(),
                purchasePrice,
                quantity
        );

        item.setProductName(product.getProductName());
        currentItems.add(item);

        BigDecimal lineTotal = purchasePrice.multiply(BigDecimal.valueOf(quantity));

        itemTableModel.addRow(new Object[]{
                product.getProductId(),
                product.getProductName(),
                quantity,
                purchasePrice,
                lineTotal
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

    private void saveInvoice() {
        Supplier supplier = (Supplier) supplierComboBox.getSelectedItem();
        WarehouseOption warehouse = (WarehouseOption) warehouseComboBox.getSelectedItem();

        if (supplier == null) {
            MessageUtil.showError("Select a supplier.");
            return;
        }

        if (warehouse == null) {
            MessageUtil.showError("Select a warehouse.");
            return;
        }

        if (currentItems.isEmpty()) {
            MessageUtil.showError("Add at least one item to the invoice.");
            return;
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
            return;
        }

        if (!ValidationUtil.isNonNegativeDecimal(paymentField.getText())) {
            MessageUtil.showError("Payment must be a valid non-negative number.");
            return;
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

        if (purchaseInvoiceDAO.createPurchaseInvoice(invoice)) {
            MessageUtil.showSuccess("Purchase invoice saved successfully. Inventory increased.");
            clearForm();
            loadPurchaseInvoices();
        } else {
            MessageUtil.showError("Failed to save purchase invoice.");
        }
    }

    private BigDecimal calculateTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;

        for (PurchaseInvoiceItem item : currentItems) {
            BigDecimal lineTotal = item.getPurchasePrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(lineTotal);
        }

        return total;
    }

    private void updateTotalAmount() {
        totalAmountLabel.setText("Total Amount: " + calculateTotalAmount());
    }

    private void clearForm() {
        if (supplierComboBox.getItemCount() > 0) {
            supplierComboBox.setSelectedIndex(0);
        }

        if (warehouseComboBox.getItemCount() > 0) {
            warehouseComboBox.setSelectedIndex(0);
        }

        if (productComboBox.getItemCount() > 0) {
            productComboBox.setSelectedIndex(0);
        }

        invoiceDateField.setText(LocalDate.now().toString());
        estimatedArrivalField.setText(LocalDate.now().plusDays(5).toString());
        paymentField.setText("0.00");
        paymentTypeComboBox.setSelectedIndex(0);

        quantityField.setText("");
        purchasePriceField.setText("");

        clearItems();
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