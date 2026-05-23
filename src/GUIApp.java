import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class GUIApp {

    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";

    private JFrame frame;
    private JPanel cards;
    private CardLayout cardLayout;

    private final UserDAO userDAO = new UserDAO();
    private final FlightDAO flightDAO = new FlightDAO();
    private final AirlineDAO airlineDAO = new AirlineDAO();
    private final HotelDAO hotelDAO = new HotelDAO();
    private final TourDAO tourDAO = new TourDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    // Admin Dashboard Labels
    private JLabel lblTotalFlights;
    private JLabel lblTotalAirlines;
    private JLabel lblTotalHotels;
    private JLabel lblTotalPackages;

    private JPanel adminDashPanel;
    private JList<String> adminUserList;

    private JPanel userDashPanel;
    private JLabel udLblUsername, udLblFullname, udLblAge, udLblCity, udLblGender, udLblEmail;
    private JLabel udLblFlightsCount = new JLabel("0");
    private JLabel udLblHotelsCount = new JLabel("0");
    private JLabel udLblToursCount = new JLabel("0");

    private User currentUser = null;
    private JLabel headerTitle; // Promoted to field for dynamic updates

    // --- PROFESSIONAL PALETTE (2025) ---
    // #03045e (Dark Blue), #00b4d8 (Bright Blue), #caf0f8 (Light Cyan)
    private final Color PRIMARY_DARK = new Color(3, 4, 94); // #03045e
    private final Color ACCENT_BRIGHT = new Color(0, 180, 216); // #00b4d8
    private final Color BG_LIGHT = new Color(202, 240, 248); // #caf0f8
    private final Color WHITE = Color.WHITE;

    // Mappings
    private final Color DARK_BLUE = PRIMARY_DARK;
    private final Color SKY_BLUE = ACCENT_BRIGHT;
    private final Color LIGHT_BG = BG_LIGHT;
    private final Color PANEL_BG = WHITE;

    public static void main(String[] args) {
        // Attempt to load driver (Static or Dynamic)
        DriverLoader.load();

        // Setup flat look for buttons if possible
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            GUIApp app = new GUIApp();
            app.createAndShowGUI();
        });
    }

    private void createAndShowGUI() {
        frame = new JFrame("Skyport - Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        // Unified Dark Navy Background
        frame.getContentPane().setBackground(new Color(0, 31, 63));
        frame.setLayout(new BorderLayout());

        // Header (Transparent to blend with Navy background)
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 15));
        header.setOpaque(false); // Transparent
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel logoLbl = new JLabel();
        try {
            ImageIcon icon = new ImageIcon("skyport/src/logo.jpg");
            Image img = icon.getImage();
            Image scaledImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            logoLbl.setIcon(new ImageIcon(scaledImg));
        } catch (Exception e) {
            logoLbl.setText("✈");
            logoLbl.setForeground(Color.WHITE);
            logoLbl.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        }
        header.add(logoLbl);

        headerTitle = new JLabel("SKYPORT SYSTEM");
        headerTitle.setForeground(Color.WHITE);
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        headerTitle.setBorder(new EmptyBorder(0, 10, 0, 0));
        header.add(headerTitle);

        frame.add(header, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.setOpaque(false); // Transparent to show Frame's Navy BG

        // Core screens
        cards.add(mainPanel(), "MAIN");
        cards.add(adminLoginPanel(), "ADMIN_LOGIN");
        cards.add(adminHomePanel(), "ADMIN_HOME");
        cards.add(userMenuPanel(), "USER_MENU");
        cards.add(userLoginPanel(), "USER_LOGIN");
        cards.add(userRegisterPanel(), "USER_REGISTER");

        // Admin modules
        cards.add(new AdminFlightsPanel(cards, cardLayout, notificationDAO), "ADMIN_FLIGHTS");
        cards.add(new AdminAirlinesPanel(cards, cardLayout), "ADMIN_AIRLINES");
        cards.add(new AdminHotelsPanel(cards, cardLayout), "ADMIN_HOTELS");
        cards.add(new AdminToursPanel(cards, cardLayout), "ADMIN_TOURS");

        // Old admin dashboard (user listing)
        adminDashPanel = adminDashboardPanel();
        cards.add(adminDashPanel, "ADMIN_DASH");

        // User dashboard
        userDashPanel = userDashboardPanel();
        cards.add(userDashPanel, "USER_DASH");
        cards.add(new UserFlightsPanel(cards, cardLayout, userDAO), "USER_FLIGHTS");

        // HOTEL PANEL INTEGRATION
        cards.add(new UserHotelsPanel(cards, cardLayout, hotelDAO, bookingDAO, () -> currentUser, notificationDAO),
                "USER_HOTELS");

        cards.add(new OneWayFlightPanel(cards, cardLayout), "USER_FLIGHT_ONEWAY");
        cards.add(new ReturnFlightPanel(cards, cardLayout), "USER_FLIGHT_RETURN");
        cards.add(new MultiCityFlightPanel(cards, cardLayout), "USER_FLIGHT_MULTICITY");
        cards.add(new UserFlightsResultPanel(cards, cardLayout), "USER_FLIGHT_RESULTS");
        cards.add(new BookingPassengerPanel(cards, cardLayout), "BOOKING_PASSENGERS");
        cards.add(new PriceCalculationPanel(cards, cardLayout, bookingDAO, () -> currentUser, notificationDAO),
                "PRICE_CALCULATION");
        cards.add(new PaymentPanel(cards, cardLayout), "PAYMENT_PANEL");
        cards.add(new BookingConfirmPanel(cards, cardLayout), "BOOKING_CONFIRM");
        cards.add(new UserNotificationsPanel(cards, cardLayout, notificationDAO, () -> currentUser),
                "USER_NOTIFICATIONS");

        frame.add(cards, BorderLayout.CENTER);
        cardLayout.show(cards, "MAIN");

        frame.setMinimumSize(new Dimension(1000, 700));

        frame.setMinimumSize(new Dimension(1000, 700));
        frame.setVisible(true);
    }

    // ---------------- MAIN PANEL (Dark Theme + White Card) ------------------

    private JPanel mainPanel() {
        JPanel splitPanel = new JPanel(new GridLayout(1, 2));
        splitPanel.setOpaque(false); // Shows Navy BG

        // --- LEFT SIDE: Welcome Text ---
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setOpaque(false);

        // Pure Swing Layout (No HTML) - Centered Format match
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title: "Welcome to Skyport"
        JLabel title = new JLabel("Welcome to Skyport");
        title.setFont(new Font("Segoe UI", Font.BOLD, 54));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle Lines
        Font subFont = new Font("Segoe UI Light", Font.PLAIN, 20);
        Color subColor = new Color(160, 196, 255); // #a0c4ff

        JLabel s1 = new JLabel("Your gateway to seamless aviation management.");
        s1.setFont(subFont);
        s1.setForeground(subColor);
        s1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel s2 = new JLabel("Experience the future of flight control.");
        s2.setFont(subFont);
        s2.setForeground(subColor);
        s2.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Spacing & Adding
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(20)); // Gap
        textPanel.add(s1);
        textPanel.add(Box.createVerticalStrut(5)); // Small gap
        textPanel.add(s2);

        leftPanel.add(textPanel);

        // --- RIGHT SIDE: White Floating Card ---
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);

        // The Floating White Panel
        RoundedPanel whiteCard = new RoundedPanel(40, Color.WHITE, true); // White BG + Shadow
        whiteCard.setPreferredSize(new Dimension(400, 500));
        whiteCard.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 0, 10, 0);
        g.gridx = 0;
        g.gridy = 0;

        JLabel cardTitle = new JLabel("Choose Role");
        cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        cardTitle.setForeground(new Color(0, 31, 63)); // Navy Text
        whiteCard.add(cardTitle, g);

        g.gridy++;
        JLabel sub = new JLabel("Please select your access level");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(Color.GRAY);
        whiteCard.add(sub, g);

        g.gridy++;
        g.insets = new Insets(40, 0, 15, 0);
        JButton adminBtn = createModernButton("ADMINISTRATOR", new Color(0, 31, 63)); // Navy Button
        whiteCard.add(adminBtn, g);

        g.gridy++;
        g.insets = new Insets(10, 0, 0, 0);
        JButton userBtn = createModernButton("USER ACCESS", new Color(0, 180, 216)); // Cyan Button
        whiteCard.add(userBtn, g);

        // Events
        adminBtn.addActionListener(e -> cardLayout.show(cards, "ADMIN_LOGIN"));
        userBtn.addActionListener(e -> cardLayout.show(cards, "USER_MENU"));

        rightPanel.add(whiteCard);

        splitPanel.add(leftPanel);
        splitPanel.add(rightPanel);
        return splitPanel;
    }

    // Helper for Modern Buttons with Hover
    private JButton createModernButton(String text, Color baseColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(baseColor.brighter());
                } else {
                    g2.setColor(baseColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(250, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ---------------- ADMIN LOGIN PANEL (Modern Dark Theme) ------------------

    private JPanel adminLoginPanel() {
        JPanel splitPanel = new JPanel(new GridLayout(1, 2));
        splitPanel.setOpaque(false); // Translucent to show Navy BG

        // --- LEFT SIDE: Admin Note ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Vertically center the text content in the left panel
        JPanel textWrapper = new JPanel();
        textWrapper.setLayout(new BoxLayout(textWrapper, BoxLayout.Y_AXIS));
        textWrapper.setOpaque(false);
        textWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel l1 = new JLabel("Secure Access");
        l1.setFont(new Font("Segoe UI", Font.BOLD, 54));
        l1.setForeground(Color.WHITE);
        l1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Restricted to Authorized Personnel");
        sub.setFont(new Font("Segoe UI Light", Font.PLAIN, 20));
        sub.setForeground(new Color(160, 196, 255));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        textWrapper.add(l1);
        textWrapper.add(Box.createVerticalStrut(20));
        textWrapper.add(sub);

        // Add wrapper to left panel with glue for vertical centering
        leftPanel.setLayout(new GridBagLayout());
        leftPanel.add(textWrapper);

        // --- RIGHT SIDE: White Card Form ---
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);

        RoundedPanel whiteCard = new RoundedPanel(40, Color.WHITE, true);
        whiteCard.setPreferredSize(new Dimension(400, 450));
        whiteCard.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);

        // Title
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        JLabel title = new JLabel("Admin Login");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(0, 31, 63));
        whiteCard.add(title, g);

        // Inputs
        g.gridwidth = 1;
        g.gridy++;
        g.anchor = GridBagConstraints.WEST;
        JLabel uLbl = new JLabel("Username");
        uLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        uLbl.setForeground(Color.GRAY);
        whiteCard.add(uLbl, g);

        g.gridy++;
        g.fill = GridBagConstraints.HORIZONTAL;
        JTextField userF = new JTextField(20);
        userF.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        whiteCard.add(userF, g);

        g.gridy++;
        g.fill = GridBagConstraints.NONE;
        JLabel pLbl = new JLabel("Password");
        pLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pLbl.setForeground(Color.GRAY);
        whiteCard.add(pLbl, g);

        g.gridy++;
        g.fill = GridBagConstraints.HORIZONTAL;
        JPasswordField passF = new JPasswordField(20);
        passF.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        whiteCard.add(passF, g);

        // Buttons
        g.gridy++;
        g.insets = new Insets(30, 10, 10, 10);
        g.fill = GridBagConstraints.HORIZONTAL;
        JButton login = createModernButton("LOGIN", new Color(0, 31, 63));
        whiteCard.add(login, g);

        g.gridy++;
        g.insets = new Insets(10, 10, 10, 10);
        JButton back = createModernButton("BACK", new Color(108, 117, 125)); // Grey
        whiteCard.add(back, g);

        // Logic
        back.addActionListener(e -> cardLayout.show(cards, "MAIN"));

        login.addActionListener(e -> {
            if (ADMIN_USER.equals(userF.getText().trim()) &&
                    ADMIN_PASS.equals(new String(passF.getPassword()))) {
                refreshDashboardStats(); // Refresh Stats
                headerTitle.setText("SKYPORT SYSTEM - ADMIN PANEL"); // Dynamic Title
                JOptionPane.showMessageDialog(frame, "Admin Login Successful");
                cardLayout.show(cards, "ADMIN_HOME");
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid admin credentials");
            }
        });

        rightPanel.add(whiteCard);

        splitPanel.add(leftPanel);
        splitPanel.add(rightPanel);
        return splitPanel;
    }

    // ---------------- ADMIN HOME (DASHBOARD STYLE) ------------------

    private JPanel adminHomePanel() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setOpaque(false); // Show Navy Background
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- LEFT SIDEBAR ---
        // Dark Navy Sidebar
        RoundedPanel sidebar = new RoundedPanel(30, new Color(10, 20, 40), true);
        sidebar.setPreferredSize(new Dimension(300, 600));
        sidebar.setLayout(new BorderLayout(0, 20));
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));

        // Navigation Menu
        JPanel navMenu = new JPanel(new GridLayout(6, 1, 15, 15));
        navMenu.setOpaque(false);

        // Static Buttons (No Hover)
        Color btnColor = new Color(0, 180, 216); // Bright Cyan

        // Removed Unicode icons as requested
        JButton btnFlights = createStaticButton("Flights", btnColor, Color.WHITE);
        JButton btnAirlines = createStaticButton("Airlines", btnColor, Color.WHITE);
        JButton btnHotels = createStaticButton("Hotels", btnColor, Color.WHITE);
        JButton btnTours = createStaticButton("Tours", btnColor, Color.WHITE);
        // JButton btnUsers = createStaticButton("User List", btnColor, Color.WHITE); //
        // REMOVED

        navMenu.add(btnFlights);
        navMenu.add(btnAirlines);
        navMenu.add(btnHotels);
        navMenu.add(btnTours);
        // navMenu.add(btnUsers); // REMOVED

        sidebar.add(navMenu, BorderLayout.CENTER);

        // Logout Button (Bottom)
        JButton btnLogout = createStaticButton("Logout", new Color(231, 76, 60), Color.WHITE);
        btnLogout.setPreferredSize(new Dimension(100, 45));
        btnLogout.addActionListener(e -> {
            headerTitle.setText("SKYPORT SYSTEM"); // Reset Title
            cardLayout.show(cards, "MAIN");
        });

        JPanel bottomBox = new JPanel(new GridLayout(1, 1));
        bottomBox.setOpaque(false);
        bottomBox.setBorder(new EmptyBorder(20, 0, 0, 0));
        bottomBox.add(btnLogout);

        sidebar.add(bottomBox, BorderLayout.SOUTH);

        // Actions
        btnFlights.addActionListener(e -> cardLayout.show(cards, "ADMIN_FLIGHTS"));
        btnAirlines.addActionListener(e -> cardLayout.show(cards, "ADMIN_AIRLINES"));
        btnHotels.addActionListener(e -> cardLayout.show(cards, "ADMIN_HOTELS"));
        // btnUsers removed

        btnTours.addActionListener(e -> {
            try {
                cardLayout.show(cards, "ADMIN_TOURS");
            } catch (Exception ex) {
                new TourCreationFrame().setVisible(true);
            }
        });

        p.add(sidebar, BorderLayout.WEST);

        // --- RIGHT CONTENT (Status Bar) ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);

        // White Floating Card for Status
        RoundedPanel statusContainer = new RoundedPanel(40, Color.WHITE, true);
        statusContainer.setLayout(new BorderLayout());

        // Title
        JLabel statusTitle = new JLabel("Dashboard Overview", SwingConstants.CENTER);
        statusTitle.setFont(new Font("Segoe UI", Font.BOLD, 42));
        statusTitle.setForeground(new Color(0, 31, 63)); // Navy Text
        statusTitle.setBorder(new EmptyBorder(40, 0, 40, 0));
        statusContainer.add(statusTitle, BorderLayout.NORTH);

        // Stats Grid
        JPanel statsGrid = new JPanel(new GridLayout(4, 1, 25, 25));
        statsGrid.setOpaque(false);
        statsGrid.setBorder(new EmptyBorder(20, 100, 60, 100));

        // Initializing Labels
        lblTotalFlights = new JLabel("0");
        lblTotalAirlines = new JLabel("0");
        lblTotalHotels = new JLabel("0");
        lblTotalPackages = new JLabel("0");

        statsGrid.add(createStatCard("Total Flights", lblTotalFlights));
        statsGrid.add(createStatCard("Total Airlines", lblTotalAirlines));
        statsGrid.add(createStatCard("Total Hotels", lblTotalHotels));
        statsGrid.add(createStatCard("Total Packages", lblTotalPackages));

        statusContainer.add(statsGrid, BorderLayout.CENTER);
        rightPanel.add(statusContainer, BorderLayout.CENTER);

        // Spacer top
        rightPanel.add(new JPanel() {
            {
                setOpaque(false);
                setPreferredSize(new Dimension(10, 20));
            }
        }, BorderLayout.NORTH);

        // Auto-refresh stats when panel is shown
        p.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                refreshDashboardStats();
            }
        });

        p.add(rightPanel, BorderLayout.CENTER);
        return p;
    }

    // Helper to create stat card with external label
    private JPanel createStatCard(String label, JLabel valueLbl) {
        // Cyan-ish card
        RoundedPanel card = new RoundedPanel(25, ACCENT_BRIGHT);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(10, 30, 10, 30));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        lbl.setForeground(PRIMARY_DARK);

        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLbl.setForeground(Color.BLACK);

        card.add(lbl, BorderLayout.WEST);
        card.add(valueLbl, BorderLayout.EAST);

        return card;
    }

    // Helper for Static Buttons (No Hover Effects)
    private JButton createStaticButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Pure static color, no rollover check
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Increased font size for boldness
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton sidebarButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(ACCENT_BRIGHT); // Bright blue buttons
        b.setForeground(PRIMARY_DARK);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setRolloverEnabled(false); // NO HOVER EFFECT
        return b;
    }

    // ---------------- USER MENU ------------------

    private JPanel userMenuPanel() {
        JPanel splitPanel = new JPanel(new GridLayout(1, 2));
        splitPanel.setOpaque(false);

        // --- LEFT SIDE: User Note ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Vertically center content
        JPanel textWrapper = new JPanel();
        textWrapper.setLayout(new BoxLayout(textWrapper, BoxLayout.Y_AXIS));
        textWrapper.setOpaque(false);
        textWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel l1 = new JLabel("Plan Your");
        l1.setFont(new Font("Segoe UI", Font.BOLD, 54));
        l1.setForeground(Color.WHITE);
        l1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel l2 = new JLabel("Journey");
        l2.setFont(new Font("Segoe UI", Font.BOLD, 54));
        l2.setForeground(Color.WHITE);
        l2.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Book Flights, Hotels & Tours");
        sub.setFont(new Font("Segoe UI Light", Font.PLAIN, 20));
        sub.setForeground(new Color(160, 196, 255)); // Light Blue
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        textWrapper.add(l1);
        textWrapper.add(l2);
        textWrapper.add(Box.createVerticalStrut(20));
        textWrapper.add(sub);

        leftPanel.setLayout(new GridBagLayout());
        leftPanel.add(textWrapper);

        // --- RIGHT SIDE: White Card ---
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);

        RoundedPanel whiteCard = new RoundedPanel(40, Color.WHITE, true);
        whiteCard.setPreferredSize(new Dimension(400, 450));
        whiteCard.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        // Title
        g.gridx = 0;
        g.gridy = 0;
        JLabel title = new JLabel("User Menu");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(0, 31, 63));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        whiteCard.add(title, g);

        // Spacer
        g.gridy++;
        whiteCard.add(Box.createVerticalStrut(20), g);

        // Buttons
        g.gridy++;
        g.insets = new Insets(15, 10, 15, 10);
        JButton login = createModernButton("LOGIN", new Color(0, 31, 63));
        whiteCard.add(login, g);

        g.gridy++;
        JButton register = createModernButton("NEW USER", new Color(0, 180, 216));
        whiteCard.add(register, g);

        g.gridy++;
        g.insets = new Insets(30, 10, 10, 10);
        JButton back = createModernButton("BACK", new Color(108, 117, 125)); // Grey
        whiteCard.add(back, g);

        // Actions
        back.addActionListener(e -> cardLayout.show(cards, "MAIN"));
        login.addActionListener(e -> cardLayout.show(cards, "USER_LOGIN"));
        register.addActionListener(e -> cardLayout.show(cards, "USER_REGISTER"));

        rightPanel.add(whiteCard);

        splitPanel.add(leftPanel);
        splitPanel.add(rightPanel);
        return splitPanel;
    }

    // ---------------- USER LOGIN PANEL ------------------

    private JPanel userLoginPanel() {
        JPanel splitPanel = new JPanel(new GridLayout(1, 2));
        splitPanel.setOpaque(false);

        // --- LEFT SIDE: Welcome Text ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Vertically center text
        JPanel textWrapper = new JPanel();
        textWrapper.setLayout(new BoxLayout(textWrapper, BoxLayout.Y_AXIS));
        textWrapper.setOpaque(false);
        textWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel l1 = new JLabel("Secure Access");
        l1.setFont(new Font("Segoe UI", Font.BOLD, 54));
        l1.setForeground(Color.WHITE);
        l1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Please log in to your account");
        sub.setFont(new Font("Segoe UI Light", Font.PLAIN, 20));
        sub.setForeground(new Color(160, 196, 255));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        textWrapper.add(l1);
        textWrapper.add(Box.createVerticalStrut(20));
        textWrapper.add(sub);

        leftPanel.setLayout(new GridBagLayout());
        leftPanel.add(textWrapper);

        // --- RIGHT SIDE: White Card ---
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);

        RoundedPanel whiteCard = new RoundedPanel(40, Color.WHITE, true);
        whiteCard.setPreferredSize(new Dimension(400, 450));
        whiteCard.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);

        // Title
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        JLabel title = new JLabel("User Login");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(0, 31, 63));
        whiteCard.add(title, g);

        // Inputs
        g.gridwidth = 1;
        g.gridy++;
        g.anchor = GridBagConstraints.WEST;
        JLabel uLbl = new JLabel("Username");
        uLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        uLbl.setForeground(Color.GRAY);
        whiteCard.add(uLbl, g);

        g.gridy++;
        g.fill = GridBagConstraints.HORIZONTAL;
        JTextField usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        whiteCard.add(usernameField, g);

        g.gridy++;
        g.fill = GridBagConstraints.NONE;
        JLabel pLbl = new JLabel("Password");
        pLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pLbl.setForeground(Color.GRAY);
        whiteCard.add(pLbl, g);

        g.gridy++;
        g.fill = GridBagConstraints.HORIZONTAL;
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        whiteCard.add(passwordField, g);

        // Buttons
        g.gridy++;
        g.insets = new Insets(30, 10, 10, 10);
        g.fill = GridBagConstraints.HORIZONTAL;
        JButton login = createModernButton("LOGIN", new Color(0, 31, 63));
        whiteCard.add(login, g);

        g.gridy++;
        g.insets = new Insets(10, 10, 10, 10);
        JButton back = createModernButton("BACK", new Color(108, 117, 125)); // Grey
        whiteCard.add(back, g);

        // Actions
        back.addActionListener(e -> cardLayout.show(cards, "USER_MENU"));

        login.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword()).trim();

            if (username.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Enter username and password.");
                return;
            }

            new SwingWorker<User, Void>() {
                protected User doInBackground() {
                    return userDAO.getByUsername(username);
                }

                protected void done() {
                    try {
                        User u = get();
                        if (u != null && pass.equals(u.getPassword())) {
                            currentUser = u;
                            JOptionPane.showMessageDialog(frame, "Welcome " + u.getFullName());
                            showUserDashboard(u);
                            cardLayout.show(cards, "USER_DASH");
                        } else {
                            JOptionPane.showMessageDialog(frame, "Incorrect Credentials");
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Error " + ex.getMessage());
                    }
                }
            }.execute();
        });

        rightPanel.add(whiteCard);

        splitPanel.add(leftPanel);
        splitPanel.add(rightPanel);
        return splitPanel;
    }

    private void showUserDashboard(User u) {
    }

    // ---------------- USER REGISTER PANEL ------------------

    private JPanel userRegisterPanel() {
        JPanel splitPanel = new JPanel(new GridLayout(1, 2));
        splitPanel.setOpaque(false);

        // --- LEFT SIDE: Welcome Text ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Vertically center text
        JPanel textWrapper = new JPanel();
        textWrapper.setLayout(new BoxLayout(textWrapper, BoxLayout.Y_AXIS));
        textWrapper.setOpaque(false);
        textWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel l1 = new JLabel("Join Us");
        l1.setFont(new Font("Segoe UI", Font.BOLD, 54));
        l1.setForeground(Color.WHITE);
        l1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Start your seamless journey today");
        sub.setFont(new Font("Segoe UI Light", Font.PLAIN, 20));
        sub.setForeground(new Color(160, 196, 255));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        textWrapper.add(l1);
        textWrapper.add(Box.createVerticalStrut(20));
        textWrapper.add(sub);

        leftPanel.setLayout(new GridBagLayout());
        leftPanel.add(textWrapper);

        // --- RIGHT SIDE: White Card ---
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);

        // Reduced height to 550 to fit buttons
        RoundedPanel whiteCard = new RoundedPanel(40, Color.WHITE, true);
        whiteCard.setPreferredSize(new Dimension(500, 520));
        whiteCard.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(2, 10, 2, 10); // Tighter insets
        g.fill = GridBagConstraints.HORIZONTAL;

        // Title
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        JLabel title = new JLabel("New User Registration");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(0, 31, 63));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        whiteCard.add(title, g);

        g.gridy++;
        whiteCard.add(Box.createVerticalStrut(10), g);

        // Form Fields
        String[] labels = { "Username", "Password", "Full Name", "Age", "City", "Gender", "Email" };
        JComponent[] fields = new JComponent[7];

        fields[0] = new JTextField(20);
        fields[1] = new JPasswordField(20);
        fields[2] = new JTextField(20);
        fields[3] = new JTextField(10);
        fields[4] = new JTextField(15);
        fields[5] = new JTextField(10);
        fields[6] = new JTextField(20);

        for (int i = 0; i < labels.length; i++) {
            g.gridwidth = 1;
            g.gridy++;
            g.gridx = 0;
            g.anchor = GridBagConstraints.WEST;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(Color.GRAY);
            whiteCard.add(lbl, g);

            g.gridy++;
            g.gridwidth = 2;
            g.fill = GridBagConstraints.HORIZONTAL;
            fields[i].setFont(new Font("Segoe UI", Font.PLAIN, 14));
            whiteCard.add(fields[i], g);
        }

        // Buttons
        g.gridy++;
        g.insets = new Insets(15, 10, 5, 10);
        JButton register = createModernButton("REGISTER", new Color(0, 180, 216));
        whiteCard.add(register, g);

        g.gridy++;
        g.insets = new Insets(5, 10, 10, 10);
        JButton back = createModernButton("BACK", new Color(108, 117, 125)); // Grey
        whiteCard.add(back, g);

        // Logic
        back.addActionListener(e -> cardLayout.show(cards, "USER_MENU"));

        register.addActionListener(e -> {
            JTextField usernameField = (JTextField) fields[0];
            JPasswordField passwordField = (JPasswordField) fields[1];
            JTextField fullNameField = (JTextField) fields[2];
            JTextField ageField = (JTextField) fields[3];
            JTextField cityField = (JTextField) fields[4];
            JTextField genderField = (JTextField) fields[5];
            JTextField emailField = (JTextField) fields[6];

            String username = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword()).trim();
            String full = fullNameField.getText().trim();
            String city = cityField.getText().trim();
            String gender = genderField.getText().trim();
            String email = emailField.getText().trim();

            int age = 0;
            try {
                age = Integer.parseInt(ageField.getText().trim());
            } catch (Exception ignore) {
            }

            if (username.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Username and password required.");
                return;
            }

            int finalAge = age; // closure capture

            new SwingWorker<Boolean, Void>() {
                protected Boolean doInBackground() {
                    if (userDAO.getByUsername(username) != null)
                        return false;
                    User u = new User(username, pass, full, finalAge, city, gender, email);
                    return userDAO.createUser(u);
                }

                protected void done() {
                    try {
                        Boolean ok = get();
                        if (ok) {
                            JOptionPane.showMessageDialog(frame, "Registration successful.");
                            cardLayout.show(cards, "USER_MENU");
                        } else {
                            JOptionPane.showMessageDialog(frame, "Username already exists.");
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                    }
                }
            }.execute();
        });

        rightPanel.add(whiteCard);

        splitPanel.add(leftPanel);
        splitPanel.add(rightPanel);
        return splitPanel;
    }

    // ---------------- ADMIN OLD DASHBOARD (List Users) ------------------

    private JPanel adminDashboardPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(LIGHT_BG);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(DARK_BLUE);
        top.setBorder(new EmptyBorder(8, 12, 8, 12));
        JLabel label = new JLabel("Admin User List");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        top.add(label, BorderLayout.WEST);

        JButton back = smallButton("Back");
        back.addActionListener(e -> cardLayout.show(cards, "ADMIN_HOME"));
        top.add(back, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        adminUserList = new JList<>(new DefaultListModel<>());
        adminUserList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(adminUserList);
        scroll.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    // Fetches stats from Database
    private void refreshDashboardStats() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            int fCount, aCount, hCount, tCount;

            @Override
            protected Void doInBackground() {
                fCount = flightDAO.getCount();
                aCount = airlineDAO.getCount();
                hCount = hotelDAO.getCount();
                tCount = tourDAO.getCount();
                return null;
            }

            @Override
            protected void done() {
                if (lblTotalFlights != null)
                    lblTotalFlights.setText(String.valueOf(fCount));
                if (lblTotalAirlines != null)
                    lblTotalAirlines.setText(String.valueOf(aCount));
                if (lblTotalHotels != null)
                    lblTotalHotels.setText(String.valueOf(hCount));
                if (lblTotalPackages != null)
                    lblTotalPackages.setText(String.valueOf(tCount));
            }
        };
        worker.execute();
    }

    private void refreshAdminUserList() {
        new SwingWorker<List<User>, Void>() {
            protected List<User> doInBackground() {
                return userDAO.getAll();
            }

            protected void done() {
                try {
                    List<User> users = get();
                    DefaultListModel<String> model = new DefaultListModel<>();
                    for (User u : users) {
                        model.addElement(String.format("%-3d | %-12s | %-20s | %3d | %-12s | %-6s | %-20s",
                                u.getId(), safe(u.getUsername()), safe(u.getFullName()), u.getAge(),
                                safe(u.getCity()), safe(u.getGender()), safe(u.getEmail())));
                    }
                    adminUserList.setModel(model);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error loading users: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // ---------------- USER DASHBOARD ------------------

    // private JPanel userDashboardPanel() {
    // JPanel p = new JPanel(new BorderLayout());
    // p.setBackground(LIGHT_BG);
    //
    // JPanel top = new JPanel(new BorderLayout());
    // top.setBackground(DARK_BLUE);
    // top.setBorder(new EmptyBorder(8, 12, 8, 12));
    // JLabel label = new JLabel("User Dashboard");
    // label.setForeground(Color.WHITE);
    // label.setFont(new Font("Segoe UI", Font.BOLD, 20));
    // top.add(label, BorderLayout.WEST);
    //
    // JButton logout = smallButton("Logout");
    // logout.addActionListener(e -> {
    // currentUser = null;
    // cardLayout.show(cards, "MAIN");
    // });
    // top.add(logout, BorderLayout.EAST);
    // p.add(top, BorderLayout.NORTH);
    //
    // JPanel info = new JPanel(new GridLayout(6, 1, 6, 6));
    // info.setBorder(new EmptyBorder(20, 40, 20, 40));
    // info.setBackground(PANEL_BG);
    //
    // udLblUsername = new JLabel();
    // udLblFullname = new JLabel();
    // udLblAge = new JLabel();
    // udLblCity = new JLabel();
    // udLblGender = new JLabel();
    // udLblEmail = new JLabel();
    //
    // Font f = new Font("Segoe UI", Font.PLAIN, 16);
    // udLblUsername.setFont(f);
    // udLblFullname.setFont(f);
    // udLblAge.setFont(f);
    // udLblCity.setFont(f);
    // udLblGender.setFont(f);
    // udLblEmail.setFont(f);
    //
    // info.add(udLblUsername);
    // info.add(udLblFullname);
    // info.add(udLblAge);
    // info.add(udLblCity);
    // info.add(udLblGender);
    // info.add(udLblEmail);
    //
    // p.add(info, BorderLayout.CENTER);
    // return p;
    // }
    //
    // private void showUserDashboard(User u) {
    // if (u == null) return;
    // udLblUsername.setText("Username: " + safe(u.getUsername()));
    // udLblFullname.setText("Full name: " + safe(u.getFullName()));
    // udLblAge.setText("Age: " + u.getAge());
    // udLblCity.setText("City: " + safe(u.getCity()));
    // udLblGender.setText("Gender: " + safe(u.getGender()));
    // udLblEmail.setText("Email: " + safe(u.getEmail()));
    // }

    private JPanel userDashboardPanel() {
        JPanel p = new JPanel(new BorderLayout(20, 20));
        p.setOpaque(false); // Navy BG from Frame
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- LEFT SIDEBAR ---
        RoundedPanel sidebar = new RoundedPanel(30, new Color(10, 20, 40), true);
        sidebar.setPreferredSize(new Dimension(300, 600));
        sidebar.setLayout(new BorderLayout(0, 20));
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));

        // Navigation Menu
        JPanel navMenu = new JPanel(new GridLayout(6, 1, 15, 15));
        navMenu.setOpaque(false);

        Color btnColor = new Color(0, 180, 216); // Cyan

        // Static Buttons (No Hover)
        JButton btnFlights = createStaticButton("Flights", btnColor, Color.WHITE);
        JButton btnHotels = createStaticButton("Hotels", btnColor, Color.WHITE);
        JButton btnTours = createStaticButton("Tours", btnColor, Color.WHITE);
        JButton btnProfile = createStaticButton("Profile", btnColor, Color.WHITE);
        JButton btnNotify = createStaticButton("Notifications", btnColor, Color.WHITE);
        JButton btnBooked = createStaticButton("Booked", btnColor, Color.WHITE);

        navMenu.add(btnFlights);
        navMenu.add(btnHotels);
        navMenu.add(btnTours);
        navMenu.add(btnProfile);
        navMenu.add(btnNotify);
        navMenu.add(btnBooked);

        sidebar.add(navMenu, BorderLayout.CENTER);

        // Logout Button
        JButton btnLogout = createStaticButton("Logout", new Color(231, 76, 60), Color.WHITE);
        btnLogout.setPreferredSize(new Dimension(100, 45));
        btnLogout.addActionListener(e -> {
            currentUser = null;
            cardLayout.show(cards, "MAIN");
        });

        JPanel bottomBox = new JPanel(new GridLayout(1, 1));
        bottomBox.setOpaque(false);
        bottomBox.setBorder(new EmptyBorder(20, 0, 0, 0));
        bottomBox.add(btnLogout);

        sidebar.add(bottomBox, BorderLayout.SOUTH);
        p.add(sidebar, BorderLayout.WEST);

        // --- RIGHT CONTENT ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);

        // White Floating Card
        RoundedPanel contentCard = new RoundedPanel(40, Color.WHITE, true);
        contentCard.setLayout(new BorderLayout());

        // Header inside card
        JLabel dashTitle = new JLabel("User Dashboard", SwingConstants.CENTER);
        dashTitle.setFont(new Font("Segoe UI", Font.BOLD, 42));
        dashTitle.setForeground(new Color(0, 31, 63));
        dashTitle.setBorder(new EmptyBorder(40, 0, 40, 0));
        contentCard.add(dashTitle, BorderLayout.NORTH);

        // Simple Status Grid
        JPanel statsGrid = new JPanel(new GridLayout(3, 1, 25, 25));
        statsGrid.setOpaque(false);
        statsGrid.setBorder(new EmptyBorder(20, 100, 60, 100));

        statsGrid.add(createStatCard("My Flights", udLblFlightsCount));
        statsGrid.add(createStatCard("My Hotels", udLblHotelsCount));
        statsGrid.add(createStatCard("My Tours", udLblToursCount));

        contentCard.add(statsGrid, BorderLayout.CENTER);
        rightPanel.add(contentCard, BorderLayout.CENTER);

        rightPanel.add(new JPanel() {
            {
                setOpaque(false);
                setPreferredSize(new Dimension(10, 20));
            }
        }, BorderLayout.NORTH);

        p.add(rightPanel, BorderLayout.CENTER);

        cards.add(new UserHotelsPanel(cards, cardLayout, hotelDAO, bookingDAO, () -> currentUser, notificationDAO),
                "USER_HOTELS");
        cards.add(new UserToursPanel(cards, cardLayout, tourDAO, bookingDAO, () -> currentUser, notificationDAO),
                "USER_TOURS");
        cards.add(new UserBookingsPanel(cards, cardLayout, bookingDAO, () -> currentUser), "USER_BOOKINGS");

        // --- Sidebar Actions ---
        btnFlights.addActionListener(e -> cardLayout.show(cards, "USER_FLIGHTS"));
        btnHotels.addActionListener(e -> cardLayout.show(cards, "USER_HOTELS"));
        btnTours.addActionListener(e -> cardLayout.show(cards, "USER_TOURS"));
        btnBooked.addActionListener(e -> cardLayout.show(cards, "USER_BOOKINGS"));

        btnProfile.addActionListener(e -> {
            if (currentUser != null) {
                new UserProfileFrame(currentUser).setVisible(true);
            }
        });

        btnNotify.addActionListener(e -> cardLayout.show(cards, "USER_NOTIFICATIONS"));

        // Auto-refresh stats when dashboard is shown
        p.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                updateDashboardStats();
            }

            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {
            }

            public void ancestorMoved(javax.swing.event.AncestorEvent event) {
            }
        });

        return p;
    }

    private void updateDashboardStats() {
        if (currentUser == null)
            return;
        new SwingWorker<Void, Void>() {
            int f = 0, h = 0, t = 0;

            @Override
            protected Void doInBackground() {
                java.util.List<BookingDAO.Booking> list = bookingDAO.getBookingsByUserId(currentUser.getId());
                for (BookingDAO.Booking b : list) {
                    if ("FLIGHT".equalsIgnoreCase(b.getBookingType()))
                        f++;
                    else if ("HOTEL".equalsIgnoreCase(b.getBookingType()))
                        h++;
                    else if ("TOUR".equalsIgnoreCase(b.getBookingType()))
                        t++;
                }
                return null;
            }

            @Override
            protected void done() {
                udLblFlightsCount.setText(String.valueOf(f));
                udLblHotelsCount.setText(String.valueOf(h));
                udLblToursCount.setText(String.valueOf(t));
            }
        }.execute();
    }

    // ---------------- UTILITIES ------------------

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private JButton styledButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(SKY_BLUE);
        b.setForeground(PRIMARY_DARK); // Dark Blue text on Bright Blue bg
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setFocusPainted(false);
        b.setRolloverEnabled(false); // No Hover
        b.setBorder(BorderFactory.createLineBorder(DARK_BLUE, 2));
        b.setPreferredSize(new Dimension(180, 54));
        return b;
    }

    private JButton smallButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(Color.WHITE);
        b.setForeground(DARK_BLUE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setRolloverEnabled(false); // No Hover
        b.setBorder(BorderFactory.createLineBorder(DARK_BLUE, 1));
        return b;
    }
}