package manager;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FarmerManagementFrame extends JFrame {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/agriculture_system";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "TheRealJeph";

    private JTextField fullNameField, contactField, locationField;
    private JComboBox<Integer> assignedCropBox;
    private JComboBox<String> userComboBox;
    private Map<String, Integer> userIdMap = new LinkedHashMap<>(); // Map username to user_id
    private JButton saveBtn, backBtn;
    private JTable farmerTable;
    private FarmerTableModel tableModel;
    private Integer editingFarmerId = null;

    public FarmerManagementFrame() {
        setTitle("Farmer Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // --- Top Green Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(28, 107, 51));
        headerPanel.setPreferredSize(new Dimension(1000, 56));

        JLabel headerLabel = new JLabel("Farmer Management");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 21));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        backBtn = new JButton("Back");
        backBtn.setFocusPainted(false);
        backBtn.setBackground(Color.WHITE);
        backBtn.setForeground(new Color(28, 107, 51));
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        backBtn.setBorder(BorderFactory.createEmptyBorder(8, 22, 8, 22));
        backBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ManagerDashboardFrame().setVisible(true);
                FarmerManagementFrame.this.dispose();
            }
        });

        headerPanel.add(backBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // --- Content Panel for Form and Table ---
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(242, 247, 245));

        // --- Form Panel (GridBag: horizontal layout) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(242, 247, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 16, 0, 16);
        gbc.gridy = 0;
        gbc.gridx = 0;

        // User select
        JLabel lblUser = new JLabel("User");
        lblUser.setFont(new Font("SansSerif", Font.BOLD, 13));
        formPanel.add(lblUser, gbc);
        gbc.gridy++;
        userComboBox = new JComboBox<>();
        userComboBox.setPreferredSize(new Dimension(140, 23));
        userComboBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        formPanel.add(userComboBox, gbc);

        // Full Name
        gbc.gridy = 0; gbc.gridx++;
        JLabel lblFullName = new JLabel("Full Name");
        lblFullName.setFont(new Font("SansSerif", Font.BOLD, 13));
        formPanel.add(lblFullName, gbc);
        gbc.gridy++;
        fullNameField = new JTextField(13);
        formPanel.add(fullNameField, gbc);

        // Contact
        gbc.gridy = 0; gbc.gridx++;
        JLabel lblContact = new JLabel("Contact");
        lblContact.setFont(new Font("SansSerif", Font.BOLD, 13));
        formPanel.add(lblContact, gbc);
        gbc.gridy++;
        contactField = new JTextField(11);
        formPanel.add(contactField, gbc);

        // Location
        gbc.gridy = 0; gbc.gridx++;
        JLabel lblLocation = new JLabel("Location");
        lblLocation.setFont(new Font("SansSerif", Font.BOLD, 13));
        formPanel.add(lblLocation, gbc);
        gbc.gridy++;
        locationField = new JTextField(13);
        formPanel.add(locationField, gbc);

        // Assigned Crop
        gbc.gridy = 0; gbc.gridx++;
        JLabel lblCrop = new JLabel("Assigned Crop ID");
        lblCrop.setFont(new Font("SansSerif", Font.BOLD, 13));
        formPanel.add(lblCrop, gbc);
        gbc.gridy++;
        assignedCropBox = new JComboBox<>();
        assignedCropBox.setPreferredSize(new Dimension(90, 23));
        assignedCropBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        formPanel.add(assignedCropBox, gbc);

        loadUserNames();
        loadAssignedCropIds();

        // --- Save Button Row ---
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(new Color(242, 247, 245));
        btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 36, 6));
        saveBtn = new JButton("Save");
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        saveBtn.setFocusPainted(false);
        saveBtn.setBackground(new Color(28, 107, 51));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setPreferredSize(new Dimension(120, 38));
        btnPanel.add(saveBtn);

        // --- Table setup ---
        tableModel = new FarmerTableModel();
        farmerTable = new JTable(tableModel);
        farmerTable.setRowHeight(32);
        farmerTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        TableColumnModel colModel = farmerTable.getColumnModel();
        colModel.getColumn(5).setCellRenderer(new IconCellRenderer("edit"));
        colModel.getColumn(6).setCellRenderer(new IconCellRenderer("delete"));

        JScrollPane tableScroll = new JScrollPane(farmerTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder(8,24,20,24));

        // --- Layout stacking ---
        JPanel topFormAndSavePanel = new JPanel();
        topFormAndSavePanel.setLayout(new BorderLayout());
        topFormAndSavePanel.setBackground(new Color(242, 247, 245));
        topFormAndSavePanel.add(formPanel, BorderLayout.NORTH);
        topFormAndSavePanel.add(btnPanel, BorderLayout.CENTER);
        contentPanel.add(topFormAndSavePanel, BorderLayout.NORTH);
        contentPanel.add(tableScroll, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        // --- Events ---
        saveBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveOrUpdateFarmer();
            }
        });

        farmerTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = farmerTable.rowAtPoint(e.getPoint());
                int col = farmerTable.columnAtPoint(e.getPoint());
                if (col == 5) { // Edit
                    Farmer f = tableModel.getFarmerAt(row);
                    fillForm(f);
                } else if (col == 6) { // Delete
                    Farmer f = tableModel.getFarmerAt(row);
                    int confirm = JOptionPane.showConfirmDialog(FarmerManagementFrame.this, "Delete "+f.fullName+"?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        deleteFarmer(f.farmerId);
                        refreshTable();
                    }
                }
            }
        });
        refreshTable();
    }

    private void fillForm(Farmer f) {
        editingFarmerId = f.farmerId;
        fullNameField.setText(f.fullName);
        contactField.setText(f.contact);
        locationField.setText(f.location);
        assignedCropBox.setSelectedItem(f.assignedCropId);

        // Set userComboBox selection by userId
        // (If your farmer table does not contain username, retrieve it using a JOIN for best UX)
        userComboBox.setSelectedItem(getUsernameForUserId(f.userId));
    }
    private void clearForm() {
        editingFarmerId = null;
        fullNameField.setText("");
        contactField.setText("");
        locationField.setText("");
        assignedCropBox.setSelectedIndex(-1);
        userComboBox.setSelectedIndex(-1);
    }

    private void saveOrUpdateFarmer() {
        String name = fullNameField.getText().trim();
        String contact = contactField.getText().trim();
        String location = locationField.getText().trim();
        Integer cropId = (Integer) assignedCropBox.getSelectedItem();

        // Get selected user id
        String selUserName = (String)userComboBox.getSelectedItem();
        Integer userId = (selUserName == null) ? null : userIdMap.get(selUserName);

        if (userId == null || userId < 0) {
            JOptionPane.showMessageDialog(this, "Please select a User.");
            return;
        }
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name required!");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            if (editingFarmerId == null) {
                String sql = "INSERT INTO farmers(user_id, full_name, contact, location, assigned_crop_id) VALUES (?,?,?,?,?)";
                try (PreparedStatement st = conn.prepareStatement(sql)) {
                    st.setInt(1, userId);
                    st.setString(2, name);
                    st.setString(3, contact.isEmpty() ? null : contact);
                    st.setString(4, location.isEmpty() ? null : location);
                    st.setObject(5, cropId, java.sql.Types.INTEGER);
                    st.executeUpdate();
                }
            } else {
                String sql = "UPDATE farmers SET user_id=?, full_name=?, contact=?, location=?, assigned_crop_id=? WHERE farmer_id=?";
                try (PreparedStatement st = conn.prepareStatement(sql)) {
                    st.setInt(1, userId);
                    st.setString(2, name);
                    st.setString(3, contact.isEmpty() ? null : contact);
                    st.setString(4, location.isEmpty() ? null : location);
                    st.setObject(5, cropId, java.sql.Types.INTEGER);
                    st.setInt(6, editingFarmerId);
                    st.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(FarmerManagementFrame.this, "Error saving to database: " + ex.getMessage());
        }
        clearForm();
        refreshTable();
    }

    private void loadUserNames() {
        userIdMap.clear();
        userComboBox.removeAllItems();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT user_id, username FROM users WHERE role_id = (SELECT role_id FROM roles WHERE role_name='Farmer')")) {
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String username = rs.getString("username");
                userIdMap.put(username, userId);
                userComboBox.addItem(username);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not load user list: " + ex.getMessage());
        }
        userComboBox.setSelectedIndex(-1);
    }

    private void loadAssignedCropIds() {
        assignedCropBox.removeAllItems();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT crop_id FROM crops")) {
            while (rs.next()) {
                assignedCropBox.addItem(rs.getInt("crop_id"));
            }
        } catch (SQLException e) {
            assignedCropBox.addItem(-1);
        }
        assignedCropBox.setSelectedIndex(-1);
    }

    private String getUsernameForUserId(int userId) {
        for (Map.Entry<String, Integer> entry : userIdMap.entrySet()) {
            if (entry.getValue() != null && entry.getValue() == userId) return entry.getKey();
        }
        return null;
    }

    private void refreshTable() {
        List<Farmer> farmers = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM farmers")) {
            while (rs.next()) {
                Farmer f = new Farmer(rs.getInt("farmer_id"), rs.getInt("user_id"),
                        rs.getString("full_name"), rs.getString("contact"),
                        rs.getString("location"), (Integer)rs.getObject("assigned_crop_id"));
                farmers.add(f);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        tableModel.setFarmers(farmers);
        tableModel.fireTableDataChanged();
    }

    private void deleteFarmer(int farmerId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement st = conn.prepareStatement("DELETE FROM farmers WHERE farmer_id=?")) {
            st.setInt(1, farmerId);
            st.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(FarmerManagementFrame.this, "Error deleting: " + ex.getMessage());
        }
    }

    // --- Table Model and Helpers ---
    public static class Farmer {
        int farmerId, userId, assignedCropId;
        String fullName, contact, location;
        Farmer(int id, int uid, String fn, String c, String loc, Integer cropId) {
            farmerId = id; userId = uid; fullName = fn;
            contact = c; location = loc; assignedCropId = cropId == null ? -1 : cropId;
        }
    }

    public static class FarmerTableModel extends AbstractTableModel {
        private final String[] colNames = {"Full Name", "Contact", "Location", "Crop ID", "User ID", "Edit", "Delete"};
        private List<Farmer> rows = new ArrayList<>();
        public void setFarmers(List<Farmer> fs) { rows = fs; }
        public Farmer getFarmerAt(int r) { return rows.get(r); }
        public int getRowCount() { return rows.size(); }
        public int getColumnCount() { return colNames.length; }
        public String getColumnName(int c) { return colNames[c]; }
        public Object getValueAt(int r, int c) {
            Farmer f = rows.get(r);
            switch (c) {
                case 0: return f.fullName;
                case 1: return f.contact;
                case 2: return f.location;
                case 3: return f.assignedCropId;
                case 4: return f.userId;
                case 5: return ""; // Edit icon
                case 6: return ""; // Delete icon
            }
            return null;
        }
        public boolean isCellEditable(int r, int c) { return false; }
        public Class<?> getColumnClass(int c) {
            if (c == 3 || c == 4) return Integer.class;
            return String.class;
        }
    }

    public static class IconCellRenderer extends DefaultTableCellRenderer {
        String type;
        Icon editIcon = new ImageIcon("icon/pencil.png"); // Adjust path as needed
        Icon deleteIcon = new ImageIcon("icon/delete.png");
        IconCellRenderer(String t) { type = t; }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = new JLabel();
            label.setHorizontalAlignment(JLabel.CENTER);
            if ("edit".equals(type)) label.setIcon(editIcon);
            if ("delete".equals(type)) label.setIcon(deleteIcon);
            return label;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new FarmerManagementFrame().setVisible(true);
            }
        });
    }
    }
