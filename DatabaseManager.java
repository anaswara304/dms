package admin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/admin";
    private static final String USER = "anaswara";
    private static final String PASSWORD = "1234";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        }
        return connection;
    }

    public static void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create tables
            stmt.execute("CREATE TABLE IF NOT EXISTS events (" +
                "event_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "title VARCHAR(255) NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS volunteers (" +
                "volunteer_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "firstname VARCHAR(255) NOT NULL, " +
                "lastname VARCHAR(255) NOT NULL, " +
                "user_id VARCHAR(255) NOT NULL, " +
                "event_id INT, " +
                "ph_no VARCHAR(20) NOT NULL)");

            // Add foreign key constraint if tables exist
            ResultSet eventsTable = conn.getMetaData().getTables(null, null, "events", null);
            ResultSet volunteersTable = conn.getMetaData().getTables(null, null, "volunteers", null);
            
            if (eventsTable.next() && volunteersTable.next()) {
                try {
                    stmt.execute("ALTER TABLE volunteers ADD CONSTRAINT fk_volunteer_event " +
                               "FOREIGN KEY (event_id) REFERENCES events(event_id)");
                } catch (SQLException e) {
                    System.out.println("Foreign key constraint already exists or could not be created");
                }
            }
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
