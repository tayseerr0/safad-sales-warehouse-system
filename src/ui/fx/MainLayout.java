package ui.fx;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ui.fx.views.CatalogFxPage;
import ui.fx.views.ClientsFxPage;
import ui.fx.views.DashboardFxPage;
import ui.fx.views.InventoryFxPage;
import ui.fx.views.ProductsFxPage;
import ui.fx.views.PurchasesFxPage;
import ui.fx.views.ReportsFxPage;
import ui.fx.views.SalesFxPage;
import ui.fx.views.SuppliersFxPage;
import ui.fx.views.TransfersFxPage;
import ui.fx.views.WarehousesFxPage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MainLayout {

    private final BorderPane root = new BorderPane();
    private final VBox sidebar = new VBox(8);
    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private final Map<String, Supplier<Node>> pages = new LinkedHashMap<>();
    private final Runnable logoutHandler;

    public MainLayout() {
        this(null);
    }

    public MainLayout(Runnable logoutHandler) {
        this.logoutHandler = logoutHandler;
        root.getStyleClass().add("app-root");

        registerPages();
        root.setLeft(createSidebar());
        showPage("Dashboard");
    }

    public BorderPane getRoot() {
        return root;
    }

    private void registerPages() {
        pages.put("Dashboard", () -> new DashboardFxPage(this::showPage));
        pages.put("Products / Catalog", () -> new CatalogFxPage(new ProductsFxPage()));
        pages.put("Suppliers", SuppliersFxPage::new);
        pages.put("Purchases", PurchasesFxPage::new);
        pages.put("Clients", ClientsFxPage::new);
        pages.put("Warehouses", WarehousesFxPage::new);
        pages.put("Sales", SalesFxPage::new);
        pages.put("Inventory", InventoryFxPage::new);
        pages.put("Reports", ReportsFxPage::new);
        pages.put("Transfers", TransfersFxPage::new);
    }

    private VBox createSidebar() {
        sidebar.getStyleClass().add("sidebar");

        Label title = new Label("SAFAD");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("Sales & Warehouse");
        subtitle.getStyleClass().add("app-subtitle");

        sidebar.getChildren().addAll(title, subtitle);

        addSection("Overview", "Dashboard");
        addSection("Master Data", "Products / Catalog", "Suppliers", "Clients", "Warehouses");
        addSection("Operations", "Purchases", "Sales", "Inventory", "Transfers");
        addSection("Analysis", "Reports");

        Label tableModeLabel = new Label("Table Columns");
        tableModeLabel.getStyleClass().add("nav-label");

        ComboBox<FxTableUtil.ColumnMode> tableModeBox = FxTableUtil.globalColumnModeBox();
        tableModeBox.setMaxWidth(Double.MAX_VALUE);

        sidebar.getChildren().addAll(tableModeLabel, tableModeBox);

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label userLabel = new Label("Logged in: " + SessionManager.getCurrentUsername());
        userLabel.getStyleClass().add("sidebar-user");

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("logout-button");
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setOnAction(e -> logout());

        Label footer = new Label("COMP333 Project");
        footer.getStyleClass().add("sidebar-footer");

        sidebar.getChildren().addAll(spacer, userLabel, logoutButton, footer);

        return sidebar;
    }

    private void logout() {
        SessionManager.logout();
        if (logoutHandler != null) {
            logoutHandler.run();
        }
    }

    private void addSection(String title, String... pageNames) {
        Label label = new Label(title);
        label.getStyleClass().add("nav-section-label");
        sidebar.getChildren().add(label);

        for (String pageName : pageNames) {
            Button button = new Button(pageName);
            button.getStyleClass().add("nav-button");
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(e -> showPage(pageName));
            navButtons.put(pageName, button);
            sidebar.getChildren().add(button);
        }
    }

    private void showPage(String pageName) {
        Supplier<Node> pageSupplier = pages.get(pageName);
        if (pageSupplier == null) {
            return;
        }

        ScrollPane scrollPane = new ScrollPane(pageSupplier.get());
        scrollPane.getStyleClass().add("page-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        root.setCenter(scrollPane);

        for (Map.Entry<String, Button> entry : navButtons.entrySet()) {
            entry.getValue().getStyleClass().remove("active-nav-button");
            if (entry.getKey().equals(pageName)) {
                entry.getValue().getStyleClass().add("active-nav-button");
            }
        }
    }
}
