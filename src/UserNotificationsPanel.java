import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;

public class UserNotificationsPanel extends JPanel {

    private JPanel cards;
    private CardLayout cardLayout;
    private NotificationDAO notificationDAO;
    private Supplier<User> userSupplier;
    private JPanel listPanel;
    private Timer refreshTimer; // Auto-refresh timer

    public UserNotificationsPanel(JPanel cards, CardLayout cardLayout, NotificationDAO notificationDAO,
            Supplier<User> userSupplier) {
        this.cards = cards;
        this.cardLayout = cardLayout;
        this.notificationDAO = notificationDAO;
        this.userSupplier = userSupplier;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 248, 250)); // Light Gray Background

        // --- TOP BAR ---
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(0, 31, 63)); // Navy Blue
        top.setBorder(new EmptyBorder(15, 30, 15, 30));

        JLabel title = new JLabel("Your Notifications");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        // Modern Flat Button for Back
        JButton back = new SimpleModernButton("Back to Dashboard", new Color(100, 100, 100));
        back.addActionListener(e -> cardLayout.show(cards, "USER_DASH"));

        top.add(title, BorderLayout.WEST);
        top.add(back, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // --- CONTENT ---
        // Main container for list with padding
        JPanel contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(new Color(245, 248, 250));
        contentArea.setBorder(new EmptyBorder(20, 0, 20, 0));

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(245, 248, 250)); // Match parent

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(new Color(245, 248, 250));

        contentArea.add(scroll, BorderLayout.CENTER);
        add(contentArea, BorderLayout.CENTER);

        // Initialize Timer (3 seconds refresh)
        refreshTimer = new Timer(3000, e -> loadNotifications());
        refreshTimer.setRepeats(true);

        // Refresh on show & Control Timer
        this.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                loadNotifications();
                refreshTimer.start();
            }

            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {
                refreshTimer.stop();
            }

            public void ancestorMoved(javax.swing.event.AncestorEvent event) {
            }
        });
    }

    private void loadNotifications() {
        listPanel.removeAll();
        User u = userSupplier.get();
        if (u == null) {
            listPanel.repaint();
            refreshTimer.stop(); // No user, no need to refresh
            return;
        } else {
            if (!refreshTimer.isRunning())
                refreshTimer.start();
        }

        List<Notification> notifs = notificationDAO.getUserNotifications(u.getId());

        if (notifs.isEmpty()) {
            listPanel.add(Box.createVerticalStrut(50));
            JLabel lbl = new JLabel("All caught up! No new notifications.", SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            lbl.setForeground(Color.GRAY);
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(lbl);
        } else {
            listPanel.add(Box.createVerticalStrut(10));
            for (Notification n : notifs) {
                listPanel.add(createNotificationItem(n));
                listPanel.add(Box.createVerticalStrut(15)); // Spacing between cards
            }
            listPanel.add(Box.createVerticalStrut(20)); // Bottom padding
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createNotificationItem(Notification n) {
        // Card Panel
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(900, 100)); // Wide yet compact
        card.setPreferredSize(new Dimension(800, 85));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtle Border & Shadow-ish feel
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230)), // Bottom Line
                new EmptyBorder(0, 0, 0, 0)));

        // Left Colored Strip (Accent)
        JPanel strip = new JPanel();
        strip.setPreferredSize(new Dimension(5, 85));

        // Determine color based on content? Defaulting to "Info" Cyan/Navy
        if (n.getMessage().toLowerCase().contains("cancel")) {
            strip.setBackground(new Color(231, 76, 60)); // Red
        } else if (n.getMessage().toLowerCase().contains("delay")) {
            strip.setBackground(new Color(243, 156, 18)); // Orange
        } else {
            strip.setBackground(new Color(0, 180, 216)); // Cyan
        }
        card.add(strip, BorderLayout.WEST);

        // Content Area
        JPanel content = new JPanel(new BorderLayout(10, 5));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(12, 15, 12, 15));

        // Message
        JLabel msg = new JLabel("<html><body style='width: 600px'>" + n.getMessage() + "</body></html>");
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        msg.setForeground(new Color(50, 50, 50));
        content.add(msg, BorderLayout.CENTER);

        // Time
        // Pretty Date Format
        // Time with Relative Logic
        String timeStr = getRelativeTime(n.getCreatedAt().toLocalDateTime());

        JLabel time = new JLabel(timeStr);
        time.setFont(new Font("Segoe UI", Font.BOLD, 12));
        time.setForeground(new Color(150, 150, 150));
        content.add(time, BorderLayout.EAST);

        card.add(content, BorderLayout.CENTER);

        return card;
    }

    // --- Relative Time Helper ---
    private String getRelativeTime(LocalDateTime date) {
        LocalDateTime now = LocalDateTime.now();
        Duration diff = Duration.between(date, now);

        long seconds = diff.getSeconds();
        long minutes = diff.toMinutes();
        long hours = diff.toHours();
        long days = diff.toDays();

        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " mins ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else if (days < 2) {
            return "Yesterday";
        } else {
            return date.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
        }
    }

    // --- Helper UI Class ---
    private static class SimpleModernButton extends JButton {
        private Color bgColor;

        public SimpleModernButton(String text, Color bg) {
            super(text);
            this.bgColor = bg;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 20, 8, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Hover effect can be simple darkening or just static if "remove effect" was
            // strict.
            // But "professionally" implies some interaction.
            // I'll make it static as per "remove effect" request strictly,
            // or maybe just color change.
            // The user said "remove effect", might mean the glow or 3D.
            // Flat color is safe.
            if (getModel().isRollover()) {
                g2.setColor(bgColor.brighter());
            } else {
                g2.setColor(bgColor);
            }

            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
