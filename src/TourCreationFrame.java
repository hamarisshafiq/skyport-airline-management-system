import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * TourCreationFrame (final)
 *
 * - Back button in top bar
 * - Single-country flight search uses only origin+destination (no date)
 * - Full-screen table dialogs for flight/hotel selection
 *
 * Requires: FlightDAO.search(String origin, String destination)
 * HotelDAO.searchByCountry(String country)
 * TourDAO.createTour(Tour)
 * Models: Flight, Hotel, Tour, TourLeg, TourFlight, TourHotel
 */
public class TourCreationFrame extends JFrame {

    private final FlightDAO flightDAO = new FlightDAO();
    private final HotelDAO hotelDAO = new HotelDAO();
    private final TourDAO tourDAO = new TourDAO();

    private final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TourCreationFrame() {
        setTitle("Create Tour");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        initUI();
    }

    private void initUI() {
        Container c = getContentPane();
        c.setLayout(new BorderLayout());

        // Top bar with Back button
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(0, 31, 63)); // Navy Blue
        top.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Create Tour Package");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JButton btnBack = createStyledButton("Back");
        btnBack.addActionListener(e -> dispose());
        // Override for Back button to look like a "secondary" action if desired,
        // but user asked for professional "Cyan" buttons. Use standard styled button.

        top.add(title, BorderLayout.WEST);
        top.add(btnBack, BorderLayout.EAST);
        c.add(top, BorderLayout.NORTH);

