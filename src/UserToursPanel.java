import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

public class UserToursPanel extends JPanel {

    private JPanel cards;
    private CardLayout cardLayout;
    private TourDAO tourDAO;
    private BookingDAO bookingDAO;
    private Supplier<User> userSupplier;
    private JTable table;
    private DefaultTableModel model;
    private java.util.List<Tour> currentTours;
    private NotificationDAO notificationDAO;

    // Search Fields
    private JTextField txtSearch;

    public UserToursPanel(JPanel cards, CardLayout cardLayout, TourDAO tourDAO, BookingDAO bookingDAO,
            Supplier<User> userSupplier, NotificationDAO notificationDAO) {
        this.cards = cards;
        this.cardLayout = cardLayout;
        this.tourDAO = tourDAO;
        this.bookingDAO = bookingDAO;
        this.userSupplier = userSupplier;
        this.notificationDAO = notificationDAO;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 248, 250));

        // --- TOP BAR ---
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(13, 71, 161)); // Navy Blue
        top.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("Find & Book Tours");
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

        JLabel lblSearch = new JLabel("Search Tours:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchPanel.add(lblSearch);

        txtSearch = new JTextField(20);
        searchPanel.add(txtSearch);

        JButton btnSearch = new SimpleModernButton("Search", new Color(0, 180, 216)); // Cyan
        JButton btnShowAll = new SimpleModernButton("Show All", new Color(108, 117, 125)); // Grey

        searchPanel.add(btnSearch);
        searchPanel.add(btnShowAll);

        centerPanel.add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] cols = { "ID", "Title", "Type", "Total Cost", "Discount %", "Final Cost", "Created At" };
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

        JButton btnBook = new SimpleModernButton("Book Selected Tour", new Color(40, 167, 69)); // Green
        bottom.add(btnBook);
        add(bottom, BorderLayout.SOUTH);

        // --- LOGIC ---
        btnSearch.addActionListener(e -> performSearch());
        btnShowAll.addActionListener(e -> loadTours());

        btnBook.addActionListener(e -> bookSelectedTour());

        // Initial Load (wait for panel to show or just load now)
        loadTours();

        // Add refresh on show
        this.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                loadTours();
            }

            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {
            }

            public void ancestorMoved(javax.swing.event.AncestorEvent event) {
            }
        });
    }

    private void loadTours() {
        currentTours = tourDAO.getAll();
        refreshTable(currentTours);
    }

    private void performSearch() {
        String query = txtSearch.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            loadTours();
            return;
        }
        if (currentTours == null)
            return;

        List<Tour> filtered = new java.util.ArrayList<>();
        for (Tour t : currentTours) {
            if (t.getTitle().toLowerCase().contains(query)) {
                filtered.add(t);
            }
        }
        refreshTable(filtered);
    }

    private void refreshTable(List<Tour> list) {
        model.setRowCount(0);
        for (Tour t : list) {
            model.addRow(new Object[] {
                    t.getId(), t.getTitle(), t.getType(), t.getTotalCost(), t.getDiscountPct(), t.getFinalCost(),
                    t.getCreatedAt()
            });
        }
    }

    private void bookSelectedTour() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a tour to book.");
            return;
        }

        // Get Tour from master list or table model (assuming order matches if filtered,
        // careful!)
        // Safest is to find by ID from table model
        int tourId = (int) model.getValueAt(row, 0);
        Tour selectedTour = null;
        for (Tour t : currentTours) {
            if (t.getId() == tourId) {
                selectedTour = t;
                break;
            }
        }
        if (selectedTour == null)
            return;

        User currentUser = userSupplier.get();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "Error: No user logged in.");
            return;
        }

        String input = JOptionPane.showInputDialog(this, "Enter number of people:", "Book Tour",
                JOptionPane.QUESTION_MESSAGE);
        if (input == null)
            return;

        try {
            int seats = Integer.parseInt(input.trim());
            if (seats <= 0) {
                JOptionPane.showMessageDialog(this, "Invalid number of people.");
                return;
            }

            double totalCost = selectedTour.getFinalCost() * seats;

            // Generate Ticket HTML Preview
            StringBuilder ticket = new StringBuilder();
            ticket.append("<html><body style='width: 300px; font-family: Segoe UI;'>");
            ticket.append("<h2 style='color: #03045e; text-align: center;'>SKYPORT TOUR TICKET</h2>");
            ticket.append("<hr>");
            ticket.append("<b>Customer:</b> ").append(currentUser.getFullName()).append("<br>");
            ticket.append("<b>Tour:</b> ").append(selectedTour.getTitle()).append("<br>");
            ticket.append("<b>Type:</b> ").append(selectedTour.getType()).append("<br>");
            ticket.append("<b>People:</b> ").append(seats).append("<br>");
            ticket.append("<hr>");
            ticket.append(String.format("<b>Price/Person:</b> $%.2f<br>", selectedTour.getFinalCost()));
            ticket.append(String.format("<h3 style='text-align: right;'>TOTAL: $%.2f</h3>", totalCost));
            ticket.append("</body></html>");

            int confirm = JOptionPane.showConfirmDialog(this, ticket.toString(),
                    "Confirm Booking", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                // Determine Booking SubType - Use Date or something relevant?
                // For Tours, maybe just the current date or Tour Type?
                // Let's use current date as 'Booking Date'
                String dateStr = java.time.LocalDate.now().toString();

                boolean ok = bookingDAO.createBooking(
                        currentUser.getId(),
                        "TOUR", // Type
                        tourId, // Ref ID
                        dateStr, // SubType (Date) to fit schema
                        seats,
                        totalCost);

                if (ok) {
                    notificationDAO.addNotification(currentUser.getId(),
                            "Tour Booking Confirmed: " + selectedTour.getTitle() + " (" + seats + " people)");
                    try {
                        generateTicketFile(selectedTour.getTitle(), seats, totalCost, ticket.toString());
                        JOptionPane.showMessageDialog(this, "Booking Confirmed!\nTicket saved to project folder.");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this,
                                "Booking Confirmed, but ticket file error: " + ex.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Booking Failed. Please try again.");
                }
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number.");
        }
    }

    private void generateTicketFile(String tourTitle, int seats, double total, String htmlContent) {
        try {
            String filename = "TourTicket_" + System.currentTimeMillis() + ".html";
            java.io.File file = new java.io.File(filename);
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                writer.write(htmlContent);
            }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            System.out.println("Ticket Gen Error: " + e.getMessage());
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
