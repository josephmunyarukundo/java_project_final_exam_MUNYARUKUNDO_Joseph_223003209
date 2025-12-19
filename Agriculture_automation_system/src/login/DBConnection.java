package login;

import java.sql.*;

public class DBConnection {
    // database connection parameters
    private static final String URL = "jdbc:mysql://localhost:3306/agriculture_system";
    private static final String USER = "root";
    private static final String PASS = "TheRealJeph"; // replace with your actual password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
