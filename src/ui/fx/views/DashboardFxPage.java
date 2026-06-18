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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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

    public DashboardFxPage(Consumer<String> navigator) {
        this.navigator = navigator;
        getChildren().add(FxTheme.ledgerPage(
                "Operations Board",
                "Daily warehouse, sales, purchasing, and stock control overview.",
                createContent()
        ));
    }

    private VBox createContent() {
        VBox content = new VBox(10);
        content.getChildren().addAll(
                createLedgerHeader(),
                createTopMetrics(),
                createMainBoard()
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

        row.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));
        return row;
    }

    private HBox createMainBoard() {
        VBox left = new VBox(10, createOperationsStrip(), createAnalyticsPane());
        HBox.setHgrow(left, Priority.ALWAYS);

        VBox right = new VBox(10, createAttentionQueue(), createWarehouseLoadPane(), createQuickActions(), createSnapshotPane());
        right.setPrefWidth(330);
        right.setMinWidth(290);

        HBox board = new HBox(10, left, right);
        board.getStyleClass().add("dashboard-board");
        return board;
    }

    private HBox createOperationsStrip() {
        HBox row = new HBox(8,
                statusCard("Warehouse Load", warehouseLoadText(), "Capacity used across all warehouses"),
                statusCard("Sales Invoices", String.valueOf(rowCount("SalesInvoice")), "Recorded customer invoices"),
                statusCard("Purchase Invoices", String.valueOf(rowCount("PurchaseInvoice")), "Supplier invoice records")
        );
        row.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));
        return row;
    }

    private HBox createAnalyticsPane() {
        Node monthly = FxTheme.ledgerSurface("Monthly Sales Ledger", FxTheme.ledgerCommandBar(new Label("Trend")), FxChartUtil.connectedPlot(
                "Sales by Month - " + LocalDate.now().getYear(),
                monthlySales()
        ));
        Node products = FxTheme.ledgerSurface("Demand Ranking", FxTheme.ledgerCommandBar(new Label("Top sold items")), FxChartUtil.barChart(
                "Sold Quantity by Product",
                topSoldProducts()
        ));

        HBox row = new HBox(10, monthly, products);
        HBox.setHgrow(monthly, Priority.ALWAYS);
        HBox.setHgrow(products, Priority.ALWAYS);
        return row;
    }

    private VBox createAttentionQueue() {
        VBox list = new VBox(6);
        Map<String, Number> rows = lowStockRows();

        if (rows.isEmpty()) {
            list.getChildren().add(queueRow("Stock levels", "All items are above threshold", "sage"));
        } else {
            for (Map.Entry<String, Number> row : rows.entrySet()) {
                list.getChildren().add(queueRow(row.getKey(), "Short by " + row.getValue(), "clay"));
            }
        }

        return FxTheme.ledgerSurface("Attention Queue", FxTheme.ledgerCommandBar(new Label("Stock alerts")), list);
    }

    private VBox createWarehouseLoadPane() {
        VBox rows = new VBox(7);
        Map<String, WarehouseLoad> loads = warehouseLoads();

        if (loads.isEmpty()) {
            rows.getChildren().add(queueRow("Warehouses", "No capacity data available", "clay"));
        } else {
            for (Map.Entry<String, WarehouseLoad> entry : loads.entrySet()) {
                rows.getChildren().add(warehouseLoadRow(entry.getKey(), entry.getValue()));
            }
        }

        return FxTheme.ledgerSurface("Warehouse Board", FxTheme.ledgerCommandBar(new Label("Capacity use")), rows);
    }

    private VBox createQuickActions() {
        VBox actions = new VBox(7,
                actionButton("New Sale", "Sales", true),
                actionButton("New Purchase", "Purchases", true),
                actionButton("Stock Transfer", "Transfers", false),
                actionButton("Inventory Review", "Inventory", false),
                actionButton("Reports", "Reports", false)
        );
        return FxTheme.ledgerSurface("Workbench Shortcuts", FxTheme.ledgerCommandBar(new Label("Open module")), actions);
    }

    private VBox createSnapshotPane() {
        GridPane grid = new GridPane();
        grid.setHgap(7);
        grid.setVgap(7);
        grid.add(snapshotItem("Products", String.valueOf(safeCount(() -> new ProductDAO().getAllProducts().size()))), 0, 0);
        grid.add(snapshotItem("Clients", String.valueOf(safeCount(() -> new ClientDAO().getAllClients().size()))), 1, 0);
        grid.add(snapshotItem("Suppliers", String.valueOf(safeCount(() -> new SupplierDAO().getAllSuppliers().size()))), 0, 1);
        grid.add(snapshotItem("Warehouses", String.valueOf(safeCount(() -> new WarehouseDAO().getAllWarehouses().size()))), 1, 1);

        return FxTheme.ledgerSurface("Directory Snapshot", FxTheme.ledgerCommandBar(new Label("Master data")), grid);
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
