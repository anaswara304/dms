import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;

public class DonationController {

    // UI Components
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private TextField userIdField;
    @FXML private TextField otherAmountField;
    @FXML private Button amount500Btn, amount1000Btn, amount2000Btn, amount3000Btn, amount5000Btn;
    @FXML private Button makeDonationButton, requestDonationButton, logoutButton;
    
    private String selectedAmount = "";
    
    // Database Configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/admin";
    private static final String DB_USER = "anaswara";
    private static final String DB_PASSWORD = "1234";
    
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
    public void initialize() {
        // Setup payment methods
        paymentMethodComboBox.getItems().addAll(
            "Credit Card", "Debit Card", "UPI", "Net Banking", "PayPal"
        );
        
        // Setup amount buttons
        setupAmountButtons();
        
        // Button actions
        makeDonationButton.setOnAction(e -> handleDonation());
        requestDonationButton.setOnAction(e -> navigateToRequestPage());
        logoutButton.setOnAction(e -> handleLogout());
    }
    
    private void setupAmountButtons() {
        amount500Btn.setOnAction(e -> selectAmount("500"));
        amount1000Btn.setOnAction(e -> selectAmount("1000"));
        amount2000Btn.setOnAction(e -> selectAmount("2000"));
        amount3000Btn.setOnAction(e -> selectAmount("3000"));
        amount5000Btn.setOnAction(e -> selectAmount("5000"));
        
        otherAmountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                resetAmountSelection();
            }
        });
    }
    
    private void selectAmount(String amount) {
        selectedAmount = amount;
        otherAmountField.clear();
        resetButtonStyles();
        highlightSelectedButton(amount);
    }
    
    private void resetAmountSelection() {
        selectedAmount = "";
        resetButtonStyles();
    }
    
    private void resetButtonStyles() {
        String defaultStyle = "-fx-background-color: #5a748a; -fx-text-fill: white;";
        amount500Btn.setStyle(defaultStyle);
        amount1000Btn.setStyle(defaultStyle);
        amount2000Btn.setStyle(defaultStyle);
        amount3000Btn.setStyle(defaultStyle);
        amount5000Btn.setStyle(defaultStyle);
    }
    
    private void highlightSelectedButton(String amount) {
        String selectedStyle = "-fx-background-color: #3a5a78; -fx-border-color: #1a3a58; -fx-border-width: 2px;";
        switch (amount) {
            case "500": amount500Btn.setStyle(selectedStyle); break;
            case "1000": amount1000Btn.setStyle(selectedStyle); break;
            case "2000": amount2000Btn.setStyle(selectedStyle); break;
            case "3000": amount3000Btn.setStyle(selectedStyle); break;
            case "5000": amount5000Btn.setStyle(selectedStyle); break;
        }
    }
    
    private void handleDonation() {
        try {
            // Validate inputs
            String userId = userIdField.getText().trim();
            String paymentMethod = paymentMethodComboBox.getValue();
            String amountStr = selectedAmount.isEmpty() ? otherAmountField.getText().trim() : selectedAmount;
            
            if (userId.isEmpty() || paymentMethod == null || amountStr.isEmpty()) {
                showAlert("Error", "Please fill all fields");
                return;
            }
            
            BigDecimal amount;
            try {
                amount = new BigDecimal(amountStr);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    showAlert("Error", "Amount must be positive");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter a valid amount");
                return;
            }
            
            // Save to database using new donations table
            saveDonation(userId, amount, paymentMethod);
            
        } catch (Exception e) {
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void saveDonation(String userId, BigDecimal amount, String paymentMethod) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            // Establish connection
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false); // Start transaction
            
            // Prepare SQL for new donations table
            String sql = "INSERT INTO donations " +
                         "(user_id, amount, description, transaction_type, payment_method, status) " +
                         "VALUES (?, ?, ?, 'DONATION', ?, 'PENDING')";
            stmt = conn.prepareStatement(sql);
            
            // Set parameters
            stmt.setString(1, userId);
            stmt.setBigDecimal(2, amount);
            stmt.setString(3, "Payment via " + paymentMethod);
            stmt.setString(4, paymentMethod);
            
            // Execute and commit
            stmt.executeUpdate();
            conn.commit();
            
            showAlert("Success", "Thank you for your donation of ₹" + amount);
            resetForm();
            
        } catch (SQLException e) {
            // Rollback transaction on error
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            showAlert("Database Error", "Failed to save donation: " + e.getMessage());
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
    
    private void navigateToRequestPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RequestDonationPage.fxml"));
            Parent root = loader.load();
            
            Stage requestStage = new Stage();
            requestStage.setTitle("Request Donation Assistance");
            requestStage.setScene(new Scene(root));
            requestStage.show();
            
        } catch (IOException e) {
            showAlert("Error", "Could not load request donation page");
            e.printStackTrace();
        }
    }
    
    private void handleLogout() {
        try {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Logout");
            confirm.setHeaderText(null);
            confirm.setContentText("Are you sure you want to logout?");
            
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Login");
                stage.show();
            }
        } catch (IOException e) {
            showAlert("Error", "Could not load login page");
            e.printStackTrace();
        }
    }
    
    private void resetForm() {
        userIdField.clear();
        paymentMethodComboBox.getSelectionModel().clearSelection();
        otherAmountField.clear();
        resetAmountSelection();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
