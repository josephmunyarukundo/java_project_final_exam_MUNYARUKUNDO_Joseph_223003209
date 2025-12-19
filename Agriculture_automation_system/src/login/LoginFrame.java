package login;

import javax.swing.*;

import farmer.FarmerDashboardFrame;
import manager.ManagerDashboardFrame;
import admin.AdminDashboardFrame;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;
    private JButton loginButton;

    // use a light background to look like the screenshot
    private static final Color DARK_GREEN = new Color(0x1A5D1A);
    private static final Color LIGHT_BG   = new Color(0xF4F4F4);

    public LoginFrame() {
        setTitle("Agriculture Automation System - Login");
        setSize(600, 420);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(LIGHT_BG);
        setResizable(false);

        // ---------- Top green header ----------
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(DARK_GREEN);
        header.setPreferredSize(new Dimension(600, 80));

        JLabel headerTitle = new JLabel("Agriculture Automation System", JLabel.CENTER);
        headerTitle.setForeground(Color.WHITE);
        headerTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        header.add(headerTitle, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);

        // ---------- Center wrapper to center the white card ----------
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(LIGHT_BG);
        add(centerWrapper, BorderLayout.CENTER);

        // ---------- White login card ----------
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(20, 40, 20, 40)
        ));
        card.setPreferredSize(new Dimension(380, 260));
        centerWrapper.add(card); // centers the card

        // Login title
        JLabel loginTitle = new JLabel("Login", JLabel.CENTER);
        loginTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        loginTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        card.add(loginTitle);

        // Username label above field
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        card.add(userLabel);

        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(10));

        // Password label above field
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        card.add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(16));

        // Centered green Login button
        loginButton = new JButton("Login");
        loginButton.setBackground(DARK_GREEN);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(140, 36));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(loginButton);
        card.add(Box.createVerticalStrut(14));

        // “Select Role” label + combo under button
        JLabel roleLabel = new JLabel("Select Role");
        roleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        card.add(roleLabel);

        roleBox = new JComboBox<>(new String[]{"Admin", "Manager", "Farmer"});
        roleBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        card.add(roleBox);

        // ---------- Events ----------
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        // Enter key triggers login
        getRootPane().setDefaultButton(loginButton);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        String query =
            "SELECT r.role_name FROM users u " +
            "JOIN roles r ON u.role_id = r.role_id " +
            "WHERE u.username = ? AND u.password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role_name");
                switch (role) {
                    case "Admin":
                        JOptionPane.showMessageDialog(this, "Welcome Admin");
                        new AdminDashboardFrame().setVisible(true);
                        this.dispose();
                        break;
                    case "Manager":
                        JOptionPane.showMessageDialog(this, "Welcome Manager");
                        new ManagerDashboardFrame().setVisible(true);
                        this.dispose();
                        break;
                    case "Farmer":
                        JOptionPane.showMessageDialog(this, "Welcome Farmer");
                        new FarmerDashboardFrame().setVisible(true);
                        this.dispose();
                        break;
                    default:
                        JOptionPane.showMessageDialog(this, "Welcome. Unknown role: " + role);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }
}
