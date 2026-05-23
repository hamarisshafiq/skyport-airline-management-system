import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Supplier;

public class PriceCalculationPanel extends JPanel {

    private static Flight flight;
    private static List<Passenger> passengers;
    private JPanel ticketPanel;

    // Dependencies
    private BookingDAO bookingDAO;
    private Supplier<User> userSupplier;
    private NotificationDAO notificationDAO; // Added dependency

    public static void setData(Flight f, List<Passenger> p) {
        flight = f;
        passengers = p;
    }

    public PriceCalculationPanel(JPanel cards, CardLayout cardLayout, BookingDAO bookingDAO,
            Supplier<User> userSupplier, NotificationDAO notificationDAO) {
        this.bookingDAO = bookingDAO;
        this.userSupplier = userSupplier;
        this.notificationDAO = notificationDAO; // Init

        setLayout(new GridBagLayout());
        setBackground(new Color(240, 240, 240));

        ticketPanel = new JPanel();
        ticketPanel.setLayout(new BoxLayout(ticketPanel, BoxLayout.Y_AXIS));
        ticketPanel.setBackground(Color.WHITE);
        ticketPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(30, 40, 30, 40)));
        ticketPanel.setPreferredSize(new Dimension(600, 600));

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                refresh(cards, cardLayout);
            }
        });

        add(ticketPanel);
    }

    private void refresh(JPanel cards, CardLayout cardLayout) {
        ticketPanel.removeAll();

        if (flight == null || passengers == null) {
            ticketPanel.add(new JLabel("No booking data found."));
            ticketPanel.revalidate();
            ticketPanel.repaint();
            return;
        }

        // --- HEADER ---
        JLabel title = new JLabel("SKYPORT AIRLINES");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(0, 31, 63));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        ticketPanel.add(title);

        ticketPanel.add(Box.createVerticalStrut(10));

        JLabel subTitle = new JLabel("Booking Summary & Invoice");
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subTitle.setForeground(Color.GRAY);
        subTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        ticketPanel.add(subTitle);

        ticketPanel.add(Box.createVerticalStrut(20));
        ticketPanel.add(createSeparator());
        ticketPanel.add(Box.createVerticalStrut(10));

        // --- FLIGHT INFO ---
        JPanel flightInfo = new JPanel(new GridLayout(2, 2, 10, 5));
        flightInfo.setBackground(Color.WHITE);
        flightInfo.add(createLabel("Flight", flight.getFlightNumber()));
        flightInfo.add(createLabel("Airline", flight.getAirlineName()));
        flightInfo.add(createLabel("From", flight.getOrigin()));
        flightInfo.add(createLabel("To", flight.getDestination()));
        flightInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        ticketPanel.add(flightInfo);

        ticketPanel.add(Box.createVerticalStrut(10));
        ticketPanel.add(createLabel("Date",
                flight.getDepartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

        ticketPanel.add(Box.createVerticalStrut(15));
        ticketPanel.add(createSeparator());
        ticketPanel.add(Box.createVerticalStrut(10));

        // --- PASSENGER & PRICING ---
        JPanel priceList = new JPanel();
        priceList.setLayout(new BoxLayout(priceList, BoxLayout.Y_AXIS));
        priceList.setBackground(Color.WHITE);

        double grandTotal = 0;
        // Logic copy from previous (ensure no regression)
        for (Passenger p : passengers) {
            double basePrice = p.getTravelClass().equals("Business") ? flight.getBusinessPrice()
                    : flight.getEconomyPrice();
            long daysBefore = ChronoUnit.DAYS.between(LocalDate.now(), flight.getDepartDateTime().toLocalDate());

            double discountPercent = 0;
            StringBuilder reason = new StringBuilder();

            if (p.getAge() < 12) {
                discountPercent += 10;
                reason.append("Child (10%) ");
            } else if (p.getAge() > 65) {
                discountPercent += 12;
                reason.append("Senior >65 (12%) ");
            } else if (p.getAge() > 50) {
                discountPercent += 8;
                reason.append("Senior >50 (8%) ");
            }

            if (daysBefore >= 30) {
                discountPercent += 7;
                reason.append("Early 30d+ (7%) ");
            } else if (daysBefore >= 20) {
                discountPercent += 5;
                reason.append("Early 20d+ (5%) ");
            }

            double discountAmount = basePrice * (discountPercent / 100);
            double finalPrice = basePrice - discountAmount;

            p.setFinalPrice(finalPrice);
            p.setDiscountReason(reason.toString().trim().isEmpty() ? "Standard" : reason.toString());
            grandTotal += finalPrice;

            JPanel row = new JPanel(new GridLayout(3, 1));
            row.setBackground(Color.WHITE);
            row.setBorder(new MatteBorder(0, 0, 1, 0, new Color(240, 240, 240))); // Separator line per row

            // Top Line: Name and Final Price
            JPanel top = new JPanel(new BorderLayout());
            top.setBackground(Color.WHITE);
            top.add(new JLabel(p.getName() + " (" + p.getTravelClass() + ")"), BorderLayout.WEST);
            JLabel lblFinal = new JLabel(String.format("$%.2f", finalPrice));
            lblFinal.setFont(new Font("Segoe UI", Font.BOLD, 14));
            top.add(lblFinal, BorderLayout.EAST);

            // Middle Line: Base Price
            JPanel mid = new JPanel(new BorderLayout());
            mid.setBackground(Color.WHITE);
            JLabel lblBase = new JLabel("Base Price: $" + basePrice);
            lblBase.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblBase.setForeground(Color.GRAY);
            mid.add(lblBase, BorderLayout.WEST);

            // Bottom Line: Discount info (if any)
            JPanel bot = new JPanel(new BorderLayout());
            bot.setBackground(Color.WHITE);
            if (discountAmount > 0) {
                JLabel lblDisc = new JLabel("Discount: -$" + String.format("%.2f", discountAmount));
                lblDisc.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lblDisc.setForeground(new Color(231, 76, 60)); // Red for discount

                JLabel lblReason = new JLabel("(" + reason.toString().trim() + ")");
                lblReason.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                lblReason.setForeground(Color.GRAY);

                JPanel discContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                discContainer.setBackground(Color.WHITE);
                discContainer.add(lblDisc);
                discContainer.add(lblReason);

                bot.add(discContainer, BorderLayout.WEST);
            } else {
                JLabel lblNoDisc = new JLabel("No Discounts Applied");
                lblNoDisc.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                lblNoDisc.setForeground(Color.LIGHT_GRAY);
                bot.add(lblNoDisc, BorderLayout.WEST);
            }

            row.add(top);
            row.add(mid);
            row.add(bot);

            priceList.add(row);
            priceList.add(Box.createVerticalStrut(10));
        }

        // Must be effectively final for lambda? No, primitive is fine. But wait,
        // grandTotal modification.
        // Actually the loop above is for UI. I need the total for save.
        // I will recalc total or capture it from the UI loop.
        final double totalPayable = grandTotal; // Capture for lambda

        priceList.setAlignmentX(Component.CENTER_ALIGNMENT);
        ticketPanel.add(priceList);

        ticketPanel.add(Box.createVerticalStrut(15));
        ticketPanel.add(createSeparator());
        ticketPanel.add(Box.createVerticalStrut(15));

        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBackground(Color.WHITE);
        JLabel lblTotal = new JLabel("TOTAL PAYABLE");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel valTotal = new JLabel(String.format("$%.2f", grandTotal));
        valTotal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valTotal.setForeground(new Color(40, 167, 69));

        totalPanel.add(lblTotal, BorderLayout.WEST);
        totalPanel.add(valTotal, BorderLayout.EAST);
        totalPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        ticketPanel.add(totalPanel);

        ticketPanel.add(Box.createVerticalStrut(30));

        JButton confirm = new JButton("Confirm & Generate Ticket");
        confirm.setBackground(new Color(40, 167, 69)); // Green
        confirm.setForeground(Color.WHITE);
        confirm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        confirm.setFocusPainted(false);
        confirm.setAlignmentX(Component.CENTER_ALIGNMENT);

        confirm.addActionListener(e -> {
            // 🚀 SAVE TO DATABASE
            saveBooking(totalPayable);

            // Generate Ticket
            TicketPDFGenerator.generateTickets(flight, passengers);

            // Go to Confirm
            cardLayout.show(cards, "BOOKING_CONFIRM");
        });

        ticketPanel.add(confirm);

        ticketPanel.revalidate();
        ticketPanel.repaint();
    }

    private void saveBooking(double totalAmount) {
        User u = userSupplier.get();
        if (u != null) {
            boolean success = bookingDAO.createBooking(
                    u.getId(),
                    "FLIGHT",
                    flight.getId(),
                    "ONEWAY", // Assuming oneway for this flow
                    passengers.size(),
                    totalAmount);
            if (success) {
                notificationDAO.addNotification(u.getId(), "Flight Booked: " + flight.getFlightNumber() + " ("
                        + flight.getOrigin() + "->" + flight.getDestination() + ")");
            } else {
                JOptionPane.showMessageDialog(this, "Warning: Failed to save booking to database.", "Error",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private JComponent createSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(Color.LIGHT_GRAY);
        sep.setMaximumSize(new Dimension(500, 1));
        return sep;
    }

    private JPanel createLabel(String key, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        JLabel k = new JLabel(key);
        k.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        k.setForeground(Color.GRAY);
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 14));
        p.add(k, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }
}
