import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class AdminAirlinesPanel extends JPanel {

    private final AirlineDAO airlineDAO = new AirlineDAO();
    private final JPanel cards;
    private final CardLayout layout;

    // Table Components
    private DefaultTableModel tableModel;
    private JTable airlineTable;
    private List<Airline> currentAirlines;

    public AdminAirlinesPanel(JPanel cards, CardLayout layout) {
        this.cards = cards;
        this.layout = layout;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 248, 250)); // Light Gray BG

        // ===== LEFT SIDEBAR =====
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(new Color(0, 31, 63)); // Navy Blue
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));
        sidebar.setPreferredSize(new Dimension(220, 0));

        JLabel menuTitle = new JLabel("Manage Airlines", SwingConstants.CENTER);
        menuTitle.setForeground(Color.WHITE);
        menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));

        // Buttons
        JButton btnAdd = createSidebarButton("Add Airline");
        JButton btnEdit = createSidebarButton("Edit Selected");
        JButton btnDelete = createSidebarButton("Delete Airline");
        JButton btnShow = createSidebarButton("Refresh Airlines");
        JButton btnBack = createSidebarButton("Back to Home");

        JPanel buttonsPanel = new JPanel(new GridLayout(6, 1, 0, 15));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(menuTitle);
        buttonsPanel.add(btnAdd);
        buttonsPanel.add(btnEdit);
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(btnShow); // Kept functionality
        buttonsPanel.add(btnBack);

        sidebar.add(buttonsPanel, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);

        // ===== CENTER TABLE =====
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Table Setup
        String[] columns = { "ID", "Name", "Code", "Country" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        airlineTable = new JTable(tableModel);
        airlineTable.setRowHeight(30);
        airlineTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        airlineTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        airlineTable.setShowVerticalLines(false);
        airlineTable.setGridColor(new Color(220, 220, 220));

        // Header Style (Navy BG, Cyan Text, Centered)
        JTableHeader header = airlineTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(0, 31, 63)); // Navy Blue
        header.setForeground(new Color(0, 180, 216)); // Cyan Text
        header.setOpaque(true);

        // Center Headers
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) airlineTable.getTableHeader()
                .getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        header.setDefaultRenderer(headerRenderer);

        JScrollPane scrollPane = new JScrollPane(airlineTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Load initial data
        loadAirlines();

        // ===== ACTIONS =====
        btnAdd.addActionListener(e -> openEditor(null));

        btnEdit.addActionListener(e -> {
            int row = airlineTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select an airline first.");
                return;
            }
            Airline a = currentAirlines.get(row);
            openEditor(a);
        });

        btnDelete.addActionListener(e -> {
            int row = airlineTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select an airline first.");
                return;
            }
            Airline a = currentAirlines.get(row);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete " + a.getName() + "?", "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = airlineDAO.delete(a.getId());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Deleted.");
                    loadAirlines();
                } else {
                    JOptionPane.showMessageDialog(this, "Delete failed.");
                }
            }
        });

        // "Show Available" - Reuse existing modal logic but maybe just refresh table
        // since table is now main view
        // The user might want the separate modal view, but let's just make it a refresh
        // for now or show modal
        // Actually, let's keep it as "Refresh" effectively for the main table since the
        // main table IS the data now.
        // Or show the modal if they really want it. Let's make it show the modal to
        // stay safe to original logic.
        btnShow.addActionListener(e -> showAirlinesTableModal());

        btnBack.addActionListener(e -> {
            if (layout != null && cards != null)
                layout.show(cards, "ADMIN_HOME");
        });
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

    private void loadAirlines() {
        SwingWorker<List<Airline>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Airline> doInBackground() {
                return airlineDAO.getAll();
            }

            @Override
            protected void done() {
                try {
                    currentAirlines = get();
                    tableModel.setRowCount(0);

                    for (Airline a : currentAirlines) {
                        Object[] row = {
                                a.getId(),
                                a.getName(),
                                (a.getCode() != null ? a.getCode() : ""),
                                (a.getCountry() != null ? a.getCountry() : "")
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

    private void openEditor(Airline editing) {
        JTextField nameF = new JTextField();
        JTextField codeF = new JTextField();
        JTextField countryF = new JTextField();

        if (editing != null) {
            nameF.setText(editing.getName());
            codeF.setText(editing.getCode());
            countryF.setText(editing.getCountry());
        }

        JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
        f.add(new JLabel("Name:"));
        f.add(nameF);
        f.add(new JLabel("Code (e.g. PIA):"));
        f.add(codeF);
        f.add(new JLabel("Country:"));
        f.add(countryF);

        int r = JOptionPane.showConfirmDialog(this, f, editing == null ? "Add Airline" : "Edit Airline",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION)
            return;

        String name = nameF.getText().trim();
        String code = codeF.getText().trim();
        String country = countryF.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required.");
            return;
        }

        try {
            if (editing == null) {
                Airline a = new Airline(name, code, country);
                int newId = airlineDAO.createAirline(a);
                if (newId > 0)
                    JOptionPane.showMessageDialog(this, "Airline added.");
                else
                    JOptionPane.showMessageDialog(this, "Failed to add airline.");
            } else {
                editing.setName(name);
                editing.setCode(code);
                editing.setCountry(country);
                boolean ok = airlineDAO.updateAirline(editing);
                JOptionPane.showMessageDialog(this, ok ? "Airline updated." : "Update failed.");
            }
            loadAirlines();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // Kept original modal logic just in case user wants separate popup
    private void showAirlinesTableModal() {
        // Just refresh the main table since it's already there
        loadAirlines();
        JOptionPane.showMessageDialog(this, "Airlines list refreshed from database.");
    }
}
