import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class UserFlightsResultPanel extends JPanel {

    private JPanel cards;
    private CardLayout cardLayout;

    private JTable table;
    private DefaultTableModel model;
    private List<Flight> currentFlights; // Store reference to retrieve object by index

    public UserFlightsResultPanel(JPanel cards, CardLayout cardLayout) {
        this.cards = cards;
        this.cardLayout = cardLayout;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 248, 250));

        // ---------- TITLE ----------
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(0, 31, 63)); // Navy
        top.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Available Flights");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JButton back = new JButton("Back to Search");
        back.addActionListener(e -> cardLayout.show(cards, "USER_FLIGHT_ONEWAY"));

        top.add(title, BorderLayout.WEST);
        top.add(back, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // ---------- TABLE ----------
        String[] cols = { "Airline", "Flight No", "From", "To", "Departure", "Arrival", "Price (Eco)" };
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(20, 20, 20, 20));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // ---------- BOTTOM ACTIONS ----------
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        bottom.setBackground(new Color(245, 248, 250));

        JButton btnBook = new JButton("Book Selected Flight");
        btnBook.setBackground(new Color(0, 31, 63)); // Navy Blue
        btnBook.setForeground(Color.WHITE);
        btnBook.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBook.setFocusPainted(false);
        btnBook.setOpaque(true);
        btnBook.setBorderPainted(false);
        btnBook.setPreferredSize(new Dimension(200, 45));

        btnBook.addActionListener(e -> bookSelected());

        bottom.add(btnBook);
        add(bottom, BorderLayout.SOUTH);
    }

    public void showFlights(List<Flight> flights) {
        this.currentFlights = flights;
        model.setRowCount(0);

        if (flights == null)
            return;

        for (Flight f : flights) {
            model.addRow(new Object[] {
                    f.getAirlineName(),
                    f.getFlightNumber(),
                    f.getOrigin() + " (" + (f.getOriginCountry() != null ? f.getOriginCountry() : "-") + ")",
                    f.getDestination() + " (" + (f.getDestinationCountry() != null ? f.getDestinationCountry() : "-")
                            + ")",
                    f.getDepartDateTime(),
                    f.getArriveDateTime(),
                    "$" + f.getEconomyPrice()
            });
        }
    }

    private void bookSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a flight to book.");
            return;
        }

        Flight f = currentFlights.get(row);
        BookingPassengerPanel.setSelectedFlight(f);
        cardLayout.show(cards, "BOOKING_PASSENGERS");
    }

    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.BLACK);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setGridColor(new Color(230, 230, 230));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(new Color(100, 100, 100));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230)));
        header.setPreferredSize(new Dimension(0, 40));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
    }
}
