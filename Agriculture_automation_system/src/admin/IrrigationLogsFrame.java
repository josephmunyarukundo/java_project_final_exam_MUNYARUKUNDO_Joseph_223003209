package admin;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class IrrigationLogsFrame extends JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/agriculture_system";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "TheRealJeph";

    private JTable logsTable;
    private DefaultTableModel tableModel;
    private JButton addBtn, refreshBtn, backBtn;
    private JComboBox<String> cropDropdown;
    private JTextField moistureBeforeField, moistureAfterField, dateField;
    private JDialog addEditDialog;
    private Map<String, Integer> cropNameToId = new HashMap<>();
    private ImageIcon editIcon = new ImageIcon("icon/pencil.png");
    private ImageIcon deleteIcon = new ImageIcon("icon/delete.png");

    public IrrigationLogsFrame() {
        setTitle("Irrigation Logs Management");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBackground(new Color(240, 247, 245));

        // Section Panel (white card)
        JPanel sectionPanel = new JPanel();
        sectionPanel.setBackground(Color.WHITE);
        sectionPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(220,220,220), 2, true),
                new EmptyBorder(18, 30, 18, 30)));
        sectionPanel.setLayout(new BorderLayout(16, 18));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(22, 102, 56));

        JLabel titleLbl = new JLabel("Irrigation Logs Management");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setBorder(new EmptyBorder(20, 30, 20, 0));

        backBtn = new JButton("Back");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        backBtn.setFocusPainted(false);
        backBtn.setBackground(Color.WHITE);
        backBtn.setForeground(new Color(22, 102, 56));
        backBtn.setBorder(new LineBorder(new Color(22, 102, 56), 2, true));
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        headerPanel.add(titleLbl, BorderLayout.WEST);
        headerPanel.add(backBtn, BorderLayout.EAST);

        // Button Panel (only Add and Refresh)
        addBtn = styleGreenButton("Add Log");
        refreshBtn = styleGreenButton("Refresh");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 9));
        buttonPanel.setOpaque(false);
        buttonPanel.add(addBtn);
        buttonPanel.add(refreshBtn);

        // Table with action column
        tableModel = new DefaultTableModel(
            new Object[]{"Log ID", "Crop Name", "Moisture Before(%)", "Moisture After(%)", "Irrigation Date", "Action"}, 0) {
            public boolean isCellEditable(int row, int col) { return col == 5; }
        };
        logsTable = new JTable(tableModel);
        logsTable.setFillsViewportHeight(true);
        logsTable.setRowHeight(32);
        logsTable.setFont(new Font("SansSerif", Font.PLAIN, 15));
        logsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 15));
        JScrollPane tableScroll = new JScrollPane(logsTable);

        // Custom renderer/editor for Action column
        logsTable.getColumn("Action").setCellRenderer(new ActionButtonRenderer());
        logsTable.getColumn("Action").setCellEditor(new ActionButtonEditor());

        // Layout
        sectionPanel.add(buttonPanel, BorderLayout.NORTH);
        sectionPanel.add(tableScroll, BorderLayout.CENTER);
        JPanel mainBg = new JPanel(new BorderLayout());
        mainBg.setBackground(new Color(240, 247, 245));
        mainBg.add(headerPanel, BorderLayout.NORTH);
        mainBg.add(sectionPanel, BorderLayout.CENTER);

        setContentPane(mainBg);

        // Events
        backBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new AdminDashboardFrame().setVisible(true);
                dispose();
            }
        });

        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showAddEditDialog(false, -1);
            }
        });

        refreshBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fetchLogs();
            }
        });

        fetchLogs();
    }

    // Button styling utility
    private JButton styleGreenButton(String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setBackground(new Color(22, 102, 56));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(new Color(22, 102, 56), 2, true));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(125, 34));
        return btn;
    }

    // Fetch and populate log data
    private void fetchLogs() {
        tableModel.setRowCount(0);
        cropNameToId.clear();
        loadCropNames();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT l.log_id, c.crop_name, l.moisture_before, l.moisture_after, l.irrigation_date " +
                "FROM irrigation_logs l JOIN crops c ON l.crop_id = c.crop_id ORDER BY l.irrigation_date DESC")) {
            while (rs.next()) {
                int logId = rs.getInt("log_id");
                String cropName = rs.getString("crop_name");
                double before = rs.getDouble("moisture_before");
                double after = rs.getDouble("moisture_after");
                Timestamp irrigDate = rs.getTimestamp("irrigation_date");
                String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(irrigDate);
                tableModel.addRow(new Object[]{logId, cropName, before, after, dateStr, ""});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading logs: " + e.getMessage());
        }
    }

    // Load crops for dropdown and map
    private void loadCropNames() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT crop_id, crop_name FROM crops")) {
            while (rs.next()) {
                int id = rs.getInt("crop_id");
                String name = rs.getString("crop_name");
                cropNameToId.put(name, id);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Could not load crops: " + e.getMessage());
        }
    }

    // Add/Edit Log dialog
    private void showAddEditDialog(final boolean isEdit, final int row) {
        addEditDialog = new JDialog(this, (isEdit ? "Edit Log" : "Add Log"), true);
        addEditDialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cropDropdown = new JComboBox<>(cropNameToId.keySet().toArray(new String[0]));
        moistureBeforeField = new JTextField(8);
        moistureAfterField = new JTextField(8);
        dateField = new JTextField(15);

        gbc.gridx = 0; gbc.gridy = 0; addEditDialog.add(new JLabel("Crop:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; addEditDialog.add(cropDropdown, gbc);
        gbc.gridx = 0; gbc.gridy = 1; addEditDialog.add(new JLabel("Moisture Before (%):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; addEditDialog.add(moistureBeforeField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; addEditDialog.add(new JLabel("Moisture After (%):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; addEditDialog.add(moistureAfterField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; addEditDialog.add(new JLabel("Irrigation Date (yyyy-MM-dd HH:mm):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; addEditDialog.add(dateField, gbc);

        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        JPanel btnPanel = new JPanel();
        btnPanel.add(saveBtn); btnPanel.add(cancelBtn);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; addEditDialog.add(btnPanel, gbc);

        if (isEdit && row >= 0) {
            cropDropdown.setSelectedItem(tableModel.getValueAt(row, 1));
            moistureBeforeField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
            moistureAfterField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
            dateField.setText((String)tableModel.getValueAt(row, 4));
        }

        saveBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String cropName = (String)cropDropdown.getSelectedItem();
                int cropId = cropNameToId.getOrDefault(cropName, -1);
                String beforeStr = moistureBeforeField.getText().trim();
                String afterStr = moistureAfterField.getText().trim();
                String dateStr = dateField.getText().trim();

                // Pad with :00 if user only entered hour/minute
                if (dateStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$")) {
                    dateStr = dateStr + ":00";
                }

                if (!dateStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$")) {
                    JOptionPane.showMessageDialog(addEditDialog,
                        "Irrigation Date format must be: yyyy-MM-dd HH:mm:ss\nFor example: 2025-10-29 16:45:00");
                    return;
                }

                double before, after;
                try {
                    before = Double.parseDouble(beforeStr);
                    after = Double.parseDouble(afterStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(addEditDialog,
                        "Moisture values must be numerical.");
                    return;
                }

                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                    if (isEdit && row >= 0) {
                        int logId = (int)tableModel.getValueAt(row, 0);
                        String sql = "UPDATE irrigation_logs SET crop_id=?, moisture_before=?, moisture_after=?, irrigation_date=? WHERE log_id=?";
                        PreparedStatement ps = conn.prepareStatement(sql);
                        ps.setInt(1, cropId);
                        ps.setDouble(2, before);
                        ps.setDouble(3, after);
                        ps.setTimestamp(4, Timestamp.valueOf(dateStr));  // Now guaranteed correct format!
                        ps.setInt(5, logId);
                        ps.executeUpdate();
                    } else {
                        String sql = "INSERT INTO irrigation_logs (crop_id, moisture_before, moisture_after, irrigation_date) VALUES (?, ?, ?, ?)";
                        PreparedStatement ps = conn.prepareStatement(sql);
                        ps.setInt(1, cropId);
                        ps.setDouble(2, before);
                        ps.setDouble(3, after);
                        ps.setTimestamp(4, Timestamp.valueOf(dateStr));
                        ps.executeUpdate();
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(addEditDialog,
                        "DB Error: " + ex.getMessage());
                    return;
                }
                addEditDialog.dispose();
                fetchLogs();
            }
        });


        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                addEditDialog.dispose();
            }
        });

        addEditDialog.pack();
        addEditDialog.setLocationRelativeTo(this);
        addEditDialog.setVisible(true);
    }

    // Delete logic
    private void deleteSelectedLog(int row) {
        int logId = (int)tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete log #" + logId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "DELETE FROM irrigation_logs WHERE log_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, logId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not delete log: " + ex.getMessage());
        }
        fetchLogs();
    }

    // Action Button Renderer
    class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        JButton editBtn = new JButton(editIcon);
        JButton deleteBtn = new JButton(deleteIcon);
        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 6, 0));
            editBtn.setBorderPainted(false); editBtn.setContentAreaFilled(false);
            deleteBtn.setBorderPainted(false); deleteBtn.setContentAreaFilled(false);
            add(editBtn); add(deleteBtn);
        }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // Action Button Editor
    class ActionButtonEditor extends AbstractCellEditor implements TableCellEditor {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        JButton editBtn = new JButton(editIcon);
        JButton deleteBtn = new JButton(deleteIcon);
        int currentRow = -1;

        public ActionButtonEditor() {
            editBtn.setBorderPainted(false); editBtn.setContentAreaFilled(false);
            deleteBtn.setBorderPainted(false); deleteBtn.setContentAreaFilled(false);

            editBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                    showAddEditDialog(true, currentRow);
                }
            });
            deleteBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                    deleteSelectedLog(currentRow);
                }
            });
            panel.add(editBtn); panel.add(deleteBtn);
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            currentRow = row;
            return panel;
        }
        public Object getCellEditorValue() { return ""; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new IrrigationLogsFrame().setVisible(true);
            }
        });
    }
}

