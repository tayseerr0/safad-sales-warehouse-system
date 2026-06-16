package ui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleBarChartPanel extends JPanel {

    private String title = "Chart";
    private String message = "Run a report to view chart data.";
    private List<ChartItem> items = new ArrayList<>();

    public SimpleBarChartPanel() {
        setBackground(UIStyle.PANEL_BACKGROUND);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIStyle.BORDER),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        setPreferredSize(new Dimension(780, 220));
        setMinimumSize(new Dimension(420, 170));
    }

    public void setChartData(String title, List<ChartItem> items) {
        this.title = title;
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
        this.message = this.items.isEmpty() ? "No chart data available." : "";
        repaint();
    }

    public void setMessage(String message) {
        this.message = message;
        this.items = new ArrayList<>();
        repaint();
    }

    public void clearChart() {
        this.message = "Run a report to view chart data.";
        this.items = new ArrayList<>();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        g2.setColor(UIStyle.TEXT_DARK);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g2.drawString(title, 20, 30);

        if (items == null || items.isEmpty()) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            g2.setColor(UIStyle.TEXT_MUTED);
            g2.drawString(message, 20, 60);
            return;
        }

        double max = 0;
        for (ChartItem item : items) {
            max = Math.max(max, item.value);
        }
        if (max <= 0) max = 1;

        int left = 160;
        int right = 52;
        int top = 52;
        int bottom = 26;
        int chartWidth = Math.max(40, width - left - right);
        int chartHeight = Math.max(40, height - top - bottom);
        int barGap = 8;
        int barHeight = Math.max(15, (chartHeight / items.size()) - barGap);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        for (int i = 0; i < items.size(); i++) {
            ChartItem item = items.get(i);
            int y = top + i * (barHeight + barGap);
            int barWidth = Math.max(2, (int) ((item.value / max) * chartWidth));

            g2.setColor(UIStyle.PRIMARY);
            g2.fillRoundRect(left, y, barWidth, barHeight, 8, 8);

            g2.setColor(UIStyle.TEXT_DARK);
            g2.drawString(shorten(item.label, 20), 20, y + barHeight - 3);

            g2.setColor(UIStyle.TEXT_MUTED);
            g2.drawString(formatNumber(item.value), Math.min(left + barWidth + 8, width - 45), y + barHeight - 3);
        }
    }

    private String shorten(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    private String formatNumber(double value) {
        if (value >= 1_000_000) {
            return String.format("%.1fM", value / 1_000_000.0);
        }
        if (value >= 1000) {
            return String.format("%.1fK", value / 1000.0);
        }
        if (value == Math.floor(value)) {
            return String.format("%.0f", value);
        }
        return String.format("%.2f", value);
    }

    public static class ChartItem {
        private final String label;
        private final double value;

        public ChartItem(String label, double value) {
            this.label = label;
            this.value = value;
        }
    }
}
