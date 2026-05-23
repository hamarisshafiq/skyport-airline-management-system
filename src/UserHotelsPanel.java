import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

public class UserHotelsPanel extends JPanel {

    private JPanel cards;
    private CardLayout cardLayout;
    private HotelDAO hotelDAO;
    private BookingDAO bookingDAO;
    private Supplier<User> userSupplier;
    private JTable table;
    private DefaultTableModel model;
    private NotificationDAO notificationDAO;

    // Search Fields
    private JComboBox<String> cmbSearchMethod;
    private JTextField txtSearch;

    public UserHotelsPanel(JPanel cards, CardLayout cardLayout, HotelDAO hotelDAO, BookingDAO bookingDAO,
            Supplier<User> userSupplier, NotificationDAO notificationDAO) {
        this.cards = cards;
        this.cardLayout = cardLayout;
        this.hotelDAO = hotelDAO;
        this.bookingDAO = bookingDAO;
        this.userSupplier = userSupplier;
        this.notificationDAO = notificationDAO;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 248, 250));

        // --- TOP BAR ---
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(13, 71, 161)); // Navy Blue
        top.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("Find & Book Hotels");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JButton back = new SimpleModernButton("Back", new Color(100, 100, 100));
        back.addActionListener(e -> cardLayout.show(cards, "USER_DASH"));

        top.add(title, BorderLayout.WEST);
        top.add(back, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // --- CENTER: SEARCH + TABLE ---
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        JLabel lblMethod = new JLabel("Search By:");
        lblMethod.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchPanel.add(lblMethod);

        String[] methods = { "Name", "City", "Country" };
        cmbSearchMethod = new JComboBox<>(methods);
        cmbSearchMethod.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbSearchMethod.setBackground(Color.WHITE);
        searchPanel.add(cmbSearchMethod);

        txtSearch = new JTextField(20);
        searchPanel.add(txtSearch);

        JButton btnSearch = new SimpleModernButton("Search", new Color(0, 180, 216)); // Cyan
        JButton btnShowAll = new SimpleModernButton("Show All Hotels", new Color(108, 117, 125)); // Grey

        searchPanel.add(btnSearch);
        searchPanel.add(btnShowAll);

        centerPanel.add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] cols = { "ID", "Name", "City", "Country", "Category", "Contact", "Price/Night" };
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        centerPanel.add(scroll, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // --- BOTTOM: BOOK BUTTON ---
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        bottom.setOpaque(false);

        JButton btnBook = new SimpleModernButton("Book Selected Hotel", new Color(40, 167, 69)); // Green
        bottom.add(btnBook);
        add(bottom, BorderLayout.SOUTH);

        // --- LOGIC ---
        btnSearch.addActionListener(e -> performSearch());
        btnShowAll.addActionListener(e -> {
            txtSearch.setText("");
            loadHotels(hotelDAO.getAll());
        });

        btnBook.addActionListener(e -> bookSelectedHotel());

        // Initial Load
        loadHotels(hotelDAO.getAll());
    }

    private void performSearch() {
        String method = (String) cmbSearchMethod.getSelectedItem();
        String query = txtSearch.getText().trim();

        if (query.isEmpty()) {
            loadHotels(hotelDAO.getAll());
            return;
        }

        List<Hotel> results;
        if ("Name".equals(method)) {
            results = hotelDAO.searchByName(query);
        } else if ("City".equals(method)) {
            results = hotelDAO.searchByCity(query);
        } else {
            results = hotelDAO.searchByCountry(query);
        }
        loadHotels(results);
    }

    private void loadHotels(List<Hotel> list) {
        model.setRowCount(0);
        for (Hotel h : list) {
            model.addRow(new Object[] {
                    h.getId(), h.getName(), h.getCity(), h.getCountry(),
                    h.getCategory(), h.getContact(), h.getPricePerNight()
            });
        }
    }

    private void bookSelectedHotel() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a hotel to book.");
            return;
        }

        int hotelId = (int) model.getValueAt(row, 0);
        String hotelName = (String) model.getValueAt(row, 1);
        double pricePerNight = (double) model.getValueAt(row, 6);

        User currentUser = userSupplier.get();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Error: No user logged in.");
            return;
        }

        // 1. Enter Details Panel
        JPanel inputPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        JTextField txtDate = new JTextField("2025-01-01");
        JTextField txtNights = new JTextField("1");

        inputPanel.add(new JLabel("Check-in Date (YYYY-MM-DD):"));
        inputPanel.add(txtDate);
        inputPanel.add(new JLabel("Number of Nights:"));
        inputPanel.add(txtNights);

        int result = JOptionPane.showConfirmDialog(this, inputPanel,
                "Booking Details for " + hotelName, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION)
            return;

        try {
            String dateStr = txtDate.getText().trim();
            int nights = Integer.parseInt(txtNights.getText().trim());

            if (nights <= 0) {
                JOptionPane.showMessageDialog(this, "Invalid number of nights.");
                return;
            }

            // 2. Calculate Costs & Discounts
            double subtotal = pricePerNight * nights;
            double discountPercent = 0;

            if (nights > 20) {
                discountPercent = 0.10; // 10%
            } else if (nights > 10) {
                discountPercent = 0.05; // 5%
            }

            double discountAmount = subtotal * discountPercent;
            double finalTotal = subtotal - discountAmount;

            // 3. Generate Ticket / Confirmation Dialog
            StringBuilder ticket = new StringBuilder();
            ticket.append("<html><body style='width: 250px; font-family: Segoe UI;'>");
            ticket.append("<h2 style='color: #03045e; text-align: center;'>SKYPORT HOTEL TICKET</h2>");
            ticket.append("<hr>");
            ticket.append("<b>Customer:</b> ").append(currentUser.getFullName()).append("<br>");
            ticket.append("<b>Hotel:</b> ").append(hotelName).append("<br>");
            ticket.append("<b>Check-in:</b> ").append(dateStr).append("<br>");
            ticket.append("<b>Duration:</b> ").append(nights).append(" Nights<br>");
            ticket.append("<hr>");
            ticket.append(String.format("<b>Price/Night:</b> $%.2f<br>", pricePerNight));
            ticket.append(String.format("<b>Subtotal:</b> $%.2f<br>", subtotal));

            if (discountPercent > 0) {
                ticket.append(String.format("<b style='color: green;'>Discount (%.0f%%):</b> -$%.2f<br>",
                        discountPercent * 100, discountAmount));
            } else {
                ticket.append("<b>Discount:</b> None<br>");
            }

            ticket.append("<hr>");
            ticket.append(String.format("<h3 style='text-align: right;'>TOTAL: $%.2f</h3>", finalTotal));
            ticket.append("</body></html>");

            int confirm = JOptionPane.showConfirmDialog(this, ticket.toString(),
                    "Confirm Payment", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                // Store Date in Booking SubType for now (e.g., "Start Date | Standard")
                String bookingSubType = dateStr + " | Standard";

                boolean ok = bookingDAO.createBooking(
                        currentUser.getId(),
                        "HOTEL",
                        hotelId,
                        bookingSubType,
                        nights,
                        finalTotal);

                if (ok) {
                    notificationDAO.addNotification(currentUser.getId(),
                            "Hotel Booked: " + hotelName + " (" + nights + " nights)");
                    JOptionPane.showMessageDialog(this, "Booking Confirmed!\nYour ticket has been generated.");
                } else {
                    JOptionPane.showMessageDialog(this, "Booking Failed. Please try again.");
                }
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number entered for nights.");
        }
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.BLACK);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230, 230, 230));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(245, 248, 250));
        header.setForeground(new Color(100, 100, 100));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
    }

    // Custom Button for Visibility on Windows
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
