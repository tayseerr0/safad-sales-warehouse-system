package ui.theme;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {

    private final Color backgroundColor;
    private final Color borderColor;
    private final int arc;

    public RoundedPanel(Color backgroundColor, Color borderColor, int arc) {
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.arc = arc;

        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth() - 1;
        int height = getHeight() - 1;

        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, width, height, arc, arc);

        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, width, height, arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }
}
