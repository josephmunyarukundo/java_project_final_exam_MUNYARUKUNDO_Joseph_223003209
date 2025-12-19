package admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ManageUsersFrame extends JFrame {

    private JTextField usernameField, passwordField;
    private JComboBox<String> roleComboBox;
    private JButton saveButton, backButton;
    private JTable userTable;
    private DefaultTableModel tableModel;
    // role name to ID map for lookup
    private Map<String, Integer> roleMap = new HashMap<>();

    private static final String DB_URL = "jdbc:mysql://localhost:3306/agriculture_system";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "TheRealJeph";

    public ManageUsersFrame() {
        setTitle("Manage Users");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top green bar
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(38,120,74));
        topPanel.setPreferredSize(new Dimension(900, 80));
        JLabel titleLabel = new JLabel("Manage Users");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 19));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(0, 18, 0, 0));
        topPanel.add(titleLabel, BorderLayout.WEST);

        backButton = new JButton("Back");
        backButton.setBackground(Color.WHITE);
        backButton.setForeground(new Color(38,120,74));
        backButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        backButton.setPreferredSize(new Dimension(90, 36));
        backButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(38,120,74), 2, true),
            BorderFactory.createEmptyBorder(8,18,8,18)
        ));
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.setOpaque(true);
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);
        topPanel.add(backButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Form: centered, labels above fields
        JPanel formVertical = new JPanel();
        formVertical.setLayout(new BoxLayout(formVertical, BoxLayout.Y_AXIS));
        formVertical.setBorder(new EmptyBorder(20, 24, 12, 24));

        JPanel labelPanel = new JPanel(new GridLayout(1,3,24,0));
        labelPanel.setOpaque(false);
        labelPanel.setMaximumSize(new Dimension(850, 25));
        labelPanel.add(buildFieldLabel("Username"));
        labelPanel.add(buildFieldLabel("Password"));
        labelPanel.add(buildFieldLabel("Role"));
        formVertical.add(labelPanel);

        JPanel fieldRow = new JPanel(new GridLayout(1,3,24,0));
        fieldRow.setOpaque(false);
        fieldRow.setMaximumSize(new Dimension(850, 44));
        usernameField = new JTextField();
        passwordField = new JTextField();

        roleComboBox = new JComboBox<>();
        loadRoles(); // fetch role names/IDs, fill combo

        fieldRow.add(usernameField);
        fieldRow.add(passwordField);
        fieldRow.add(roleComboBox);
        formVertical.add(fieldRow);

        formVertical.add(Box.createVerticalStrut(10));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        buttonRow.setOpaque(false);
        saveButton = new JButton("Save");
        saveButton.setBackground(new Color(38,120,74));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        saveButton.setPreferredSize(new Dimension(100, 36));
        saveButton.setFocusPainted(false);
        saveButton.setBorder(BorderFactory.createEmptyBorder(7,28,7,28));
        saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        buttonRow.add(saveButton);
        formVertical.add(buttonRow);

        formVertical.add(Box.createVerticalStrut(30));

        // Table
        tableModel = new DefaultTableModel(new String[]{"Username", "Role", "Created At", "Action"}, 0) {
            public boolean isCellEditable(int row, int col) { return col == 3; }
        };
        userTable = new JTable(tableModel);
        userTable.setRowHeight(30);
        userTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 15));
        userTable.getColumn("Action").setCellRenderer(new ActionRenderer());
        userTable.getColumn("Action").setCellEditor(new ActionEditor());

        JScrollPane scrollPane = new JScrollPane(userTable);
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(new EmptyBorder(0, 24, 18, 24));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(formVertical, BorderLayout.NORTH);
        centerPanel.add(tablePanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        loadUsers();
        setSaveAction();

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ((JFrame) SwingUtilities.getWindowAncestor(backButton)).dispose();
                new AdminDashboardFrame().setVisible(true);
            }
        });
    }

    private JLabel buildFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setForeground(new Color(38,120,74));
        return lbl;
    }

    private void loadRoles() {
        roleComboBox.removeAllItems();
        roleMap.clear();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT role_id, role_name FROM roles")) {
            while (rs.next()) {
                String name = rs.getString("role_name");
                int id = rs.getInt("role_id");
                roleComboBox.addItem(name);
                roleMap.put(name, id);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB Error loading roles: " + e.getMessage());
        }
    }

    private void loadUsers() {
        tableModel.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT u.user_id, u.username, u.password, r.role_name, u.created_at " +
                 "FROM users u JOIN roles r ON u.role_id = r.role_id")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("username"),
                    rs.getString("role_name"),
                    rs.getString("created_at"),
                    rs.getInt("user_id")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
        }
    }

    private void saveUser() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String roleName = (String) roleComboBox.getSelectedItem();
        Integer roleId = roleMap.get(roleName);
        if (username.isEmpty() || password.isEmpty() || roleId == null) {
            JOptionPane.showMessageDialog(this, "All fields required!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO users (username, password, role_id) VALUES (?, ?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setInt(3, roleId);
            stmt.executeUpdate();
            loadUsers();
            usernameField.setText("");
            passwordField.setText("");
            roleComboBox.setSelectedIndex(0);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
        }
    }

    private void updateUser(int userId) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String roleName = (String) roleComboBox.getSelectedItem();
        Integer roleId = roleMap.get(roleName);
        if (username.isEmpty() || password.isEmpty() || roleId == null) {
            JOptionPane.showMessageDialog(this, "All fields required!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE users SET username=?, password=?, role_id=? WHERE user_id=?")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setInt(3, roleId);
            stmt.setInt(4, userId);
            stmt.executeUpdate();
            loadUsers();
            setSaveAction();
            usernameField.setText("");
            passwordField.setText("");
            roleComboBox.setSelectedIndex(0);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
        }
    }

    private void setSaveAction() {
        for (ActionListener al : saveButton.getActionListeners()) {
            saveButton.removeActionListener(al);
        }
        saveButton.setText("Save");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveUser();
            }
        });
    }

    private void setUpdateAction(final int userId) {
        for (ActionListener al : saveButton.getActionListeners()) {
            saveButton.removeActionListener(al);
        }
        saveButton.setText("Update");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateUser(userId);
            }
        });
    }

    class ActionRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            panel.setOpaque(false);
            JLabel edit = new JLabel(new ImageIcon("icon/pencil.png"));
            JLabel del = new JLabel(new ImageIcon("icon/delete.png"));
            panel.add(edit); panel.add(del);
            return panel;
        }
    }

    class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        JPanel panel = new JPanel();
        JLabel edit = new JLabel(new ImageIcon("icon/pencil.png"));
        JLabel del = new JLabel(new ImageIcon("icon/delete.png"));
        int editingRow = -1;
        public ActionEditor() {
            panel.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 0));
            panel.setOpaque(false);
            panel.add(edit); panel.add(del);

            edit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            del.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            edit.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { editUser(editingRow); }
            });
            del.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { deleteUser(editingRow); }
            });
        }
        public Object getCellEditorValue() { return null; }
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            editingRow = row;
            return panel;
        }
    }

    private void editUser(int row) {
        usernameField.setText((String) tableModel.getValueAt(row, 0));
        passwordField.setText(""); // Do not show password for security!
        roleComboBox.setSelectedItem((String) tableModel.getValueAt(row, 1));
        int userId = (int) tableModel.getValueAt(row, 3);
        setUpdateAction(userId);
    }

    private void deleteUser(int row) {
        int userId = (int) tableModel.getValueAt(row, 3);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this user?", "Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE user_id=?")) {
                stmt.setInt(1, userId);
                stmt.executeUpdate();
                loadUsers();
                setSaveAction();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ManageUsersFrame().setVisible(true);
            }
        });
    }
}
