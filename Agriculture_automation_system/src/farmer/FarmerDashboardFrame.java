 package farmer;

import javax.swing.*;

import admin.AdminDashboardFrame;
import admin.AlertsManagementFrame;
import admin.CropManagementFrame;
import admin.IrrigationLogsFrame;
import admin.ManageUsersFrame;
import admin.TaskManagementFrame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import login.LoginFrame;


public class FarmerDashboardFrame extends JFrame {
	private JButton logoutBtn;


    private static final String DB_URL = "jdbc:mysql://localhost:3306/agriculture_system";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "TheRealJeph";
    private int farmerId;
    
    private JButton cropsBtn, tasksBtn,AlertsBtn;

    public FarmerDashboardFrame() {
        this.farmerId = farmerId;
        

        setTitle("Agriculture Automation System - Farmer Dashboard");
        setSize(950, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(41, 48, 59));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(170, getHeight()));
        sidebar.add(Box.createRigidArea(new Dimension(0, 22)));

        String[][] menu = {
                {"Dashboard", "icon/grid.png"},
                {"My Crops", "icon/leaf1.png"},
                {"My Tasks", "icon/clipboard1.png"},
                {"Alerts", "icon/bell.png"},
                {" ", " "},
                {" ", " "},
                {" ", " "},
                {" ", " "},
                {" ", " "},
                {"Logout", "icon/logout.png"}
        };

        for (String[] item : menu) {
            JButton btn = new JButton("  " + item[0]);
            btn.setMaximumSize(new Dimension(160, 36));
            btn.setForeground(Color.WHITE);
            btn.setBackground(new Color(41, 48, 59));
            btn.setFocusPainted(false);
            btn.setFont(new Font("SansSerif", Font.BOLD, 13));
            btn.setBorder(BorderFactory.createEmptyBorder(2, 28, 2, 8)); // Uniform left margin
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setHorizontalTextPosition(SwingConstants.RIGHT);
            btn.setIconTextGap(16);
            try {
                ImageIcon iconImg = new ImageIcon(item[1]);
                Image img = iconImg.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                btn.setIcon(new ImageIcon(img));
            } catch (Exception ex) {}
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            sidebar.add(btn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 9)));
            
            // Save references for event wiring
            if ("My Crops".equals(item[0])) cropsBtn = btn;
            if ("My Tasks".equals(item[0])) tasksBtn = btn;
            if ("Alerts".equals(item[0])) AlertsBtn = btn;
            if ("Logout".equals(item[0])) logoutBtn = btn;
        
        }
        
        // --- Sidebar action wiring ---
        if (cropsBtn != null) {
            cropsBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new MyCropsFrame().setVisible(true);
                    FarmerDashboardFrame.this.dispose();   // Dispose the dashboard
                }
            });
        }

        if (tasksBtn != null) {
            tasksBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new MyTasksFrame().setVisible(true);
                    FarmerDashboardFrame.this.dispose();
                }
            });
        }

        if (AlertsBtn != null) {
        	AlertsBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new AlertsManagementFrame().setVisible(true);
                    FarmerDashboardFrame.this.dispose();
                }
            });
        }

        if (logoutBtn != null) {
        	logoutBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new LoginFrame().setVisible(true);
                    FarmerDashboardFrame.this.dispose();
                }
            });
        }
        
        

      
        

        // Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(38, 120, 74));
        topBar.setPreferredSize(new Dimension(950, 50));
        JLabel titleLabel = new JLabel("Agriculture Automation System");
        JLabel userLabel = new JLabel("Welcome, Farmer");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        titleLabel.setForeground(Color.WHITE);
        userLabel.setForeground(Color.WHITE);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 25));
        topBar.add(titleLabel, BorderLayout.WEST);
        topBar.add(userLabel, BorderLayout.EAST);

        // Live stats for logged-in farmer
        int assignedCrops = getCount("SELECT COUNT(*) FROM crops WHERE farmer_id = ?", farmerId);
        int pendingTasks = getCount("SELECT COUNT(*) FROM tasks WHERE farmer_id = ? AND status='Pending'", farmerId);
        int completedTasks = getCount("SELECT COUNT(*) FROM tasks WHERE farmer_id = ? AND status='Completed'", farmerId);
        int activeAlerts = getCount("SELECT COUNT(*) FROM alerts WHERE crop_id IN (SELECT crop_id FROM crops WHERE farmer_id = ?) AND status IN ('Unseen','Active')", farmerId);

        // Summary Cards Panel (Top Row)
        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new GridLayout(1, 4, 18, 0));
        cardsPanel.setBackground(new Color(242, 247, 245));
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(28, 16, 10, 16));

        cardsPanel.add(createCard("Assigned Crops", String.valueOf(assignedCrops), "icon/leaf.png", new Color(39, 174, 96)));
        cardsPanel.add(createCard("Pending Tasks", String.valueOf(pendingTasks), "icon/load.png", new Color(41,128,185)));
        cardsPanel.add(createCard("Completed Tasks", String.valueOf(completedTasks), "icon/correct.png", new Color(44,62,80)));
        cardsPanel.add(createCard("Active Alerts", String.valueOf(activeAlerts), "icon/notification-bell.png", new Color(231,76,60)));

        // Main Section: 2 Panels Below Cards
        JPanel belowPanel = new JPanel();
        belowPanel.setLayout(new GridLayout(1, 2, 20, 0));
        belowPanel.setBackground(new Color(242, 247, 245));
        belowPanel.setBorder(BorderFactory.createEmptyBorder(4, 16, 18, 16));

        JPanel taskPanel = new JPanel(new BorderLayout());
        taskPanel.setBackground(Color.WHITE);
        taskPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210,220,210),2,true),
                BorderFactory.createEmptyBorder(18,18,18,18)));
        JLabel taskLabel = new JLabel("Upcoming Tasks");
        taskLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        taskPanel.add(taskLabel, BorderLayout.NORTH);
        // TODO: Add JTable or JList to show real upcoming tasks for farmer

        JPanel cropPanel = new JPanel(new BorderLayout());
        cropPanel.setBackground(Color.WHITE);
        cropPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210,220,210),2,true),
                BorderFactory.createEmptyBorder(18,18,18,18)));
        JLabel cropLabel = new JLabel("Crop Progress");
        cropLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        cropPanel.add(cropLabel, BorderLayout.NORTH);
        // TODO: Add JTable or JList to show names & growth stage for farmer's crops

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

    // Summary card with icon and live data
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

    // Query DB, with farmer_id
    private int getCount(String sql, int param) {
        int count = 0;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) count = rs.getInt(1);
            }
        } catch (SQLException e) {}
        return count;
    }

    // Usage: pass logged-in farmerId
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new FarmerDashboardFrame().setVisible(true);
            }
        });
    }
}
