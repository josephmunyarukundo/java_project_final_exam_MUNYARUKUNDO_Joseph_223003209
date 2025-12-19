package farmer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class AlertsManagementFrame extends JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/agriculture_system";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "TheRealJeph";

    private JTable alertsTable;
    private DefaultTableModel tableModel;
    private JButton markSeenBtn, markAllBtn, deleteBtn, refreshBtn, backBtn;
    private JLabel unseenBadge;
    private int unseenCount = 0;

    public AlertsManagementFrame() {
        setTitle("Alerts Management");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Header Section
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(22, 102, 56));

        JLabel titleLbl = new JLabel("Alerts Management");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setBorder(new EmptyBorder(20, 28, 20, 0));

        unseenBadge = new JLabel("[Unseen: 0]");
        unseenBadge.setFont(new Font("SansSerif", Font.BOLD, 18));
        unseenBadge.setForeground(Color.RED);
        unseenBadge.setBorder(new EmptyBorder(0, 16, 0, 28));

        backBtn = new JButton("Back");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        backBtn.setFocusPainted(false);
        backBtn.setBackground(Color.WHITE);
        backBtn.setForeground(new Color(22, 102, 56));
        backBtn.setBorder(new LineBorder(new Color(22, 102, 56), 2, true));
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        headerPanel.add(titleLbl, BorderLayout.WEST);
        headerPanel.add(unseenBadge, BorderLayout.CENTER);
        headerPanel.add(backBtn, BorderLayout.EAST);

        backBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new FarmerDashboardFrame().setVisible(true);
                dispose();
            }
        });

        // Action Buttons Panel
        markSeenBtn = styleBlueButton("Mark as Seen");
        markAllBtn = styleBlueButton("Mark All as Seen");
        deleteBtn = styleBlueButton("Delete Alert");
        refreshBtn = styleBlueButton("Refresh");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.add(markSeenBtn);
        buttonPanel.add(markAllBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);

        // Table Setup
        tableModel = new DefaultTableModel(
            new Object[]{"Alert ID", "Message", "Created At", "Status"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        alertsTable = new JTable(tableModel) {
            // Highlight Unseen row
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                String status = (String) getValueAt(row, 3);
                if ("Unseen".equalsIgnoreCase(status)) {
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                    c.setBackground(new Color(255, 234, 234));
                } else {
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        };
        alertsTable.setFillsViewportHeight(true);
        alertsTable.setRowHeight(26);
        alertsTable.setFont(new Font("SansSerif", Font.PLAIN, 15));
        alertsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 15));
        alertsTable.getTableHeader().setBackground(new Color(240,240,250));
        JScrollPane tableScroll = new JScrollPane(alertsTable);

        // Layout Assembly
        JPanel cardPanel = new JPanel(new BorderLayout(12,14));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(new CompoundBorder(
            new LineBorder(new Color(220,220,220), 2, true),
            new EmptyBorder(16, 28, 16, 28)));
        cardPanel.add(buttonPanel, BorderLayout.NORTH);
        cardPanel.add(tableScroll, BorderLayout.CENTER);

        JPanel mainBg = new JPanel(new BorderLayout());
        mainBg.setBackground(new Color(240, 247, 245));
        mainBg.add(headerPanel, BorderLayout.NORTH);
        mainBg.add(cardPanel, BorderLayout.CENTER);
        setContentPane(mainBg);

        // Button Actions
        refreshBtn.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { fetchAlerts(); } });
        markAllBtn.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { markAllAsSeen(); } });
        markSeenBtn.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { markSelectedAsSeen(); } });
        deleteBtn.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { deleteSelectedAlert(); } });

        fetchAlerts();
    }

    private JButton styleBlueButton(String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setBackground(new Color(41, 128, 185));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(new Color(41, 128, 185), 2, true));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 34));
        return btn;
    }

    // Load and show alerts
    private void fetchAlerts() {
        tableModel.setRowCount(0);
        unseenCount = 0;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM alerts ORDER BY created_at DESC")) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            while (rs.next()) {
                int alertId = rs.getInt("alert_id");
                String msg = rs.getString("message");
                Timestamp created = rs.getTimestamp("created_at");
                String status = rs.getString("status");
                if ("Unseen".equalsIgnoreCase(status)) unseenCount++;
                tableModel.addRow(new Object[]{
                    alertId,
                    msg,
                    sdf.format(created),
                    status
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not load alerts: " + ex.getMessage());
        }
        unseenBadge.setText("Unseen: " + unseenCount);
    }

    // Mark selected alert as Seen
    private void markSelectedAsSeen() {
        int row = alertsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an alert to mark as Seen.");
            return;
        }
        int alertId = (int) tableModel.getValueAt(row, 0);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE alerts SET status='Seen' WHERE alert_id=?");
            ps.setInt(1, alertId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not update alert: " + ex.getMessage());
            return;
        }
        fetchAlerts();
    }

    // Mark all alerts as Seen
    private void markAllAsSeen() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("UPDATE alerts SET status='Seen' WHERE status='Unseen'");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not update alerts: " + ex.getMessage());
            return;
        }
        fetchAlerts();
    }

    // Delete selected alert
    private void deleteSelectedAlert() {
        int row = alertsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an alert to delete.");
            return;
        }
        int alertId = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,"Delete alert #" + alertId + "?","Confirm Delete",JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM alerts WHERE alert_id=?");
            ps.setInt(1, alertId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not delete alert: " + ex.getMessage());
            return;
        }
        fetchAlerts();
    }

    // Optional: Auto-delete old alerts (call on startup/refresh if wanted)
    private void autoDeleteOldAlerts() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM alerts WHERE created_at < NOW() - INTERVAL 30 DAY");
            ps.executeUpdate();
        } catch (SQLException ex) {
            // ignore error for auto-delete
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new AlertsManagementFrame().setVisible(true);
            }
        });
    }
}
