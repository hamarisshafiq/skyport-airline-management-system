import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminFlightsPanel extends JPanel {

    private final FlightDAO flightDAO = new FlightDAO();
    private final AirlineDAO airlineDAO = new AirlineDAO();
    private final BookingDAO bookingDAO = new BookingDAO(); // Added
    private final NotificationDAO notificationDAO; // Added dependency

    // Table Components
    private DefaultTableModel tableModel;
    private JTable flightTable;
    private List<Flight> currentFlights;

    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    private final JPanel cardsContainer;
    private final CardLayout cardLayout;

    public AdminFlightsPanel(JPanel cardsContainer, CardLayout cardLayout, NotificationDAO notificationDAO) {
        this.cardsContainer = cardsContainer;
        this.cardLayout = cardLayout;
        this.notificationDAO = notificationDAO;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 248, 250)); // Light gray bg for table area

        // ===== LEFT SIDEBAR =====
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(6, 1, 0, 20)); // Vertical buttons with spacing
        sidebar.setBackground(new Color(0, 31, 63)); // Navy Blue
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));
        sidebar.setPreferredSize(new Dimension(220, 0));

        JLabel menuTitle = new JLabel("Manage Flights", SwingConstants.CENTER);
        menuTitle.setForeground(Color.WHITE);
        menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));

        // Wrap title in a panel to not mess up grid if needed, but grid 6,1 is fine
        // Actually let's put title separately or as first item
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(0, 31, 63));
        titlePanel.add(menuTitle, BorderLayout.CENTER);
        titlePanel.setPreferredSize(new Dimension(200, 50));

        // Buttons
        JButton btnAdd = createSidebarButton("Add Flight");
        JButton btnEdit = createSidebarButton("Edit Selected");
        JButton btnCancel = createSidebarButton("Cancel Flight");
        JButton btnRefresh = createSidebarButton("Refresh Data");
        JButton btnBack = createSidebarButton("Back to Home");

        // Container for sidebar content to allowing spacing at top
        JPanel sidebarContent = new JPanel(new BorderLayout());
        sidebarContent.setBackground(new Color(0, 31, 63));

        JPanel buttonsPanel = new JPanel(new GridLayout(6, 1, 0, 15));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(menuTitle); // Add title as top element
        buttonsPanel.add(btnAdd);
        buttonsPanel.add(btnEdit);
        buttonsPanel.add(btnCancel);
        buttonsPanel.add(btnRefresh);
        buttonsPanel.add(btnBack);

        sidebarContent.add(buttonsPanel, BorderLayout.NORTH);
        sidebar.add(sidebarContent); // Re-using sidebar var as container was wrong logic in my head

        // Let's redo sidebar structure properly
        sidebar.removeAll();
        sidebar.setLayout(new BorderLayout());
        sidebar.add(buttonsPanel, BorderLayout.NORTH);

        add(sidebar, BorderLayout.WEST);

        // ===== CENTER TABLE =====
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Table Setup
        String[] columns = { "ID", "Flight No", "Airline", "Origin", "Destination", "Depart", "Price", "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        flightTable = new JTable(tableModel);
        flightTable.setRowHeight(30);
        flightTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        flightTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        flightTable.setShowVerticalLines(false);
        flightTable.setGridColor(new Color(220, 220, 220));

        // Header Style
        JTableHeader header = flightTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(0, 31, 63)); // Navy Blue
        header.setForeground(new Color(0, 180, 216)); // Cyan Text for prominence
        header.setOpaque(true);

        // Center Alignment for Header
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) flightTable.getTableHeader()
                .getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        header.setDefaultRenderer(headerRenderer);

        JScrollPane scrollPane = new JScrollPane(flightTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // ===== BUTTON ACTIONS =====
        btnRefresh.addActionListener(e -> loadFlights());
        btnAdd.addActionListener(e -> openFlightEditor(null));
        btnBack.addActionListener(e -> {
            if (cardLayout != null)
                cardLayout.show(cardsContainer, "ADMIN_HOME");
        });

        btnEdit.addActionListener(e -> {
            int row = flightTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a flight from the table.");
                return;
            }
            Flight f = currentFlights.get(row);
            openFlightEditor(f);
        });

        btnCancel.addActionListener(e -> {
            int row = flightTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a flight from the table.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Cancel selected flight?", "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = flightDAO.cancelFlight(currentFlights.get(row).getId());
                if (ok)
                    loadFlights();
                else
                    JOptionPane.showMessageDialog(this, "Failed to cancel.");
            }
        });

        loadFlights();
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0, 180, 216)); // Cyan
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Simple flat style
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);

        return btn;
    }

    // ===== LOAD FLIGHTS =====
    private void loadFlights() {
        SwingWorker<List<Flight>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Flight> doInBackground() {
                return flightDAO.getAllGroupedByAirline();
            }

            @Override
            protected void done() {
                try {
                    currentFlights = get();
                    tableModel.setRowCount(0);

                    for (Flight f : currentFlights) {
                        Object[] row = {
                                f.getId(),
                                f.getFlightNumber(),
                                f.getAirlineName(),
                                f.getOrigin(),
                                f.getDestination(),
                                f.getDepartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                                String.format("$%.2f", f.getEconomyPrice()),
                                f.getStatus()
                        };
                        tableModel.addRow(row);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    // ===== ADD / EDIT DIALOG (Kept logic same, just ensured imports/vars match)
    // =====
    private void openFlightEditor(Flight editing) {
        List<Airline> airlines = airlineDAO.getAll();
        JComboBox<String> airlineCombo = new JComboBox<>();
        for (Airline a : airlines)
            airlineCombo.addItem(a.getName());

        JTextField flightNoF = new JTextField();
        JTextField originF = new JTextField();
        JTextField destinationF = new JTextField();

        // Date/Time Spinners
        JSpinner departDateF = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor departDateEditor = new JSpinner.DateEditor(departDateF, "yyyy-MM-dd");
        departDateF.setEditor(departDateEditor);

        JSpinner departTimeF = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor departTimeEditor = new JSpinner.DateEditor(departTimeF, "HH:mm");
        departTimeF.setEditor(departTimeEditor);

        JSpinner arriveDateF = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor arriveDateEditor = new JSpinner.DateEditor(arriveDateF, "yyyy-MM-dd");
        arriveDateF.setEditor(arriveDateEditor);

        JSpinner arriveTimeF = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor arriveTimeEditor = new JSpinner.DateEditor(arriveTimeF, "HH:mm");
        arriveTimeF.setEditor(arriveTimeEditor);

        JTextField econSeatsF = new JTextField();
        JTextField busSeatsF = new JTextField();
        JTextField econPriceF = new JTextField();
        JTextField busPriceF = new JTextField();
        JCheckBox internationalCB = new JCheckBox("International");

        if (editing != null) {
            airlineCombo.setSelectedIndex(getAirlineIndex(airlines, editing.getAirlineId()));
            flightNoF.setText(editing.getFlightNumber());
            originF.setText(editing.getOrigin());
            destinationF.setText(editing.getDestination());

            // Set values for spinners
            java.util.Date depDate = java.sql.Timestamp.valueOf(editing.getDepartDateTime());
            java.util.Date arrDate = java.sql.Timestamp.valueOf(editing.getArriveDateTime());

            departDateF.setValue(depDate);
            departTimeF.setValue(depDate);
            arriveDateF.setValue(arrDate);
            arriveTimeF.setValue(arrDate);

            econSeatsF.setText(String.valueOf(editing.getEconomySeats()));
            busSeatsF.setText(String.valueOf(editing.getBusinessSeats()));
            econPriceF.setText(String.valueOf(editing.getEconomyPrice()));
            busPriceF.setText(String.valueOf(editing.getBusinessPrice()));
            econPriceF.setText(String.valueOf(editing.getEconomyPrice()));
            busPriceF.setText(String.valueOf(editing.getBusinessPrice()));
            internationalCB.setSelected(editing.isInternational());
        }

        // Status Combo
        JComboBox<String> statusCombo = new JComboBox<>(
                new String[] { "SCHEDULED", "DELAYED", "CANCELLED", "COMPLETED" });
        if (editing != null && editing.getStatus() != null) {
            statusCombo.setSelectedItem(editing.getStatus());
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Airline"));
        form.add(airlineCombo);
        form.add(new JLabel("Flight Number"));
        form.add(flightNoF);
        form.add(new JLabel("Origin"));
        form.add(originF);
        form.add(new JLabel("Destination"));
        form.add(destinationF);
        form.add(new JLabel("Depart Date (YYYY-MM-DD)"));
        form.add(departDateF);
        form.add(new JLabel("Depart Time (HH:MM)"));
        form.add(departTimeF);
        form.add(new JLabel("Arrive Date (YYYY-MM-DD)"));
        form.add(arriveDateF);
        form.add(new JLabel("Arrive Time (HH:MM)"));
        form.add(arriveTimeF);
        form.add(new JLabel("Economy Seats"));
        form.add(econSeatsF);
        form.add(new JLabel("Business Seats"));
        form.add(busSeatsF);
        form.add(new JLabel("Economy Price"));
        form.add(econPriceF);
        form.add(new JLabel("Business Price"));
        form.add(busPriceF);
        form.add(new JLabel("International"));
        form.add(new JLabel("International"));
        form.add(internationalCB);
        form.add(new JLabel("Status"));
        form.add(statusCombo);

        while (true) {
            int res = JOptionPane.showConfirmDialog(this, form, editing == null ? "Add Flight" : "Edit Flight",
                    JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION)
                return;

            try {
                Flight f = (editing == null) ? new Flight() : editing;
                int idx = airlineCombo.getSelectedIndex();
                f.setAirlineId(airlines.get(idx).getId());
                f.setAirlineName(airlines.get(idx).getName());
                f.setFlightNumber(flightNoF.getText().trim());
                f.setOrigin(originF.getText().trim());
                f.setDestination(destinationF.getText().trim());

                // Parse Date/Time from spinners
                java.util.Date dDate = (java.util.Date) departDateF.getValue();
                java.util.Date dTime = (java.util.Date) departTimeF.getValue();
                java.util.Date aDate = (java.util.Date) arriveDateF.getValue();
                java.util.Date aTime = (java.util.Date) arriveTimeF.getValue();

                // Combine Date and Time
                LocalDateTime dep = LocalDateTime.of(
                        convertToLocalDate(dDate),
                        convertToLocalTime(dTime));
                LocalDateTime arr = LocalDateTime.of(
                        convertToLocalDate(aDate),
                        convertToLocalTime(aTime));

                f.setDepartDateTime(dep);
                f.setArriveDateTime(arr);
                f.setDurationMinutes((int) java.time.Duration.between(dep, arr).toMinutes());

                String newStatus = (String) statusCombo.getSelectedItem();
                String oldStatus = (editing != null) ? editing.getStatus() : "SCHEDULED";

                f.setEconomySeats(Integer.parseInt(econSeatsF.getText()));
                f.setBusinessSeats(Integer.parseInt(busSeatsF.getText()));
                f.setEconomyPrice(Double.parseDouble(econPriceF.getText()));
                f.setBusinessPrice(Double.parseDouble(busPriceF.getText()));
                f.setInternational(internationalCB.isSelected());
                f.setStatus(newStatus); // Use selected status

                if (editing == null) {
                    f.setFlightCode(airlines.get(idx).getName().substring(0, 2).toUpperCase() + "-"
                            + System.currentTimeMillis());
                    flightDAO.createFlight(f);
                } else {
                    flightDAO.updateFlight(f);

                    // Check for Status Change Notification
                    if (!newStatus.equals(oldStatus)
                            && (newStatus.equals("DELAYED") || newStatus.equals("CANCELLED"))) {
                        List<Integer> userIds = bookingDAO.getUserIdsForFlight(f.getId());
                        String msg = "Flight " + f.getFlightNumber() + " has been " + newStatus + ".";
                        for (int uid : userIds) {
                            notificationDAO.addNotification(uid, msg);
                        }
                    }
                }
                loadFlights();
                return;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private LocalDate convertToLocalDate(java.util.Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
    }

    private java.time.LocalTime convertToLocalTime(java.util.Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalTime();
    }

    private int getAirlineIndex(List<Airline> airlines, int airlineId) {
        for (int i = 0; i < airlines.size(); i++) {
            if (airlines.get(i).getId() == airlineId)
                return i;
        }
        return 0;
    }
}
