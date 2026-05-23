package ui;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class UIStyle {

    public static final Color PRIMARY = new Color(30, 64, 175);
    public static final Color PRIMARY_DARK = new Color(23, 37, 84);
    public static final Color BACKGROUND = new Color(245, 247, 250);
    public static final Color PANEL_BACKGROUND = Color.WHITE;
    public static final Color SIDEBAR_BACKGROUND = new Color(15, 23, 42);
    public static final Color SIDEBAR_BUTTON = new Color(30, 41, 59);
    public static final Color SIDEBAR_BUTTON_HOVER = new Color(51, 65, 85);
    public static final Color TEXT_DARK = new Color(31, 41, 55);

    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    public static void stylePrimaryButton(JButton button) {
        button.setFont(BUTTON_FONT);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 36));
    }

    public static void styleSidebarButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(SIDEBAR_BUTTON);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
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
        field.setPreferredSize(new Dimension(180, 34));
    }

    public static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(LABEL_FONT);
        comboBox.setPreferredSize(new Dimension(180, 34));
    }

    public static void styleTable(JTable table) {
        table.setFont(TABLE_FONT);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(229, 231, 235));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(PRIMARY_DARK);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
    }

    public static JPanel createPagePanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return panel;
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
        label.setForeground(new Color(75, 85, 99));
        return label;
    }
}