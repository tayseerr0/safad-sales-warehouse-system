package ui.fx.views;

import dao.ClientDAO;
import dao.ProductDAO;
import dao.SupplierDAO;
import dao.WarehouseDAO;
import db.DBConnection;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import ui.fx.FxChartUtil;
import ui.fx.FxTheme;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class DashboardFxPage extends VBox {

    private final Consumer<String> navigator;
    private static final int CHART_SLIDE_COUNT = 2;
    private int chartSlideIndex;
    private BorderPane chartSlidePane;

    public DashboardFxPage(Consumer<String> navigator) {
        this.navigator = navigator;
        getStyleClass().add("ledger-page");
        VBox content = createContent();
        VBox.setVgrow(content, Priority.ALWAYS);
        getChildren().add(content);
    }

    private VBox createContent() {
        VBox content = new VBox(8);
        Node mainBoard = createMainBoard();
        VBox.setVgrow(mainBoard, Priority.ALWAYS);
        content.getChildren().addAll(
                createLedgerHeader(),
                createTopMetrics(),
                mainBoard
        );
        return content;
    }

    private HBox createLedgerHeader() {
        Label date = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")));
        date.getStyleClass().add("dashboard-ledger-date");

        Label workflow = new Label("Stock -> Sales -> Purchases -> Reports");
        workflow.getStyleClass().add("dashboard-ledger-path");

        HBox row = new HBox(10, date, workflow);
        row.getStyleClass().add("dashboard-ledger-header");
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(workflow, Priority.ALWAYS);
        return row;
    }

    private HBox createTopMetrics() {
        BigDecimal sales = amount("""
                SELECT COALESCE(SUM(sii.quantity * sii.selling_price), 0) AS amount
                FROM SalesInvoiceItem sii
                """);
        BigDecimal purchases = amount("""
                SELECT COALESCE(SUM(pii.quantity * pii.purchase_price), 0) AS amount
                FROM PurchaseInvoiceItem pii
                """);
        BigDecimal estimatedProfit = estimatedProfit();
        BigDecimal receivable = amount("""
                SELECT COALESCE(SUM(GREATEST(amount - payment, 0)), 0) AS amount
                FROM SalesInvoice
                """);

        HBox row = new HBox(8,
                metricCard("Sales Ledger", money(sales), "Total item revenue", "primary"),
                metricCard("Purchase Cost", money(purchases), "Inventory bought", "sage"),
                metricCard("Est. Profit", money(estimatedProfit), "Average margin method", "primary"),
                metricCard("Receivable", money(receivable), "Unpaid sales balance", "clay"),
                metricCard("Low Stock", String.valueOf(lowStockCount()), "Rows under threshold", "clay")
        );

        row.getStyleClass().add("dashboard-kpi-strip");
        row.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));
        return row;
    }

    private HBox createMainBoard() {
        Node chart = createChartSlideshow();
        Node operations = createOperationsPanel();
        Node workbench = createWorkbenchPanel();

        HBox board = new HBox(10, chart, operations, workbench);
        board.getStyleClass().add("dashboard-board");
        board.getStyleClass().add("dashboard-fit-board");
        board.setFillHeight(true);

        setBoardColumn(chart, 2.1);
        setBoardColumn(operations, 1.35);
        setBoardColumn(workbench, 1.0);
        return board;
    }

    private void setBoardColumn(Node node, double growWeight) {
        if (node instanceof Region region) {
            region.setPrefWidth(growWeight * 260);
            region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }
        HBox.setHgrow(node, Priority.ALWAYS);
    }

    private Node createChartSlideshow() {
        Label titleLabel = new Label("Dashboard Charts");
        titleLabel.getStyleClass().add("ledger-surface-title");

        Button previous = FxTheme.secondaryButton("Previous");
        Button next = FxTheme.secondaryButton("Next");
        previous.setOnAction(e -> showChartSlide(chartSlideIndex - 1));
        next.setOnAction(e -> showChartSlide(chartSlideIndex + 1));

        chartSlidePane = new BorderPane();
        chartSlidePane.getStyleClass().add("dashboard-chart-frame");
        chartSlidePane.setMinWidth(0);

        HBox controls = FxTheme.ledgerCommandBar(previous, next);
        controls.getStyleClass().add("dashboard-chart-controls");
        controls.setAlignment(Pos.CENTER);
        controls.setMaxWidth(Double.MAX_VALUE);

        VBox chartBody = new VBox(6, chartSlidePane, controls);
        chartBody.setMinWidth(0);
        VBox.setVgrow(chartSlidePane, Priority.ALWAYS);

        VBox surface = new VBox(8, titleLabel, chartBody);
        surface.getStyleClass().add("ledger-surface");
        surface.setMinWidth(0);
        VBox.setVgrow(chartBody, Priority.ALWAYS);

        showChartSlide(0);
        return surface;
    }

    private Node createOperationsAlertsPane() {
        HBox content = new HBox(8, titledBlock("Stock Alerts", createAttentionList()), titledBlock("Operations", createOperationsGrid()));
        content.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));
        return FxTheme.ledgerSurface("Operations + Alerts", FxTheme.ledgerCommandBar(new Label("Daily work")), content);
    }

    private GridPane createOperationsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(7);
        grid.setVgap(7);

        Node warehouse = statusCard("Warehouse Load", warehouseLoadText(), "Capacity used");
        Node sales = statusCard("Sales Invoices", String.valueOf(rowCount("SalesInvoice")), "Customer invoices");
        Node purchases = statusCard("Purchase Invoices", String.valueOf(rowCount("PurchaseInvoice")), "Supplier records");

        grid.add(warehouse, 0, 0, 2, 1);
        grid.add(sales, 0, 1);
        grid.add(purchases, 1, 1);
        growGridNode(warehouse);
        growGridNode(sales);
        growGridNode(purchases);
        return grid;
    }

    private Node createOperationsPanel() {
        VBox content = new VBox(8,
                createOperationsGrid(),
                titledBlock("Recent Invoices", createRecentInvoiceList()),
                titledBlock("Stock Alerts", createAttentionList()),
                titledBlock("Warehouse Load", createWarehouseLoadList())
        );
        content.getStyleClass().add("dashboard-balanced-column");
        VBox.setVgrow(content.getChildren().get(2), Priority.ALWAYS);
        VBox.setVgrow(content.getChildren().get(3), Priority.ALWAYS);
        return FxTheme.ledgerSurface("Operations", FxTheme.ledgerCommandBar(new Label("Alerts and capacity")), content);
    }

    private Node createWorkbenchPanel() {
        VBox content = new VBox(8,
                titledBlock("Shortcuts", createQuickActions()),
                titledBlock("Directory Snapshot", createSnapshotGrid()),
                titledBlock("Recent Transfers", createRecentTransferList())
        );
        content.getStyleClass().add("dashboard-balanced-column");
        VBox.setVgrow(content.getChildren().get(1), Priority.ALWAYS);
        VBox.setVgrow(content.getChildren().get(2), Priority.ALWAYS);
        return FxTheme.ledgerSurface("Workbench", FxTheme.ledgerCommandBar(new Label("Open and inspect")), content);
    }

    private void growGridNode(Node node) {
        if (node instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        GridPane.setHgrow(node, Priority.ALWAYS);
    }

    private void showChartSlide(int requestedIndex) {
        chartSlideIndex = Math.floorMod(requestedIndex, CHART_SLIDE_COUNT);
        chartSlidePane.setCenter(createChartSlide());
    }

    private String chartSlideTitle() {
        return chartSlideIndex == 0 ? "Monthly Sales Ledger" : "Demand Ranking";
    }

    private Node createChartSlide() {
        Node chart;
        if (chartSlideIndex == 0) {
            chart = FxChartUtil.connectedPlot(
                    "Sales by Month - " + LocalDate.now().getYear(),
                    monthlySales()
            );
        } else {
            chart = FxChartUtil.barChart(
                    "Sold Quantity by Product",
                    topSoldProducts()
            );
        }

        if (chart instanceof Region region) {
            region.setMinHeight(250);
            region.setPrefHeight(300);
            region.setMaxHeight(Double.MAX_VALUE);
        }
        return chart;
    }

    private VBox createAttentionList() {
        VBox list = new VBox(6);
        Map<String, Number> rows = lowStockRows();

        if (rows.isEmpty()) {
            list.getChildren().add(queueRow("Stock levels", "All items are above threshold", "sage"));
        } else {
            for (Map.Entry<String, Number> row : rows.entrySet()) {
                list.getChildren().add(queueRow(row.getKey(), "Short by " + row.getValue(), "clay"));
            }
        }

        return list;
    }

    private VBox createWarehouseLoadPane() {
        return FxTheme.ledgerSurface("Warehouse Board", FxTheme.ledgerCommandBar(new Label("Capacity use")), createWarehouseLoadList());
    }

    private VBox createWarehouseLoadList() {
        VBox rows = new VBox(7);
        Map<String, WarehouseLoad> loads = warehouseLoads();

        if (loads.isEmpty()) {
            rows.getChildren().add(queueRow("Warehouses", "No capacity data available", "clay"));
        } else {
            for (Map.Entry<String, WarehouseLoad> entry : loads.entrySet()) {
                rows.getChildren().add(warehouseLoadRow(entry.getKey(), entry.getValue()));
            }
        }

        return rows;
    }

    private Node createShortcutsSnapshotPane() {
        HBox content = new HBox(8, titledBlock("Shortcuts", createQuickActions()), titledBlock("Directory", createSnapshotGrid()));
        content.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));
        return FxTheme.ledgerSurface("Workbench + Snapshot", FxTheme.ledgerCommandBar(new Label("Open and inspect")), content);
    }

    private GridPane createQuickActions() {
        GridPane actions = new GridPane();
        actions.getStyleClass().add("dashboard-shortcut-grid");
        actions.setHgap(7);
        actions.setVgap(7);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(50);
            column.setHgrow(Priority.ALWAYS);
            column.setFillWidth(true);
            actions.getColumnConstraints().add(column);
        }
        for (int i = 0; i < 3; i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(33.333);
            row.setVgrow(Priority.ALWAYS);
            row.setFillHeight(true);
            actions.getRowConstraints().add(row);
        }

        addShortcut(actions, actionButton("New Sale", "Sales", true), 0, 0);
        addShortcut(actions, actionButton("New Purchase", "Purchases", true), 1, 0);
        addShortcut(actions, actionButton("Stock Transfer", "Transfers", false), 0, 1);
        addShortcut(actions, actionButton("Inventory Review", "Inventory", false), 1, 1);
        addShortcut(actions, actionButton("Reports", "Reports", false), 0, 2);
        addShortcut(actions, actionButton("Products / Catalog", "Products / Catalog", false), 1, 2);
        return actions;
    }

    private void addShortcut(GridPane grid, Button button, int column, int row) {
        button.getStyleClass().add("dashboard-shortcut-tile");
        button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        button.setWrapText(true);
        GridPane.setHgrow(button, Priority.ALWAYS);
        GridPane.setVgrow(button, Priority.ALWAYS);
        grid.add(button, column, row);
    }

    private GridPane createSnapshotGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(7);
        grid.setVgap(7);
        grid.add(snapshotItem("Products", String.valueOf(safeCount(() -> new ProductDAO().getAllProducts().size()))), 0, 0);
        grid.add(snapshotItem("Clients", String.valueOf(safeCount(() -> new ClientDAO().getAllClients().size()))), 1, 0);
        grid.add(snapshotItem("Suppliers", String.valueOf(safeCount(() -> new SupplierDAO().getAllSuppliers().size()))), 0, 1);
        grid.add(snapshotItem("Warehouses", String.valueOf(safeCount(() -> new WarehouseDAO().getAllWarehouses().size()))), 1, 1);

        return grid;
    }

    private VBox titledBlock(String title, Node content) {
        Label label = new Label(title);
        label.getStyleClass().add("ledger-section-label");

        VBox block = new VBox(6, label, content);
        block.getStyleClass().add("dashboard-compact-block");
        block.setMinWidth(0);
        block.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(content, Priority.ALWAYS);
        return block;
    }

    private VBox createRecentInvoiceList() {
        VBox rows = new VBox(6);
        rows.getStyleClass().add("dashboard-recent-list");
        String sql = """
                SELECT title, detail FROM (
                    SELECT invoice_date AS event_date,
                           sales_invoice_id AS event_id,
                           CONCAT('Sale #', sales_invoice_id, '  ', DATE_FORMAT(invoice_date, '%b %d')) AS title,
                           CONCAT('Amount ', FORMAT(amount, 2), ' | Paid ', FORMAT(payment, 2)) AS detail
                    FROM SalesInvoice
                    UNION ALL
                    SELECT invoice_date AS event_date,
                           purchase_invoice_id AS event_id,
                           CONCAT('Purchase #', purchase_invoice_id, '  ', DATE_FORMAT(invoice_date, '%b %d')) AS title,
                           CONCAT('Amount ', FORMAT(amount, 2), ' | Paid ', FORMAT(payment, 2)) AS detail
                    FROM PurchaseInvoice
                ) recent
                ORDER BY event_date DESC, event_id DESC
                LIMIT 3
                """;
        addRecentRows(rows, sql, "Invoices", "No recent invoices");
        return rows;
    }

    private VBox createRecentTransferList() {
        VBox rows = new VBox(6);
        rows.getStyleClass().add("dashboard-recent-list");
        String sql = """
                SELECT CONCAT('Transfer #', wt.transfer_id, '  ', DATE_FORMAT(wt.transfer_date, '%b %d')) AS title,
                       CONCAT(COALESCE(source.warehouse_name, wt.from_warehouse_id),
                              ' -> ',
                              COALESCE(destination.warehouse_name, wt.to_warehouse_id)) AS detail
                FROM WarehouseTransfer wt
                LEFT JOIN Warehouse source ON wt.from_warehouse_id = source.warehouse_id
                LEFT JOIN Warehouse destination ON wt.to_warehouse_id = destination.warehouse_id
                ORDER BY wt.transfer_date DESC, wt.transfer_id DESC
                LIMIT 3
                """;
        addRecentRows(rows, sql, "Transfers", "No recent transfers");
        return rows;
    }

    private void addRecentRows(VBox rows, String sql, String emptyTitle, String emptyDetail) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                rows.getChildren().add(recentRow(rs.getString("title"), rs.getString("detail")));
            }

        } catch (SQLException e) {
            rows.getChildren().clear();
        }

        if (rows.getChildren().isEmpty()) {
            rows.getChildren().add(recentRow(emptyTitle, emptyDetail));
        }
    }

    private HBox recentRow(String title, String detail) {
        HBox row = queueRow(title, detail, "sage");
        row.getStyleClass().add("dashboard-recent-row");
        return row;
    }

    private Button actionButton(String text, String page, boolean primary) {
        Button button = primary ? FxTheme.primaryButton(text) : FxTheme.secondaryButton(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> navigator.accept(page));
        return button;
    }

    private VBox metricCard(String title, String value, String subtitle, String tone) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("dashboard-metric-title");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("dashboard-metric-value");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("muted-label");
        subtitleLabel.setWrapText(true);

        VBox card = new VBox(3, titleLabel, valueLabel, subtitleLabel);
        card.getStyleClass().add("dashboard-metric-card");
        card.getStyleClass().add("metric-" + tone);
        card.setMinWidth(145);
        return card;
    }

    private VBox statusCard(String title, String value, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("dashboard-status-value");
        valueLabel.setWrapText(true);

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("muted-label");
        subtitleLabel.setWrapText(true);

        VBox card = new VBox(4, titleLabel, valueLabel, subtitleLabel);
        card.getStyleClass().add("dashboard-status-card");
        return card;
    }

    private HBox queueRow(String title, String detail, String tone) {
        Label marker = new Label(" ");
        marker.getStyleClass().add("queue-marker");
        marker.getStyleClass().add("queue-" + tone);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("queue-title");
        titleLabel.setWrapText(true);

        Label detailLabel = new Label(detail);
        detailLabel.getStyleClass().add("queue-detail");
        detailLabel.setWrapText(true);

        VBox text = new VBox(1, titleLabel, detailLabel);
        HBox.setHgrow(text, Priority.ALWAYS);

        HBox row = new HBox(7, marker, text);
        row.getStyleClass().add("queue-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox warehouseLoadRow(String name, WarehouseLoad load) {
        Label label = new Label(name + "  " + load.used + "/" + load.capacity);
        label.getStyleClass().add("queue-title");

        ProgressBar bar = new ProgressBar(load.ratio());
        bar.getStyleClass().add("warehouse-progress");
        bar.setMaxWidth(Double.MAX_VALUE);

        VBox row = new VBox(3, label, bar);
        row.getStyleClass().add("warehouse-load-row");
        return row;
    }

    private VBox snapshotItem(String title, String value) {
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("dashboard-snapshot-value");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("muted-label");

        VBox box = new VBox(1, valueLabel, titleLabel);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("dashboard-snapshot-item");
        box.setMinWidth(130);
        return box;
    }

    private BigDecimal amount(String sql) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                BigDecimal value = rs.getBigDecimal("amount");
                return value == null ? BigDecimal.ZERO : value;
            }

        } catch (SQLException e) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.ZERO;
    }

    private int rowCount(String table) {
        String sql = "SELECT COUNT(*) AS total FROM " + table;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() ? rs.getInt("total") : 0;

        } catch (SQLException e) {
            return 0;
        }
    }

    private int lowStockCount() {
        String sql = "SELECT COUNT(*) AS total FROM Inventory WHERE quantity < threshold";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() ? rs.getInt("total") : 0;

        } catch (SQLException e) {
            return 0;
        }
    }

    private Map<String, Number> lowStockRows() {
        Map<String, Number> values = new LinkedHashMap<>();
        String sql = """
                SELECT p.product_name, w.warehouse_name, i.quantity, i.threshold,
                       (i.threshold - i.quantity) AS shortage
                FROM Inventory i
                JOIN Product p ON i.product_id = p.product_id
                JOIN Warehouse w ON i.warehouse_id = w.warehouse_id
                WHERE i.quantity < i.threshold
                ORDER BY shortage DESC, p.product_name
                LIMIT 5
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String label = shorten(rs.getString("product_name"), 20)
                        + " @ " + shorten(rs.getString("warehouse_name"), 16)
                        + " (" + rs.getInt("quantity") + "/" + rs.getInt("threshold") + ")";
                values.put(label, rs.getInt("shortage"));
            }

        } catch (SQLException e) {
            return values;
        }

        return values;
    }

    private BigDecimal estimatedProfit() {
        String sql = """
                SELECT COALESCE(SUM((sales.avg_selling_price - COALESCE(purchases.avg_buying_price, 0)) * sales.total_sold), 0) AS amount
                FROM (
                    SELECT product_id,
                           AVG(selling_price) AS avg_selling_price,
                           SUM(quantity) AS total_sold
                    FROM SalesInvoiceItem
                    GROUP BY product_id
                ) sales
                LEFT JOIN (
                    SELECT product_id,
                           AVG(purchase_price) AS avg_buying_price
                    FROM PurchaseInvoiceItem
                    GROUP BY product_id
                ) purchases ON sales.product_id = purchases.product_id
                """;

        return amount(sql);
    }

    private String warehouseLoadText() {
        String sql = """
                SELECT COALESCE(SUM(i.quantity), 0) AS used_quantity,
                       COALESCE(SUM(w.capacity), 0) AS capacity
                FROM Warehouse w
                LEFT JOIN (
                    SELECT warehouse_id, SUM(quantity) AS quantity
                    FROM Inventory
                    GROUP BY warehouse_id
                ) i ON w.warehouse_id = i.warehouse_id
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int used = rs.getInt("used_quantity");
                int capacity = rs.getInt("capacity");
                if (capacity <= 0) return used + " units";
                int percent = (int) Math.round((used * 100.0) / capacity);
                return used + " / " + capacity + " (" + percent + "%)";
            }

        } catch (SQLException e) {
            return "Unavailable";
        }

        return "Unavailable";
    }

    private Map<String, WarehouseLoad> warehouseLoads() {
        Map<String, WarehouseLoad> values = new LinkedHashMap<>();
        String sql = """
                SELECT w.warehouse_name,
                       COALESCE(SUM(i.quantity), 0) AS used_quantity,
                       COALESCE(w.capacity, 0) AS capacity
                FROM Warehouse w
                LEFT JOIN Inventory i ON w.warehouse_id = i.warehouse_id
                GROUP BY w.warehouse_id, w.warehouse_name, w.capacity
                ORDER BY w.warehouse_name
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                values.put(shorten(rs.getString("warehouse_name"), 30),
                        new WarehouseLoad(rs.getInt("used_quantity"), rs.getInt("capacity")));
            }

        } catch (SQLException e) {
            return values;
        }

        return values;
    }

    private Map<String, Number> monthlySales() {
        Map<String, Number> values = new LinkedHashMap<>();
        String sql = """
                SELECT DATE_FORMAT(si.invoice_date, '%b') AS month_name,
                       COALESCE(SUM(sii.quantity * sii.selling_price), 0) AS amount
                FROM SalesInvoice si
                JOIN SalesInvoiceItem sii ON si.sales_invoice_id = sii.sales_invoice_id
                WHERE YEAR(si.invoice_date) = YEAR(CURRENT_DATE)
                GROUP BY YEAR(si.invoice_date), MONTH(si.invoice_date), DATE_FORMAT(si.invoice_date, '%b')
                ORDER BY MONTH(si.invoice_date)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                values.put(rs.getString("month_name"), rs.getBigDecimal("amount"));
            }

        } catch (SQLException e) {
            values.put("No data", 0);
        }

        if (values.isEmpty()) values.put("No data", 0);
        return values;
    }

    private Map<String, Number> topSoldProducts() {
        Map<String, Number> values = new LinkedHashMap<>();
        String sql = """
                SELECT p.product_name, SUM(sii.quantity) AS quantity_sold
                FROM SalesInvoiceItem sii
                JOIN Product p ON sii.product_id = p.product_id
                GROUP BY p.product_id, p.product_name
                ORDER BY quantity_sold DESC
                LIMIT 8
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                values.put(shorten(rs.getString("product_name"), 16), rs.getInt("quantity_sold"));
            }

        } catch (SQLException e) {
            values.put("No data", 0);
        }

        if (values.isEmpty()) values.put("No data", 0);
        return values;
    }

    private String shorten(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) return text;
        return text.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private String money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toString();
    }

    private int safeCount(CountLoader loader) {
        try {
            return loader.load();
        } catch (Exception e) {
            return 0;
        }
    }

    private interface CountLoader {
        int load();
    }

    private static class WarehouseLoad {
        private final int used;
        private final int capacity;

        private WarehouseLoad(int used, int capacity) {
            this.used = used;
            this.capacity = capacity;
        }

        private double ratio() {
            if (capacity <= 0) return 0;
            return Math.min(1, Math.max(0, used / (double) capacity));
        }
    }
}
