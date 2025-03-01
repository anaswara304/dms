package admin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/admin";
    private static final String USER = "anaswara";  // Replace with your MySQL username
    private static final String PASSWORD = "1234";  

    // Method to get MySQL connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }

    // Method to initialize the database (create tables)
    public static void initializeDatabase() {
        // SQL statement to create the news table
        String createNewsTable = "CREATE TABLE IF NOT EXISTS news (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "title VARCHAR(255) NOT NULL, " +
                "description TEXT NOT NULL, " +
                "place VARCHAR(255) NOT NULL, " +
                "date DATE NOT NULL, " +
                "time TIME NOT NULL);";

        // SQL statement to create the donations table
        String createDonationsTable = "CREATE TABLE IF NOT EXISTS donations (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "amount DECIMAL(10, 2) NOT NULL, " +
                "organisation VARCHAR(255) NOT NULL, " +
                "actions TEXT NOT NULL);";

        // SQL statement to create the allocations table
        String createAllocationsTable = "CREATE TABLE IF NOT EXISTS allocations (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "amount DECIMAL(10, 2) NOT NULL, " +
                "place VARCHAR(255) NOT NULL, " +
                "status VARCHAR(255) NOT NULL);";

        // SQL statement to create the events table
        String createEventsTable = "CREATE TABLE IF NOT EXISTS events (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "title VARCHAR(255) NOT NULL, " +
                "state VARCHAR(100) NOT NULL, " +
                "district VARCHAR(100) NOT NULL, " +
                "place VARCHAR(255) NOT NULL, " +
                "description TEXT NOT NULL, " +
                "date DATE NOT NULL, " +
                "time TIME NOT NULL);";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Execute the SQL statements to create tables
            stmt.execute(createNewsTable);
            stmt.execute(createDonationsTable);
            stmt.execute(createAllocationsTable);
            stmt.execute(createEventsTable);
            System.out.println("Database tables initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    // Method to test the connection
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("Connection to MySQL has been established.");
            }
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Test the connection and initialize the database
        testConnection();
        initializeDatabase();
    }
}