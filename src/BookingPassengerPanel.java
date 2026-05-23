import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BookingPassengerPanel extends JPanel {

    private static Flight selectedFlight;

    private JPanel cards;
    private CardLayout cardLayout;

    private JPanel listPanel;
    private List<PassengerForm> forms = new ArrayList<>();

    // 🔹 Called from UserFlightsResultPanel
    public static void setSelectedFlight(Flight f) {
        selectedFlight = f;
    }

    public BookingPassengerPanel(JPanel cards, CardLayout cardLayout) {
        this.cards = cards;
        this.cardLayout = cardLayout;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // ---------- TITLE ----------
        JLabel title = new JLabel("Passenger Details", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setBorder(new EmptyBorder(15, 10, 15, 10));
        add(title, BorderLayout.NORTH);

        // ---------- LIST ----------
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        add(new JScrollPane(listPanel), BorderLayout.CENTER);

        addPassenger(); // at least one passenger

        // ---------- BUTTONS ----------
        JButton add = new JButton("+ Add Passenger");
        JButton back = new JButton("Back");
        JButton next = new JButton("Continue");

        add.addActionListener(e -> addPassenger());

        back.addActionListener(e ->
                cardLayout.show(cards, "USER_FLIGHT_RESULTS")
        );

        next.addActionListener(e -> proceed());

        JPanel bottom = new JPanel();
        bottom.add(add);
        bottom.add(back);
        bottom.add(next);

        add(bottom, BorderLayout.SOUTH);
    }

    // ---------- LOGIC ----------
    private void addPassenger() {
        PassengerForm f = new PassengerForm(forms.size() + 1);
        forms.add(f);
        listPanel.add(f);
        revalidate();
        repaint();
    }

    private void removePassenger(PassengerForm f) {
        forms.remove(f);
        listPanel.remove(f);
        revalidate();
        repaint();
    }

    private void proceed() {

        if (selectedFlight == null) {
            JOptionPane.showMessageDialog(this,
                    "No flight selected",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Passenger> passengers = new ArrayList<>();

        for (PassengerForm f : forms) {

            if (!f.isValidData()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please fill all passenger details first",
                        "Validation",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            passengers.add(f.toPassenger());
        }

        // 🚀 SEND TO STEP 4
        PriceCalculationPanel.setData(selectedFlight, passengers);
        cardLayout.show(cards, "PRICE_CALCULATION");
    }

    // ================= INNER FORM =================
    class PassengerForm extends JPanel {

        JTextField name = new JTextField();
        JTextField doc = new JTextField();
        JTextField age = new JTextField();
        JComboBox<String> cls =
                new JComboBox<>(new String[]{"Economy", "Business"});

        PassengerForm(int no) {

            setBorder(BorderFactory.createTitledBorder("Passenger " + no));
            setLayout(new GridLayout(5, 2, 8, 8));

            add(new JLabel("Full Name"));
            add(name);

            add(new JLabel("CNIC / Passport"));
            add(doc);

            add(new JLabel("Age"));
            add(age);

            add(new JLabel("Class"));
            add(cls);

            JButton remove = new JButton("Remove");
            remove.addActionListener(e -> removePassenger(this));

            add(new JLabel());
            add(remove);
        }

        boolean isValidData() {
            return !name.getText().trim().isEmpty()
                    && !doc.getText().trim().isEmpty()
                    && !age.getText().trim().isEmpty();
        }

        Passenger toPassenger() {
            return new Passenger(
                    name.getText().trim(),
                    doc.getText().trim(),
                    Integer.parseInt(age.getText().trim()),
                    cls.getSelectedItem().toString()
            );
        }
    }
}
