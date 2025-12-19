package admin;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CropManagementFrame extends JFrame {

    private JTextField nameField, soilField, dateField, stageField;
    private JButton saveButton, backButton;
    private JTable cropTable;
    private DefaultTableModel tableModel;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/agriculture_system";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "TheRealJeph";

    public CropManagementFrame() {
        setTitle("Crop Management");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel with green bar and Back button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(38, 120, 74));
        topPanel.setPreferredSize(new Dimension(900, 54));
        JLabel titleLabel = new JLabel("Crop Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
        topPanel.add(titleLabel, BorderLayout.WEST);

        backButton = new JButton("Back");
        backButton.setFocusPainted(false);
        backButton.setBackground(Color.WHITE);
        backButton.setForeground(new Color(38,120,74));
        backButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        backButton.setPreferredSize(new Dimension(80, 35));
        topPanel.add(backButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // FORM panel: horizontal row with fields and Save
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 14));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 24, 14, 24));
        nameField = new JTextField(12);
        soilField = new JTextField("0", 7);
        dateField = new JTextField(10);
        stageField = new JTextField(10);

        formPanel.add(new JLabel("Crop Name"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Soil Moisture Level"));
        formPanel.add(soilField);
        formPanel.add(new JLabel("Planting Date"));
        formPanel.add(dateField);
        formPanel.add(new JLabel("Growth Stage"));
        formPanel.add(stageField);

        saveButton = new JButton("Save");
        saveButton.setBackground(new Color(38,120,74));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        formPanel.add(saveButton);

        // Main center panel containing form + table (vertical stack)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(formPanel, BorderLayout.NORTH);

        // Table for crops
        tableModel = new DefaultTableModel(new String[]{
            "Crop Name", "Soil Moisture Level", "Planting Date", "Growth Stage", "Action"
        }, 0) {
            public boolean isCellEditable(int row, int col) { return col == 4; }
        };
        cropTable = new JTable(tableModel);
        cropTable.setRowHeight(30);
        cropTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 15));

        cropTable.getColumn("Action").setCellRenderer(new ActionRenderer());
        cropTable.getColumn("Action").setCellEditor(new ActionEditor());

        JScrollPane scrollPane = new JScrollPane(cropTable);
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 18, 24));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(tablePanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Load crop list
        loadCrops();

        // Set initial save action on button
        setSaveAction();

        // Back button
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ((JFrame) SwingUtilities.getWindowAncestor(backButton)).dispose();
                new AdminDashboardFrame().setVisible(true);
            }
        });
    }

    private void loadCrops() {
        tableModel.setRowCount(0); // Clear previous rows
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT crop_id, crop_name, soil_moisture_level, planting_date, growth_stage FROM crops")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("crop_name"),
                    rs.getString("soil_moisture_level"),
                    rs.getString("planting_date"),
                    rs.getString("growth_stage"),
                    rs.getInt("crop_id")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
        }
    }

    private void saveCrop() {
        String name = nameField.getText().trim();
        String soil = soilField.getText().trim();
        String date = dateField.getText().trim();
        String stage = stageField.getText().trim();
        if (name.isEmpty() || soil.isEmpty() || date.isEmpty() || stage.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields required!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO crops (crop_name, soil_moisture_level, planting_date, growth_stage) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, name);
            stmt.setDouble(2, Double.parseDouble(soil));
            stmt.setString(3, date);
            stmt.setString(4, stage);
            stmt.executeUpdate();
            loadCrops();
            nameField.setText(""); soilField.setText("0"); dateField.setText(""); stageField.setText("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
        }
    }

    private void updateCrop(int cropId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(
             "UPDATE crops SET crop_name=?, soil_moisture_level=?, planting_date=?, growth_stage=? WHERE crop_id=?")) {
            stmt.setString(1, nameField.getText().trim());
            stmt.setDouble(2, Double.parseDouble(soilField.getText().trim()));
            stmt.setString(3, dateField.getText().trim());
            stmt.setString(4, stageField.getText().trim());
            stmt.setInt(5, cropId);
            stmt.executeUpdate();
            loadCrops();
            setSaveAction(); // Restore to Save mode after update
            nameField.setText(""); soilField.setText("0"); dateField.setText(""); stageField.setText("");
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
                saveCrop();
            }
        });
    }

    private void setUpdateAction(final int cropId) {
        for (ActionListener al : saveButton.getActionListeners()) {
            saveButton.removeActionListener(al);
        }
        saveButton.setText("Update");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateCrop(cropId);
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
                public void mouseClicked(MouseEvent e) { editCrop(editingRow); }
            });
            del.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { deleteCrop(editingRow); }
            });
        }
        public Object getCellEditorValue() { return null; }
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            editingRow = row; return panel;
        }
    }

    private void editCrop(int row) {
        nameField.setText((String) tableModel.getValueAt(row, 0));
        soilField.setText((String) tableModel.getValueAt(row, 1));
        dateField.setText((String) tableModel.getValueAt(row, 2));
        stageField.setText((String) tableModel.getValueAt(row, 3));
        int cropId = (int) tableModel.getValueAt(row, 4);
        setUpdateAction(cropId);
    }

    private void deleteCrop(int row) {
        int cropId = (int) tableModel.getValueAt(row, 4);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this crop?", "Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM crops WHERE crop_id=?")) {
                stmt.setInt(1, cropId);
                stmt.executeUpdate();
                loadCrops();
                setSaveAction();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new CropManagementFrame().setVisible(true);
            }
        });
    }
}
