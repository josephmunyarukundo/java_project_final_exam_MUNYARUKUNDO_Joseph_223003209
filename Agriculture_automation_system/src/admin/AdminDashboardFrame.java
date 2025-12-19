package admin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

import admin.CropManagementFrame;       
import admin.TaskManagementFrame;        
import admin.ManageUsersFrame; 
import login.LoginFrame;

public class AdminDashboardFrame extends JFrame {
	private JButton logoutBtn;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/agriculture_system";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "TheRealJeph";

    // Sidebar button references for wiring
    private JButton cropsBtn, tasksBtn, manageUsersBtn,IrrigationLogsBtn,AlertsBtn;

    public AdminDashboardFrame() {
        setTitle("Agriculture Automation System - Admin Dashboard");
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
            {"Crops Man.", "icon/leaf1.png"},
            {"Tasks Man.", "icon/clipboard1.png"},
            {"Manage Users", "icon/user1.png"},
            {"Irrigation Logs", "icon/drop.png"},
            {"Alerts", "icon/bell.png"},
            {"Reports", "icon/report.png"},
            {" ", " "},
            {" ", " "},
            {"Logout", "icon/logout.png"}
        };

        // Sidebar button creation + reference storage
        for (String[] item : menu) {
            JButton btn = new JButton(item[0]);
            btn.setMaximumSize(new Dimension(170, 36));
            btn.setForeground(Color.WHITE);
            btn.setBackground(new Color(41, 48, 59));
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(2, 28, 2, 8));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setHorizontalTextPosition(SwingConstants.RIGHT);
            btn.setIconTextGap(16);

            // Sidebar icon
            try {
                ImageIcon iconImg = new ImageIcon(item[1]);
                Image img = iconImg.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                btn.setIcon(new ImageIcon(img));
            } catch (Exception ex) {}

            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            sidebar.add(btn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 9)));

            // Save references for event wiring
            if ("Crops Man.".equals(item[0])) cropsBtn = btn;
            if ("Tasks Man.".equals(item[0])) tasksBtn = btn;
            if ("Manage Users".equals(item[0])) manageUsersBtn = btn;
            if ("Irrigation Logs".equals(item[0])) IrrigationLogsBtn = btn;
            if ("Alerts".equals(item[0])) AlertsBtn = btn;
            if ("Logout".equals(item[0])) logoutBtn = btn;
        }

        // --- Sidebar action wiring ---
        if (cropsBtn != null) {
            cropsBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new CropManagementFrame().setVisible(true);
                    AdminDashboardFrame.this.dispose();   // Dispose the dashboard
                }
            });
        }

        if (tasksBtn != null) {
            tasksBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new TaskManagementFrame().setVisible(true);
                    AdminDashboardFrame.this.dispose();
                }
            });
        }

        if (manageUsersBtn != null) {
            manageUsersBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new ManageUsersFrame().setVisible(true);
                    AdminDashboardFrame.this.dispose();
                }
            });
        }
        if (IrrigationLogsBtn != null) {
        	IrrigationLogsBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new IrrigationLogsFrame().setVisible(true);
                    AdminDashboardFrame.this.dispose();
                }
            });
        }
        if (AlertsBtn != null) {
        	AlertsBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new AlertsManagementFrame().setVisible(true);
                    AdminDashboardFrame.this.dispose();
                }
            });
        }
        
        if (logoutBtn != null) {
        	logoutBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new LoginFrame().setVisible(true);
                    AdminDashboardFrame.this.dispose();
                }
            });
        }
        
        
        
        


        // -----------------------------

        // Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(38, 120, 74));
        topBar.setPreferredSize(new Dimension(950, 50));
        JLabel titleLabel = new JLabel("Agriculture Automation System");
        JLabel userLabel = new JLabel("Welcome, Admin");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        titleLabel.setForeground(Color.WHITE);
        userLabel.setForeground(Color.WHITE);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 25));
        topBar.add(titleLabel, BorderLayout.WEST);
        topBar.add(userLabel, BorderLayout.EAST);

        // Main Cards Panel
        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new GridLayout(1, 4, 18, 0));
        cardsPanel.setBackground(new Color(242, 247, 245));
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(24, 12, 8, 12));

        int totalUsers = getCount("SELECT COUNT(*) FROM users");
        int totalCrops = getCount("SELECT COUNT(*) FROM crops");
        int pendingTasks = getCount("SELECT COUNT(*) FROM tasks WHERE status='Pending'");
        int activeAlerts = getCount("SELECT COUNT(*) FROM alerts WHERE status='Unseen' OR status='Active'");

        cardsPanel.add(createCard("Users", totalUsers, "icon/user.png", new Color(44, 62, 80)));
        cardsPanel.add(createCard("Crops", totalCrops, "icon/leaf.png", new Color(39, 174, 96)));
        cardsPanel.add(createCard("Tasks", pendingTasks, "icon/clipboard.png", new Color(41, 128, 185)));
        cardsPanel.add(createCard("Alerts", activeAlerts, "icon/notification-bell.png", new Color(231, 76, 60)));

        // Status Panel
        JPanel statusPanel = new JPanel();
        statusPanel.setBackground(Color.WHITE);
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 12, 10, 12),
            BorderFactory.createLineBorder(new Color(220, 220, 220), 2, true)));
        statusPanel.setPreferredSize(new Dimension(1, 320));
        JLabel statusLabel = new JLabel("Current Status");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 21));
        statusLabel.setForeground(new Color(46, 56, 35));
        statusPanel.setLayout(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.NORTH);

        // Main panel layout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(242, 247, 245));
        mainPanel.add(cardsPanel, BorderLayout.NORTH);
        mainPanel.add(statusPanel, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(topBar, BorderLayout.NORTH);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createCard(String label, int value, String iconPath, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 2, true),
            BorderFactory.createEmptyBorder(12, 8, 12, 8)));

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
        iconLabel.setVerticalAlignment(JLabel.CENTER);

        JLabel title = new JLabel(label, JLabel.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 17));
        title.setForeground(new Color(39, 54, 80));

        JLabel count = new JLabel(String.valueOf(value), JLabel.CENTER);
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

    private int getCount(String query) {
        int count = 0;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next())
                count = rs.getInt(1);
        } catch (SQLException e) {
            // Handle or log error
            // JOptionPane.showMessageDialog(this, e.getMessage());
        }
        return count;
    }

    

    public static void main(String[] args) {
    	SwingUtilities.invokeLater(new Runnable() {
    	    public void run() {
    	        new AdminDashboardFrame().setVisible(true);
    	    }
    	});

    }
}