        // Center content
        JPanel center = new JPanel(new GridBagLayout());
        center.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);

        JLabel choose = new JLabel("Choose Tour Type:");
        choose.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        g.gridx = 0;
        g.gridy = 0;
        g.anchor = GridBagConstraints.WEST;
        center.add(choose, g);

        JRadioButton rbSingle = new JRadioButton("Single Country Tour");
        JRadioButton rbMulti = new JRadioButton("Multiple Countries Tour");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbSingle);
        bg.add(rbMulti);
        rbSingle.setSelected(true);

        JPanel radios = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        radios.add(rbSingle);
        radios.add(rbMulti);
        g.gridx = 1;
        center.add(radios, g);

        JButton btnNext = createStyledButton("Next");
        // btnNext.setFont(new Font("Segoe UI", Font.BOLD, 14)); // already in helper
        g.gridx = 0;
        g.gridy = 1;
        g.gridwidth = 2;
        center.add(btnNext, g);

        btnNext.addActionListener(e -> {
            if (rbSingle.isSelected())
                openSingleCountryFlow();
            else
                openMultiCountryFlow();
        });

        c.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancel = createStyledButton("Cancel");
        btnCancel.setBackground(new Color(220, 53, 69)); // Red for cancel? Or just Cyan?
        // Let's stick to theme or simple grey for Cancel usually, but let's use the
        // helper and maybe override color if needed.
        // For consistency safely use the helper.
        btnCancel.addActionListener(e -> dispose());
        bottom.add(btnCancel);
        c.add(bottom, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0, 180, 216)); // Cyan
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Re-add hand cursor if general preference, or remove if user
                                                       // dislikes universally
        // User disliked "effect" in search buttons. Might be safest to avoid hand
        // cursor here too to be "no other change" but better.
        // Actually user said "remove effect from every search button". I'll keep it
        // standard for now to be safe.
        // btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        return btn;
    }

    // ---------------- SINGLE COUNTRY FLOW ----------------
    private void openSingleCountryFlow() {
        JDialog dlg = new JDialog(this, "Single Country Tour - Step 1", true);
        dlg.setSize(820, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);

        g.gridx = 0;
        g.gridy = 0;
        dlg.add(new JLabel("Origin Country:"), g);
        g.gridx = 1;
        JTextField originCountry = new JTextField(18);
        dlg.add(originCountry, g);

        g.gridx = 0;
        g.gridy++;
        dlg.add(new JLabel("Origin City (optional):"), g);
        g.gridx = 1;
        JTextField originCity = new JTextField(18);
        dlg.add(originCity, g);

        g.gridx = 0;
        g.gridy++;
        dlg.add(new JLabel("Destination Country:"), g);
        g.gridx = 1;
        JTextField destCountry = new JTextField(18);
        dlg.add(destCountry, g);

        g.gridx = 0;
        g.gridy++;
        dlg.add(new JLabel("Destination City (optional):"), g);
        g.gridx = 1;
        JTextField destCity = new JTextField(18);
        dlg.add(destCity, g);

        // NOTE: no flight date fields — search by origin+destination only
        g.gridx = 0;
        g.gridy++;
        JButton btnFindFlights = createStyledButton("Find Flights & Continue");
        dlg.add(btnFindFlights, g);
        JButton btnCancel = createStyledButton("Cancel");
        g.gridx = 1;
        dlg.add(btnCancel, g);

        btnCancel.addActionListener(a -> dlg.dispose());

        btnFindFlights.addActionListener(a -> {
            String oCountry = originCountry.getText().trim();
            String oCity = originCity.getText().trim();
            String dCountry = destCountry.getText().trim();
            String dCity = destCity.getText().trim();

            if (oCountry.isEmpty() || dCountry.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Origin and destination country required.");
                return;
            }
            dlg.dispose();

            // search flights without dates — use originCity if present else originCountry
            String originKey = oCity.isEmpty() ? oCountry : oCity;
            String destKey = dCity.isEmpty() ? dCountry : dCity;

            selectFlightsAndHotelSingle(originKey, destKey, oCountry, dCountry, oCity, dCity);
        });

        dlg.setVisible(true);
    }

    /**
     * originKey/destKey: city if provided else country (used for flight search)
     * oCountry/dCountry and oCity/dCity kept for hotel country selection and labels
     */
    private void selectFlightsAndHotelSingle(String originKey, String destKey,
            String originCountry, String destCountry,
            String originCity, String destCity) {
        // Step A: outbound flight (search by originKey -> destKey)
        List<Flight> outFlights = flightDAO.search(originKey, destKey);
        Flight chosenOut = chooseFlightFromList(outFlights,
                "Choose outbound flight (" + originKey + " → " + destKey + ")");
        if (chosenOut == null)
            return;

        // Step B: return flight (search destKey -> originKey)
        List<Flight> returnFlights = flightDAO.search(destKey, originKey);
        Flight chosenReturn = chooseFlightFromList(returnFlights,
                "Choose return flight (" + destKey + " → " + originKey + ")");
        if (chosenReturn == null)
            return;

        // Step C: choose hotel in destination country (use destCountry)
        List<Hotel> hotels = hotelDAO.searchByCountry(destCountry);
        Hotel chosenHotel = chooseHotelFromList(hotels, "Choose hotel in " + destCountry);
        if (chosenHotel == null)
            return;

        // Step D: ask hotel stay dates
        String fromS = JOptionPane.showInputDialog(this, "Hotel stay FROM (yyyy-MM-dd):", "");
        if (fromS == null)
            return;
        String toS = JOptionPane.showInputDialog(this, "Hotel stay TO (yyyy-MM-dd):", "");
        if (toS == null)
            return;

        java.time.LocalDate stayFrom, stayTo;
        try {
            stayFrom = java.time.LocalDate.parse(fromS);
            stayTo = java.time.LocalDate.parse(toS);
            if (!stayTo.isAfter(stayFrom) && !stayTo.equals(stayFrom)) {
                JOptionPane.showMessageDialog(this, "Invalid hotel stay dates.");
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use yyyy-MM-dd.");
            return;
        }

        // cost calc
        double outPrice = safeFlightPrice(chosenOut);
        double retPrice = safeFlightPrice(chosenReturn);
        long nights = java.time.temporal.ChronoUnit.DAYS.between(stayFrom, stayTo);
        if (nights == 0)
            nights = 1;
        double hotelCost = safeHotelPricePerNight(chosenHotel) * nights;
        double total = outPrice + retPrice + hotelCost;
        double finalCost = round2(total * 0.8);

        String summary = String.format(
                "Outbound: %s (%.2f)\nReturn: %s (%.2f)\nHotel: %s x %d nights = %.2f\n\nTotal: %.2f\nAfter 20%% discount: %.2f\n\nCreate tour?",
                flightSummary(chosenOut), outPrice,
                flightSummary(chosenReturn), retPrice,
                chosenHotel.getName(), nights, hotelCost,
                total, finalCost);

        int c = JOptionPane.showConfirmDialog(this, summary, "Confirm Tour", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION)
            return;

        // build tour object (simple single leg)
        Tour tour = new Tour();
        tour.setTitle("Single: " + destCountry + " tour");
        tour.setType("SINGLE");
        tour.setTotalCost(total);
        tour.setDiscountPct(20.0);
        tour.setFinalCost(finalCost);

        TourLeg leg = new TourLeg();
        leg.setSeqNo(1);
        leg.setCountry(destCountry);
        leg.setCity(destCity);
        leg.setStayFrom(stayFrom);
        leg.setStayTo(stayTo);

        TourFlight tfOut = new TourFlight();
        tfOut.setDirection("OUTBOUND");
        tfOut.setFlightId(chosenOut.getId());
        tfOut.setPrice(outPrice);
        tfOut.setFlightDatetime(chosenOut.getDepartDateTime());

        TourFlight tfRet = new TourFlight();
        tfRet.setDirection("RETURN");
        tfRet.setFlightId(chosenReturn.getId());
        tfRet.setPrice(retPrice);
        tfRet.setFlightDatetime(chosenReturn.getDepartDateTime());

        TourHotel th = new TourHotel();
        th.setHotelId(chosenHotel.getId());
        th.setPriceTotal(hotelCost);

        leg.setOutboundFlight(tfOut);
        leg.setReturnFlight(tfRet);
        leg.setSelectedHotel(th);

        tour.getLegs().add(leg);

        // persist
        new SwingWorker<Integer, Void>() {
            protected Integer doInBackground() {
                return tourDAO.createTour(tour);
            }

            protected void done() {
                try {
                    int id = get();
                    if (id > 0)
                        JOptionPane.showMessageDialog(TourCreationFrame.this, "Tour created. ID: " + id);
                    else
                        JOptionPane.showMessageDialog(TourCreationFrame.this, "Failed to create tour.");
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(TourCreationFrame.this, "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // ---------------- MULTI-COUNTRY FLOW ----------------
    private void openMultiCountryFlow() {
        String[] options = { "2", "3", "4", "5" };
        String sel = (String) JOptionPane.showInputDialog(this, "Number of countries in tour:", "Multi-country",
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (sel == null)
            return;
        int n = Integer.parseInt(sel);

        JTextField originCountryF = new JTextField(15);
        JTextField originCityF = new JTextField(15);
        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        p.add(new JLabel("Origin country:"));
        p.add(originCountryF);
        p.add(new JLabel("Origin city (optional):"));
        p.add(originCityF);
        int r = JOptionPane.showConfirmDialog(this, p, "Enter origin", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION)
            return;
        String currentCountry = originCountryF.getText().trim();
        String currentCity = originCityF.getText().trim();
        if (currentCountry.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Origin country required.");
            return;
        }

        Tour tour = new Tour();
        tour.setType("MULTI");
        tour.setTitle("Multi-country tour (" + n + " countries)");
        double totalCost = 0.0;

        for (int idx = 1; idx <= n; idx++) {
            JTextField destCountryF = new JTextField(15);
            JTextField destCityF = new JTextField(15);
            JPanel pp = new JPanel(new GridLayout(2, 2, 8, 8));
            pp.add(new JLabel("Destination country #" + idx + ":"));
            pp.add(destCountryF);
            pp.add(new JLabel("City (optional):"));
            pp.add(destCityF);
            int rr = JOptionPane.showConfirmDialog(this, pp, "Destination " + idx, JOptionPane.OK_CANCEL_OPTION);
            if (rr != JOptionPane.OK_OPTION)
                return;
            String destCountry = destCountryF.getText().trim();
            String destCity = destCityF.getText().trim();
            if (destCountry.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Destination country required.");
                return;
            }

            // flight search without date (currentCity or currentCountry -> destCity or
            // destCountry)
            String originKey = currentCity.isEmpty() ? currentCountry : currentCity;
            String destKey = destCity.isEmpty() ? destCountry : destCity;
            List<Flight> flights = flightDAO.search(originKey, destKey);
            Flight chosenFlight = chooseFlightFromList(flights,
                    "Choose flight from " + currentCountry + " -> " + destCountry);
            if (chosenFlight == null)
                return;

            // hotels in destCountry
            List<Hotel> hotels = hotelDAO.searchByCountry(destCountry);
            Hotel chosenHotel = chooseHotelFromList(hotels, "Choose hotel in " + destCountry);
            if (chosenHotel == null)
                return;

            String fromS = JOptionPane.showInputDialog(this, "Hotel stay FROM (yyyy-MM-dd):", "");
            if (fromS == null)
                return;
            String toS = JOptionPane.showInputDialog(this, "Hotel stay TO (yyyy-MM-dd):", "");
            if (toS == null)
                return;
            java.time.LocalDate stayFrom, stayTo;
            try {
                stayFrom = java.time.LocalDate.parse(fromS);
                stayTo = java.time.LocalDate.parse(toS);
                if (!stayTo.isAfter(stayFrom) && !stayTo.equals(stayFrom)) {
                    JOptionPane.showMessageDialog(this, "Invalid hotel stay dates.");
                    return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format.");
                return;
            }

            long nights = java.time.temporal.ChronoUnit.DAYS.between(stayFrom, stayTo);
            if (nights == 0)
                nights = 1;
            double hotelCost = safeHotelPricePerNight(chosenHotel) * nights;
            double flightCost = safeFlightPrice(chosenFlight);

            TourLeg leg = new TourLeg();
            leg.setSeqNo(idx);
            leg.setCountry(destCountry);
            leg.setCity(destCity);
            leg.setStayFrom(stayFrom);
            leg.setStayTo(stayTo);

            TourFlight tf = new TourFlight();
            tf.setDirection("OUTBOUND");
            tf.setFlightId(chosenFlight.getId());
            tf.setPrice(flightCost);
            tf.setFlightDatetime(chosenFlight.getDepartDateTime());

            TourHotel th = new TourHotel();
            th.setHotelId(chosenHotel.getId());
            th.setPriceTotal(hotelCost);

            leg.setOutboundFlight(tf);
            leg.setSelectedHotel(th);

            tour.getLegs().add(leg);
            totalCost += flightCost + hotelCost;

            currentCountry = destCountry;
            currentCity = destCity;
        }

        double finalCost = round2(totalCost * 0.8);
        tour.setTotalCost(totalCost);
        tour.setDiscountPct(20.0);
        tour.setFinalCost(finalCost);

        String msg = String.format("Total cost: %.2f\nAfter 20%% discount: %.2f\nCreate tour?", totalCost, finalCost);
        int c = JOptionPane.showConfirmDialog(this, msg, "Confirm Multi Tour", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION)
            return;

        new SwingWorker<Integer, Void>() {
            protected Integer doInBackground() {
                return tourDAO.createTour(tour);
            }

            protected void done() {
                try {
                    int id = get();
                    if (id > 0)
                        JOptionPane.showMessageDialog(TourCreationFrame.this, "Tour created. ID: " + id);
                    else
                        JOptionPane.showMessageDialog(TourCreationFrame.this, "Failed to create tour.");
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(TourCreationFrame.this, "Error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // --------------- Helpers & dialogs ----------------

    private Flight chooseFlightFromList(List<Flight> flights, String title) {
        if (flights == null || flights.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No flights found for this route.");
            return null;
        }
        FlightSelectionDialog dlg = new FlightSelectionDialog(this, flights, title);
        return dlg.getSelectedFlight();
    }

    private Hotel chooseHotelFromList(List<Hotel> hotels, String title) {
        if (hotels == null || hotels.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hotels found for this country.");
            return null;
        }
        HotelSelectionDialog dlg = new HotelSelectionDialog(this, hotels, title);
        return dlg.getSelectedHotel();
    }

    private double safeFlightPrice(Flight f) {
        try {
            return f.getEconomyPrice();
        } catch (Throwable t) {
            return 0.0;
        }
    }

    private double safeHotelPricePerNight(Hotel h) {
        try {
            return h.getPricePerNight();
        } catch (Throwable t) {
            return 0.0;
        }
    }

    private String flightSummary(Flight f) {
        if (f == null)
            return "n/a";
        String dt = f.getDepartDateTime() != null ? f.getDepartDateTime().format(dtFmt) : "n/a";
        return f.getFlightNumber() + " " + f.getOrigin() + "->" + f.getDestination() + " dep:" + dt;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    // ---- FlightSelectionDialog (table + filter) ----
    private class FlightSelectionDialog extends JDialog {
        private Flight selectedFlight = null;

        public FlightSelectionDialog(Frame parent, List<Flight> flights, String title) {
            super(parent, title, true);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setLayout(new BorderLayout());

            JLabel header = new JLabel(title, SwingConstants.CENTER);
            header.setFont(new Font("Segoe UI", Font.BOLD, 22));
            header.setOpaque(true);
            header.setBackground(new Color(0, 31, 63)); // Navy Blue
            header.setForeground(Color.WHITE);
            header.setBorder(new EmptyBorder(15, 20, 15, 20));
            add(header, BorderLayout.NORTH);

            String[] cols = { "#", "Flight", "Origin", "Destination", "Depart", "Duration(min)", "Price" };
            Object[][] data = new Object[flights.size()][cols.length];
            for (int i = 0; i < flights.size(); i++) {
                Flight f = flights.get(i);
                data[i][0] = i + 1;
                data[i][1] = f.getFlightNumber();
                data[i][2] = f.getOrigin();
                data[i][3] = f.getDestination();
                data[i][4] = f.getDepartDateTime() != null ? f.getDepartDateTime().format(dtFmt) : "N/A";
                data[i][5] = f.getDurationMinutes();
                data[i][6] = safeFlightPrice(f);
            }
            DefaultTableModel model = new DefaultTableModel(data, cols) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            JTable table = new JTable(model);
            table.setRowHeight(30);
            table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setShowVerticalLines(false);
            table.setGridColor(new Color(220, 220, 220));

            javax.swing.table.JTableHeader th = table.getTableHeader();
            th.setFont(new Font("Segoe UI", Font.BOLD, 14));
            th.setBackground(new Color(0, 31, 63));
            th.setForeground(new Color(0, 180, 216));
            th.setOpaque(true);
            ((javax.swing.table.DefaultTableCellRenderer) th.getDefaultRenderer())
                    .setHorizontalAlignment(JLabel.CENTER);

            JScrollPane sp = new JScrollPane(table);
            sp.getViewport().setBackground(Color.WHITE);
            sp.setBorder(BorderFactory.createEmptyBorder());
            add(sp, BorderLayout.CENTER);

            JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT));
            filter.setBorder(new EmptyBorder(6, 6, 6, 6));
            filter.add(new JLabel("Origin:"));
            JTextField tfO = new JTextField(10);
            filter.add(tfO);
            filter.add(new JLabel("Dest:"));
            JTextField tfD = new JTextField(10);
            filter.add(tfD);
            JButton btnFilter = createStyledButton("Filter");
            filter.add(btnFilter);
            JButton btnClear = createStyledButton("Clear");
            filter.add(btnClear);
            add(filter, BorderLayout.AFTER_LAST_LINE);

            btnFilter.addActionListener(e -> {
                String o = tfO.getText().trim().toLowerCase();
                String d = tfD.getText().trim().toLowerCase();
                DefaultTableModelFilter.applyFilter(table, flights, o, d);
            });
            btnClear.addActionListener(e -> {
                tfO.setText("");
                tfD.setText("");
                DefaultTableModelFilter.resetTable(table, flights);
            });

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnSelect = createStyledButton("Select");
            JButton btnCancel = createStyledButton("Cancel");
            bottom.add(btnSelect);
            bottom.add(btnCancel);
            add(bottom, BorderLayout.SOUTH);

            btnCancel.addActionListener(e -> {
                selectedFlight = null;
                dispose();
            });
            btnSelect.addActionListener(e -> {
                int r = table.getSelectedRow();
                if (r < 0) {
                    JOptionPane.showMessageDialog(this, "Select a flight first.");
                    return;
                }
                selectedFlight = flights.get(r);
                dispose();
            });

            pack();
            setVisible(true);
        }

        public Flight getSelectedFlight() {
            return selectedFlight;
        }
    }

    // ---- HotelSelectionDialog (table + filters) ----
    private class HotelSelectionDialog extends JDialog {
        private Hotel selectedHotel = null;

        public HotelSelectionDialog(Frame parent, List<Hotel> hotels, String title) {
            super(parent, title, true);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setLayout(new BorderLayout());

            JLabel header = new JLabel(title, SwingConstants.CENTER);
            header.setFont(new Font("Segoe UI", Font.BOLD, 22));
            header.setOpaque(true);
            header.setBackground(new Color(0, 31, 63)); // Navy Blue
            header.setForeground(Color.WHITE);
            header.setBorder(new EmptyBorder(15, 20, 15, 20));
            add(header, BorderLayout.NORTH);

            String[] cols = { "#", "Hotel", "Country", "City", "Category", "Price/Night" };
            Object[][] data = new Object[hotels.size()][cols.length];
            for (int i = 0; i < hotels.size(); i++) {
                Hotel h = hotels.get(i);
                data[i][0] = i + 1;
                data[i][1] = h.getName();
                data[i][2] = h.getCountry();
                data[i][3] = h.getCity();
                data[i][4] = h.getCategory();
                data[i][5] = safeHotelPricePerNight(h);
            }
            DefaultTableModel model = new DefaultTableModel(data, cols) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            JTable table = new JTable(model);
            table.setRowHeight(30);
            table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setShowVerticalLines(false);
            table.setGridColor(new Color(220, 220, 220));

            javax.swing.table.JTableHeader th = table.getTableHeader();
            th.setFont(new Font("Segoe UI", Font.BOLD, 14));
            th.setBackground(new Color(0, 31, 63));
            th.setForeground(new Color(0, 180, 216));
            th.setOpaque(true);
            ((javax.swing.table.DefaultTableCellRenderer) th.getDefaultRenderer())
                    .setHorizontalAlignment(JLabel.CENTER);

            JScrollPane sp = new JScrollPane(table);
            sp.getViewport().setBackground(Color.WHITE);
            sp.setBorder(BorderFactory.createEmptyBorder());
            add(sp, BorderLayout.CENTER);

            JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT));
            filter.setBorder(new EmptyBorder(6, 6, 6, 6));
            filter.add(new JLabel("Name:"));
            JTextField tfN = new JTextField(12);
            filter.add(tfN);
            filter.add(new JLabel("Country:"));
            JTextField tfC = new JTextField(10);
            filter.add(tfC);
            filter.add(new JLabel("City:"));
            JTextField tfCi = new JTextField(10);
            filter.add(tfCi);
            JButton btnFilter = createStyledButton("Filter");
            filter.add(btnFilter);
            JButton btnClear = createStyledButton("Clear");
            filter.add(btnClear);
            add(filter, BorderLayout.AFTER_LAST_LINE);

            btnFilter.addActionListener(e -> {
                String n = tfN.getText().trim().toLowerCase();
                String c = tfC.getText().trim().toLowerCase();
                String ci = tfCi.getText().trim().toLowerCase();
                DefaultTableModelFilter.applyHotelFilter(table, hotels, n, c, ci);
            });
            btnClear.addActionListener(e -> {
                tfN.setText("");
                tfC.setText("");
                tfCi.setText("");
                DefaultTableModelFilter.resetHotelTable(table, hotels);
            });

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnSelect = createStyledButton("Select");
            JButton btnCancel = createStyledButton("Cancel");
            bottom.add(btnSelect);
            bottom.add(btnCancel);
            add(bottom, BorderLayout.SOUTH);

            btnCancel.addActionListener(e -> {
                selectedHotel = null;
                dispose();
            });
            btnSelect.addActionListener(e -> {
                int r = table.getSelectedRow();
                if (r < 0) {
                    JOptionPane.showMessageDialog(this, "Select a hotel first.");
                    return;
                }
                selectedHotel = hotels.get(r);
                dispose();
            });

            pack();
            setVisible(true);
        }

        public Hotel getSelectedHotel() {
            return selectedHotel;
        }
    }

    // ---- simple table filter helpers ----
    private static class DefaultTableModelFilter {
        static void applyFilter(JTable table, List<Flight> flights, String originSub, String destSub) {
            originSub = originSub == null ? "" : originSub;
            destSub = destSub == null ? "" : destSub;
            String[] cols = { "#", "Flight", "Origin", "Destination", "Depart", "Duration(min)", "Price" };
            String finalOriginSub = originSub;
            String finalDestSub = destSub;
            Object[][] data = flights.stream()
                    .filter(f -> f.getOrigin() != null && f.getDestination() != null)
                    .filter(f -> f.getOrigin().toLowerCase().contains(finalOriginSub)
                            && f.getDestination().toLowerCase().contains(finalDestSub))
                    .map(f -> new Object[] {
                            0,
                            f.getFlightNumber(),
                            f.getOrigin(),
                            f.getDestination(),
                            f.getDepartDateTime() != null
                                    ? f.getDepartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                                    : "N/A",
                            f.getDurationMinutes(),
                            f.getEconomyPrice()
                    }).toArray(Object[][]::new);
            for (int i = 0; i < data.length; i++)
                data[i][0] = i + 1;
            table.setModel(new DefaultTableModel(data, cols) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            });
        }

        static void resetTable(JTable table, List<Flight> flights) {
            String[] cols = { "#", "Flight", "Origin", "Destination", "Depart", "Duration(min)", "Price" };
            Object[][] data = new Object[flights.size()][cols.length];
            for (int i = 0; i < flights.size(); i++) {
                Flight f = flights.get(i);
                data[i][0] = i + 1;
                data[i][1] = f.getFlightNumber();
                data[i][2] = f.getOrigin();
                data[i][3] = f.getDestination();
                data[i][4] = f.getDepartDateTime() != null
                        ? f.getDepartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        : "N/A";
                data[i][5] = f.getDurationMinutes();
                data[i][6] = f.getEconomyPrice();
            }
            table.setModel(new DefaultTableModel(data, cols) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            });
        }

        static void applyHotelFilter(JTable table, List<Hotel> hotels, String nameSub, String countrySub,
                String citySub) {
            nameSub = nameSub == null ? "" : nameSub;
            countrySub = countrySub == null ? "" : countrySub;
            citySub = citySub == null ? "" : citySub;
            String[] cols = { "#", "Hotel", "Country", "City", "Category", "Price/Night" };
            String finalNameSub = nameSub;
            String finalCountrySub = countrySub;
            String finalCitySub = citySub;
            Object[][] data = hotels.stream()
                    .filter(h -> (h.getName() != null && h.getName().toLowerCase().contains(finalNameSub)) &&
                            (h.getCountry() != null && h.getCountry().toLowerCase().contains(finalCountrySub)) &&
                            (h.getCity() != null && h.getCity().toLowerCase().contains(finalCitySub)))
                    .map(h -> new Object[] {
                            0,
                            h.getName(),
                            h.getCountry(),
                            h.getCity(),
                            h.getCategory(),
                            h.getPricePerNight()
                    }).toArray(Object[][]::new);
            for (int i = 0; i < data.length; i++)
                data[i][0] = i + 1;
            table.setModel(new DefaultTableModel(data, cols) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            });
        }

        static void resetHotelTable(JTable table, List<Hotel> hotels) {
            String[] cols = { "#", "Hotel", "Country", "City", "Category", "Price/Night" };
            Object[][] data = new Object[hotels.size()][cols.length];
            for (int i = 0; i < hotels.size(); i++) {
                Hotel h = hotels.get(i);
                data[i][0] = i + 1;
                data[i][1] = h.getName();
                data[i][2] = h.getCountry();
                data[i][3] = h.getCity();
                data[i][4] = h.getCategory();
                data[i][5] = h.getPricePerNight();
            }
            table.setModel(new DefaultTableModel(data, cols) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            });
        }
    }
}
