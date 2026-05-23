import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PaymentPanel extends JPanel {

    public PaymentPanel(JPanel cards, CardLayout cardLayout) {

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Payment", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setBorder(new EmptyBorder(15, 10, 15, 10));
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setBorder(new EmptyBorder(40, 200, 40, 200));

        form.add(new JLabel("Card Number"));
        form.add(new JTextField());

        form.add(new JLabel("Expiry Date"));
        form.add(new JTextField("MM/YY"));

        form.add(new JLabel("CVV"));
        form.add(new JPasswordField());

        JButton pay = new JButton("Pay Now");
        form.add(new JLabel());
        form.add(pay);

        add(form, BorderLayout.CENTER);

        pay.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "Payment Successful!\nNext: Ticket PDF",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE)
        );

        JButton back = new JButton("Back");
        back.addActionListener(e ->
                cardLayout.show(cards, "PRICE_PANEL")
        );

        JPanel bottom = new JPanel();
        bottom.add(back);
        add(bottom, BorderLayout.SOUTH);
    }
}
