import javax.swing.*;
import java.awt.*;

public class BookingConfirmPanel extends JPanel {

    public BookingConfirmPanel(JPanel cards, CardLayout cardLayout) {

        setLayout(new BorderLayout());

        JLabel msg = new JLabel(
                "✅ Booking Confirmed! Tickets Generated Successfully",
                SwingConstants.CENTER);
        msg.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JButton home = new JButton("Go Home");
        home.addActionListener(e -> cardLayout.show(cards, "USER_DASH"));

        add(msg, BorderLayout.CENTER);
        add(home, BorderLayout.SOUTH);
    }
}
