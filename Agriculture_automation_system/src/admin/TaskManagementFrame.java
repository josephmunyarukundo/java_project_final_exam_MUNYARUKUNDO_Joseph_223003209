package admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class TaskManagementFrame extends JFrame {
    private JComboBox<String> cropComboBox, statusComboBox;
    private JTextField taskTypeField, dueDateField;
    private JButton saveButton, backButton;
    private JTable taskTable;
    private DefaultTableModel tableModel;

    private Map<String, Integer> cropMap = new HashMap<>();

    private static final String DB_URL = "jdbc:mysql://localhost:3306/agriculture_system";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "TheRealJeph";

    public TaskManagementFrame() {
        setTitle("Task Management");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top green bar with Back button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(38,120,74));
        topPanel.setPreferredSize(new Dimension(900, 60)); // Smaller height
        JLabel titleLabel = new JLabel("Task Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 19));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(0, 18, 0, 0));
        topPanel.add(titleLabel, BorderLayout.WEST);

        backButton = new JButton("Back");
        backButton.setFocusPainted(false);
        backButton.setBackground(Color.WHITE);
        backButton.setForeground(new Color(38,120,74));
        backButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        backButton.setPreferredSize(new Dimension(80, 24));
        backButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(38,120,74), 2, true),
            BorderFactory.createEmptyBorder(8,18,8,18)
        ));
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.setContentAreaFilled(true);
        backButton.setOpaque(true);
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);
        backButton.setBackground(Color.WHITE);
        // Rounded effect:
        backButton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void installUI(JComponent c) {
                super.installUI(c);
                ((JButton)c).setBorder(BorderFactory.createLineBorder(new Color(38,120,74), 2, true));
            }
        });
        topPanel.add(backButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Form panel: vertical box layout
        JPanel formVertical = new JPanel();
        formVertical.setLayout(new BoxLayout(formVertical, BoxLayout.Y_AXIS));
        formVertical.setBorder(new EmptyBorder(20, 24, 12, 24));

        // First row: three fields in one line, labels above
        JPanel labelPanel = new JPanel(new GridLayout(1,3,24,0));
        labelPanel.setOpaque(false);
        labelPanel.setMaximumSize(new Dimension(850, 25));
        labelPanel.add(buildFieldLabel("Task Type"));
        labelPanel.add(buildFieldLabel("Due Date"));
        labelPanel.add(buildFieldLabel("Crop"));
        formVertical.add(labelPanel);

        JPanel fieldRow = new JPanel(new GridLayout(1,3,24,0));
        fieldRow.setOpaque(false);
        fieldRow.setMaximumSize(new Dimension(850, 44));
        taskTypeField = new JTextField();
        dueDateField = new JTextField();
        cropComboBox = new JComboBox<>();
        loadCrops(); // Fill crop options from DB
        fieldRow.add(taskTypeField);
        fieldRow.add(dueDateField);
        fieldRow.add(cropComboBox);
        formVertical.add(fieldRow);

        // SPACE before status and save
        formVertical.add(Box.createVerticalStrut(10));

        // Second row: status label+field, Save button (aligned left)
        JPanel statusSaveRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        statusSaveRow.setOpaque(false);
        JPanel statusCol = new JPanel();
        statusCol.setLayout(new BoxLayout(statusCol, BoxLayout.Y_AXIS));
        statusCol.setOpaque(false);
        statusCol.add(buildFieldLabel("Status"));
        statusComboBox = new JComboBox<>(new String[]{"Pending", "Completed"});
        statusComboBox.setMaximumSize(new Dimension(130, 28));
        statusCol.add(statusComboBox);
        statusSaveRow.add(statusCol);

        saveButton = new JButton("Save");
        saveButton.setBackground(new Color(38,120,74));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        saveButton.setPreferredSize(new Dimension(120, 36));
        saveButton.setFocusPainted(false);
        saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveButton.setBorder(BorderFactory.createEmptyBorder(7,28,7,28));
        statusSaveRow.add(Box.createHorizontalStrut(18));
        statusSaveRow.add(saveButton);

        formVertical.add(statusSaveRow);

        // Add space before table
        formVertical.add(Box.createVerticalStrut(30));

        // Table
        tableModel = new DefaultTableModel(new String[]{"Task Type", "Due Date", "Crop", "Status", "Action"}, 0) {
            public boolean isCellEditable(int row, int col) { return col == 4; }
        };
        taskTable = new JTable(tableModel);
        taskTable.setRowHeight(30);
        taskTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 15));
        taskTable.getColumn("Action").setCellRenderer(new ActionRenderer());
        taskTable.getColumn("Action").setCellEditor(new ActionEditor());
        JScrollPane scrollPane = new JScrollPane(taskTable);
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(new EmptyBorder(0, 24, 18, 24));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Stack form + table vertically centered
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(formVertical, BorderLayout.NORTH);
        centerPanel.add(tablePanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Load task list
        loadTasks();

        setSaveAction();

        // Back button
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ((JFrame) SwingUtilities.getWindowAncestor(backButton)).dispose();
                new AdminDashboardFrame().setVisible(true);
            }
        });
    }

    // Label helper
    private JLabel buildFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setForeground(new Color(38,120,74));
        return lbl;
    }

    private void loadCrops() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT crop_id, crop_name FROM crops")) {
            cropComboBox.removeAllItems();
            cropMap.clear();
            while (rs.next()) {
                String name = rs.getString("crop_name");
                int id = rs.getInt("crop_id");
                cropComboBox.addItem(name);
                cropMap.put(name, id);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
        }
    }

    private void loadTasks() {
        tableModel.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT t.task_id, t.task_type, t.due_date, t.status, c.crop_name " +
                 "FROM tasks t LEFT JOIN crops c ON t.crop_id = c.crop_id")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("task_type"),
                        rs.getString("due_date"),
                        rs.getString("crop_name"),
                        rs.getString("status"),
                        rs.getInt("task_id")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
        }
    }

    private void saveTask() {
        String taskType = taskTypeField.getText().trim();
        String dueDate = dueDateField.getText().trim();
        String crop = (String) cropComboBox.getSelectedItem();
        String status = (String) statusComboBox.getSelectedItem();
        if (taskType.isEmpty() || dueDate.isEmpty() || crop == null || status.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields required!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO tasks (task_type, due_date, crop_id, status) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, taskType);
            stmt.setString(2, dueDate);
            stmt.setInt(3, cropMap.get(crop));
            stmt.setString(4, status);
            stmt.executeUpdate();
            loadTasks();
            taskTypeField.setText("");
            dueDateField.setText("");
            cropComboBox.setSelectedIndex(0);
            statusComboBox.setSelectedIndex(0);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
        }
    }

    private void updateTask(int taskId) {
        String taskType = taskTypeField.getText().trim();
        String dueDate = dueDateField.getText().trim();
        String crop = (String) cropComboBox.getSelectedItem();
        String status = (String) statusComboBox.getSelectedItem();
        if (taskType.isEmpty() || dueDate.isEmpty() || crop == null || status.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields required!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE tasks SET task_type=?, due_date=?, crop_id=?, status=? WHERE task_id=?")) {
            stmt.setString(1, taskType);
            stmt.setString(2, dueDate);
            stmt.setInt(3, cropMap.get(crop));
            stmt.setString(4, status);
            stmt.setInt(5, taskId);
            stmt.executeUpdate();
            loadTasks();
            setSaveAction();
            taskTypeField.setText("");
            dueDateField.setText("");
            cropComboBox.setSelectedIndex(0);
            statusComboBox.setSelectedIndex(0);
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
                saveTask();
            }
        });
    }

    private void setUpdateAction(final int taskId) {
        for (ActionListener al : saveButton.getActionListeners()) {
            saveButton.removeActionListener(al);
        }
        saveButton.setText("Update");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateTask(taskId);
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
                public void mouseClicked(MouseEvent e) { editTask(editingRow); }
            });
            del.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { deleteTask(editingRow); }
            });
        }
        public Object getCellEditorValue() { return null; }
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            editingRow = row;
            return panel;
        }
    }

    private void editTask(int row) {
        taskTypeField.setText((String) tableModel.getValueAt(row, 0));
        dueDateField.setText((String) tableModel.getValueAt(row, 1));
        String crop = (String) tableModel.getValueAt(row, 2);
        cropComboBox.setSelectedItem(crop);
        statusComboBox.setSelectedItem((String) tableModel.getValueAt(row, 3));
        int taskId = (int) tableModel.getValueAt(row, 4);
        setUpdateAction(taskId);
    }

    private void deleteTask(int row) {
        int taskId = (int) tableModel.getValueAt(row, 4);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this task?", "Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM tasks WHERE task_id=?")) {
                stmt.setInt(1, taskId);
                stmt.executeUpdate();
                loadTasks();
                setSaveAction();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new TaskManagementFrame().setVisible(true);
            }
        });
    }
}
