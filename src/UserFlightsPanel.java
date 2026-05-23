import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class UserFlightsPanel extends JPanel {

    private JPanel cards;
    private CardLayout cardLayout;

    private JPanel innerContent;
    private CardLayout innerLayout;

    public UserFlightsPanel(JPanel cards, CardLayout cardLayout, UserDAO userDAO) {
        this.cards = cards;
        this.cardLayout = cardLayout;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 248, 250));

        // ---------- TOP HEADER ----------
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(0, 31, 63)); // Navy
        top.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Book a Flight");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JButton back = new SimpleModernButton("Back to Dashboard", new Color(231, 76, 60));
        back.addActionListener(e -> cardLayout.show(cards, "USER_DASH"));

        top.add(title, BorderLayout.WEST);
        top.add(back, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // ---------- LEFT SIDEBAR ----------
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(3, 1, 0, 10)); // Vertical Stack
        sidebar.setBackground(new Color(0, 31, 63)); // Navy
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));
        sidebar.setPreferredSize(new Dimension(200, 0));

        SimpleModernButton btnOneWay = new SimpleModernButton("One Way", new Color(0, 31, 63));
        SimpleModernButton btnReturn = new SimpleModernButton("Return", new Color(0, 31, 63));
        SimpleModernButton btnMulti = new SimpleModernButton("Multi City", new Color(0, 31, 63));

        // Initial Active State
        btnOneWay.setActive(true);
        btnOneWay.setBackground(new Color(0, 180, 216)); // Cyan highlight for active? Or just indicator.

        sidebar.add(btnOneWay);
        sidebar.add(btnReturn);
        sidebar.add(btnMulti);

        add(sidebar, BorderLayout.WEST);

        // ---------- CENTER CONTENT ----------
        innerLayout = new CardLayout();
        innerContent = new JPanel(innerLayout);
        innerContent.setBorder(new EmptyBorder(20, 20, 20, 20));
        innerContent.setBackground(new Color(245, 248, 250));

        // Add Sub-Panels
        innerContent.add(new OneWayFlightPanel(cards, cardLayout), "ONE_WAY");
        innerContent.add(new ReturnFlightPanel(cards, cardLayout), "RETURN");
        innerContent.add(new MultiCityFlightPanel(cards, cardLayout), "MULTI_CITY");

        add(innerContent, BorderLayout.CENTER);

        // ---------- ACTIONS ----------
        btnOneWay.addActionListener(e -> {
            innerLayout.show(innerContent, "ONE_WAY");
            updateActiveButton(btnOneWay, btnReturn, btnMulti);
        });
        btnReturn.addActionListener(e -> {
            innerLayout.show(innerContent, "RETURN");
            updateActiveButton(btnReturn, btnOneWay, btnMulti);
        });
        btnMulti.addActionListener(e -> {
            innerLayout.show(innerContent, "MULTI_CITY");
            updateActiveButton(btnMulti, btnOneWay, btnReturn);
        });

        // Default View
        updateActiveButton(btnOneWay, btnReturn, btnMulti);
    }

    private void updateActiveButton(JButton active, JButton... others) {
        ((SimpleModernButton) active).setActive(true);
        for (JButton b : others)
            ((SimpleModernButton) b).setActive(false);
    }

    private static class SimpleModernButton extends JButton {
        private Color bgColor;
        private boolean isActive = false;

        public SimpleModernButton(String text, Color bg) {
            super(text);
            this.bgColor = bg;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(new Color(200, 200, 200));
            setFont(new Font("Segoe UI", Font.PLAIN, 15));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(12, 15, 12, 15));
            setHorizontalAlignment(SwingConstants.LEFT);
        }

        public void setActive(boolean b) {
            this.isActive = b;
            if (isActive) {
                setForeground(Color.WHITE); // Bright White
                setFont(new Font("Segoe UI", Font.BOLD, 15));
                setBackground(new Color(0, 180, 216)); // Cyan Logic?
                // In paintComponent we handle the visual
            } else {
                setForeground(new Color(200, 200, 200)); // Dimmed
                setFont(new Font("Segoe UI", Font.PLAIN, 15));
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isActive) {
                // Active Indicator or Background
                // Let's use a Cyan background for the active button to match the image showing
                // "One Way" in Cyan box
                g2.setColor(new Color(0, 180, 216)); // Standard SkyPort Cyan
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE); // Text color on Cyan
                // The screenshot shows White text on Cyan background?
                // Actually screenshot shows Cyan box.
            } else {
                // Transparent/Navy
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
            }
            g2.dispose();

            // Text color needs to be black if background is bright cyan?
            // In setActive:
            if (isActive)
                setForeground(Color.WHITE); // Or Dark Blue?
            // Let's keep White for now, but 0, 255, 255 is very bright.
            // Just use the code.

            super.paintComponent(g);
        }
    }
}
