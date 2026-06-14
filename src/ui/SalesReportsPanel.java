package ui;

import dao.SalesReportDAO;
import util.MessageUtil;
import util.TableUtil;
import util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class SalesReportsPanel extends JPanel {

    private final SalesReportDAO salesReportDAO = new SalesReportDAO();

    private JComboBox<String> reportComboBox;
    private JTextField startDateField;
    private JTextField endDateField;
    private JTextField yearField;
    private JPanel startDateFilterPanel;
    private JPanel endDateFilterPanel;
    private JPanel yearFilterPanel;

    private JTable reportTable;
    private DefaultTableModel tableModel;

    private JPanel summaryPanel;
    private JLabel summaryLabel;

    public SalesReportsPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UIStyle.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        return UIStyle.createHeaderPanel(
                "Sales Reports",
                "Advanced sales reports using joins, filtering, aggregation, and summaries."
        );
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UIStyle.BACKGROUND);

        panel.add(createFilterPanel(), BorderLayout.NORTH);
        panel.add(createTablePanel(), BorderLayout.CENTER);
        panel.add(createSummaryPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFilterPanel() {
        JPanel panel = UIStyle.createCardPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));

        reportComboBox = new JComboBox<>(new String[]{
                "Total Sales Between Dates",
                "Monthly Sales",
                "Top Customers",
                "Most Sold Products",
                "Sales By Warehouse",
                "Valid Warranty Items",
                "Sales By Client Type",
                "Most Sold Categories",
                "Sales By Client City"
        });

        startDateField = new JTextField("2026-01-01");
        endDateField = new JTextField("2026-12-31");
        yearField = new JTextField("2026");

        JButton runButton = new JButton("Run Report");
        JButton clearButton = new JButton("Clear");

        UIStyle.styleComboBox(reportComboBox);
        UIStyle.styleTextField(startDateField);
        UIStyle.styleTextField(endDateField);
        UIStyle.styleTextField(yearField);
        UIStyle.stylePrimaryButton(runButton);
        UIStyle.stylePrimaryButton(clearButton);

        startDateFilterPanel = createFilterField("Start Date", startDateField);
        endDateFilterPanel = createFilterField("End Date", endDateField);
        yearFilterPanel = createFilterField("Year", yearField);

        panel.add(createFilterField("Report", reportComboBox));
        panel.add(startDateFilterPanel);
        panel.add(endDateFilterPanel);
        panel.add(yearFilterPanel);
        panel.add(runButton);
        panel.add(clearButton);

        reportComboBox.addActionListener(e -> updateFilterVisibility());
        runButton.addActionListener(e -> runSelectedReport());
        clearButton.addActionListener(e -> clearReport());
        updateFilterVisibility();

        return panel;
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
        if (reportComboBox == null || startDateFilterPanel == null) {
            return;
        }

        String selectedReport = String.valueOf(reportComboBox.getSelectedItem());

        boolean needsYear = selectedReport.equals("Monthly Sales");
        boolean needsDates = !needsYear && !selectedReport.equals("Valid Warranty Items");

        startDateFilterPanel.setVisible(needsDates);
        endDateFilterPanel.setVisible(needsDates);
        yearFilterPanel.setVisible(needsYear);

        revalidate();
        repaint();
    }

    private JPanel createTablePanel() {
        JPanel panel = UIStyle.createCardPanel();

        tableModel = TableUtil.createNonEditableTableModel(new String[]{"Result"});
        reportTable = new JTable(tableModel);
        TableUtil.setupTable(reportTable);

        panel.add(new JScrollPane(reportTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSummaryPanel() {
        summaryPanel = UIStyle.createCardPanel();
        summaryPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        summaryLabel = new JLabel("Select a report and click Run Report.");
        summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        summaryLabel.setForeground(UIStyle.TEXT_DARK);

        summaryPanel.add(summaryLabel);

        return summaryPanel;
    }

    private void runSelectedReport() {
        try {
            String selectedReport = reportComboBox.getSelectedItem().toString();

            switch (selectedReport) {
                case "Total Sales Between Dates" -> runTotalSalesBetweenDates();
                case "Monthly Sales" -> runMonthlySales();
                case "Top Customers" -> runTopCustomers();
                case "Most Sold Products" -> runMostSoldProducts();
                case "Sales By Warehouse" -> runSalesByWarehouse();
                case "Valid Warranty Items" -> runValidWarrantyItems();
                case "Sales By Client Type" -> runSalesByClientType();
                case "Most Sold Categories" -> runMostSoldCategories();
                case "Sales By Client City" -> runSalesByClientCity();
                default -> MessageUtil.showWarning("Please select a valid report.");
            }

        } catch (Exception e) {
            MessageUtil.showError(e.getMessage());
        }
    }

    private Date getStartDate() {
        String text = startDateField.getText().trim();

        if (ValidationUtil.isEmpty(text)) {
            throw new IllegalArgumentException("Start date is required.");
        }

        try {
            LocalDate date = LocalDate.parse(text);
            return Date.valueOf(date);
        } catch (Exception e) {
            throw new IllegalArgumentException("Start date must be in YYYY-MM-DD format.");
        }
    }

    private Date getEndDate() {
        String text = endDateField.getText().trim();

        if (ValidationUtil.isEmpty(text)) {
            throw new IllegalArgumentException("End date is required.");
        }

        try {
            LocalDate date = LocalDate.parse(text);
            return Date.valueOf(date);
        } catch (Exception e) {
            throw new IllegalArgumentException("End date must be in YYYY-MM-DD format.");
        }
    }

    private int getYear() {
        String text = yearField.getText().trim();

        if (!ValidationUtil.isPositiveInteger(text)) {
            throw new IllegalArgumentException("Year must be a positive number.");
        }

        return Integer.parseInt(text);
    }

    private void runTotalSalesBetweenDates() {
        List<Object[]> rows = salesReportDAO.getTotalSalesBetweenDates(getStartDate(), getEndDate());

        setColumns("Start Date", "End Date", "Total Sales");
        fillTable(rows);

        if (!rows.isEmpty()) {
            summaryLabel.setText("Total sales: " + rows.get(0)[2]);
        }
    }

    private void runMonthlySales() {
        List<Object[]> rows = salesReportDAO.getMonthlySales(getYear());

        setColumns("Year", "Month", "Total Sales");
        fillTable(rows);

        summaryLabel.setText("Monthly sales report returned " + rows.size() + " rows.");
    }

    private void runTopCustomers() {
        List<Object[]> rows = salesReportDAO.getTopCustomers(getStartDate(), getEndDate());

        setColumns("Client ID", "Client Name", "Client Type", "Total Spent");
        fillTable(rows);

        summaryLabel.setText(rows.isEmpty() ? "No results." : "Top customer: " + rows.get(0)[1]);
    }

    private void runMostSoldProducts() {
        List<Object[]> rows = salesReportDAO.getMostSoldProducts(getStartDate(), getEndDate());

        setColumns("Product ID", "Product Name", "Quantity Sold", "Total Sales");
        fillTable(rows);

        summaryLabel.setText(rows.isEmpty() ? "No results." : "Most sold product: " + rows.get(0)[1]);
    }

    private void runSalesByWarehouse() {
        List<Object[]> rows = salesReportDAO.getSalesByWarehouse(getStartDate(), getEndDate());

        setColumns("Warehouse ID", "Warehouse Name", "Total Sales");
        fillTable(rows);

        summaryLabel.setText("Sales by warehouse report returned " + rows.size() + " rows.");
    }

    private void runValidWarrantyItems() {
        List<Object[]> rows = salesReportDAO.getValidWarrantyItems();

        setColumns("Invoice ID", "Client Name", "Product Name", "Quantity", "Warranty End Date");
        fillTable(rows);

        summaryLabel.setText("Valid warranty items: " + rows.size());
    }

    private void runSalesByClientType() {
        List<Object[]> rows = salesReportDAO.getSalesByClientType(getStartDate(), getEndDate());

        setColumns("Client Type", "Total Sales");
        fillTable(rows);

        summaryLabel.setText("Sales by client type report returned " + rows.size() + " rows.");
    }

    private void runMostSoldCategories() {
        List<Object[]> rows = salesReportDAO.getMostSoldCategories(getStartDate(), getEndDate());

        setColumns("Category ID", "Category Name", "Quantity Sold", "Total Sales");
        fillTable(rows);

        summaryLabel.setText(rows.isEmpty() ? "No results." : "Most sold category: " + rows.get(0)[1]);
    }

    private void runSalesByClientCity() {
        List<Object[]> rows = salesReportDAO.getSalesByClientCity(getStartDate(), getEndDate());

        setColumns("City", "Total Sales");
        fillTable(rows);

        summaryLabel.setText(rows.isEmpty() ? "No results." : "Top city: " + rows.get(0)[0]);
    }

    private void setColumns(String... columns) {
        tableModel = TableUtil.createNonEditableTableModel(columns);
        reportTable.setModel(tableModel);
        TableUtil.setupTable(reportTable);
    }

    private void fillTable(List<Object[]> rows) {
        TableUtil.clearTable(tableModel);

        for (Object[] row : rows) {
            tableModel.addRow(row);
        }
    }

    private void clearReport() {
        setColumns("Result");
        TableUtil.clearTable(tableModel);
        summaryLabel.setText("Select a report and click Run Report.");

        startDateField.setText("2026-01-01");
        endDateField.setText("2026-12-31");
        yearField.setText("2026");
        updateFilterVisibility();
    }
}
