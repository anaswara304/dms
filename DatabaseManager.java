package admin;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.sql.Connection;
import java.sql.DriverManager;
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

    private static void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            String createDonationsTable = "CREATE TABLE IF NOT EXISTS donations (" +
                "donation_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "donor_name VARCHAR(255) NOT NULL, " +
                "item_type VARCHAR(255) NOT NULL, " +
                "quantity VARCHAR(50) NOT NULL, " +
                "donation_date DATE NOT NULL, " +
                "status VARCHAR(50) NOT NULL, " +
                "contact_number VARCHAR(20) NOT NULL)";
                
            String createAllocationsTable = "CREATE TABLE IF NOT EXISTS allocations (" +
                "allocation_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "donation_id INT NOT NULL, " +
                "allocated_to VARCHAR(255) NOT NULL, " +
                "allocation_date DATE NOT NULL, " +
                "status VARCHAR(50) NOT NULL, " +
                "FOREIGN KEY (donation_id) REFERENCES donations(donation_id))";

            stmt.execute(createDonationsTable);
            stmt.execute(createAllocationsTable);
            
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            showAlert("Connection Error", "Error closing database connection: " + e.getMessage());
        }
    }
}
