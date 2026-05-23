import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {
    private int cornerRadius = 25;
    private Color backgroundColor;
    private int strokeWidth = 0;
    private Color strokeColor = null;
    private boolean useShadow = false;
    private int shadowGap = 6;

    public RoundedPanel(int radius, Color bgColor) {
        this(radius, bgColor, false);
    }

    public RoundedPanel(int radius, Color bgColor, boolean useShadow) {
        super();
        this.cornerRadius = radius;
        this.backgroundColor = bgColor;
        this.useShadow = useShadow;
        setOpaque(false);
    }

    public RoundedPanel(int radius, Color bgColor, Color strokeColor, int strokeWidth) {
        this(radius, bgColor, strokeColor, strokeWidth, false);
    }

    public RoundedPanel(int radius, Color bgColor, Color strokeColor, int strokeWidth, boolean useShadow) {
        super();
        this.cornerRadius = radius;
        this.backgroundColor = bgColor;
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.useShadow = useShadow;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int drawW = getWidth();
        int drawH = getHeight();
        int drawX = 0;
        int drawY = 0;

        if (useShadow) {
            drawW -= shadowGap;
            drawH -= shadowGap;

            // Draw Shadow
            g2.setColor(new Color(0, 0, 0, 60)); // Soft black shadow
            g2.fillRoundRect(drawX + shadowGap, drawY + shadowGap, drawW, drawH, cornerRadius, cornerRadius);
        }

        // Adjust for stroke
        if (strokeWidth > 0 && strokeColor != null) {
            drawX += strokeWidth / 2;
            drawY += strokeWidth / 2;
            drawW -= strokeWidth;
            drawH -= strokeWidth;
        }

        // Main Background
        g2.setColor(backgroundColor);
        g2.fillRoundRect(drawX, drawY, drawW, drawH, cornerRadius, cornerRadius);

        // Border
        if (strokeColor != null && strokeWidth > 0) {
            g2.setColor(strokeColor);
            g2.setStroke(new BasicStroke(strokeWidth));
            g2.drawRoundRect(drawX, drawY, drawW, drawH, cornerRadius, cornerRadius);
        }

        g2.dispose();
    }
}
