package farmer;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

public class MyTasksFrame extends JFrame {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/agriculture_system";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "TheRealJeph";

    private int farmerId = 1; // set from login/session

    private JTable taskTable;
    private TaskTableModel tableModel;
    private JComboBox<String> filterBox;
    private JButton backBtn;

    public MyTasksFrame() {
        setTitle("My Tasks");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(960, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Green Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(39, 174, 96));
        headerPanel.setPreferredSize(new Dimension(960, 61));

        JLabel headerLabel = new JLabel("My Tasks", JLabel.CENTER);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        backBtn = new JButton("Back");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        backBtn.setBackground(Color.WHITE);
        backBtn.setForeground(new Color(39, 174, 96));
        backBtn.setFocusPainted(false);
        backBtn.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        headerPanel.add(backBtn, BorderLayout.EAST);

        String[] statusOptions = {"All", "Pending", "Completed"};
        filterBox = new JComboBox<String>(statusOptions);
        filterBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        filterBox.setBackground(Color.WHITE);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 11));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterBox);

        JPanel headerContainer = new JPanel();
        headerContainer.setLayout(new BoxLayout(headerContainer, BoxLayout.Y_AXIS));
        headerContainer.add(headerPanel);
        headerContainer.add(filterPanel);

        add(headerContainer, BorderLayout.NORTH);

        // --- Table Area (view only) ---
        tableModel = new TaskTableModel();
        taskTable = new JTable(tableModel);
        taskTable.setRowHeight(32);
        taskTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 15));
        taskTable.getTableHeader().setBackground(new Color(39, 174, 96));
        taskTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane tableScroll = new JScrollPane(taskTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder(10, 32, 24, 32));
        add(tableScroll, BorderLayout.CENTER);

        // --- Events ---
        backBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new FarmerDashboardFrame().setVisible(true);
                MyTasksFrame.this.dispose();
            }
        });

        filterBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshTable();
            }
        });

        refreshTable();
    }

    private void refreshTable() {
        ArrayList<TaskRow> tasks = new ArrayList<TaskRow>();
        String statusFilter = (String) filterBox.getSelectedItem();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement st = conn.prepareStatement(
                "SELECT t.task_id, c.crop_name, t.task_type, t.due_date, t.status " +
                "FROM tasks t JOIN crops c ON t.crop_id = c.crop_id " +
                "WHERE t.farmer_id = ? " +
                (statusFilter.equals("All") ? "" : "AND t.status = ? ") +
                "ORDER BY t.due_date ASC")) {

            st.setInt(1, farmerId);
            if (!statusFilter.equals("All")) {
                st.setString(2, statusFilter);
            }
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                tasks.add(new TaskRow(
                    rs.getInt("task_id"),
                    rs.getString("crop_name"),
                    rs.getString("task_type"),
                    rs.getString("due_date"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        tableModel.setTasks(tasks);
        tableModel.fireTableDataChanged();
    }

    // --- Data holder ---
    public static class TaskRow {
        int taskId;
        String cropName;
        String taskType;
        String dueDate;
        String status;

        TaskRow(int id, String cn, String tt, String dd, String st) {
            taskId = id;
            cropName = cn;
            taskType = tt;
            dueDate = dd;
            status = st;
        }
    }

    // --- Table model: view only ---
    public static class TaskTableModel extends AbstractTableModel {
        private final String[] colNames = {
            "Task ID", "Crop Name", "Task Type", "Due Date", "Status"
        };
        private ArrayList<TaskRow> tasks = new ArrayList<TaskRow>();

        public void setTasks(ArrayList<TaskRow> newTasks) {
            tasks = (newTasks == null) ? new ArrayList<TaskRow>() : newTasks;
        }

        public TaskRow getTaskAt(int r) {
            return tasks.get(r);
        }

        public int getRowCount() {
            return tasks.size();
        }

        public int getColumnCount() {
            return colNames.length;
        }

        public String getColumnName(int c) {
            return colNames[c];
        }

        public Object getValueAt(int r, int c) {
            TaskRow t = tasks.get(r);
            switch (c) {
                case 0: return t.taskId;
                case 1: return t.cropName;
                case 2: return t.taskType;
                case 3: return t.dueDate;
                case 4: return t.status;
            }
            return null;
        }

        public boolean isCellEditable(int r, int c) {
            return false;
        }

        public Class<?> getColumnClass(int c) {
            if (c == 0) return Integer.class;
            return String.class;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MyTasksFrame().setVisible(true);
            }
        });
    }
}
