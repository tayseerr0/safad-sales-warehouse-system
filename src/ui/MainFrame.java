package ui;

import db.DBConnection;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;

    public MainFrame() {
        setTitle("SAFAD Sales and Warehouse Management System");
        setSize(1200, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 650));

        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createContentPanel(), BorderLayout.CENTER);

        showPage("Dashboard");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIStyle.SIDEBAR_BACKGROUND);
        sidebar.setPreferredSize(new Dimension(230, getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel title = new JLabel("SAFAD");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Management System");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(203, 213, 225));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(title);
        sidebar.add(subtitle);
        sidebar.add(Box.createVerticalStrut(25));

        String[] pages = {
                "Dashboard",
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

        contentPanel.add(createDashboardPanel(), "Dashboard");
        contentPanel.add(createPlaceholderPanel("Products", "Product management screen will be added here."), "Products");
        contentPanel.add(createPlaceholderPanel("Suppliers", "Supplier management screen will be added here."), "Suppliers");
        contentPanel.add(createPlaceholderPanel("Purchases", "Purchase invoice screen will be added here."), "Purchases");
        contentPanel.add(createPlaceholderPanel("Clients", "Client management screen will be added here."), "Clients");
        contentPanel.add(createPlaceholderPanel("Warehouses", "Warehouse management screen will be added here."), "Warehouses");
        contentPanel.add(createPlaceholderPanel("Sales", "Sales invoice screen will be added here."), "Sales");
        contentPanel.add(createPlaceholderPanel("Inventory", "Inventory screen will be added here."), "Inventory");
        contentPanel.add(createPlaceholderPanel("Reports", "Reports and charts screen will be added here."), "Reports");
        contentPanel.add(createPlaceholderPanel("Transfers", "Warehouse transfer screen will be added here."), "Transfers");

        return contentPanel;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = UIStyle.createPagePanel();

        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBackground(UIStyle.BACKGROUND);

        headerPanel.add(UIStyle.createTitle("Dashboard"));
        headerPanel.add(UIStyle.createSubtitle("Welcome to the SAFAD Sales and Warehouse Management System."));

        JPanel cardsPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        cardsPanel.setBackground(UIStyle.BACKGROUND);

        cardsPanel.add(createDashboardCard("Products", "Manage product catalog, categories, and brands."));
        cardsPanel.add(createDashboardCard("Purchases", "Record supplier purchases and increase inventory."));
        cardsPanel.add(createDashboardCard("Sales", "Create sales invoices and validate stock."));
        cardsPanel.add(createDashboardCard("Inventory", "Track product quantities across warehouses."));
        cardsPanel.add(createDashboardCard("Reports", "View advanced SQL reports and charts."));
        cardsPanel.add(createDashboardCard("Transfers", "Move stock between warehouses."));

        JPanel statusPanel = createDatabaseStatusPanel();

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(cardsPanel, BorderLayout.CENTER);
        panel.add(statusPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createDashboardCard(String title, String description) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(UIStyle.PANEL_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(UIStyle.TEXT_DARK);

        JTextArea descArea = new JTextArea(description);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descArea.setForeground(new Color(75, 85, 99));
        descArea.setBackground(UIStyle.PANEL_BACKGROUND);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setFocusable(false);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(descArea, BorderLayout.CENTER);

        return card;
    }

    private JPanel createDatabaseStatusPanel() {
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(UIStyle.BACKGROUND);

        boolean connected = DBConnection.testConnection();

        JLabel statusLabel = new JLabel(
                connected
                        ? "Database status: Connected to safad_db"
                        : "Database status: Failed to connect to safad_db"
        );

        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusLabel.setForeground(connected ? new Color(22, 163, 74) : new Color(220, 38, 38));

        statusPanel.add(statusLabel);

        return statusPanel;
    }

    private JPanel createPlaceholderPanel(String title, String message) {
        JPanel panel = UIStyle.createPagePanel();

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(UIStyle.BACKGROUND);

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(UIStyle.PANEL_BACKGROUND);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));

        JLabel titleLabel = UIStyle.createTitle(title);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel messageLabel = UIStyle.createSubtitle(message);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(titleLabel);
        box.add(Box.createVerticalStrut(10));
        box.add(messageLabel);

        centerPanel.add(box);

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private void showPage(String pageName) {
        cardLayout.show(contentPanel, pageName);
    }
}