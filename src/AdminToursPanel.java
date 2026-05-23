import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * AdminToursPanel
 * Refactored to match standard Sidebar+Table design.
 */
public class AdminToursPanel extends JPanel {
    private final JPanel cards;
    private final CardLayout cardLayout;
    private final TourDAO tourDAO = new TourDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private List<Tour> currentTours;

    public AdminToursPanel(JPanel cards, CardLayout cardLayout) {
        this.cards = cards;
        this.cardLayout = cardLayout;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 248, 250)); // Light Gray BG

        // ===== LEFT SIDEBAR =====
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(new Color(0, 31, 63)); // Navy Blue
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));
        sidebar.setPreferredSize(new Dimension(220, 0));

        JLabel menuTitle = new JLabel("Manage Tours", SwingConstants.CENTER);
        menuTitle.setForeground(Color.WHITE);
        menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));

        // Buttons
        JButton btnAdd = createSidebarButton("Add Tour");
        JButton btnUpdate = createSidebarButton("Update Selected");
        JButton btnDelete = createSidebarButton("Delete Tour");
        JButton btnShowAll = createSidebarButton("Show All");
        JButton btnSearch = createSidebarButton("Search");
        JButton btnBack = createSidebarButton("Back to Home");

        JPanel buttonsPanel = new JPanel(new GridLayout(7, 1, 0, 15));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(menuTitle);
        buttonsPanel.add(btnAdd);
        buttonsPanel.add(btnUpdate);
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(btnShowAll);
        buttonsPanel.add(btnSearch);
        buttonsPanel.add(btnBack);

        sidebar.add(buttonsPanel, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);

        // ===== CENTER TABLE =====
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Table Setup
        String[] cols = { "ID", "Title", "Type", "Total Cost", "Final Cost", "Created At" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(220, 220, 220));

        // Header Style (Navy BG, Cyan Text, Centered)
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(0, 31, 63)); // Navy Blue
        header.setForeground(new Color(0, 180, 216)); // Cyan Text
        header.setOpaque(true);

        // Center Headers
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) table.getTableHeader()
                .getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        header.setDefaultRenderer(headerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Initial Load
        loadAllTours();

        // ===== ACTIONS =====
        btnBack.addActionListener(e -> cardLayout.show(cards, "ADMIN_HOME"));

        btnShowAll.addActionListener(e -> loadAllTours());

        btnAdd.addActionListener(e -> {
            TourCreationFrame f = new TourCreationFrame();
            f.setVisible(true);
            // Ideally should wait/refresh, but without modal we assume user hits refresh
            // Or add a window listener to 'f' if TourCreationFrame extends JFrame
            f.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    loadAllTours();
                }
            });
        });

        btnUpdate.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a tour from the table first.");
                return;
            }
            Tour t = currentTours.get(row);

            JTextField tfTitle = new JTextField(t.getTitle(), 30);
            String[] types = { "SINGLE", "MULTI" };
            JComboBox<String> cbType = new JComboBox<>(types);
            cbType.setSelectedItem(t.getType());

            JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
            p.add(new JLabel("Title:"));
            p.add(tfTitle);
            p.add(new JLabel("Type:"));
            p.add(cbType);

            int ok = JOptionPane.showConfirmDialog(this, p, "Edit tour (ID: " + t.getId() + ")",
                    JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION)
                return;

            boolean updated = updateTourMeta(t.getId(), tfTitle.getText().trim(), (String) cbType.getSelectedItem());
            if (updated) {
                JOptionPane.showMessageDialog(this, "Tour updated.");
                loadAllTours();
            } else {
                JOptionPane.showMessageDialog(this, "Update failed.");
            }
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a tour from the table first.");
                return;
            }
            Tour t = currentTours.get(row);
            int c = JOptionPane.showConfirmDialog(this,
                    "Delete tour ID " + t.getId() + "? This includes flights/hotels.", "Confirm delete",
                    JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                boolean ok = tourDAO.deleteTour(t.getId());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Deleted.");
                    loadAllTours();
                } else {
                    JOptionPane.showMessageDialog(this, "Delete failed.");
                }
            }
        });

        btnSearch.addActionListener(e -> {
            String title = JOptionPane.showInputDialog(this, "Search by Tour Title:");
            if (title != null && !title.trim().isEmpty()) {
                filterTours(title.trim());
            } else if (title != null) {
                loadAllTours(); // If empty search, reset
            }
        });

        // Double click details
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = table.getSelectedRow();
                    if (r >= 0) {
                        Tour t = currentTours.get(r);
                        JOptionPane.showMessageDialog(AdminToursPanel.this, renderTourDetails(t), "Tour Details",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
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

    private void loadAllTours() {
        new SwingWorker<List<Tour>, Void>() {
            @Override
            protected List<Tour> doInBackground() {
                return tourDAO.getAll();
            }

            @Override
            protected void done() {
                try {
                    currentTours = get();
                    refreshTable(currentTours);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    // Client-side filter for simplicity as per original code behavior
    private void filterTours(String query) {
        if (currentTours == null)
            return;
        String q = query.toLowerCase();
        java.util.List<Tour> filtered = new java.util.ArrayList<>();
        for (Tour t : currentTours) {
            if (t.getTitle() != null && t.getTitle().toLowerCase().contains(q)) {
                filtered.add(t);
            }
        }
        refreshTable(filtered); // Update table but don't overwrite currentTours master list if we want to
                                // restore later?
        // Actually simplest is just show filtered. To restore user clicks "Show All".
    }

    private void refreshTable(List<Tour> list) {
        tableModel.setRowCount(0);
        if (list == null)
            return;
        for (Tour t : list) {
            tableModel.addRow(new Object[] {
                    t.getId(),
                    t.getTitle(),
                    t.getType(),
                    t.getTotalCost(),
                    t.getFinalCost(),
                    t.getCreatedAt()
            });
        }
    }

    private boolean updateTourMeta(int id, String title, String type) {
        String sql = "UPDATE tours SET title=?, type=? WHERE id=?";
        try (java.sql.Connection c = ConnectionDB.getConnection();
                java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, type);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private String renderTourDetails(Tour t) {
        if (t == null)
            return "";
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(t.getId()).append("\n");
        sb.append("Title: ").append(t.getTitle()).append("\n");
        sb.append("Type: ").append(t.getType()).append("\n");
        sb.append("Total cost: ").append(t.getTotalCost()).append("\n");
        sb.append("Discount %: ").append(t.getDiscountPct()).append("\n");
        sb.append("Final cost: ").append(t.getFinalCost()).append("\n");
        return sb.toString();
    }
}
