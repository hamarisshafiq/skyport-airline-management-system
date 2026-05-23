import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class OneWayFlightPanel extends JPanel {

    private JTextField txtFrom;
    private JTextField txtTo;
    private JSpinner dateSpinner;
    private JCheckBox chkDate;

    private JPanel cards;
    private CardLayout cardLayout;

    private FlightDAO flightDAO = new FlightDAO();

    public OneWayFlightPanel(JPanel cards, CardLayout cardLayout) {
        this.cards = cards;
        this.cardLayout = cardLayout;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 248, 250)); // Light Gray Background

        // ---------- CENTER CARD ----------
        JPanel centerCard = new JPanel(new GridBagLayout());
        centerCard.setBackground(Color.WHITE);
        centerCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(40, 50, 40, 50)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel title = new JLabel("One Way Flight Search", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(0, 31, 63));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 30, 0);
        centerCard.add(title, gbc);

        // Reset insets
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridwidth = 1;

        // Input Fields
        txtFrom = createStyledTextField();
        txtTo = createStyledTextField();

        // Row 1: From
        gbc.gridx = 0;
        gbc.gridy = 1;
        centerCard.add(createLabel("From"), gbc);
        gbc.gridx = 1;
        centerCard.add(txtFrom, gbc);

        // Row 2: To
        gbc.gridx = 0;
        gbc.gridy = 2;
        centerCard.add(createLabel("To"), gbc);
        gbc.gridx = 1;
        centerCard.add(txtTo, gbc);

        // Row 3: Date
        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 5, 5, 5)));
        dateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateSpinner.setPreferredSize(new Dimension(200, 35));
        dateSpinner.setEnabled(false);

        gbc.gridx = 0;
        gbc.gridy = 3;
        centerCard.add(createLabel("Departure Date"), gbc);
        gbc.gridx = 1;
        centerCard.add(dateSpinner, gbc);

        // Row 4: Checkbox
        chkDate = new JCheckBox("Search with date");
        chkDate.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chkDate.setBackground(Color.WHITE);
        chkDate.setFocusPainted(false);
        chkDate.addActionListener(e -> dateSpinner.setEnabled(chkDate.isSelected()));

        gbc.gridx = 1;
        gbc.gridy = 4;
        centerCard.add(chkDate, gbc);

        // Row 5: Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);

        // Back Button (Red/Gray style or just hidden if sidebar handles it?
        // User asked for "Back" improvements. Let's keep it but make it styled.)
        JButton back = new JButton("Back");
        styleButton(back, new Color(150, 150, 150));
        back.addActionListener(e -> cardLayout.show(cards, "USER_FLIGHTS"));

        JButton search = new JButton("Search Flights");
        styleButton(search, new Color(0, 180, 216)); // Cyan
        search.addActionListener(e -> searchFlights());

        // Assuming sidebar handles main navigation, but this back might exist for
        // mobile-ish flows?
        // Let's just keep them.
        btnPanel.add(back);
        btnPanel.add(Box.createHorizontalStrut(10));
        btnPanel.add(search);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 0, 0);
        centerCard.add(btnPanel, gbc);

        // Wrapper to center the card in the panel
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(new Color(245, 248, 250));
        wrapper.add(centerCard);

        add(wrapper, BorderLayout.CENTER);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
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

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
    }

    // ---------- SEARCH LOGIC ----------
    private void searchFlights() {

        String from = txtFrom.getText().trim();
        String to = txtTo.getText().trim();

        if (from.isEmpty() || to.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter From and To",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate date = null;

        if (chkDate.isSelected()) {
            date = ((java.util.Date) dateSpinner.getValue())
                    .toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
        }

        List<Flight> flights = flightDAO.search(from, to, date != null ? date.atStartOfDay() : null);

        UserFlightsResultPanel resultPanel = getResultPanel();
        if (resultPanel != null) {
            resultPanel.showFlights(flights);
            cardLayout.show(cards, "USER_FLIGHT_RESULTS");
        } else {
            JOptionPane.showMessageDialog(this, "Result Panel not found!");
        }
    }

    private UserFlightsResultPanel getResultPanel() {
        for (Component c : cards.getComponents()) {
            if (c instanceof UserFlightsResultPanel) {
                return (UserFlightsResultPanel) c;
            }
        }
        return null; // Should not happen
    }
}
