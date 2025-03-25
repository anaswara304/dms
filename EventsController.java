import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import java.sql.*;

public class EventsController {

    @FXML
    private VBox eventsContainer; // VBox to display events dynamically

    // MySQL Database Connection Details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/admin";
    private static final String DB_USER = "anaswara";  // Replace with your MySQL username
    private static final String DB_PASSWORD = "1234";  

    @FXML
    public void initialize() {
        loadEventsFromDatabase();
    }

    private void loadEventsFromDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT event_id, title, state, district, place, date, time, description FROM events")) {

            while (rs.next()) {
                int eventId = rs.getInt("event_id");
                String title = rs.getString("title");
                String state = rs.getString("state");
                String district = rs.getString("district");
                String place = rs.getString("place");
                String date = rs.getString("date");
                String time = rs.getString("time");
                String description = rs.getString("description");

                addEventCard(eventId, title, state, district, place, date, time, description);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addEventCard(int eventId, String title, String state, String district, String place, String date, String time, String description) {
        VBox eventCard = new VBox();
        eventCard.setSpacing(5);
        eventCard.setPadding(new Insets(10));
        eventCard.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 15px;");

        Label idLabel = new Label("Event ID: " + eventId);
        idLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label locationLabel = new Label("Location: " + state + ", " + district + ", " + place);
        locationLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");

        Label dateTimeLabel = new Label("Date: " + date + " | Time: " + time);
        dateTimeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");

        Label descLabel = new Label("Description: " + description);
        descLabel.setStyle("-fx-font-size: 14px; -fx-wrap-text: true;");

        Button registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: #5a748a; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8px 12px; -fx-border-radius: 5;");

        registerButton.setOnAction(e -> openRegistrationForm(eventId, title, registerButton));

        eventCard.getChildren().addAll(idLabel, titleLabel, locationLabel, dateTimeLabel, descLabel, registerButton);

        eventsContainer.getChildren().add(eventCard);
    }

    private void openRegistrationForm(int eventId, String title, Button registerButton) {
        Stage stage = new Stage();
        stage.setTitle("Event Registration");

        VBox formLayout = new VBox(10);
        formLayout.setPadding(new Insets(20));

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");

        TextField userIdField = new TextField();
        userIdField.setPromptText("User ID");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");

        TextField eventIdField = new TextField(String.valueOf(eventId));
        eventIdField.setEditable(false);

        Button submitButton = new Button("Submit");
        submitButton.setStyle("-fx-background-color: #5a748a; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8px 12px; -fx-border-radius: 5;");

        submitButton.setOnAction(e -> {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String userId = userIdField.getText();
            String phone = phoneField.getText();

            if (firstName.isEmpty() || lastName.isEmpty() || userId.isEmpty() || phone.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please fill all fields!", ButtonType.OK);
                alert.showAndWait();
            } else {
                if (registerVolunteer(eventId, firstName, lastName, userId, phone)) {
                    registerButton.setText("Registered");
                    registerButton.setDisable(true);
                    registerButton.setStyle("-fx-background-color: #4c6278; -fx-text-fill: white;");
                    stage.close();
                }
            }
        });

        formLayout.getChildren().addAll(
            new Label("First Name:"), firstNameField,
            new Label("Last Name:"), lastNameField,
            new Label("Event ID:"), eventIdField,
            new Label("User ID:"), userIdField,
            new Label("Phone Number:"), phoneField,
            submitButton
        );

        ScrollPane scrollPane = new ScrollPane(formLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(10));

        Scene scene = new Scene(scrollPane, 350, 450);
        stage.setScene(scene);
        stage.show();
    }

    private boolean registerVolunteer(int eventId, String firstName, String lastName, String userId, String phone) {
        String sql = "INSERT INTO volunteers (event_id, firstname, lastname, user_id, ph_no) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, eventId);
            pstmt.setString(2, firstName);
            pstmt.setString(3, lastName);
            pstmt.setString(4, userId);
            pstmt.setString(5, phone);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Registration successful!", ButtonType.OK);
                alert.showAndWait();
                return true;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "User ID already registered!", ButtonType.OK);
            alert.showAndWait();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Database Error: " + e.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
        return false;
    }
}
