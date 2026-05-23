import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UserProfileFrame extends JFrame {

    private User user;

    public UserProfileFrame(User user) {
        this.user = user;
        setTitle("User Profile");
        setSize(500, 700); // Increased height to fit all fields comfortably
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildUI();
    }

    private void buildUI() {
        // Main Container with Navy Border/Header feel
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.WHITE);
        setContentPane(main);

        // ---------- HEADER ----------
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 31, 63)); // Navy Blue
        header.setBorder(new EmptyBorder(25, 0, 25, 0));

        JLabel title = new JLabel("MY PROFILE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(title, BorderLayout.CENTER);

        main.add(header, BorderLayout.NORTH);

        // ---------- CENTER CONTENT ----------
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(Color.WHITE);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(Color.WHITE);
        center.setBorder(new EmptyBorder(20, 40, 20, 40));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 0, 8, 0); // Reduced vertical spacing slightly
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;
        g.gridx = 0;
        g.gridy = 0;

        // Add Fields
        addField(center, g, "USERNAME", user.getUsername());
        addField(center, g, "FULL NAME", user.getFullName());
        addField(center, g, "EMAIL ADDRESS", user.getEmail());
        addField(center, g, "CITY", user.getCity());
        addField(center, g, "GENDER", user.getGender());
        addField(center, g, "AGE", String.valueOf(user.getAge()));

        // Add scroll pane within center wrapper
        JScrollPane scroll = new JScrollPane(center);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        centerWrapper.add(scroll, BorderLayout.CENTER);

        main.add(centerWrapper, BorderLayout.CENTER);

        // ---------- FOOTER (Close Button) ----------
        JPanel footer = new JPanel();
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(0, 0, 30, 0));

        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setForeground(Color.WHITE);
        btnClose.setBackground(new Color(108, 117, 125)); // Grey
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setPreferredSize(new Dimension(150, 40));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        footer.add(btnClose);
        main.add(footer, BorderLayout.SOUTH);
    }

    private void addField(JPanel panel, GridBagConstraints g, String labelText, String valueText) {
        // Label
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(100, 100, 100)); // Light Gray title
        panel.add(lbl, g);

        g.gridy++;
        // Value
        JLabel val = new JLabel(valueText);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        val.setForeground(new Color(0, 31, 63)); // Navy text
        val.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230))); // Bottom line
        panel.add(val, g);

        g.gridy++;
        // Spacer
        panel.add(Box.createVerticalStrut(10), g);
        g.gridy++;
    }
}
