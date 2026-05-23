package ui;

import dao.ClientDAO;
import dao.InventoryDAO;
import dao.SalesInvoiceDAO;
import dao.WarehouseDAO;
import db.DBConnection;
import model.Client;
import model.SalesInvoice;
import model.SalesInvoiceItem;
import model.Warehouse;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SalesInvoicePanel extends JPanel {

    private final ClientDAO clientDAO = new ClientDAO();
    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final SalesInvoiceDAO salesInvoiceDAO = new SalesInvoiceDAO();

    private JComboBox<Client> clientComboBox;
    private JComboBox<Warehouse> warehouseComboBox;
    private JComboBox<ProductOption> productComboBox;
    private JComboBox<String> paymentTypeComboBox;

    private JTextField invoiceDateField;
    private JTextField paymentField;
    private JTextField availableStockField;
    private JTextField quantityField;
    private JTextField sellingPriceField;
    private JTextField warrantyEndDateField;

    private JTable itemTable;
    private DefaultTableModel itemTableModel;

    private JTable invoiceTable;
    private DefaultTableModel invoiceTableModel;

    public SalesInvoicePanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIStyle.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        loadComboBoxes();
        loadInvoices();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setBackground(UIStyle.BACKGROUND);

        panel.add(UIStyle.createTitle("Sales Invoice Management"));
        panel.add(UIStyle.createSubtitle("Create sales invoices, validate stock, and decrease inventory."));

        return panel;
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIStyle.BACKGROUND);

        JPanel invoicePanel = createInvoicePanel();
        JPanel historyPanel = createInvoiceHistoryPanel();

        invoicePanel.setMinimumSize(new Dimension(550, 0));
        historyPanel.setMinimumSize(new Dimension(450, 0));

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                invoicePanel,
                historyPanel
        );

        splitPane.setResizeWeight(0.55);
        splitPane.setDividerLocation(720);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);

        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createInvoicePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIStyle.PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        panel.add(createInvoiceHeaderForm(), BorderLayout.NORTH);
        panel.add(createItemSection(), BorderLayout.CENTER);
        panel.add(createSaveButtonPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createInvoiceHeaderForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIStyle.PANEL_BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        clientComboBox = new JComboBox<>();
        warehouseComboBox = new JComboBox<>();
        paymentTypeComboBox = new JComboBox<>(new String[]{"Cash", "Card", "Bank Transfer", "Cheque"});

        invoiceDateField = new JTextField(LocalDate.now().toString());
        paymentField = new JTextField("0.00");

        addField(panel, gbc, 0, 0, "Client *", clientComboBox);
        addField(panel, gbc, 1, 0, "Warehouse *", warehouseComboBox);
        addField(panel, gbc, 0, 1, "Invoice Date *", invoiceDateField);
        addField(panel, gbc, 1, 1, "Payment Type *", paymentTypeComboBox);
        addField(panel, gbc, 0, 2, "Payment *", paymentField);

        return panel;
    }

    private JPanel createItemSection() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIStyle.PANEL_BACKGROUND);

        panel.add(createItemForm(), BorderLayout.NORTH);
        panel.add(createItemTable(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createItemForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIStyle.PANEL_BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        productComboBox = new JComboBox<>();
        availableStockField = new JTextField();
        availableStockField.setEditable(false);

        quantityField = new JTextField();
        sellingPriceField = new JTextField();
        warrantyEndDateField = new JTextField();

        productComboBox.addActionListener(e -> updateProductInfo());
        warehouseComboBox.addActionListener(e -> updateProductInfo());

        addField(panel, gbc, 0, 0, "Product *", productComboBox);
        addField(panel, gbc, 1, 0, "Available Stock", availableStockField);
        addField(panel, gbc, 0, 1, "Quantity *", quantityField);
        addField(panel, gbc, 1, 1, "Selling Price *", sellingPriceField);
        addField(panel, gbc, 0, 2, "Warranty End Date", warrantyEndDateField);

        JButton addItemButton = new JButton("Add Item");
        JButton removeItemButton = new JButton("Remove Item");
        JButton clearItemsButton = new JButton("Clear Items");

        UIStyle.stylePrimaryButton(addItemButton);
        UIStyle.stylePrimaryButton(removeItemButton);
        UIStyle.stylePrimaryButton(clearItemsButton);

        addItemButton.addActionListener(e -> addItem());
        removeItemButton.addActionListener(e -> removeSelectedItem());
        clearItemsButton.addActionListener(e -> clearItems());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        buttonPanel.add(addItemButton);
        buttonPanel.add(removeItemButton);
        buttonPanel.add(clearItemsButton);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        gbc.gridwidth = 1;        return panel;
    }

    private JScrollPane createItemTable() {
        String[] columns = {"Product ID", "Product", "Price", "Quantity", "Warranty End", "Line Total"};

        itemTableModel = TableUtil.createNonEditableTableModel(columns);
        itemTable = new JTable(itemTableModel);
        TableUtil.setupTable(itemTable);

        return new JScrollPane(itemTable);
    }

    private JPanel createSaveButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panel.setBackground(UIStyle.PANEL_BACKGROUND);

        JButton saveButton = new JButton("Save Sales Invoice");
        JButton clearButton = new JButton("Clear Invoice");

        UIStyle.stylePrimaryButton(saveButton);
        UIStyle.stylePrimaryButton(clearButton);

        // Fix button text being cut
        saveButton.setPreferredSize(new Dimension(170, 38));
        clearButton.setPreferredSize(new Dimension(140, 38));

        saveButton.addActionListener(e -> saveInvoice());
        clearButton.addActionListener(e -> clearInvoice());

        panel.add(clearButton);
        panel.add(saveButton);

        return panel;
    }
    private JPanel createInvoiceHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UIStyle.BACKGROUND);

        JLabel title = UIStyle.createTitle("Sales Invoices");
        panel.add(title, BorderLayout.NORTH);

        String[] columns = {"ID", "Date", "Client ID", "Warehouse ID", "Payment Type", "Payment", "Amount"};

        invoiceTableModel = TableUtil.createNonEditableTableModel(columns);
        invoiceTable = new JTable(invoiceTableModel);
        TableUtil.setupTable(invoiceTable);

        panel.add(new JScrollPane(invoiceTable), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        UIStyle.stylePrimaryButton(refreshButton);
        refreshButton.addActionListener(e -> loadInvoices());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(UIStyle.BACKGROUND);
        bottomPanel.add(refreshButton);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int x, int y, String label, JComponent field) {
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(UIStyle.LABEL_FONT);
        jLabel.setForeground(UIStyle.TEXT_DARK);

        UIStyle.styleTextFieldIfPossible(field);

        gbc.gridx = x;
        gbc.gridy = y * 2;
        panel.add(jLabel, gbc);

        gbc.gridx = x;
        gbc.gridy = y * 2 + 1;
        panel.add(field, gbc);
    }

    private void loadComboBoxes() {
        clientComboBox.removeAllItems();
        for (Client client : clientDAO.getAllClients()) {
            clientComboBox.addItem(client);
        }

        warehouseComboBox.removeAllItems();
        for (Warehouse warehouse : warehouseDAO.getAllWarehouses()) {
            warehouseComboBox.addItem(warehouse);
        }

        productComboBox.removeAllItems();
        for (ProductOption product : getAllProducts()) {
            productComboBox.addItem(product);
        }

        updateProductInfo();
    }

    private List<ProductOption> getAllProducts() {
        List<ProductOption> products = new ArrayList<>();

        String sql = """
                SELECT product_id, product_name, default_selling_price
                FROM Product
                ORDER BY product_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(new ProductOption(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getBigDecimal("default_selling_price")
                ));
            }

        } catch (Exception e) {
            MessageUtil.showError("Error loading products: " + e.getMessage());
        }

        return products;
    }

    private void updateProductInfo() {
        ProductOption product = (ProductOption) productComboBox.getSelectedItem();
        Warehouse warehouse = (Warehouse) warehouseComboBox.getSelectedItem();

        if (product == null || warehouse == null) {
            availableStockField.setText("");
            return;
        }

        int stock = inventoryDAO.getAvailableStock(product.getProductId(), warehouse.getWarehouseId());
        availableStockField.setText(String.valueOf(stock));

        sellingPriceField.setText(product.getDefaultSellingPrice().toString());
    }

    private void addItem() {
        try {
            ProductOption product = (ProductOption) productComboBox.getSelectedItem();
            Warehouse warehouse = (Warehouse) warehouseComboBox.getSelectedItem();

            if (product == null) {
                throw new IllegalArgumentException("Please select a product.");
            }

            if (warehouse == null) {
                throw new IllegalArgumentException("Please select a warehouse.");
            }

            String quantityText = quantityField.getText().trim();
            String sellingPriceText = sellingPriceField.getText().trim();
            String warrantyText = warrantyEndDateField.getText().trim();

            if (!ValidationUtil.isPositiveInteger(quantityText)) {
                throw new IllegalArgumentException("Quantity must be a positive integer.");
            }

            if (!ValidationUtil.isPositiveDecimal(sellingPriceText)) {
                throw new IllegalArgumentException("Selling price must be a positive number.");
            }

            int quantity = Integer.parseInt(quantityText);
            BigDecimal sellingPrice = new BigDecimal(sellingPriceText);

            int availableStock = inventoryDAO.getAvailableStock(product.getProductId(), warehouse.getWarehouseId());
            int alreadyAddedQuantity = getTemporaryQuantityForProduct(product.getProductId());

            if (quantity + alreadyAddedQuantity > availableStock) {
                throw new IllegalArgumentException(
                        "Not enough stock. Available: " + availableStock +
                                ", already added: " + alreadyAddedQuantity +
                                ", requested now: " + quantity
                );
            }

            LocalDate warrantyEndDate = null;

            if (!ValidationUtil.isEmpty(warrantyText)) {
                try {
                    warrantyEndDate = LocalDate.parse(warrantyText);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Warranty date must be in YYYY-MM-DD format.");
                }
            }

            BigDecimal lineTotal = sellingPrice.multiply(BigDecimal.valueOf(quantity));

            itemTableModel.addRow(new Object[]{
                    product.getProductId(),
                    product.getProductName(),
                    sellingPrice,
                    quantity,
                    warrantyEndDate,
                    lineTotal
            });

            quantityField.setText("");
            warrantyEndDateField.setText("");

            updatePaymentToTotal();

        } catch (Exception e) {
            MessageUtil.showError(e.getMessage());
        }
    }

    private int getTemporaryQuantityForProduct(int productId) {
        int total = 0;

        for (int i = 0; i < itemTableModel.getRowCount(); i++) {
            int rowProductId = Integer.parseInt(itemTableModel.getValueAt(i, 0).toString());
            int quantity = Integer.parseInt(itemTableModel.getValueAt(i, 3).toString());

            if (rowProductId == productId) {
                total += quantity;
            }
        }

        return total;
    }

    private void removeSelectedItem() {
        int selectedRow = itemTable.getSelectedRow();

        if (selectedRow == -1) {
            MessageUtil.showWarning("Please select an item to remove.");
            return;
        }

        int modelRow = itemTable.convertRowIndexToModel(selectedRow);
        itemTableModel.removeRow(modelRow);

        updatePaymentToTotal();
    }

    private void clearItems() {
        TableUtil.clearTable(itemTableModel);
        updatePaymentToTotal();
    }

    private void saveInvoice() {
        try {
            Client client = (Client) clientComboBox.getSelectedItem();
            Warehouse warehouse = (Warehouse) warehouseComboBox.getSelectedItem();

            if (client == null) {
                throw new IllegalArgumentException("Please select a client.");
            }

            if (warehouse == null) {
                throw new IllegalArgumentException("Please select a warehouse.");
            }

            if (itemTableModel.getRowCount() == 0) {
                throw new IllegalArgumentException("Please add at least one item.");
            }

            LocalDate invoiceDate;

            try {
                invoiceDate = LocalDate.parse(invoiceDateField.getText().trim());
            } catch (Exception e) {
                throw new IllegalArgumentException("Invoice date must be in YYYY-MM-DD format.");
            }

            String paymentText = paymentField.getText().trim();

            if (!ValidationUtil.isNonNegativeDecimal(paymentText)) {
                throw new IllegalArgumentException("Payment must be a non-negative number.");
            }

            BigDecimal payment = new BigDecimal(paymentText);
            BigDecimal amount = calculateInvoiceTotal();

            List<SalesInvoiceItem> items = readItemsFromTable();

            SalesInvoice invoice = new SalesInvoice(
                    invoiceDate,
                    payment,
                    paymentTypeComboBox.getSelectedItem().toString(),
                    amount,
                    client.getClientId(),
                    warehouse.getWarehouseId(),
                    items
            );

            boolean success = salesInvoiceDAO.createSalesInvoice(invoice);

            if (success) {
                MessageUtil.showSuccess("Sales invoice saved successfully.");
                clearInvoice();
                loadInvoices();
                updateProductInfo();
            } else {
                MessageUtil.showError("Failed to save sales invoice.");
            }

        } catch (Exception e) {
            MessageUtil.showError(e.getMessage());
        }
    }

    private List<SalesInvoiceItem> readItemsFromTable() {
        List<SalesInvoiceItem> items = new ArrayList<>();

        for (int i = 0; i < itemTableModel.getRowCount(); i++) {
            int productId = Integer.parseInt(itemTableModel.getValueAt(i, 0).toString());
            BigDecimal sellingPrice = new BigDecimal(itemTableModel.getValueAt(i, 2).toString());
            int quantity = Integer.parseInt(itemTableModel.getValueAt(i, 3).toString());

            Object warrantyObj = itemTableModel.getValueAt(i, 4);
            LocalDate warrantyEndDate = null;

            if (warrantyObj != null && !warrantyObj.toString().isBlank()) {
                warrantyEndDate = LocalDate.parse(warrantyObj.toString());
            }

            items.add(new SalesInvoiceItem(productId, sellingPrice, quantity, warrantyEndDate));
        }

        return items;
    }

    private void updatePaymentToTotal() {
        paymentField.setText(calculateInvoiceTotal().toString());
    }

    private BigDecimal calculateInvoiceTotal() {
        BigDecimal total = BigDecimal.ZERO;

        for (int i = 0; i < itemTableModel.getRowCount(); i++) {
            total = total.add(new BigDecimal(itemTableModel.getValueAt(i, 5).toString()));
        }

        return total;
    }

    private void loadInvoices() {
        TableUtil.clearTable(invoiceTableModel);

        for (SalesInvoice invoice : salesInvoiceDAO.getAllSalesInvoices()) {
            invoiceTableModel.addRow(new Object[]{
                    invoice.getSalesInvoiceId(),
                    invoice.getInvoiceDate(),
                    invoice.getClientId(),
                    invoice.getWarehouseId(),
                    invoice.getPaymentType(),
                    invoice.getPayment(),
                    invoice.getAmount()
            });
        }
    }

    private void clearInvoice() {
        invoiceDateField.setText(LocalDate.now().toString());
        paymentField.setText("0.00");
        paymentTypeComboBox.setSelectedIndex(0);
        quantityField.setText("");
        warrantyEndDateField.setText("");
        clearItems();

        if (clientComboBox.getItemCount() > 0) {
            clientComboBox.setSelectedIndex(0);
        }

        if (warehouseComboBox.getItemCount() > 0) {
            warehouseComboBox.setSelectedIndex(0);
        }

        if (productComboBox.getItemCount() > 0) {
            productComboBox.setSelectedIndex(0);
        }

        updateProductInfo();
    }

    private static class ProductOption {
        private final int productId;
        private final String productName;
        private final BigDecimal defaultSellingPrice;

        public ProductOption(int productId, String productName, BigDecimal defaultSellingPrice) {
            this.productId = productId;
            this.productName = productName;
            this.defaultSellingPrice = defaultSellingPrice;
        }

        public int getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public BigDecimal getDefaultSellingPrice() {
            return defaultSellingPrice;
        }

        @Override
        public String toString() {
            return productName;
        }
    }
}