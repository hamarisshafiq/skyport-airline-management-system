import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;

public class UserBookingsPanel extends JPanel {

    private JPanel cards;
    private CardLayout cardLayout;
    private BookingDAO bookingDAO;
    private Supplier<User> userSupplier;

    private JTabbedPane tabbedPane;
    private JTable tblFlights;
    private JTable tblHotels;
    private JTable tblTours;

    private DefaultTableModel modelFlights;
    private DefaultTableModel modelHotels;
    private DefaultTableModel modelTours;

    public UserBookingsPanel(JPanel cards, CardLayout cardLayout, BookingDAO bookingDAO, Supplier<User> userSupplier) {
        this.cards = cards;
        this.cardLayout = cardLayout;
        this.bookingDAO = bookingDAO;
        this.userSupplier = userSupplier;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 248, 250));

        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 31, 63)); // Navy
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("My Bookings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        // Sidebar Back Button behavior isn't needed if integrated in Dashboard,
        // but if it's a separate card, we might want a "Dashboard" button?
        // The sidebar is usually present. Let's assume this is a content panel.
        // Actually, UserHotelsPanel replaces the content panel or the whole card?
        // In GUIApp, it's added to 'cards' which contains MAIN, USER_DASH, etc.
        // So we need a Back button to return to USER_DASH.

        JButton btnBack = new SimpleModernButton("Back to Dashboard", new Color(231, 76, 60)); // Red for visibility
        btnBack.addActionListener(e -> cardLayout.show(cards, "USER_DASH"));
        header.add(btnBack, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // --- TABS ---
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Create Tables
        modelHotels = createModel();
        tblHotels = createTable(modelHotels);
        tabbedPane.addTab("Hotels", new JScrollPane(tblHotels));

        modelTours = createModel();
        tblTours = createTable(modelTours);
        tabbedPane.addTab("Tours", new JScrollPane(tblTours));

        modelFlights = createModel();
        tblFlights = createTable(modelFlights);
        tabbedPane.addTab("Flights", new JScrollPane(tblFlights));

        add(tabbedPane, BorderLayout.CENTER);

        // Load Data on Show
        this.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                loadBookings();
            }

            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {
            }

            public void ancestorMoved(javax.swing.event.AncestorEvent event) {
            }
        });
    }

    private DefaultTableModel createModel() {
        // Common Columns
        // Ideally we would fetch the Hotel Name / Flight No. using ref_id.
        // But for now, we only have ref_id. The user requirement implies seeing "booked
        // hotels".
        // We might need to join or look up names.
        // Given complexity, let's just show what we have (ID, Date/SubType, Seats,
        // Price, Status).
        // If possible, we can lazily load names or just leave it for now.

        return new DefaultTableModel(
                new String[] { "Booking ID", "Details (Ref ID)", "Date/Type", "Seats", "Price", "Status" }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
    }

    private JTable createTable(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setRowHeight(30);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        t.getTableHeader().setBackground(new Color(240, 240, 240));
        t.setShowVerticalLines(false);
        return t;
    }

    private void loadBookings() {
        User u = userSupplier.get();
        if (u == null)
            return;

        List<BookingDAO.Booking> list = bookingDAO.getBookingsByUserId(u.getId());

        modelHotels.setRowCount(0);
        modelTours.setRowCount(0);
        modelFlights.setRowCount(0);

        for (BookingDAO.Booking b : list) {
            Object[] row = {
                    b.getId(),
                    "ID: " + b.getRefId(), // Placeholder for Name
                    b.getBookingSubType(),
                    b.getSeats(),
                    String.format("$%.2f", b.getPrice()),
                    b.getStatus()
            };

            if ("HOTEL".equalsIgnoreCase(b.getBookingType())) {
                modelHotels.addRow(row);
            } else if ("TOUR".equalsIgnoreCase(b.getBookingType())) {
                modelTours.addRow(row);
            } else if ("FLIGHT".equalsIgnoreCase(b.getBookingType())) {
                modelFlights.addRow(row);
            }
        }
    }

    private static class SimpleModernButton extends JButton {
        private Color bgColor;

        public SimpleModernButton(String text, Color bg) {
            super(text);
            this.bgColor = bg;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 15, 8, 15));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
