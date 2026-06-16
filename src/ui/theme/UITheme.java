package ui.theme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UITheme {

    public static final Color PRIMARY = new Color(0, 132, 255);
    public static final Color PRIMARY_DARK = new Color(16, 42, 92);
    public static final Color PRIMARY_HOVER = new Color(0, 102, 204);
    public static final Color CYAN = new Color(14, 165, 233);
    public static final Color SECONDARY = new Color(71, 85, 105);
    public static final Color SECONDARY_HOVER = new Color(51, 65, 85);
    public static final Color SUCCESS = new Color(22, 163, 74);
    public static final Color DANGER = new Color(220, 38, 38);
    public static final Color DANGER_HOVER = new Color(185, 28, 28);

    public static final Color BACKGROUND = new Color(243, 247, 252);
    public static final Color PANEL_BACKGROUND = Color.WHITE;
    public static final Color PANEL_SOFT = new Color(248, 250, 252);
    public static final Color SIDEBAR_BACKGROUND = new Color(10, 18, 32);
    public static final Color SIDEBAR_BUTTON = new Color(15, 27, 46);
    public static final Color SIDEBAR_BUTTON_ACTIVE = new Color(0, 132, 255);
    public static final Color SIDEBAR_BUTTON_HOVER = new Color(20, 42, 70);
    public static final Color TEXT_DARK = new Color(24, 32, 45);
    public static final Color TEXT_MUTED = new Color(96, 111, 130);
    public static final Color BORDER = new Color(220, 228, 238);
    public static final Color FIELD_BORDER = new Color(197, 209, 224);
    public static final Color TABLE_STRIPE = new Color(248, 251, 255);

    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font SECTION_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    private static final String HOVER_PROPERTY = "safadHoverInstalled";
    private static final String SIDEBAR_HOVER_PROPERTY = "safadSidebarHoverInstalled";
    private static final String SIDEBAR_ACTIVE_PROPERTY = "safadSidebarActive";

    public static void applyGlobalStyle() {
        Font defaultFont = LABEL_FONT;

        UIManager.put("Label.font", defaultFont);
        UIManager.put("Button.font", BUTTON_FONT);
        UIManager.put("TextField.font", defaultFont);
        UIManager.put("ComboBox.font", defaultFont);
        UIManager.put("Table.font", TABLE_FONT);
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("TabbedPane.font", BUTTON_FONT);
        UIManager.put("OptionPane.messageFont", defaultFont);
        UIManager.put("OptionPane.buttonFont", BUTTON_FONT);
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
        UIManager.put("SplitPane.border", BorderFactory.createEmptyBorder());
    }

    public static void stylePrimaryButton(JButton button) {
        styleButton(button, PRIMARY, PRIMARY_HOVER, Color.WHITE);
    }

    public static void styleButton(JButton button) {
        stylePrimaryButton(button);
    }

    public static void styleSecondaryButton(JButton button) {
        styleButton(button, SECONDARY, SECONDARY_HOVER, Color.WHITE);
    }

    public static void styleDangerButton(JButton button) {
        styleButton(button, DANGER, DANGER_HOVER, Color.WHITE);
    }

    private static void styleButton(JButton button, Color background, Color hoverBackground, Color foreground) {
        button.setFont(BUTTON_FONT);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(8, 16, 8, 16));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        int width = Math.max(112, button.getPreferredSize().width + 24);
        button.setPreferredSize(new Dimension(width, 38));
        button.setMinimumSize(new Dimension(96, 38));

        installButtonHover(button, background, hoverBackground);
    }

    private static void installButtonHover(JButton button, Color background, Color hoverBackground) {
        if (Boolean.TRUE.equals(button.getClientProperty(HOVER_PROPERTY))) {
            return;
        }

        button.putClientProperty(HOVER_PROPERTY, true);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hoverBackground);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(background);
            }
        });
    }

    public static void styleSidebarButton(JButton button) {
        styleSidebarButton(button, false);
    }

    public static void styleSidebarButton(JButton button, boolean active) {
        button.putClientProperty(SIDEBAR_ACTIVE_PROPERTY, active);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(active ? SIDEBAR_BUTTON_ACTIVE : SIDEBAR_BUTTON);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(10, 16, 10, 16));
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setPreferredSize(new Dimension(200, 42));

        installSidebarHover(button);
    }

    private static void installSidebarHover(JButton button) {
        if (Boolean.TRUE.equals(button.getClientProperty(SIDEBAR_HOVER_PROPERTY))) {
            return;
        }

        button.putClientProperty(SIDEBAR_HOVER_PROPERTY, true);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!Boolean.TRUE.equals(button.getClientProperty(SIDEBAR_ACTIVE_PROPERTY))) {
                    button.setBackground(SIDEBAR_BUTTON_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boolean active = Boolean.TRUE.equals(button.getClientProperty(SIDEBAR_ACTIVE_PROPERTY));
                button.setBackground(active ? SIDEBAR_BUTTON_ACTIVE : SIDEBAR_BUTTON);
            }
        });
    }

    public static void styleTextFieldIfPossible(JComponent component) {
        if (component instanceof JTextField field) {
            styleTextField(field);
        } else if (component instanceof JComboBox<?> comboBox) {
            styleComboBox(comboBox);
        }
    }

    public static void styleTextField(JTextField field) {
        field.setFont(LABEL_FONT);
        field.setForeground(TEXT_DARK);
        field.setBackground(field.isEditable() ? Color.WHITE : PANEL_SOFT);
        field.setCaretColor(PRIMARY);
        field.setBorder(createInputBorder());
        field.setPreferredSize(new Dimension(190, 36));
        field.setMinimumSize(new Dimension(130, 36));
    }

    public static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(LABEL_FONT);
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(TEXT_DARK);
        comboBox.setBorder(createInputBorder());
        comboBox.setPreferredSize(new Dimension(190, 36));
        comboBox.setMinimumSize(new Dimension(130, 36));
    }

    public static Border createInputBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        );
    }

    public static void styleLabel(JLabel label) {
        label.setFont(LABEL_FONT);
        label.setForeground(TEXT_DARK);
    }

    public static void styleTable(JTable table) {
        table.setFont(TABLE_FONT);
        table.setRowHeight(34);
        table.setForeground(TEXT_DARK);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(217, 235, 255));
        table.setSelectionForeground(TEXT_DARK);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(BORDER);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(PRIMARY_DARK);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.LEFT);
        table.setDefaultRenderer(Object.class, renderer);
    }

    public static JPanel createPagePanel() {
        JPanel panel = new JPanel(new BorderLayout(18, 18));
        panel.setBackground(BACKGROUND);
        panel.setBorder(createPageMargin());
        return panel;
    }

    public static Border createPageMargin() {
        return BorderFactory.createEmptyBorder(24, 26, 24, 26);
    }

    public static JPanel createHeaderPanel(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBackground(BACKGROUND);
        panel.add(createTitle(title), BorderLayout.NORTH);
        panel.add(createSubtitle(subtitle), BorderLayout.CENTER);
        return panel;
    }

    public static JPanel createCardPanel() {
        JPanel panel = new RoundedPanel(PANEL_BACKGROUND, BORDER, 18);
        panel.setLayout(new BorderLayout(14, 14));
        panel.setBackground(PANEL_BACKGROUND);
        return panel;
    }

    public static JPanel createSectionHeader(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BACKGROUND);

        JLabel label = new JLabel(title);
        label.setFont(SECTION_TITLE_FONT);
        label.setForeground(TEXT_DARK);

        panel.add(label, BorderLayout.WEST);
        return panel;
    }

    public static JScrollPane createTableScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(18);
        return scrollPane;
    }

    public static JScrollPane createPageScrollPane(Component component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BACKGROUND);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(18);
        return scrollPane;
    }

    public static JPanel createButtonPanel(int alignment) {
        JPanel panel = new JPanel(new FlowLayout(alignment, 8, 4));
        panel.setBackground(PANEL_BACKGROUND);
        return panel;
    }

    public static void styleSplitPane(JSplitPane splitPane, double resizeWeight) {
        splitPane.setResizeWeight(resizeWeight);
        splitPane.setOneTouchExpandable(false);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(7);
        splitPane.setBorder(null);
        splitPane.setBackground(BACKGROUND);
    }

    public static void styleTabbedPane(JTabbedPane tabs) {
        tabs.setFont(BUTTON_FONT);
        tabs.setBackground(PANEL_BACKGROUND);
        tabs.setForeground(TEXT_DARK);
        tabs.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }

    public static JLabel createTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(TITLE_FONT);
        label.setForeground(TEXT_DARK);
        return label;
    }

    public static JLabel createSubtitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SUBTITLE_FONT);
        label.setForeground(TEXT_MUTED);
        return label;
    }

    public static JLabel createPageTitle(String title, String subtitle) {
        JLabel label = new JLabel("<html><b>" + title + "</b><br><span style='font-size:10px;color:#607082;'>"
                + subtitle + "</span></html>");
        label.setFont(TITLE_FONT);
        label.setForeground(TEXT_DARK);
        return label;
    }
}
