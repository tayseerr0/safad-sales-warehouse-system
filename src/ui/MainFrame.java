package ui;

import dao.ClientDAO;
import dao.ProductDAO;
import dao.SupplierDAO;
import dao.WarehouseDAO;
import db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private final Map<String, JButton> sidebarButtons = new LinkedHashMap<>();

    public MainFrame() {
        UIStyle.applyGlobalStyle();

        setTitle("SAFAD Sales and Warehouse Management System");
        setSize(1280, 760);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1080, 680));

        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createContentPanel(), BorderLayout.CENTER);

        showPage("Dashboard");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIStyle.SIDEBAR_BACKGROUND);
        sidebar.setPreferredSize(new Dimension(245, getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(22, 16, 18, 16));

        JLabel title = new JLabel("SAFAD");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Sales & Warehouse");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(203, 213, 225));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel navLabel = new JLabel("Navigation");
        navLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        navLabel.setForeground(new Color(148, 163, 184));
        navLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(title);
        sidebar.add(subtitle);
        sidebar.add(Box.createVerticalStrut(26));
        sidebar.add(navLabel);
        sidebar.add(Box.createVerticalStrut(8));

        String[] pages = {
                "Dashboard",
                "Catalog",
                "Products",
                "Suppliers",
                "Purchases",
                "Clients",
                "Warehouses",
                "Sales",
                "Inventory",
                "Reports",
                "Transfers"
        };

        for (String page : pages) {
            JButton button = new JButton(page);
            UIStyle.styleSidebarButton(button);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);

            button.addActionListener(e -> showPage(page));
            sidebarButtons.put(page, button);

            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(8));
        }

        sidebar.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("COMP333 Project");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footer.setForeground(new Color(148, 163, 184));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(footer);

        return sidebar;
    }

    private JPanel createContentPanel() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIStyle.BACKGROUND);

        addPage("Dashboard", createDashboardPanel());
        addPage("Catalog", new CategoryBrandPanel());
        addPage("Products", new ProductPanel());
        addPage("Suppliers", new SupplierPanel());
        addPage("Purchases", new PurchaseInvoicePanel());
        addPage("Clients", new ClientPanel());
        addPage("Warehouses", new WarehousePanel());
        addPage("Sales", new SalesInvoicePanel());
        addPage("Inventory", new InventoryPanel());
        addPage("Reports", createReportsPanel());
        addPage("Transfers", new WarehouseTransferPanel());
        return contentPanel;
    }

    private void addPage(String pageName, JComponent page) {
        contentPanel.add(UIStyle.createPageScrollPane(page), pageName);
    }

    private JPanel createDashboardPanel() {
        JPanel panel = UIStyle.createPagePanel();

        JPanel headerPanel = UIStyle.createHeaderPanel(
                "Dashboard",
                "Welcome to the SAFAD Sales and Warehouse Management System."
        );

        JPanel contentPanel = new JPanel(new BorderLayout(18, 18));
        contentPanel.setBackground(UIStyle.BACKGROUND);
        contentPanel.add(createSummaryPanel(), BorderLayout.NORTH);
        contentPanel.add(createNavigationCardsPanel(), BorderLayout.CENTER);

        JPanel statusPanel = createDatabaseStatusPanel();

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(statusPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 15));
        panel.setBackground(UIStyle.BACKGROUND);

        panel.add(createSummaryCard("Products", safeCount(() -> new ProductDAO().getAllProducts().size())));
        panel.add(createSummaryCard("Suppliers", safeCount(() -> new SupplierDAO().getAllSuppliers().size())));
        panel.add(createSummaryCard("Clients", safeCount(() -> new ClientDAO().getAllClients().size())));
        panel.add(createSummaryCard("Warehouses", safeCount(() -> new WarehouseDAO().getAllWarehouses().size())));

        return panel;
    }

    private JPanel createNavigationCardsPanel() {
        JPanel cardsPanel = new JPanel(new GridLayout(2, 4, 15, 15));
        cardsPanel.setBackground(UIStyle.BACKGROUND);

        cardsPanel.add(createDashboardCard("Catalog", "Manage product categories and brands.", "Catalog"));
        cardsPanel.add(createDashboardCard("Products", "Maintain product details and prices.", "Products"));
        cardsPanel.add(createDashboardCard("Suppliers", "Manage suppliers and supplied products.", "Suppliers"));
        cardsPanel.add(createDashboardCard("Purchases", "Record supplier purchases and stock increases.", "Purchases"));
        cardsPanel.add(createDashboardCard("Sales", "Create sales invoices and validate stock.", "Sales"));
        cardsPanel.add(createDashboardCard("Inventory", "Track stock and low inventory by warehouse.", "Inventory"));
        cardsPanel.add(createDashboardCard("Reports", "View sales and purchase SQL reports.", "Reports"));
        cardsPanel.add(createDashboardCard("Transfers", "Move stock between warehouses.", "Transfers"));

        return cardsPanel;
    }

    private JPanel createSummaryCard(String title, int value) {
        JPanel card = UIStyle.createCardPanel();

        JLabel valueLabel = new JLabel(String.valueOf(value));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(UIStyle.PRIMARY);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(UIStyle.TEXT_MUTED);

        card.add(valueLabel, BorderLayout.NORTH);
        card.add(titleLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createDashboardCard(String title, String description, String pageName) {
        JPanel card = UIStyle.createCardPanel();

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(UIStyle.TEXT_DARK);

        JTextArea descArea = new JTextArea(description);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descArea.setForeground(UIStyle.TEXT_MUTED);
        descArea.setBackground(UIStyle.PANEL_BACKGROUND);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setFocusable(false);

        JButton openButton = new JButton("Open");
        UIStyle.styleSecondaryButton(openButton);
        openButton.addActionListener(e -> showPage(pageName));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(descArea, BorderLayout.CENTER);
        card.add(openButton, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createDatabaseStatusPanel() {
        JPanel statusPanel = UIStyle.createCardPanel();

        boolean connected = DBConnection.testConnection();

        JLabel statusLabel = new JLabel(
                connected
                        ? "Database status: Connected to safad_db"
                        : "Database status: Failed to connect to safad_db"
        );

        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusLabel.setForeground(connected ? UIStyle.SUCCESS : UIStyle.DANGER);

        statusPanel.add(statusLabel, BorderLayout.WEST);

        return statusPanel;
    }

    private JPanel createReportsPanel() {
        JPanel panel = UIStyle.createPagePanel();

        panel.add(UIStyle.createHeaderPanel(
                "Reports",
                "Run sales and purchase reports from one place."
        ), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        UIStyle.styleTabbedPane(tabs);
        tabs.addTab("Sales Reports", new SalesReportsPanel());
        tabs.addTab("Purchase Reports", new PurchaseReportsPanel());

        panel.add(tabs, BorderLayout.CENTER);

        return panel;
    }

    private void showPage(String pageName) {
        cardLayout.show(contentPanel, pageName);
        updateActiveSidebarButton(pageName);
    }

    private void updateActiveSidebarButton(String activePage) {
        for (Map.Entry<String, JButton> entry : sidebarButtons.entrySet()) {
            UIStyle.styleSidebarButton(entry.getValue(), entry.getKey().equals(activePage));
        }
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
