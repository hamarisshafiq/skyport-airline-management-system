import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class ReturnFlightPanel extends JPanel {

    private JTextField txtFrom;
    private JTextField txtTo;
    private JSpinner depDateSpinner;
    private JSpinner retDateSpinner;
    private JCheckBox chkDate;
    private JSpinner spinPassengers;
    private JComboBox<String> comboClass;

    private JPanel cards;
    private CardLayout cardLayout;

    private FlightDAO flightDAO = new FlightDAO();

    public ReturnFlightPanel(JPanel cards, CardLayout cardLayout) {
        this.cards = cards;
        this.cardLayout = cardLayout;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 248, 250)); // Light Gray

        // ---------- CENTER CARD ----------
        JPanel centerCard = new JPanel(new GridBagLayout());
        centerCard.setBackground(Color.WHITE);
        centerCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(30, 40, 30, 40)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel title = new JLabel("Return Flight Search", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(0, 31, 63));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 25, 0);
        centerCard.add(title, gbc);

        // Reset
        gbc.gridwidth = 1;
        gbc.insets = new Insets(8, 10, 8, 10);

        // --- Inputs ---
        txtFrom = createStyledTextField();
        txtTo = createStyledTextField();

        // Row 1: From & To
        gbc.gridx = 0;
        gbc.gridy = 1;
        centerCard.add(createLabel("From (City/Country)"), gbc);
        gbc.gridx = 1;
        centerCard.add(txtFrom, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        centerCard.add(createLabel("To (City/Country)"), gbc);
        gbc.gridx = 1;
        centerCard.add(txtTo, gbc);

        // Row 2: Dates
        depDateSpinner = createStyledSpinner();
        retDateSpinner = createStyledSpinner();
        depDateSpinner.setEnabled(false);
        retDateSpinner.setEnabled(false);

        gbc.gridx = 0;
        gbc.gridy = 3;
        centerCard.add(createLabel("Departure Date"), gbc);
        gbc.gridx = 1;
        centerCard.add(depDateSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        centerCard.add(createLabel("Return Date"), gbc);
        gbc.gridx = 1;
        centerCard.add(retDateSpinner, gbc);

        // Row 3: Checkbox
        chkDate = new JCheckBox("Search with dates");
        chkDate.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chkDate.setBackground(Color.WHITE);
        chkDate.setFocusPainted(false);
        chkDate.addActionListener(e -> {
            boolean on = chkDate.isSelected();
            depDateSpinner.setEnabled(on);
            retDateSpinner.setEnabled(on);
        });

        gbc.gridx = 1;
        gbc.gridy = 5;
        centerCard.add(chkDate, gbc);

        // Row 4: Passengers & Class
        spinPassengers = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        styleSpinner(spinPassengers);

        comboClass = new JComboBox<>(new String[] { "Economy", "Business", "First Class" });
        comboClass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboClass.setBackground(Color.WHITE);

        gbc.gridx = 0;
        gbc.gridy = 6;
        centerCard.add(createLabel("Passengers"), gbc);
        gbc.gridx = 1;
        centerCard.add(spinPassengers, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        centerCard.add(createLabel("Class"), gbc);
        gbc.gridx = 1;
        centerCard.add(comboClass, gbc);

        // Row 5: Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);

        JButton back = new JButton("Back");
        styleButton(back, new Color(150, 150, 150));
        back.addActionListener(e -> cardLayout.show(cards, "USER_FLIGHTS"));

        JButton search = new JButton("Search Flights");
        styleButton(search, new Color(0, 180, 216)); // Cyan
        search.addActionListener(e -> performSearch());

        btnPanel.add(back);
        btnPanel.add(Box.createHorizontalStrut(10));
        btnPanel.add(search);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 0, 0, 0);
        centerCard.add(btnPanel, gbc);

        // Wrapper
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(new Color(245, 248, 250));
        wrapper.add(centerCard);

        add(wrapper, BorderLayout.CENTER);

        // Scroll Pane
        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(new Color(245, 248, 250));

        add(scroll, BorderLayout.CENTER);
    }

    private void performSearch() {
        String from = txtFrom.getText().trim();
        String to = txtTo.getText().trim();

        if (from.isEmpty() || to.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter origin and destination.", "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Flight> flights;
        if (chkDate.isSelected()) {
            Date d = (Date) depDateSpinner.getValue();
            // Convert to LocalDateTime for DAO if needed, assuming DAO accepts
            // LocalDateTime or similar
            // Old code used: new java.sql.Timestamp(d.getTime()).toLocalDateTime()
            // Simplified:
            flights = flightDAO.search(from, to, d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        } else {
            flights = flightDAO.search(from, to);
        }

        // Show Results
        boolean foundPanel = false;
        for (Component c : cards.getComponents()) {
            if (c instanceof UserFlightsResultPanel) {
                ((UserFlightsResultPanel) c).showFlights(flights);
                foundPanel = true;
                break;
            }
        }
        if (foundPanel) {
            cardLayout.show(cards, "USER_FLIGHT_RESULTS");
        } else {
            JOptionPane.showMessageDialog(this, "Error: Result Panel not found.");
        }
    }

    // --- Helpers ---
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField(20);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                new EmptyBorder(8, 10, 8, 10)));
        return tf;
    }

    private JSpinner createStyledSpinner() {
        JSpinner sp = new JSpinner(new SpinnerDateModel());
        sp.setEditor(new JSpinner.DateEditor(sp, "yyyy-MM-dd"));
        styleSpinner(sp);
        return sp;
    }

    private void styleSpinner(JSpinner sp) {
        sp.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 5, 5, 5)));
        sp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sp.setPreferredSize(new Dimension(200, 35));
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
    }
}
