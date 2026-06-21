package ui;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import ui.views.CatalogPage;
import ui.views.ClientsPage;
import ui.views.DashboardPage;
import ui.views.InventoryPage;
import ui.views.ProductsPage;
import ui.views.PurchasesPage;
import ui.views.ReportsPage;
import ui.views.SalesPage;
import ui.views.SuppliersPage;
import ui.views.TransfersPage;
import ui.views.WarehousesPage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MainLayout {

    private final BorderPane root = new BorderPane();
    private final HBox topNavBar = new HBox(6);
    private final Label currentPageLabel = new Label("Dashboard");
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
        root.setTop(createTopArea());
        showPage("Dashboard");
    }

    public BorderPane getRoot() {
        return root;
    }

    private void registerPages() {
        pages.put("Dashboard", () -> new DashboardPage(this::showPage));
        pages.put("Products / Catalog", () -> new CatalogPage(new ProductsPage()));
        pages.put("Suppliers", SuppliersPage::new);
        pages.put("Purchases", PurchasesPage::new);
        pages.put("Clients", ClientsPage::new);
        pages.put("Warehouses", WarehousesPage::new);
        pages.put("Sales", SalesPage::new);
        pages.put("Inventory", InventoryPage::new);
        pages.put("Reports", ReportsPage::new);
        pages.put("Transfers", TransfersPage::new);
    }

    private VBox createTopArea() {
        return new VBox(createTopRibbon(), createTopNavigation());
    }

    private HBox createTopRibbon() {
        HBox ribbon = new HBox(14);
        ribbon.getStyleClass().add("top-ribbon");

        currentPageLabel.getStyleClass().add("current-page-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label tableModeLabel = new Label("Columns");
        tableModeLabel.getStyleClass().add("ribbon-utility-label");

        Node tableModeControls = TableUtil.globalColumnModeControls();
        tableModeControls.getStyleClass().add("ribbon-column-mode");

        Label userLabel = new Label("User: " + SessionManager.getCurrentUsername());
        userLabel.getStyleClass().add("ribbon-user");

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("logout-button");
        logoutButton.setOnAction(e -> logout());

        ribbon.getChildren().addAll(
                Theme.logo(42),
                currentPageLabel,
                spacer,
                tableModeLabel,
                tableModeControls,
                userLabel,
                logoutButton
        );

        return ribbon;
    }

    private HBox createTopNavigation() {
        topNavBar.getStyleClass().add("top-nav-bar");

        Label modulesLabel = new Label("Modules");
        modulesLabel.getStyleClass().add("top-nav-label");
        topNavBar.getChildren().add(modulesLabel);

        addSection("Dashboard");
        addSection("Products / Catalog", "Suppliers", "Clients", "Warehouses");
        addSection("Purchases", "Sales", "Inventory", "Transfers");
        addSection("Reports");

        return topNavBar;
    }

    private void logout() {
        SessionManager.logout();
        if (logoutHandler != null) {
            logoutHandler.run();
        }
    }

    private void addSection(String... pageNames) {
        for (String pageName : pageNames) {
            Button button = new Button(pageName);
            button.getStyleClass().setAll("button", "module-nav-button");
            button.setMinHeight(30);
            button.setOnAction(e -> showPage(pageName));
            navButtons.put(pageName, button);
            topNavBar.getChildren().add(button);
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
        currentPageLabel.setText(pageName);

        for (Map.Entry<String, Button> entry : navButtons.entrySet()) {
            entry.getValue().getStyleClass().remove("active-nav-button");
            if (entry.getKey().equals(pageName)) {
                entry.getValue().getStyleClass().add("active-nav-button");
            }
        }
    }
}
