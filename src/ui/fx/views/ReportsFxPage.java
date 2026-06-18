package ui.fx.views;

import dao.PurchaseReportDAO;
import dao.PurchaseReportDAO.ComboOption;
import dao.SalesReportDAO;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ui.fx.FxChartUtil;
import ui.fx.FxTableUtil;
import ui.fx.FxTheme;

import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportsFxPage extends VBox {

    private final SalesReportDAO salesReportDAO = new SalesReportDAO();
    private final PurchaseReportDAO purchaseReportDAO = new PurchaseReportDAO();

    private final TableView<Map<String, Object>> salesTable = new TableView<>();
    private final TableView<Map<String, Object>> purchaseTable = new TableView<>();
    private final TableView<Map<String, Object>> analysisTable = new TableView<>();
    private final TextField salesSearchField = FxTheme.textField("Search sales report");
    private final TextField purchaseSearchField = FxTheme.textField("Search purchase report");
    private final TextField analysisSearchField = FxTheme.textField("Search analysis report");
    private final Label salesSummaryLabel = new Label("Select a report and run it.");
    private final Label purchaseSummaryLabel = new Label("Select a report and run it.");
    private final Label analysisSummaryLabel = new Label("Select a report and run it.");

    private ComboBox<String> salesReportComboBox;
    private DatePicker salesStartDatePicker;
    private DatePicker salesEndDatePicker;
    private TextField salesYearField;
    private BorderPane salesChartPane;
    private Node salesChartCard;
    private SplitPane salesSplit;
    private DefaultTableModel currentSalesModel;

    private ComboBox<String> purchaseReportComboBox;
    private ComboBox<ComboOption> supplierComboBox;
    private ComboBox<ComboOption> productComboBox;
    private DatePicker purchaseStartDatePicker;
    private DatePicker purchaseEndDatePicker;
    private BorderPane purchaseChartPane;
    private Node purchaseChartCard;
    private SplitPane purchaseSplit;
    private DefaultTableModel currentPurchaseModel;

    private ComboBox<String> analysisReportComboBox;
    private BorderPane analysisChartPane;
    private Node analysisChartCard;
    private SplitPane analysisSplit;
    private DefaultTableModel currentAnalysisModel;

    public ReportsFxPage() {
        getStyleClass().add("ledger-page");
        TabPane content = createContent();
        VBox.setVgrow(content, Priority.ALWAYS);
        getChildren().add(content);
    }

    private TabPane createContent() {
        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("clean-tabs");
        tabs.getStyleClass().add("report-tabs");
        tabs.getTabs().add(new Tab("Sales", createSalesTab()));
        tabs.getTabs().add(new Tab("Purchases", createPurchaseTab()));
        tabs.getTabs().add(new Tab("Analysis", createAnalysisTab()));
        tabs.getTabs().forEach(tab -> tab.setClosable(false));
        return tabs;
    }

    private BorderPane createSalesTab() {
        salesReportComboBox = new ComboBox<>();
        salesReportComboBox.getItems().addAll(
                "Total Sales Between Dates",
                "Monthly Sales",
                "Top Customers",
                "Most Sold Products",
                "Sales By Warehouse",
                "Valid Warranty Items",
                "Sales By Client Type",
                "Most Sold Categories",
                "Sales By Client City"
        );
        salesReportComboBox.getStyleClass().add("report-selector");
        salesReportComboBox.getSelectionModel().selectFirst();
        salesStartDatePicker = new DatePicker(LocalDate.of(2026, 1, 1));
        salesEndDatePicker = new DatePicker(LocalDate.of(2026, 12, 31));
        salesYearField = FxTheme.textField("2026");
        salesYearField.setText("2026");
        salesSummaryLabel.getStyleClass().add("muted-label");

        Button run = FxTheme.primaryButton("Run Report");
        Button clear = FxTheme.secondaryButton("Clear");
        run.setOnAction(e -> runSalesReport());
        clear.setOnAction(e -> clearSalesReport());

        HBox filters = FxTheme.ledgerCommandBar(salesReportComboBox, salesStartDatePicker, salesEndDatePicker, salesYearField, run, clear);
        filters.getStyleClass().add("report-filter-bar");
        filters.getStyleClass().add("report-ledger-filters");
        salesReportComboBox.setOnAction(e -> updateSalesFilters());
        updateSalesFilters();

        FxTheme.styleTable(salesTable);
        salesSearchField.textProperty().addListener((obs, oldText, newText) -> applySalesSearch());

        salesChartPane = new BorderPane();
        salesChartPane.setCenter(FxChartUtil.barChart("Sales Chart", Map.of("No data", 0)));

        salesChartCard = FxTheme.ledgerSurface("Sales Chart", FxTheme.ledgerCommandBar(new Label("Muted report chart")), salesChartPane);
        salesChartCard.getStyleClass().add("report-chart-card");
        VBox salesResultsCard = FxTheme.ledgerSurface("Sales Results", FxTheme.ledgerCommandBar(salesSearchField), new VBox(6,
                salesSummaryLabel,
                salesTable
        ));
        salesResultsCard.getStyleClass().add("report-result-card");
        salesSplit = new SplitPane(
                salesResultsCard,
                salesChartCard
        );
        salesSplit.getStyleClass().add("report-split");
        salesSplit.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        salesSplit.setDividerPositions(0.62);
        updateSalesChartVisibility();

        BorderPane pane = new BorderPane();
        pane.getStyleClass().add("report-workbench");
        pane.setTop(filters);
        pane.setCenter(salesSplit);
        BorderPane.setMargin(filters, new javafx.geometry.Insets(0, 0, 16, 0));
        return pane;
    }

    private BorderPane createPurchaseTab() {
        purchaseReportComboBox = new ComboBox<>();
        purchaseReportComboBox.getItems().addAll(
                "Suppliers for Product",
                "Purchase Invoices by Supplier and Date",
                "Total Quantity Purchased per Product",
                "Total Purchase Amount per Product",
                "Total Purchase Amount per Supplier",
                "Purchase Amount per Supplier Between Dates",
                "Purchase Amount by Month"
        );
        purchaseReportComboBox.getStyleClass().add("report-selector");
        purchaseReportComboBox.getSelectionModel().selectFirst();

        supplierComboBox = new ComboBox<>();
        productComboBox = new ComboBox<>();
        FxTheme.styleComboBox(supplierComboBox);
        FxTheme.styleComboBox(productComboBox);
        purchaseStartDatePicker = new DatePicker(LocalDate.of(2026, 1, 1));
        purchaseEndDatePicker = new DatePicker(LocalDate.now());
        purchaseSummaryLabel.getStyleClass().add("muted-label");
        loadPurchaseFilters();

        Button run = FxTheme.primaryButton("Run Report");
        Button refresh = FxTheme.secondaryButton("Refresh Lists");
        Button clear = FxTheme.secondaryButton("Clear");
        run.setOnAction(e -> runPurchaseReport());
        refresh.setOnAction(e -> loadPurchaseFilters());
        clear.setOnAction(e -> clearPurchaseReport());

        HBox filters = FxTheme.ledgerCommandBar(purchaseReportComboBox, supplierComboBox, productComboBox,
                purchaseStartDatePicker, purchaseEndDatePicker, refresh, run, clear);
        filters.getStyleClass().add("report-filter-bar");
        filters.getStyleClass().add("report-ledger-filters");
        purchaseReportComboBox.setOnAction(e -> updatePurchaseFilters());
        updatePurchaseFilters();

        FxTheme.styleTable(purchaseTable);
        purchaseSearchField.textProperty().addListener((obs, oldText, newText) -> applyPurchaseSearch());

        purchaseChartPane = new BorderPane();
        purchaseChartPane.setCenter(FxChartUtil.barChart("Purchase Chart", Map.of("No data", 0)));

        purchaseChartCard = FxTheme.ledgerSurface("Report Chart", FxTheme.ledgerCommandBar(new Label("Muted report chart")), purchaseChartPane);
        purchaseChartCard.getStyleClass().add("report-chart-card");
        VBox purchaseResultsCard = FxTheme.ledgerSurface("Purchase Results", FxTheme.ledgerCommandBar(purchaseSearchField), new VBox(6,
                purchaseSummaryLabel,
                purchaseTable
        ));
        purchaseResultsCard.getStyleClass().add("report-result-card");
        purchaseSplit = new SplitPane(
                purchaseResultsCard,
                purchaseChartCard
        );
        purchaseSplit.getStyleClass().add("report-split");
        purchaseSplit.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        purchaseSplit.setDividerPositions(0.62);
        updatePurchaseChartVisibility();

        BorderPane pane = new BorderPane();
        pane.getStyleClass().add("report-workbench");
        pane.setTop(filters);
        pane.setCenter(purchaseSplit);
        BorderPane.setMargin(filters, new javafx.geometry.Insets(0, 0, 16, 0));
        return pane;
    }

    private BorderPane createAnalysisTab() {
        analysisReportComboBox = new ComboBox<>();
        analysisReportComboBox.getItems().addAll(
                "Highest Demand and Supply Products",
                "Average Selling Price and Profit"
        );
        analysisReportComboBox.getStyleClass().add("report-selector");
        analysisReportComboBox.getSelectionModel().selectFirst();
        analysisSummaryLabel.getStyleClass().add("muted-label");

        Button run = FxTheme.primaryButton("Run Report");
        Button clear = FxTheme.secondaryButton("Clear");
        run.setOnAction(e -> runAnalysisReport());
        clear.setOnAction(e -> clearAnalysisReport());

        HBox filters = FxTheme.ledgerCommandBar(analysisReportComboBox, run, clear);
        filters.getStyleClass().add("report-filter-bar");
        filters.getStyleClass().add("report-ledger-filters");
        analysisReportComboBox.setOnAction(e -> updateAnalysisChartVisibility());

        FxTheme.styleTable(analysisTable);
        analysisSearchField.textProperty().addListener((obs, oldText, newText) -> applyAnalysisSearch());

        analysisChartPane = new BorderPane();
        analysisChartPane.setCenter(FxChartUtil.barChart("Analysis Chart", Map.of("No data", 0)));

        analysisChartCard = FxTheme.ledgerSurface("Analysis Chart", FxTheme.ledgerCommandBar(new Label("Muted report chart")), analysisChartPane);
        analysisChartCard.getStyleClass().add("report-chart-card");
        VBox analysisResultsCard = FxTheme.ledgerSurface("Analysis Results", FxTheme.ledgerCommandBar(analysisSearchField), new VBox(6,
                analysisSummaryLabel,
                analysisTable
        ));
        analysisResultsCard.getStyleClass().add("report-result-card");
        analysisSplit = new SplitPane(
                analysisResultsCard,
                analysisChartCard
        );
        analysisSplit.getStyleClass().add("report-split");
        analysisSplit.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        analysisSplit.setDividerPositions(0.62);
        updateAnalysisChartVisibility();

        BorderPane pane = new BorderPane();
        pane.getStyleClass().add("report-workbench");
        pane.setTop(filters);
        pane.setCenter(analysisSplit);
        BorderPane.setMargin(filters, new javafx.geometry.Insets(0, 0, 16, 0));
        return pane;
    }

    private void updateSalesFilters() {
        String report = salesReportComboBox.getValue();
        boolean year = "Monthly Sales".equals(report);
        boolean dates = !year && !"Valid Warranty Items".equals(report);
        setVisible(salesStartDatePicker, dates);
        setVisible(salesEndDatePicker, dates);
        setVisible(salesYearField, year);
        updateSalesChartVisibility();
    }

    private void updatePurchaseFilters() {
        String report = purchaseReportComboBox.getValue();
        setVisible(supplierComboBox, false);
        setVisible(productComboBox, false);
        setVisible(purchaseStartDatePicker, false);
        setVisible(purchaseEndDatePicker, false);

        if ("Suppliers for Product".equals(report)) {
            setVisible(productComboBox, true);
        } else if ("Purchase Invoices by Supplier and Date".equals(report)) {
            setVisible(supplierComboBox, true);
            setVisible(purchaseStartDatePicker, true);
            setVisible(purchaseEndDatePicker, true);
        } else if ("Purchase Amount per Supplier Between Dates".equals(report)) {
            setVisible(purchaseStartDatePicker, true);
            setVisible(purchaseEndDatePicker, true);
        }
        updatePurchaseChartVisibility();
    }

    private void updateSalesChartVisibility() {
        setChartVisible(salesSplit, salesChartCard, salesReportUsesChart(salesReportComboBox.getValue()));
    }

    private void updatePurchaseChartVisibility() {
        setChartVisible(purchaseSplit, purchaseChartCard, purchaseReportUsesChart(purchaseReportComboBox.getValue()));
    }

    private void updateAnalysisChartVisibility() {
        setChartVisible(analysisSplit, analysisChartCard, analysisReportUsesChart(analysisReportComboBox.getValue()));
    }

    private void setChartVisible(SplitPane split, Node chartCard, boolean visible) {
        if (split == null || chartCard == null) return;
        boolean currentlyShown = split.getItems().contains(chartCard);
        if (visible && !currentlyShown) {
            split.getItems().add(chartCard);
            split.setDividerPositions(0.62);
        } else if (!visible && currentlyShown) {
            split.getItems().remove(chartCard);
        }
    }

    private boolean salesReportUsesChart(String report) {
        return "Monthly Sales".equals(report)
                || "Top Customers".equals(report)
                || "Most Sold Products".equals(report)
                || "Sales By Warehouse".equals(report)
                || "Sales By Client Type".equals(report)
                || "Most Sold Categories".equals(report)
                || "Sales By Client City".equals(report);
    }

    private boolean purchaseReportUsesChart(String report) {
        return "Total Quantity Purchased per Product".equals(report)
                || "Total Purchase Amount per Product".equals(report)
                || "Total Purchase Amount per Supplier".equals(report)
                || "Purchase Amount per Supplier Between Dates".equals(report)
                || "Purchase Amount by Month".equals(report);
    }

    private boolean analysisReportUsesChart(String report) {
        return "Highest Demand and Supply Products".equals(report)
                || "Average Selling Price and Profit".equals(report);
    }

    private void setVisible(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void loadPurchaseFilters() {
        supplierComboBox.getItems().setAll(purchaseReportDAO.getSuppliers());
        productComboBox.getItems().setAll(purchaseReportDAO.getProducts());
        if (!supplierComboBox.getItems().isEmpty()) supplierComboBox.getSelectionModel().selectFirst();
        if (!productComboBox.getItems().isEmpty()) productComboBox.getSelectionModel().selectFirst();
    }

    private void runSalesReport() {
        try {
            String report = salesReportComboBox.getValue();
            DefaultTableModel model;

            switch (report) {
                case "Monthly Sales" -> {
                    model = modelFromRows(new String[]{"Year", "Month", "Total Sales"}, salesReportDAO.getMonthlySales(Integer.parseInt(salesYearField.getText())));
                    salesSummaryLabel.setText("Monthly sales rows: " + model.getRowCount());
                    setSalesChart("Monthly Sales", chartFromModel(model, "Month", "Total Sales", 12), true);
                }
                case "Top Customers" -> {
                    model = modelFromRows(new String[]{"Client ID", "Client Name", "Client Type", "Total Spent"}, salesReportDAO.getTopCustomers(startDate(), endDate()));
                    salesSummaryLabel.setText(model.getRowCount() == 0 ? "No customers found." : "Top customer: " + model.getValueAt(0, 1));
                    setSalesChart("Top Customers", chartFromModel(model, "Client Name", "Total Spent", 8));
                }
                case "Most Sold Products" -> {
                    model = modelFromRows(new String[]{"Product ID", "Product Name", "Quantity Sold", "Total Sales"}, salesReportDAO.getMostSoldProducts(startDate(), endDate()));
                    salesSummaryLabel.setText(model.getRowCount() == 0 ? "No products found." : "Most sold product: " + model.getValueAt(0, 1));
                    setSalesChart("Most Sold Products", chartFromModel(model, "Product Name", "Quantity Sold", 8));
                }
                case "Sales By Warehouse" -> {
                    model = modelFromRows(new String[]{"Warehouse ID", "Warehouse", "Invoice ID", "Date", "Client", "Invoice Amount", "Warehouse Total"}, salesReportDAO.getSalesInvoicesByWarehouse(startDate(), endDate()));
                    salesSummaryLabel.setText("Invoice rows grouped by warehouse: " + model.getRowCount());
                    setSalesChart("Sales by Warehouse", latestValueChart(model, "Warehouse", "Warehouse Total"));
                }
                case "Valid Warranty Items" -> {
                    model = modelFromRows(new String[]{"Invoice ID", "Client Name", "Product Name", "Quantity", "Warranty End Date"}, salesReportDAO.getValidWarrantyItems());
                    salesSummaryLabel.setText("Valid warranty items: " + model.getRowCount());
                    setSalesChart("Warranty Items", Map.of("Items", model.getRowCount()));
                }
                case "Sales By Client Type" -> {
                    model = modelFromRows(new String[]{"Client Type", "Total Sales"}, salesReportDAO.getSalesByClientType(startDate(), endDate()));
                    salesSummaryLabel.setText("Client type rows: " + model.getRowCount());
                    setSalesChart("Sales by Client Type", chartFromModel(model, "Client Type", "Total Sales", 8));
                }
                case "Most Sold Categories" -> {
                    model = modelFromRows(new String[]{"Category ID", "Category Name", "Quantity Sold", "Total Sales"}, salesReportDAO.getMostSoldCategories(startDate(), endDate()));
                    salesSummaryLabel.setText(model.getRowCount() == 0 ? "No categories found." : "Most sold category: " + model.getValueAt(0, 1));
                    setSalesChart("Most Sold Categories", chartFromModel(model, "Category Name", "Quantity Sold", 8));
                }
                case "Sales By Client City" -> {
                    model = modelFromRows(new String[]{"City", "Total Sales"}, salesReportDAO.getSalesByClientCity(startDate(), endDate()));
                    salesSummaryLabel.setText(model.getRowCount() == 0 ? "No cities found." : "Top city: " + model.getValueAt(0, 0));
                    setSalesChart("Sales by Client City", chartFromModel(model, "City", "Total Sales", 8));
                }
                default -> {
                    model = modelFromRows(new String[]{"Invoice ID", "Date", "Client ID", "Client", "Warehouse ID", "Warehouse", "Payment Type", "Payment", "Amount"}, salesReportDAO.getSalesInvoicesBetweenDates(startDate(), endDate()));
                    salesSummaryLabel.setText("Total sales: " + sum(model, "Amount") + " | Invoices: " + model.getRowCount());
                    setSalesChart("Sales Invoices", chartFromModel(model, "Date", "Amount", 10));
                }
            }

            currentSalesModel = model;
            applySalesSearch();
        } catch (Exception e) {
            FxTheme.showError("Could not run sales report: " + e.getMessage());
        }
    }

    private void runPurchaseReport() {
        try {
            String report = purchaseReportComboBox.getValue();
            DefaultTableModel model;

            switch (report) {
                case "Suppliers for Product" -> model = purchaseReportDAO.getSuppliersForProduct(productComboBox.getValue().getId());
                case "Purchase Invoices by Supplier and Date" -> model = purchaseReportDAO.getPurchaseInvoicesBySupplierAndDate(supplierComboBox.getValue().getId(), purchaseStartDatePicker.getValue(), purchaseEndDatePicker.getValue());
                case "Total Quantity Purchased per Product" -> model = purchaseReportDAO.getTotalQuantityPurchasedPerProduct();
                case "Total Purchase Amount per Product" -> model = purchaseReportDAO.getTotalPurchaseAmountPerProduct();
                case "Total Purchase Amount per Supplier" -> model = purchaseReportDAO.getTotalPurchaseAmountPerSupplier();
                case "Purchase Amount per Supplier Between Dates" -> model = purchaseReportDAO.getTotalPurchaseAmountPerSupplierBetweenDates(purchaseStartDatePicker.getValue(), purchaseEndDatePicker.getValue());
                case "Purchase Amount by Month" -> model = purchaseReportDAO.getPurchaseAmountByMonth();
                default -> model = purchaseReportDAO.getTotalPurchaseAmountPerProduct();
            }

            currentPurchaseModel = model;
            purchaseSummaryLabel.setText(report + " | Rows: " + model.getRowCount());
            setPurchaseChart(report, purchaseChartData(report, model), "Purchase Amount by Month".equals(report));
            applyPurchaseSearch();
        } catch (Exception e) {
            FxTheme.showError("Could not run purchase report: " + e.getMessage());
        }
    }

    private void runAnalysisReport() {
        try {
            String report = analysisReportComboBox.getValue();
            DefaultTableModel model;

            switch (report) {
                case "Highest Demand and Supply Products" -> model = purchaseReportDAO.getHighestDemandAndSupplyProducts();
                case "Average Selling Price and Profit" -> model = purchaseReportDAO.getAverageSellingPriceAndProfitPerProduct();
                default -> model = purchaseReportDAO.getHighestDemandAndSupplyProducts();
            }

            currentAnalysisModel = model;
            analysisSummaryLabel.setText(report + " | Rows: " + model.getRowCount());
            setAnalysisChart(report, purchaseChartData(report, model));
            applyAnalysisSearch();
        } catch (Exception e) {
            FxTheme.showError("Could not run analysis report: " + e.getMessage());
        }
    }

    private Date startDate() {
        return Date.valueOf(salesStartDatePicker.getValue());
    }

    private Date endDate() {
        return Date.valueOf(salesEndDatePicker.getValue());
    }

    private DefaultTableModel modelFromRows(String[] columns, List<Object[]> rows) {
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        for (Object[] row : rows) model.addRow(row);
        return model;
    }

    private void applySalesSearch() {
        filterModel(currentSalesModel, salesTable, salesSearchField.getText());
    }

    private void applyPurchaseSearch() {
        filterModel(currentPurchaseModel, purchaseTable, purchaseSearchField.getText());
    }

    private void applyAnalysisSearch() {
        filterModel(currentAnalysisModel, analysisTable, analysisSearchField.getText());
    }

    private void filterModel(DefaultTableModel source, TableView<Map<String, Object>> table, String keyword) {
        if (source == null) return;

        DefaultTableModel filtered = new DefaultTableModel();
        for (int col = 0; col < source.getColumnCount(); col++) filtered.addColumn(source.getColumnName(col));

        String text = keyword == null ? "" : keyword.trim().toLowerCase();
        for (int row = 0; row < source.getRowCount(); row++) {
            if (text.isEmpty() || rowMatches(source, row, text)) {
                Object[] values = new Object[source.getColumnCount()];
                for (int col = 0; col < source.getColumnCount(); col++) values[col] = source.getValueAt(row, col);
                filtered.addRow(values);
            }
        }

        FxTableUtil.fillFromModel(table, filtered);
    }

    private boolean rowMatches(DefaultTableModel model, int row, String keyword) {
        for (int col = 0; col < model.getColumnCount(); col++) {
            Object value = model.getValueAt(row, col);
            if (value != null && value.toString().toLowerCase().contains(keyword)) return true;
        }
        return false;
    }

    private Map<String, Number> purchaseChartData(String report, DefaultTableModel model) {
        if ("Total Quantity Purchased per Product".equals(report)) return chartFromModel(model, "Product", "Total Quantity Purchased", 8);
        if ("Total Purchase Amount per Product".equals(report)) return chartFromModel(model, "Product", "Total Purchase Amount", 8);
        if ("Total Purchase Amount per Supplier".equals(report)) return chartFromModel(model, "Supplier", "Total Purchase Amount", 8);
        if ("Purchase Amount per Supplier Between Dates".equals(report)) return chartFromModel(model, "Supplier", "Total Purchase Amount", 8);
        if ("Purchase Amount by Month".equals(report)) return chartFromModel(model, "Month", "Total Purchase Amount", 12);
        if ("Highest Demand and Supply Products".equals(report)) return chartFromModel(model, "Product", "Demand / Supply %", 8);
        if ("Average Selling Price and Profit".equals(report)) return chartFromModel(model, "Product", "Average Profit", 8);
        return Map.of("Rows", model.getRowCount());
    }

    private Map<String, Number> chartFromModel(DefaultTableModel model, String labelColumn, String valueColumn, int limit) {
        Map<String, Number> values = new LinkedHashMap<>();
        int labelIndex = columnIndex(model, labelColumn);
        int valueIndex = columnIndex(model, valueColumn);
        if (labelIndex < 0 || valueIndex < 0) return Map.of("No chart", 0);

        int rowLimit = Math.min(model.getRowCount(), limit);
        for (int row = 0; row < rowLimit; row++) {
            values.put(shorten(String.valueOf(model.getValueAt(row, labelIndex))), number(model.getValueAt(row, valueIndex)));
        }

        if (values.isEmpty()) values.put("No data", 0);
        return values;
    }

    private Map<String, Number> latestValueChart(DefaultTableModel model, String labelColumn, String valueColumn) {
        Map<String, Number> values = new LinkedHashMap<>();
        int labelIndex = columnIndex(model, labelColumn);
        int valueIndex = columnIndex(model, valueColumn);
        if (labelIndex < 0 || valueIndex < 0) return Map.of("No chart", 0);

        for (int row = 0; row < model.getRowCount(); row++) {
            values.put(String.valueOf(model.getValueAt(row, labelIndex)), number(model.getValueAt(row, valueIndex)));
        }

        if (values.isEmpty()) values.put("No data", 0);
        return values;
    }

    private BigDecimal sum(DefaultTableModel model, String columnName) {
        int index = columnIndex(model, columnName);
        BigDecimal total = BigDecimal.ZERO;
        if (index < 0) return total;

        for (int row = 0; row < model.getRowCount(); row++) {
            Object value = model.getValueAt(row, index);
            if (value instanceof BigDecimal amount) total = total.add(amount);
            else if (value instanceof Number number) total = total.add(BigDecimal.valueOf(number.doubleValue()));
        }
        return total;
    }

    private int columnIndex(DefaultTableModel model, String columnName) {
        for (int i = 0; i < model.getColumnCount(); i++) {
            if (columnName.equals(model.getColumnName(i))) return i;
        }
        return -1;
    }

    private Number number(Object value) {
        if (value instanceof Number number) return number;
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }

    private String shorten(String text) {
        if (text == null || text.length() <= 16) return text;
        return text.substring(0, 13) + "...";
    }

    private void setSalesChart(String title, Map<String, Number> data) {
        salesChartPane.setCenter(FxChartUtil.barChart(title, data));
    }

    private void setSalesChart(String title, Map<String, Number> data, boolean connectedPlot) {
        salesChartPane.setCenter(connectedPlot ? FxChartUtil.connectedPlot(title, data) : FxChartUtil.barChart(title, data));
    }

    private void setPurchaseChart(String title, Map<String, Number> data) {
        purchaseChartPane.setCenter(FxChartUtil.barChart(title, data));
    }

    private void setPurchaseChart(String title, Map<String, Number> data, boolean connectedPlot) {
        purchaseChartPane.setCenter(connectedPlot ? FxChartUtil.connectedPlot(title, data) : FxChartUtil.barChart(title, data));
    }

    private void setAnalysisChart(String title, Map<String, Number> data) {
        analysisChartPane.setCenter(FxChartUtil.barChart(title, data));
    }

    private void clearSalesReport() {
        currentSalesModel = null;
        salesTable.getItems().clear();
        salesTable.getColumns().clear();
        salesSummaryLabel.setText("Select a report and run it.");
        setSalesChart("Sales Chart", Map.of("No data", 0));
    }

    private void clearPurchaseReport() {
        currentPurchaseModel = null;
        purchaseTable.getItems().clear();
        purchaseTable.getColumns().clear();
        purchaseSummaryLabel.setText("Select a report and run it.");
        setPurchaseChart("Purchase Chart", Map.of("No data", 0));
    }

    private void clearAnalysisReport() {
        currentAnalysisModel = null;
        analysisTable.getItems().clear();
        analysisTable.getColumns().clear();
        analysisSummaryLabel.setText("Select a report and run it.");
        setAnalysisChart("Analysis Chart", Map.of("No data", 0));
    }
}
