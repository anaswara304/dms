import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.sql.*;

public class RequestDonationController {

    // Database Configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/admin";
    private static final String DB_USER = "anaswara";
    private static final String DB_PASSWORD = "1234";
    
    @FXML private TextField userIdField;
    @FXML private TextField amountNeededField;
    @FXML private TextArea reasonField;
    @FXML private Button submitRequestButton;

    // Static block to load JDBC driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver Registered!");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        submitRequestButton.setOnAction(event -> handleRequestSubmission());
    }

    private void handleRequestSubmission() {
        String userId = userIdField.getText().trim();
        String amountNeeded = amountNeededField.getText().trim();
        String reason = reasonField.getText().trim();

        // Validate inputs
        if (userId.isEmpty() || amountNeeded.isEmpty() || reason.isEmpty()) {
            showAlert("Error", "All fields are required!", Alert.AlertType.ERROR);
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(amountNeeded);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert("Error", "Amount must be a positive number", Alert.AlertType.ERROR);
                return;
            }

            saveRequest(userId, amount, reason);
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid amount. Please enter a valid number.", Alert.AlertType.ERROR);
        }
    }

    private void saveRequest(String userId, BigDecimal amount, String reason) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            // Establish connection
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false); // Start transaction
            
            // SQL for new donations table structure
            String sql = "INSERT INTO donations " +
                         "(user_id, amount, description, transaction_type, status) " +
                         "VALUES (?, ?, ?, 'REQUEST', 'PENDING')";
            
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            stmt.setBigDecimal(2, amount);
            stmt.setString(3, "Assistance needed: " + reason);
            
            // Execute and commit
            int rowsInserted = stmt.executeUpdate();
            conn.commit();
            
            if (rowsInserted > 0) {
                showAlert("Success", "Your request for ₹" + amount + " has been submitted.", Alert.AlertType.INFORMATION);
                closeWindow();
            }
        } catch (SQLException e) {
            // Rollback transaction on error
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            showAlert("Database Error", "Failed to submit request: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) submitRequestButton.getScene().getWindow();
        stage.close();
    }
}
