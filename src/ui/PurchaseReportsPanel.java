package ui;

import dao.PurchaseReportDAO;
import dao.PurchaseReportDAO.ComboOption;
import util.MessageUtil;
import util.TableUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class PurchaseReportsPanel extends JPanel {

    private final PurchaseReportDAO reportDAO = new PurchaseReportDAO();

    private JComboBox<String> reportComboBox;
    private JComboBox<ComboOption> supplierComboBox;
    private JComboBox<ComboOption> warehouseComboBox;
    private JComboBox<ComboOption> productComboBox;
    private JComboBox<ComboOption> invoiceComboBox;

    private JTextField fromDateField;
    private JTextField toDateField;

    private JPanel supplierFilterPanel;
    private JPanel warehouseFilterPanel;
    private JPanel productFilterPanel;
    private JPanel invoiceFilterPanel;
    private JPanel fromDateFilterPanel;
    private JPanel toDateFilterPanel;

    private JTable reportTable;
    private DefaultTableModel reportTableModel;
    private SimpleBarChartPanel chartPanel;
    private JLabel statusLabel;

    private static final String PRODUCTS_CATEGORY_BRAND = "Products with Category and Brand";
    private static final String PRODUCTS_IN_WAREHOUSE = "Products in Warehouse";
    private static final String SUPPLIERS_FOR_PRODUCT = "Suppliers for Product";
    private static final String PURCHASES_BY_SUPPLIER_DATE = "Purchase Invoices by Supplier and Date";
    private static final String PURCHASE_INVOICE_DETAILS = "Purchase Invoice Details";
    private static final String TOTAL_QTY_PRODUCT = "Total Quantity Purchased per Product";
    private static final String CURRENT_STOCK = "Current Stock by Warehouse";
    private static final String LOW_STOCK = "Low Stock Products";
    private static final String TOTAL_AMOUNT_SUPPLIER = "Total Purchase Amount per Supplier";
    private static final String PURCHASE_AMOUNT_MONTH = "Purchase Amount by Month";

    public PurchaseReportsPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIStyle.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        loadFilterData();
        runSelectedReport();
    }

    private JPanel createHeaderPanel() {
        return UIStyle.createHeaderPanel(
                "Purchase Reports",
                "Advanced purchase, supplier, product, and inventory reports with charts."
        );
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UIStyle.BACKGROUND);

        panel.add(createFilterPanel(), BorderLayout.NORTH);
        panel.add(createReportViewPanel(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFilterPanel() {
        JPanel wrapper = UIStyle.createCardPanel();

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        formPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        reportComboBox = new JComboBox<>(new String[]{
                PRODUCTS_CATEGORY_BRAND,
                PRODUCTS_IN_WAREHOUSE,
                SUPPLIERS_FOR_PRODUCT,
                PURCHASES_BY_SUPPLIER_DATE,
                PURCHASE_INVOICE_DETAILS,
                TOTAL_QTY_PRODUCT,
                CURRENT_STOCK,
                LOW_STOCK,
                TOTAL_AMOUNT_SUPPLIER,
                PURCHASE_AMOUNT_MONTH
        });

        supplierComboBox = new JComboBox<>();
        warehouseComboBox = new JComboBox<>();
        productComboBox = new JComboBox<>();
        invoiceComboBox = new JComboBox<>();

        fromDateField = new JTextField("2026-01-01");
        toDateField = new JTextField(LocalDate.now().toString());

        UIStyle.styleComboBox(reportComboBox);
        UIStyle.styleComboBox(supplierComboBox);
        UIStyle.styleComboBox(warehouseComboBox);
        UIStyle.styleComboBox(productComboBox);
        UIStyle.styleComboBox(invoiceComboBox);
        UIStyle.styleTextField(fromDateField);
        UIStyle.styleTextField(toDateField);

        JPanel reportFilterPanel = createFilterField("Report", reportComboBox);
        supplierFilterPanel = createFilterField("Supplier", supplierComboBox);
        warehouseFilterPanel = createFilterField("Warehouse", warehouseComboBox);
        productFilterPanel = createFilterField("Product", productComboBox);
        invoiceFilterPanel = createFilterField("Purchase Invoice", invoiceComboBox);
        fromDateFilterPanel = createFilterField("From Date", fromDateField);
        toDateFilterPanel = createFilterField("To Date", toDateField);

        formPanel.add(reportFilterPanel);
        formPanel.add(supplierFilterPanel);
        formPanel.add(warehouseFilterPanel);
        formPanel.add(productFilterPanel);
        formPanel.add(invoiceFilterPanel);
        formPanel.add(fromDateFilterPanel);
        formPanel.add(toDateFilterPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        JButton runButton = new JButton("Run Report");
        JButton refreshButton = new JButton("Refresh Lists");
        JButton clearButton = new JButton("Clear Chart");

        UIStyle.stylePrimaryButton(runButton);
        UIStyle.stylePrimaryButton(refreshButton);
        UIStyle.stylePrimaryButton(clearButton);

        reportComboBox.addActionListener(e -> updateFilterVisibility());
        runButton.addActionListener(e -> runSelectedReport());
        refreshButton.addActionListener(e -> loadFilterData());
        clearButton.addActionListener(e -> chartPanel.clearChart());

        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(runButton);

        wrapper.add(formPanel, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        updateFilterVisibility();

        return wrapper;
    }

    private JPanel createFilterField(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBackground(UIStyle.PANEL_BACKGROUND);

        JLabel label = new JLabel(labelText);
        UIStyle.styleLabel(label);

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }

    private void updateFilterVisibility() {
        if (supplierFilterPanel == null || reportComboBox == null) {
            return;
        }

        String report = String.valueOf(reportComboBox.getSelectedItem());

        supplierFilterPanel.setVisible(false);
        warehouseFilterPanel.setVisible(false);
        productFilterPanel.setVisible(false);
        invoiceFilterPanel.setVisible(false);
        fromDateFilterPanel.setVisible(false);
        toDateFilterPanel.setVisible(false);

        switch (report) {
            case PRODUCTS_IN_WAREHOUSE -> warehouseFilterPanel.setVisible(true);
            case SUPPLIERS_FOR_PRODUCT -> productFilterPanel.setVisible(true);
            case PURCHASES_BY_SUPPLIER_DATE -> {
                supplierFilterPanel.setVisible(true);
                fromDateFilterPanel.setVisible(true);
                toDateFilterPanel.setVisible(true);
            }
            case PURCHASE_INVOICE_DETAILS -> invoiceFilterPanel.setVisible(true);
            case LOW_STOCK -> warehouseFilterPanel.setVisible(true);
        }

        revalidate();
        repaint();
    }

    private JSplitPane createReportViewPanel() {
        JPanel tablePanel = UIStyle.createCardPanel();

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIStyle.PANEL_BACKGROUND);

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        String[] columns = {"Result"};
        reportTableModel = TableUtil.createNonEditableTableModel(columns);
        reportTable = new JTable(reportTableModel);
        TableUtil.setupTable(reportTable);

        JButton columnsButton = TableUtil.createColumnVisibilityButton(reportTable, "Columns");

        topPanel.add(statusLabel, BorderLayout.WEST);
        topPanel.add(columnsButton, BorderLayout.EAST);

        tablePanel.add(topPanel, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(reportTable), BorderLayout.CENTER);

        chartPanel = new SimpleBarChartPanel();

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                tablePanel,
                chartPanel
        );

        UIStyle.styleSplitPane(splitPane, 0.65);

        return splitPane;
    }

    private void loadFilterData() {
        loadComboBox(supplierComboBox, reportDAO.getSuppliers());
        loadComboBox(warehouseComboBox, reportDAO.getWarehouses());
        loadComboBox(productComboBox, reportDAO.getProducts());
        loadComboBox(invoiceComboBox, reportDAO.getPurchaseInvoices());
    }

    private void loadComboBox(JComboBox<ComboOption> comboBox, List<ComboOption> options) {
        comboBox.removeAllItems();

        for (ComboOption option : options) {
            comboBox.addItem(option);
        }
    }

    private void runSelectedReport() {
        String report = (String) reportComboBox.getSelectedItem();
        if (report == null) return;

        DefaultTableModel model;

        switch (report) {
            case PRODUCTS_CATEGORY_BRAND:
                model = reportDAO.getProductsWithCategoryAndBrand();
                break;

            case PRODUCTS_IN_WAREHOUSE:
                ComboOption warehouseForProducts = getSelectedOption(warehouseComboBox, "Select a warehouse.");
                if (warehouseForProducts == null) return;
                model = reportDAO.getProductsInWarehouse(warehouseForProducts.getId());
                break;

            case SUPPLIERS_FOR_PRODUCT:
                ComboOption selectedProduct = getSelectedOption(productComboBox, "Select a product.");
                if (selectedProduct == null) return;
                model = reportDAO.getSuppliersForProduct(selectedProduct.getId());
                break;

            case PURCHASES_BY_SUPPLIER_DATE:
                ComboOption selectedSupplier = getSelectedOption(supplierComboBox, "Select a supplier.");
                if (selectedSupplier == null) return;

                LocalDate fromDate = parseDate(fromDateField.getText(), "From date");
                LocalDate toDate = parseDate(toDateField.getText(), "To date");
                if (fromDate == null || toDate == null) return;

                model = reportDAO.getPurchaseInvoicesBySupplierAndDate(
                        selectedSupplier.getId(),
                        fromDate,
                        toDate
                );
                break;

            case PURCHASE_INVOICE_DETAILS:
                ComboOption selectedInvoice = getSelectedOption(invoiceComboBox, "Select a purchase invoice.");
                if (selectedInvoice == null) return;
                model = reportDAO.getPurchaseInvoiceDetails(selectedInvoice.getId());
                break;

            case TOTAL_QTY_PRODUCT:
                model = reportDAO.getTotalQuantityPurchasedPerProduct();
                break;

            case CURRENT_STOCK:
                model = reportDAO.getCurrentStockByWarehouse();
                break;

            case LOW_STOCK:
                ComboOption warehouseForLowStock = getSelectedOption(warehouseComboBox, "Select a warehouse.");
                if (warehouseForLowStock == null) return;

                model = reportDAO.getLowStockProducts(warehouseForLowStock.getId());
                break;

            case TOTAL_AMOUNT_SUPPLIER:
                model = reportDAO.getTotalPurchaseAmountPerSupplier();
                break;

            case PURCHASE_AMOUNT_MONTH:
                model = reportDAO.getPurchaseAmountByMonth();
                break;

            default:
                MessageUtil.showError("Unknown report selected.");
                return;
        }

        showReport(model, report);
    }

    private ComboOption getSelectedOption(JComboBox<ComboOption> comboBox, String message) {
        ComboOption option = (ComboOption) comboBox.getSelectedItem();

        if (option == null) {
            MessageUtil.showWarning(message);
            return null;
        }

        return option;
    }

    private LocalDate parseDate(String text, String fieldName) {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            MessageUtil.showError(fieldName + " must be in YYYY-MM-DD format.");
            return null;
        }
    }

    private void showReport(DefaultTableModel model, String reportName) {
        reportTableModel = model;
        reportTable.setModel(reportTableModel);
        TableUtil.setupTable(reportTable);

        statusLabel.setText(reportName + " | Rows: " + reportTableModel.getRowCount());
        updateChart(reportName);
    }

    private void updateChart(String reportName) {
        if (reportTableModel == null || reportTableModel.getRowCount() == 0) {
            chartPanel.clearChart();
            return;
        }

        if (reportName.equals(TOTAL_QTY_PRODUCT)) {
            chartPanel.setChartData(
                    "Total Quantity Purchased per Product",
                    buildChartItems("Product", "Total Quantity Purchased", 8)
            );
            return;
        }

        if (reportName.equals(TOTAL_AMOUNT_SUPPLIER)) {
            chartPanel.setChartData(
                    "Total Purchase Amount per Supplier",
                    buildChartItems("Supplier", "Total Purchase Amount", 8)
            );
            return;
        }

        if (reportName.equals(PURCHASE_AMOUNT_MONTH)) {
            chartPanel.setChartData(
                    "Purchase Amount by Month",
                    buildChartItems("Month", "Total Purchase Amount", 12)
            );
            return;
        }

        chartPanel.setMessage("Chart available for total quantity, total supplier amount, and monthly purchase amount reports.");
    }

    private List<SimpleBarChartPanel.ChartItem> buildChartItems(String labelColumn,
                                                                String valueColumn,
                                                                int limit) {
        List<SimpleBarChartPanel.ChartItem> items = new ArrayList<>();

        int labelIndex = findColumnIndex(labelColumn);
        int valueIndex = findColumnIndex(valueColumn);

        if (labelIndex == -1 || valueIndex == -1) {
            return items;
        }

        int rowLimit = Math.min(reportTableModel.getRowCount(), limit);

        for (int i = 0; i < rowLimit; i++) {
            String label = String.valueOf(reportTableModel.getValueAt(i, labelIndex));
            double value = toDouble(reportTableModel.getValueAt(i, valueIndex));
            items.add(new SimpleBarChartPanel.ChartItem(label, value));
        }

        return items;
    }

    private int findColumnIndex(String columnName) {
        for (int i = 0; i < reportTableModel.getColumnCount(); i++) {
            if (columnName.equals(reportTableModel.getColumnName(i))) {
                return i;
            }
        }

        return -1;
    }

    private double toDouble(Object value) {
        if (value == null) return 0;

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).doubleValue();
        }

        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static class SimpleBarChartPanel extends JPanel {

        private String title = "Chart";
        private String message = "Run an aggregation report to view a chart.";
        private List<ChartItem> items = new ArrayList<>();

        public SimpleBarChartPanel() {
            setBackground(UIStyle.PANEL_BACKGROUND);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIStyle.BORDER),
                    BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));
            setPreferredSize(new Dimension(800, 220));
        }

        public void setChartData(String title, List<ChartItem> items) {
            this.title = title;
            this.items = items;
            this.message = items.isEmpty() ? "No chart data available." : "";
            repaint();
        }

        public void setMessage(String message) {
            this.message = message;
            this.items = new ArrayList<>();
            repaint();
        }

        public void clearChart() {
            this.message = "Run an aggregation report to view a chart.";
            this.items = new ArrayList<>();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            g2.setColor(UIStyle.TEXT_DARK);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g2.drawString(title, 20, 30);

            if (items == null || items.isEmpty()) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.setColor(new Color(75, 85, 99));
                g2.drawString(message, 20, 60);
                return;
            }

            double max = 0;
            for (ChartItem item : items) {
                if (item.value > max) {
                    max = item.value;
                }
            }

            if (max <= 0) max = 1;

            int left = 160;
            int right = 30;
            int top = 50;
            int bottom = 25;
            int chartWidth = width - left - right;
            int chartHeight = height - top - bottom;
            int barGap = 8;
            int barHeight = Math.max(16, (chartHeight / items.size()) - barGap);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            for (int i = 0; i < items.size(); i++) {
                ChartItem item = items.get(i);

                int y = top + i * (barHeight + barGap);
                int barWidth = (int) ((item.value / max) * chartWidth);

                g2.setColor(new Color(59, 130, 246));
                g2.fillRoundRect(left, y, barWidth, barHeight, 8, 8);

                g2.setColor(UIStyle.TEXT_DARK);
                g2.drawString(shorten(item.label, 20), 20, y + barHeight - 3);

                g2.setColor(new Color(75, 85, 99));
                g2.drawString(formatNumber(item.value), left + barWidth + 8, y + barHeight - 3);
            }
        }

        private String shorten(String text, int maxLength) {
            if (text == null) return "";
            if (text.length() <= maxLength) return text;
            return text.substring(0, maxLength - 3) + "...";
        }

        private String formatNumber(double value) {
            if (value >= 1000) {
                return String.format("%.1fK", value / 1000.0);
            }

            if (value == Math.floor(value)) {
                return String.format("%.0f", value);
            }

            return String.format("%.2f", value);
        }

        private static class ChartItem {
            private final String label;
            private final double value;

            public ChartItem(String label, double value) {
                this.label = label;
                this.value = value;
            }
        }
    }
}
