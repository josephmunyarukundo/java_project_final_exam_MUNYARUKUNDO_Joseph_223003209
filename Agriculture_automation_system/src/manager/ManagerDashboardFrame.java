package manager;

import javax.swing.*;

import farmer.FarmerDashboardFrame;

import login.LoginFrame;

import admin.AdminDashboardFrame;
import admin.CropManagementFrame;
import admin.IrrigationLogsFrame;
import admin.ManageUsersFrame;
import admin.TaskManagementFrame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ManagerDashboardFrame extends JFrame {
	private JButton logoutBtn;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/agriculture_system";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "TheRealJeph";
    private int managerId; // Pass manager's user_id to constructor for filtering data
    
    private JButton cropsBtn, tasksBtn, farmerbtn,IrrigationLogsBtn,AlertsBtn;

    public ManagerDashboardFrame() {
        this.managerId = managerId;
        setTitle("Agriculture Automation System - Manager Dashboard");
        setSize(1000, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(41, 48, 59));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(180, getHeight()));
        sidebar.add(Box.createRigidArea(new Dimension(0, 24)));

        String[][] menu = {
                {"Dashboard", "icon/grid.png"},
                {"Irrigation Logs", "icon/drop.png"},
                {"Task Man.", "icon/clipboard1.png"},
                {"Crops", "icon/leaf1.png"},
                {"Farmers", "icon/user1.png"},
                {"Alerts", "icon/bell.png"},
                {" ", " "},
                {" ", " "},
                {" ", " "},
                {" ", " "},
                {"Logout", "icon/logout.png"},
        };

        for (String[] item : menu) {
            JButton btn = new JButton(item[0]);
            btn.setMaximumSize(new Dimension(170, 36));
            btn.setForeground(Color.WHITE);
            btn.setBackground(new Color(41, 48, 59));
            btn.setFocusPainted(false);
            btn.setFont(new Font("SansSerif", Font.BOLD, 13));
            // Big left margin: 28px. Top, Left, Bottom, Right.
            btn.setBorder(BorderFactory.createEmptyBorder(2, 28, 2, 8));
            btn.setHorizontalAlignment(SwingConstants.LEFT); // align text left
            btn.setHorizontalTextPosition(SwingConstants.RIGHT); // text right of icon
            btn.setIconTextGap(16); // space between icon and text

            // Sidebar icon
            try {
                ImageIcon iconImg = new ImageIcon(item[1]);
                Image img = iconImg.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                btn.setIcon(new ImageIcon(img));
            } catch (Exception ex) {}

            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            sidebar.add(btn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 9)));
            
            
            if ("Crops".equals(item[0])) cropsBtn = btn;
            if ("Task Man.".equals(item[0])) tasksBtn = btn;
            if ("Farmers".equals(item[0])) farmerbtn = btn;
            if ("Irrigation Logs".equals(item[0])) IrrigationLogsBtn = btn;
            if ("Alerts".equals(item[0])) AlertsBtn = btn;
            if ("Logout".equals(item[0])) logoutBtn = btn;
        }

        // --- Sidebar action wiring ---
        if (cropsBtn != null) {
            cropsBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new CropManagementFrame1().setVisible(true);
                    ManagerDashboardFrame.this.dispose();   // Dispose the dashboard
                }
            });
        }

        if (tasksBtn != null) {
            tasksBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new TaskManagementFrame1().setVisible(true);
                    ManagerDashboardFrame.this.dispose();
                }
            });
        }

        if (farmerbtn != null) {
        	farmerbtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new FarmerManagementFrame().setVisible(true);
                    ManagerDashboardFrame.this.dispose();
                }
            });
        }
        if (IrrigationLogsBtn != null) {
        	IrrigationLogsBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new IrrigationLogsFrame1().setVisible(true);
                    ManagerDashboardFrame.this.dispose();
                }
            });
        }
        if (AlertsBtn != null) {
        	AlertsBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new AlertsManagementFrame1().setVisible(true);
                    ManagerDashboardFrame.this.dispose();
                }
            });
        }
        
        
        if (logoutBtn != null) {
        	logoutBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new LoginFrame().setVisible(true);
                    ManagerDashboardFrame.this.dispose();
                }
            });
        }


        // Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(38, 120, 74));
        topBar.setPreferredSize(new Dimension(1000, 60));
        JLabel titleLabel = new JLabel("Agriculture Automation System");
        JLabel userLabel = new JLabel("Welcome, Manager");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 19));
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        titleLabel.setForeground(Color.WHITE);
        userLabel.setForeground(Color.WHITE);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));
        topBar.add(titleLabel, BorderLayout.WEST);
        topBar.add(userLabel, BorderLayout.EAST);

        // Fetch manager stats (replace SQL as needed for your schema)
        int totalTasks = getCount("SELECT COUNT(*) FROM tasks WHERE crop_id IN (SELECT crop_id FROM crops WHERE manager_id = ?)", managerId);
        int cropsManaged = getCount("SELECT COUNT(*) FROM crops WHERE manager_id = ?", managerId);
        int activeFarmers = getCount("SELECT COUNT(*) FROM users WHERE role_id = (SELECT role_id FROM roles WHERE role_name = 'Farmer') AND user_id IN (SELECT farmer_id FROM crop_assignments WHERE manager_id = ?)", managerId);
        int activeAlerts = getCount("SELECT COUNT(*) FROM alerts WHERE status IN ('Unseen','Active')");

        // Summary Cards Panel (Top Row)
        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new GridLayout(1, 4, 18, 0));
        cardsPanel.setBackground(new Color(242, 247, 245));
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(28, 16, 10, 16));

        cardsPanel.add(createCard("Total Tasks", String.valueOf(totalTasks), "icon/clipboard.png", new Color(41, 128, 185)));
        cardsPanel.add(createCard("Crops Under Management", String.valueOf(cropsManaged), "icon/leaf.png", new Color(39, 174, 96)));
        cardsPanel.add(createCard("Active Farmers", String.valueOf(activeFarmers), "icon/user.png", new Color(44, 62, 80)));
        cardsPanel.add(createCard("Active Alerts", String.valueOf(activeAlerts), "icon/notification-bell.png", new Color(231, 76, 60)));

        // Main Section: Two Panels Below Cards
        JPanel belowPanel = new JPanel();
        belowPanel.setLayout(new GridLayout(1, 2, 20, 0));
        belowPanel.setBackground(new Color(242, 247, 245));
        belowPanel.setBorder(BorderFactory.createEmptyBorder(4, 16, 18, 16));

        JPanel taskPanel = new JPanel(new BorderLayout());
        taskPanel.setBackground(Color.WHITE);
        taskPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210,220,210),2,true),
                BorderFactory.createEmptyBorder(18,18,18,18)));
        JLabel taskLabel = new JLabel("Task Overview");
        taskLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        taskPanel.add(taskLabel, BorderLayout.NORTH);

        JPanel cropPanel = new JPanel(new BorderLayout());
        cropPanel.setBackground(Color.WHITE);
        cropPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210,220,210),2,true),
                BorderFactory.createEmptyBorder(18,18,18,18)));
        JLabel cropLabel = new JLabel("Crop Progress");
        cropLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        cropPanel.add(cropLabel, BorderLayout.NORTH);

        belowPanel.add(taskPanel);
        belowPanel.add(cropPanel);

        // Main content layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(242, 247, 245));
        mainPanel.add(cardsPanel, BorderLayout.NORTH);
        mainPanel.add(belowPanel, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(topBar, BorderLayout.NORTH);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
    }

    // Summary card
    private JPanel createCard(String label, String value, String iconPath, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 2, true),
                BorderFactory.createEmptyBorder(12, 9, 12, 9)));

        JLabel iconLabel;
        try {
            ImageIcon iconImg = new ImageIcon(iconPath);
            Image img = iconImg.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            iconLabel = new JLabel(new ImageIcon(img));
        } catch (Exception ex) {
            iconLabel = new JLabel();
        }
        iconLabel.setPreferredSize(new Dimension(44, 40));
        iconLabel.setHorizontalAlignment(JLabel.CENTER);

        JLabel title = new JLabel(label, JLabel.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 17));
        title.setForeground(new Color(39, 54, 80));

        JLabel count = new JLabel(value, JLabel.CENTER);
        count.setFont(new Font("SansSerif", Font.BOLD, 32));
        count.setForeground(new Color(24, 32, 31));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(iconLabel, BorderLayout.WEST);
        topPanel.add(title, BorderLayout.CENTER);

        card.add(topPanel, BorderLayout.NORTH);
        card.add(count, BorderLayout.CENTER);
        return card;
    }

    // Fetch count from DB using prepared statement (for WHERE ... ?)
    private int getCount(String sql, int idValue) {
        int count = 0;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idValue);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) count = rs.getInt(1);
            }
        } catch (SQLException e) {
            // Handle error as needed
        }
        return count;
    }
    // Overload for simple queries (no parameters)
    private int getCount(String sql) {
        int count = 0;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) count = rs.getInt(1);
        } catch (SQLException e) {
            // Handle error as needed
        }
        return count;
    }

    // For demo, use your real Manager user_id here
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ManagerDashboardFrame().setVisible(true);
            }
        });
    }
}
