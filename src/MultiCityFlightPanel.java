import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MultiCityFlightPanel extends JPanel {

    private JPanel cards;
    private CardLayout cardLayout;

    private JPanel segmentsPanel;
    private List<FlightSegmentPanel> segments = new ArrayList<>();

    public MultiCityFlightPanel(JPanel cards, CardLayout cardLayout) {
        this.cards = cards;
        this.cardLayout = cardLayout;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 248, 250)); // Light Gray

        // Header
        JLabel title = new JLabel("Multi-City Flight Search", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(0, 31, 63));
        title.setBorder(new EmptyBorder(25, 10, 25, 10));
        add(title, BorderLayout.NORTH);

        // Segments Container
        segmentsPanel = new JPanel();
        segmentsPanel.setLayout(new BoxLayout(segmentsPanel, BoxLayout.Y_AXIS));
        segmentsPanel.setBackground(new Color(245, 248, 250));
        segmentsPanel.setBorder(new EmptyBorder(10, 50, 10, 50)); // Padding side

        JScrollPane scroll = new JScrollPane(segmentsPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(new Color(245, 248, 250));

        add(scroll, BorderLayout.CENTER);

        // Initial segments
        addFlightSegment();
        addFlightSegment();

        // Bottom Actions
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        bottom.setBackground(new Color(245, 248, 250));

        JButton addMore = new JButton("Add Flight");
        styleButton(addMore, new Color(40, 167, 69)); // Green for Add

        JButton search = new JButton("Search Flights");
        styleButton(search, new Color(0, 180, 216)); // Cyan

        JButton back = new JButton("Back");
        styleButton(back, new Color(150, 150, 150)); // Gray

        addMore.addActionListener(e -> addFlightSegment());
        search.addActionListener(e -> performSearch());
        back.addActionListener(e -> cardLayout.show(cards, "USER_FLIGHTS"));

        bottom.add(addMore);
        bottom.add(search);
        bottom.add(back);

        add(bottom, BorderLayout.SOUTH);
    }

    private void addFlightSegment() {
        FlightSegmentPanel seg = new FlightSegmentPanel(segments.size() + 1);
        segments.add(seg);
        segmentsPanel.add(seg);
        segmentsPanel.add(Box.createVerticalStrut(15)); // Spacing
        revalidate();
        repaint();
    }

    private void performSearch() {
        FlightDAO dao = new FlightDAO();
        List<Flight> allResults = new ArrayList<>();

        boolean hasCriteria = false;

        for (FlightSegmentPanel seg : segments) {
            String from = seg.getOrigin();
            String to = seg.getDestination();

            if (from.isEmpty() || to.isEmpty())
                continue;

            hasCriteria = true;
            if (seg.isDateSelected()) {
                Date d = seg.getDate();
                allResults.addAll(dao.search(from, to, new java.sql.Timestamp(d.getTime()).toLocalDateTime()));
            } else {
                allResults.addAll(dao.search(from, to));
            }
        }

        if (!hasCriteria) {
            JOptionPane.showMessageDialog(this, "Please enter at least one flight segment.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (Component c : cards.getComponents()) {
            if (c instanceof UserFlightsResultPanel) {
                ((UserFlightsResultPanel) c).showFlights(allResults);
                break;
            }
        }
        cardLayout.show(cards, "USER_FLIGHT_RESULTS");
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 25, 10, 25));
    }

    // -------- INNER SEGMENT PANEL --------
    static class FlightSegmentPanel extends JPanel {

        private JTextField txtFrom;
        private JTextField txtTo;
        private JCheckBox chkDate;
        private JSpinner dateSpinner;

        FlightSegmentPanel(int index) {
            setLayout(new GridBagLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(220, 220, 220), 1),
                    new EmptyBorder(15, 20, 15, 20)));
            setMaximumSize(new Dimension(800, 180)); // Limit height
            setAlignmentX(Component.CENTER_ALIGNMENT);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 10, 8, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL; // Ensure horizontal fill

            // Header
            JLabel lblTitle = new JLabel("Flight " + index);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblTitle.setForeground(new Color(0, 31, 63));

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbc.weightx = 1.0; // Header can stretch
            add(lblTitle, gbc);

            // Reset
            gbc.gridwidth = 1;

            // Inputs
            txtFrom = createStyledTextField();
            txtTo = createStyledTextField();

            // Label From
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 0.0;
            gbc.insets = new Insets(8, 10, 8, 10);
            add(createLabel("From"), gbc);

            // Field From
            gbc.gridx = 1;
            gbc.weightx = 1.0;
            gbc.insets = new Insets(8, 10, 8, 150); // Added right padding to reduce visual length
            add(txtFrom, gbc);

            // Label To
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.weightx = 0.0;
            gbc.insets = new Insets(8, 10, 8, 10);
            add(createLabel("To"), gbc);

            // Field To
            gbc.gridx = 1;
            gbc.weightx = 1.0;
            gbc.insets = new Insets(8, 10, 8, 150); // Added right padding to reduce visual length
            add(txtTo, gbc);

            // Date
            dateSpinner = new JSpinner(new SpinnerDateModel());
            dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
            styleSpinner(dateSpinner);
            dateSpinner.setEnabled(false);

            chkDate = new JCheckBox("Use Date");
            chkDate.setBackground(Color.WHITE);
            chkDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            chkDate.setFocusPainted(false);
            chkDate.addActionListener(e -> dateSpinner.setEnabled(chkDate.isSelected()));

            JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            datePanel.setBackground(Color.WHITE);
            datePanel.add(chkDate);
            datePanel.add(Box.createHorizontalStrut(15));
            datePanel.add(dateSpinner);

            gbc.gridx = 1;
            gbc.gridy = 3;
            gbc.weightx = 1.0; // Stretch date panel
            gbc.insets = new Insets(8, 10, 8, 10); // Reset insets for date panel
            add(datePanel, gbc);
        }

        String getOrigin() {
            return txtFrom.getText().trim();
        }

        String getDestination() {
            return txtTo.getText().trim();
        }

        boolean isDateSelected() {
            return chkDate.isSelected();
        }

        Date getDate() {
            return (Date) dateSpinner.getValue();
        }

        private JLabel createLabel(String text) {
            JLabel lbl = new JLabel(text);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lbl.setForeground(new Color(80, 80, 80));
            return lbl;
        }

        private JTextField createStyledTextField() {
            JTextField tf = new JTextField(15);
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            tf.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(200, 200, 200)),
                    new EmptyBorder(6, 8, 6, 8)));
            return tf;
        }

        private void styleSpinner(JSpinner sp) {
            sp.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(200, 200, 200)),
                    new EmptyBorder(4, 4, 4, 4)));
            sp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            sp.setPreferredSize(new Dimension(150, 30));
        }
    }
}
