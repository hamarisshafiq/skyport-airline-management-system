import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/**
 * AdminHotelsPanel.java
 * Refactored to match AdminFlightsPanel design pattern.
 */
public class AdminHotelsPanel extends JPanel {
    private final JPanel cards;
    private final CardLayout cardLayout;

    private final HotelDAO hotelDAO = new HotelDAO();

    private JTable hotelTable;
    private DefaultTableModel tableModel;
    private List<Hotel> currentHotels;

    public AdminHotelsPanel(JPanel cards, CardLayout cardLayout) {
        this.cards = cards;
        this.cardLayout = cardLayout;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 248, 250)); // Light Gray BG

        // ===== LEFT SIDEBAR =====
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(new Color(0, 31, 63)); // Navy Blue
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));
        sidebar.setPreferredSize(new Dimension(220, 0));

        JLabel menuTitle = new JLabel("Manage Hotels", SwingConstants.CENTER);
        menuTitle.setForeground(Color.WHITE);
        menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));

        // Buttons
        JButton btnAdd = createSidebarButton("Add Hotel");
        JButton btnEdit = createSidebarButton("Edit Selected");
        JButton btnDelete = createSidebarButton("Delete Hotel");
        JButton btnShow = createSidebarButton("Show All");
        JButton btnSearch = createSidebarButton("Search Hotels");
        JButton btnBack = createSidebarButton("Back to Home");

        JPanel buttonsPanel = new JPanel(new GridLayout(7, 1, 0, 15));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(menuTitle);
        buttonsPanel.add(btnAdd);
        buttonsPanel.add(btnEdit);
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(btnShow);
        buttonsPanel.add(btnSearch);
        buttonsPanel.add(btnBack);

        sidebar.add(buttonsPanel, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);

        // ===== CENTER TABLE =====
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Table Setup
        String[] columns = { "ID", "Name", "Country", "City", "Category", "Address", "Email", "Contact", "Price" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        hotelTable = new JTable(tableModel);
        hotelTable.setRowHeight(30);
        hotelTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        hotelTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        hotelTable.setShowVerticalLines(false);
        hotelTable.setGridColor(new Color(220, 220, 220));

        // Header Style (Navy BG, Cyan Text, Centered)
        JTableHeader header = hotelTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(0, 31, 63)); // Navy Blue
        header.setForeground(new Color(0, 180, 216)); // Cyan Text
        header.setOpaque(true);

        // Center Headers
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) hotelTable.getTableHeader()
                .getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        header.setDefaultRenderer(headerRenderer);

        JScrollPane scrollPane = new JScrollPane(hotelTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Initial Load
        loadAllHotels();

        // ===== ACTIONS =====
        btnBack.addActionListener(e -> cardLayout.show(cards, "ADMIN_HOME"));
        btnShow.addActionListener(e -> loadAllHotels());

        btnAdd.addActionListener(e -> {
            Hotel h = hotelEditorDialog(null);
            if (h != null) {
                new SwingWorker<Integer, Void>() {
                    protected Integer doInBackground() {
                        return hotelDAO.createHotel(h);
                    }

                    protected void done() {
                        try {
                            int id = get();
                            if (id > 0) {
                                JOptionPane.showMessageDialog(AdminHotelsPanel.this, "Hotel added.");
                                loadAllHotels();
                            } else {
                                JOptionPane.showMessageDialog(AdminHotelsPanel.this, "Failed to add hotel.");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }.execute();
            }
        });

        btnEdit.addActionListener(e -> {
            int row = hotelTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a hotel to edit.");
                return;
            }
            Hotel ex = currentHotels.get(row);

            Hotel edited = hotelEditorDialog(ex);
            if (edited != null) {
                new SwingWorker<Boolean, Void>() {
                    protected Boolean doInBackground() {
                        return hotelDAO.updateHotel(edited);
                    }

                    protected void done() {
                        try {
                            boolean ok = get();
                            if (ok) {
                                JOptionPane.showMessageDialog(AdminHotelsPanel.this, "Updated.");
                                loadAllHotels();
                            } else
                                JOptionPane.showMessageDialog(AdminHotelsPanel.this, "Update failed.");
                        } catch (Exception ex2) {
                            ex2.printStackTrace();
                        }
                    }
                }.execute();
            }
        });

        btnDelete.addActionListener(e -> {
            int row = hotelTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a hotel to delete.");
                return;
            }
            int id = currentHotels.get(row).getId();

            int c = JOptionPane.showConfirmDialog(this, "Delete selected hotel?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (c != JOptionPane.YES_OPTION)
                return;

            new SwingWorker<Boolean, Void>() {
                protected Boolean doInBackground() {
                    return hotelDAO.deleteHotel(id);
                }

                protected void done() {
                    try {
                        boolean ok = get();
                        if (ok) {
                            JOptionPane.showMessageDialog(AdminHotelsPanel.this, "Deleted.");
                            loadAllHotels();
                        } else
                            JOptionPane.showMessageDialog(AdminHotelsPanel.this, "Delete failed.");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.execute();
        });

        btnSearch.addActionListener(e -> openSearchWindow());
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0, 180, 216)); // Cyan
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        return btn;
    }

    private void loadAllHotels() {
        new SwingWorker<List<Hotel>, Void>() {
            protected List<Hotel> doInBackground() {
                return hotelDAO.getAll();
            }

            protected void done() {
                try {
                    currentHotels = get();
                    refreshTable(currentHotels);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void refreshTable(List<Hotel> list) {
        tableModel.setRowCount(0);
        if (list == null || list.isEmpty())
            return;
        for (Hotel h : list) {
            tableModel.addRow(new Object[] {
                    h.getId(), h.getName(), h.getCountry(), h.getCity(), h.getCategory(),
                    h.getAddress(), h.getEmail(), h.getContact(), h.getPricePerNight()
            });
        }
    }

    // Editor dialog for Add / Edit (Logic kept mostly same but cleaned up)
    private Hotel hotelEditorDialog(Hotel existing) {
        JTextField nameF = new JTextField();
        JTextField countryF = new JTextField();
        JTextField cityF = new JTextField();
        JComboBox<String> categoryCb = new JComboBox<>(new String[] { "3 Star", "5 Star", "7 Star" });
        JTextField addressF = new JTextField();
        JTextField emailF = new JTextField();
        JTextField contactF = new JTextField();
        JTextField priceF = new JTextField();

        if (existing != null) {
            nameF.setText(existing.getName());
            countryF.setText(existing.getCountry());
            cityF.setText(existing.getCity());
            categoryCb.setSelectedItem(existing.getCategory());
            addressF.setText(existing.getAddress());
            emailF.setText(existing.getEmail());
            contactF.setText(existing.getContact());
            priceF.setText(String.valueOf(existing.getPricePerNight()));
        }

        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.add(new JLabel("Name:"));
        p.add(nameF);
        p.add(new JLabel("Country:"));
        p.add(countryF);
        p.add(new JLabel("City:"));
        p.add(cityF);
        p.add(new JLabel("Category:"));
        p.add(categoryCb);
        p.add(new JLabel("Address:"));
        p.add(addressF);
        p.add(new JLabel("Email:"));
        p.add(emailF);
        p.add(new JLabel("Contact:"));
        p.add(contactF);
        p.add(new JLabel("Price/Night:"));
        p.add(priceF);

        int res = JOptionPane.showConfirmDialog(this, p, existing == null ? "Add Hotel" : "Edit Hotel",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION)
            return null;

        String name = nameF.getText().trim();
        String country = countryF.getText().trim();
        String city = cityF.getText().trim();
        String category = (String) categoryCb.getSelectedItem();
        String address = addressF.getText().trim();
        String email = emailF.getText().trim();
        String contact = contactF.getText().trim();
        String priceS = priceF.getText().trim();

        if (name.isEmpty() || country.isEmpty() || city.isEmpty() || category == null || address.isEmpty()
                || priceS.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill required fields.");
            return null;
        }

        double price;
        try {
            price = Double.parseDouble(priceS);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid price format.");
            return null;
        }

        Hotel h = existing == null ? new Hotel() : existing;
        h.setName(name);
        h.setCountry(country);
        h.setCity(city);
        h.setCategory(category);
        h.setAddress(address);
        h.setEmail(email);
        h.setContact(contact);
        h.setPricePerNight(price);
        return h;
    }

    // Open a separate Search Window (Logic kept same, UI Enhanced)
    private void openSearchWindow() {
        JFrame f = new JFrame("Search Hotels");
        f.setSize(400, 250); // Slightly larger for better spacing
        f.setLocationRelativeTo(this);
        f.setLayout(new GridBagLayout());
        f.getContentPane().setBackground(new Color(0, 31, 63)); // Navy Blue Background

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);
        g.gridx = 0;
        g.gridy = 0;
        g.fill = GridBagConstraints.HORIZONTAL; // Fill width for better button look

        JLabel lbl = new JLabel("Search Hotels By", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(Color.WHITE); // White text
        f.add(lbl, g);

        g.gridy++;
        // Buttons Panel
        JPanel p = new JPanel(new GridLayout(3, 1, 0, 10)); // Vertical stack
        p.setOpaque(false);

        JButton byName = createSidebarButton("Search by Name");
        JButton byCountry = createSidebarButton("Search by Country");
        JButton byCity = createSidebarButton("Search by City");

        // Keep them same width
        byName.setHorizontalAlignment(SwingConstants.CENTER);
        byCountry.setHorizontalAlignment(SwingConstants.CENTER);
        byCity.setHorizontalAlignment(SwingConstants.CENTER);

        p.add(byName);
        p.add(byCountry);
        p.add(byCity);

        f.add(p, g);

        byName.addActionListener(e -> {
            f.dispose();
            String term = showStyledInputDialog("Hotel name (partial ok):", "Search by Name");
            if (term != null && !term.trim().isEmpty())
                doSearch("name", term);
        });

        byCountry.addActionListener(e -> {
            f.dispose();
            String term = showStyledInputDialog("Country (partial ok):", "Search by Country");
            if (term != null && !term.trim().isEmpty())
                doSearch("country", term);
        });

        byCity.addActionListener(e -> {
            f.dispose();
            String term = showStyledInputDialog("City (partial ok):", "Search by City");
            if (term != null && !term.trim().isEmpty())
                doSearch("city", term);
        });

        f.setVisible(true);
    }

    // Custom Styled Input Dialog
    private String showStyledInputDialog(String message, String title) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        d.setSize(400, 200);
        d.setLocationRelativeTo(this);
        d.setLayout(new GridBagLayout());
        d.getContentPane().setBackground(new Color(0, 31, 63)); // Navy Blue

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 20, 10, 20);
        g.gridx = 0;
        g.gridy = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;

        JLabel lbl = new JLabel(message);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(Color.WHITE);
        d.add(lbl, g);

        g.gridy++;
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        d.add(txt, g);

        g.gridy++;
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.CENTER;

        JButton btnSearch = new JButton("Search");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setBackground(new Color(0, 180, 216)); // Cyan
        btnSearch.setFocusPainted(false);
        btnSearch.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnSearch.setContentAreaFilled(false);
        btnSearch.setOpaque(true);

        // Final container allows accessing text from button action
        final String[] result = { null };

        btnSearch.addActionListener(e -> {
            result[0] = txt.getText();
            d.dispose();
        });

        d.add(btnSearch, g);
        d.getRootPane().setDefaultButton(btnSearch); // Enter key works

        d.setVisible(true);
        return result[0];
    }

    private void doSearch(String type, String term) {
        new SwingWorker<List<Hotel>, Void>() {
            protected List<Hotel> doInBackground() {
                if ("name".equals(type))
                    return hotelDAO.searchByName(term);
                if ("country".equals(type))
                    return hotelDAO.searchByCountry(term);
                return hotelDAO.searchByCity(term);
            }

            protected void done() {
                try {
                    List<Hotel> list = get();
                    if (list == null || list.isEmpty()) {
                        JOptionPane.showMessageDialog(AdminHotelsPanel.this, "No hotels available for that search.");
                    } else {
                        SearchResultsFrame resultsFrame = new SearchResultsFrame(list);
                        resultsFrame.setLocationRelativeTo(AdminHotelsPanel.this);
                        resultsFrame.setVisible(true);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
}

// Helper class for search results (Styled)
class SearchResultsFrame extends JFrame {
    public SearchResultsFrame(java.util.List<Hotel> hotels) {
        setTitle("Search Results");
        setSize(1000, 600);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0, 31, 63)); // Navy Blue

        String[] cols = { "ID", "Name", "Country", "City", "Category", "Address", "Email", "Contact", "Price" };
        javax.swing.table.DefaultTableModel m = new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable t = new JTable(m);
        t.setRowHeight(30);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(220, 220, 220));

        // Header Style
        JTableHeader header = t.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(0, 31, 63)); // Navy Blue
        header.setForeground(new Color(0, 180, 216)); // Cyan
        header.setOpaque(true);
        DefaultTableCellRenderer hr = (DefaultTableCellRenderer) header.getDefaultRenderer();
        hr.setHorizontalAlignment(JLabel.CENTER);

        for (Hotel h : hotels) {
            m.addRow(new Object[] { h.getId(), h.getName(), h.getCountry(), h.getCity(), h.getCategory(),
                    h.getAddress(), h.getEmail(), h.getContact(), h.getPricePerNight() });
        }

        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);
        add(sp, BorderLayout.CENTER);

        JButton close = new JButton("Close Results");
        close.setFont(new Font("Segoe UI", Font.BOLD, 14));
        close.setForeground(Color.WHITE);
        close.setBackground(new Color(0, 180, 216)); // Cyan
        close.setFocusPainted(false);
        close.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        close.setContentAreaFilled(false);
        close.setOpaque(true);
        close.addActionListener(e -> dispose());

        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.setBackground(new Color(0, 31, 63)); // Navy
        p.add(close);
        add(p, BorderLayout.SOUTH);
    }
}
