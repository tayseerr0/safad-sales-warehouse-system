package ui.fx.views;

import dao.ClientDAO;
import dao.ProductDAO;
import dao.SupplierDAO;
import dao.WarehouseDAO;
import db.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class DashboardFxPage extends VBox {

    private final Consumer<String> navigator;

    public DashboardFxPage(Consumer<String> navigator) {
        this.navigator = navigator;
        getChildren().add(FxTheme.page(
                "Dashboard",
                "Operational overview for sales, stock, purchases, and reports.",
                createContent()
        ));
    }

    private VBox createContent() {
        VBox content = new VBox(12);
        content.getChildren().addAll(createTopMetrics(), createMainDashboard());
        return content;
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

        HBox row = new HBox(10,
                metricCard("Sales", money(sales), "Recorded revenue"),
                metricCard("Purchases", money(purchases), "Inventory cost"),
                metricCard("Profit", money(estimatedProfit), "Average item margin"),
                metricCard("Low Stock", String.valueOf(lowStockCount()), "Needs attention")
        );

        row.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));
        return row;
    }

    private HBox createMainDashboard() {
        VBox analytics = new VBox(10, createAnalyticsPane(), createOperationsStrip());
        HBox.setHgrow(analytics, Priority.ALWAYS);

        VBox side = new VBox(10, createQuickActions(), createSnapshotPane());
        side.setPrefWidth(275);
        side.setMinWidth(245);

        HBox row = new HBox(12, analytics, side);
        return row;
    }

    private HBox createAnalyticsPane() {
        Node monthly = FxTheme.card("Monthly Sales", FxChartUtil.connectedPlot(
                "Sales by Month - " + LocalDate.now().getYear(),
                monthlySales()
        ));
        Node products = FxTheme.card("Top Products", FxChartUtil.barChart(
                "Demand by Sold Quantity",
                topSoldProducts()
        ));

        HBox row = new HBox(10, monthly, products);
        HBox.setHgrow(monthly, Priority.ALWAYS);
        HBox.setHgrow(products, Priority.ALWAYS);
        return row;
    }

    private VBox createQuickActions() {
        VBox box = FxTheme.card("Quick Actions", new VBox(8,
                actionButton("New Sale", "Sales", true),
                actionButton("New Purchase", "Purchases", true),
                actionButton("Transfer Stock", "Transfers", false),
                actionButton("Inventory", "Inventory", false),
                actionButton("Reports", "Reports", false)
        ));
        box.getStyleClass().add("dashboard-panel");
        return box;
    }

    private VBox createSnapshotPane() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.add(snapshotItem("Products", String.valueOf(safeCount(() -> new ProductDAO().getAllProducts().size()))), 0, 0);
        grid.add(snapshotItem("Clients", String.valueOf(safeCount(() -> new ClientDAO().getAllClients().size()))), 1, 0);
        grid.add(snapshotItem("Suppliers", String.valueOf(safeCount(() -> new SupplierDAO().getAllSuppliers().size()))), 0, 1);
        grid.add(snapshotItem("Warehouses", String.valueOf(safeCount(() -> new WarehouseDAO().getAllWarehouses().size()))), 1, 1);

        VBox card = FxTheme.card("System Snapshot", grid);
        card.getStyleClass().add("dashboard-panel");
        return card;
    }

    private HBox createOperationsStrip() {
        HBox row = new HBox(10,
                statusCard("Warehouse Load", warehouseLoadText(), "Used capacity across warehouses"),
                statusCard("Open Flow", "Sales -> Inventory -> Reports", "Main review path"),
                statusCard("Focus", lowStockCount() + " low stock rows", "Check Inventory before purchases")
        );
        row.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));
        return row;
    }

    private Button actionButton(String text, String page, boolean primary) {
        Button button = primary ? FxTheme.primaryButton(text) : FxTheme.secondaryButton(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> navigator.accept(page));
        return button;
    }

    private VBox metricCard(String title, String value, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("dashboard-metric-title");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("dashboard-metric-value");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("muted-label");

        VBox card = new VBox(3, titleLabel, valueLabel, subtitleLabel);
        card.getStyleClass().add("dashboard-metric-card");
        card.setMinWidth(150);
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

    private VBox snapshotItem(String title, String value) {
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("dashboard-snapshot-value");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("muted-label");

        VBox box = new VBox(2, valueLabel, titleLabel);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("dashboard-snapshot-item");
        box.setMinWidth(115);
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
                values.put(shorten(rs.getString("product_name")), rs.getInt("quantity_sold"));
            }

        } catch (SQLException e) {
            values.put("No data", 0);
        }

        if (values.isEmpty()) values.put("No data", 0);
        return values;
    }

    private String shorten(String text) {
        if (text == null || text.length() <= 16) return text;
        return text.substring(0, 13) + "...";
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
}
