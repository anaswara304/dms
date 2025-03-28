package admin;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.geometry.Insets;
import javafx.scene.text.Text;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;
import java.sql.PreparedStatement;
import javafx.scene.paint.Color;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import java.sql.ResultSet;
import java.util.Optional;
import javafx.stage.Modality;
import java.sql.*;
import java.time.LocalTime;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AdminDashboard extends Application {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/admin";
    private static final String USER = "anaswara"; // Replace with your MySQL username
    private static final String PASSWORD = "1234";  // Replace with your MySQL password

    // Background image
    private static final String BACKGROUND_IMAGE_URL = "https://wallpapercave.com/wp/wp7429726.jpg";

    // ---------- News Section UI Components ----------
    private TextField addTitleField;
    
    private TableView<Donation> donationsTable;
    private TableView<Allocation> allocationsTable;  // Corrected spelling
    private TableView<Volunteer> volunteersTable;
    private TextArea addDescriptionArea;
    private TextField addPlaceField;
    private Connection connection;  // Database connection
    private TextArea editEventDescriptionArea;
    private DatePicker addDatePicker;
    private Spinner<Integer> addHourSpinner;
    private Spinner<Integer> addMinuteSpinner;
    private ComboBox<String> addAmPmComboBox;
    private Button addNewsButton;
    private TextArea addEventDescriptionArea;
    private ComboBox<String> editNewsComboBox;
    private TextField editNewsIdField;
    private TextField editTitleField;
    private TextArea editDescriptionArea;
    private TextField editPlaceField;
    private DatePicker editDatePicker;
    private Spinner<Integer> editHourSpinner;
    private Spinner<Integer> editMinuteSpinner;
    private ComboBox<String> editAmPmComboBox;
    private Button loadNewsButton;
    private Button updateNewsButton;
    private Button deleteNewsButton;
    private Button viewNewsButton;

    // ---------- Events Section UI Components ----------
    private TextField addEventTitleField;
    private TextField addEventStateField;
    private TextField addEventDistrictField;
    private TextField addEventPlaceField;
    private DatePicker addEventDatePicker;
    private Spinner<Integer> addEventHourSpinner;
    private Spinner<Integer> addEventMinuteSpinner;
    private ComboBox<String> addEventAmPmComboBox;
    private Button addEventButton;

    private ComboBox<String> editEventComboBox;
    private TextField editEventIdField;
    private TextField editEventTitleField;
    private TextField editEventStateField;
    private TextField editEventDistrictField;
    private TextField editEventPlaceField;
    private DatePicker editEventDatePicker;
    private Spinner<Integer> editEventHourSpinner;
    private Spinner<Integer> editEventMinuteSpinner;
    private ComboBox<String> editEventAmPmComboBox;
    private Button loadEventButton;
    private Button updateEventButton;
    private Button deleteEventButton;
    private Button viewEventsButton;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Admin Dashboard");

        // Initialize database and tables
        initializeDatabase();

        // Main layout with sidebar and central view
        BorderPane layout = new BorderPane();
        layout.setLeft(createSidebar(layout));
        layout.setCenter(createDashboard());

        Scene scene = new Scene(layout, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Initialize database and tables if they don't exist
    private void initializeDatabase() {
        try {
            // Initialize the connection
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            
            String createEventsTable = "CREATE TABLE IF NOT EXISTS events (" +
                "event_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "title VARCHAR(255) NOT NULL)";

            String createVolunteersTable = "CREATE TABLE IF NOT EXISTS volunteers (" +
                "volunteer_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "firstname VARCHAR(255) NOT NULL, " +
                "lastname VARCHAR(255) NOT NULL, " +
                "user_id VARCHAR(255) NOT NULL, " +
                "event_id INT, " +
                "ph_no VARCHAR(20) NOT NULL)";

            try (Statement stmt = connection.createStatement()) {
                // Create tables
                stmt.execute(createEventsTable);
                stmt.execute(createVolunteersTable);
                
                // Add foreign key only if both tables exist
                ResultSet eventsTable = connection.getMetaData().getTables(null, null, "events", null);
                ResultSet volunteersTable = connection.getMetaData().getTables(null, null, "volunteers", null);
                
                if (eventsTable.next() && volunteersTable.next()) {
                    try {
                        stmt.execute("ALTER TABLE volunteers ADD CONSTRAINT fk_volunteer_event " +
                                   "FOREIGN KEY (event_id) REFERENCES events(event_id)");
                    } catch (SQLException e) {
                        System.out.println("Foreign key constraint already exists or could not be created");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to initialize database: " + e.getMessage());
        }
    }
    // Enum for sections
    public enum Section {
        NEWS, EVENTS, VIEW_NEWS, VIEW_EVENTS, DONATIONS, VOLUNTEERS, ALERTS
    }
    // Update view based on selected section from the sidebar
    private void updateView(BorderPane layout, Section section) {
        VBox newSection;
        switch (section) {
            case NEWS:
                newSection = createManageNewsPage();
                break;
            case EVENTS:
                newSection = createManageEventsPage();
                break;
            case VIEW_NEWS:
                newSection = createViewNewsPage();
                break;
            case VIEW_EVENTS:
                newSection = createViewEventsPage();
                break;
            case DONATIONS:
                newSection = createDonationsSection();
                break;
            case VOLUNTEERS:
                newSection = createVolunteersSection();
                break;
            case ALERTS:
                newSection = createAlertsSection();
                break;
            default:
                newSection = createDashboard();
                break;
        }
        layout.setCenter(newSection);
    }
    // -------------------- Sidebar --------------------
    private VBox createSidebar(BorderPane layout) {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #2C3E50; -fx-pref-width: 220px;");
        sidebar.setAlignment(Pos.TOP_CENTER);

        Label adminLabel = new Label("Admin Dashboard");
        adminLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 20));
        adminLabel.setStyle("-fx-text-fill: #ECF0F1; -fx-padding: 5px;");
        sidebar.getChildren().add(adminLabel);

        String[] items = {"News", "Events", "Donations", "Volunteers", "Alerts"};
        for (String item : items) {
            Button button = new Button(item);
            button.setPrefWidth(200);
            button.setStyle("-fx-background-color: #34495E; -fx-text-fill: #ECF0F1; -fx-font-size: 16px; -fx-background-radius: 5px; -fx-padding: 10px;");
            button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #1ABC9C; -fx-text-fill: #FFFFFF; -fx-background-radius: 5px; -fx-padding: 10px;"));
            button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #34495E; -fx-text-fill: #ECF0F1; -fx-background-radius: 5px; -fx-padding: 10px;"));

            if (item.equals("News")) {
                button.setOnAction(e -> updateView(layout, Section.NEWS));
            } else if (item.equals("Events")) {
                button.setOnAction(e -> updateView(layout, Section.EVENTS));
            } else if (item.equals("Donations")) {
                button.setOnAction(e -> updateView(layout, Section.DONATIONS));
            } else if (item.equals("Volunteers")) {
                button.setOnAction(e -> updateView(layout, Section.VOLUNTEERS));
            } else if (item.equals("Alerts")) {
                button.setOnAction(e -> updateView(layout, Section.ALERTS));
            }

            VBox.setMargin(button, new Insets(10, 0, 10, 0));
            sidebar.getChildren().add(button);
        }
        return sidebar;
    }
    // -------------------- Dashboard --------------------
    private VBox createDashboard() {
        VBox dashboard = new VBox();
        dashboard.setPadding(new Insets(30));
        dashboard.setAlignment(Pos.CENTER);

        // Set background image
        Image backgroundImage = new Image(BACKGROUND_IMAGE_URL);
        BackgroundImage bgImage = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
        );
        dashboard.setBackground(new Background(bgImage));

        // Create a card-like container for the content
        VBox card = new VBox(30);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.9); " +
            "-fx-background-radius: 20px; " +
            "-fx-border-radius: 20px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 15, 0, 0, 0);"
        );
        card.setMaxWidth(900);
        card.setMaxHeight(700);

        // Welcome Label
        Label welcomeLabel = new Label("Welcome to the Admin Dashboard");
        welcomeLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 42));
        welcomeLabel.setStyle("-fx-text-fill: #2C3E50; -fx-effect: dropshadow(one-pass-box, rgba(0, 0, 0, 0.1), 2, 0, 1, 1);");
        welcomeLabel.setWrapText(true);
        welcomeLabel.setAlignment(Pos.CENTER);

        // Subtitle Label
        Label subtitleLabel = new Label("Empowering Communities Through Disaster Management");
        subtitleLabel.setFont(Font.font("Roboto", FontWeight.NORMAL, 24));
        subtitleLabel.setStyle("-fx-text-fill: #34495E;");
        subtitleLabel.setMaxWidth(800);
        subtitleLabel.setWrapText(true);
        subtitleLabel.setAlignment(Pos.CENTER);

        // Add an inspirational quote
        Label quoteLabel = new Label("\"Do what you can, with what you have, where you are.\"");
        quoteLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 28));
        quoteLabel.setStyle("-fx-text-fill: #2C3E50; -fx-padding: 20px;");
        quoteLabel.setWrapText(true);
        quoteLabel.setAlignment(Pos.CENTER);

        // Add a decorative line for separation
        Line separatorLine = new Line();
        separatorLine.setStartX(0);
        separatorLine.setEndX(600);
        separatorLine.setStroke(Color.web("#BDC3C7"));
        separatorLine.setStrokeWidth(1);

        // Add all elements to the card
        card.getChildren().addAll(welcomeLabel, subtitleLabel, quoteLabel, separatorLine);

        // Add the card to the dashboard
        dashboard.getChildren().add(card);

        return dashboard;
    }

    // -------------------- News Section --------------------
    private VBox createManageNewsPage() {
        VBox manageNewsPage = new VBox(20);
        manageNewsPage.setPadding(new Insets(20));
        manageNewsPage.setAlignment(Pos.TOP_CENTER);

        // Set background image
        Image backgroundImage = new Image(BACKGROUND_IMAGE_URL);
        BackgroundImage bgImage = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
        );
        manageNewsPage.setBackground(new Background(bgImage));

        // Header
        Label header = new Label("Manage News");
        header.setFont(Font.font("Roboto", FontWeight.BOLD, 28));
        header.setStyle("-fx-text-fill: #2C3E50;");
        VBox.setMargin(header, new Insets(0, 0, 20, 0));

        // Create a card for the content
        VBox card = new VBox(20);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.9); " +
            "-fx-background-radius: 20px; " +
            "-fx-border-radius: 20px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 15, 0, 0, 0);"
        );
        card.setMaxWidth(1000);

        // TabPane for Add/Edit News
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");

        Tab addNewsTab = new Tab("Add News", createAddNewsPane());
        Tab editNewsTab = new Tab("Edit/Delete News", createEditNewsPane());
        tabPane.getTabs().addAll(addNewsTab, editNewsTab);

        // Add a button to view news
        viewNewsButton = new Button("View News");
        viewNewsButton.setStyle("-fx-background-color: linear-gradient(to bottom right, #72a0c1, #2980B9); -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 5px;");
        viewNewsButton.setOnAction(e -> updateView((BorderPane) manageNewsPage.getParent(), Section.VIEW_NEWS));

        card.getChildren().addAll(header, tabPane, viewNewsButton);
        manageNewsPage.getChildren().add(card);

        return manageNewsPage;
    }

    // -------------------- Add News Pane --------------------
    private Pane createAddNewsPane() {
        VBox addPane = new VBox(15);
        addPane.setPadding(new Insets(20));
        addPane.setAlignment(Pos.TOP_CENTER);
        addPane.setStyle("-fx-background-color: transparent;");

        // Create a card for the input fields
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: #72A0C1; " +
            "-fx-background-radius: 10px; " +
            "-fx-border-radius: 10px; " +
            "-fx-border-color: #5E8CA8; " +
            "-fx-border-width: 1px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 10, 0, 0, 0);"
        );
        card.setMaxWidth(600);

        // Add components to the card
        addTitleField = new TextField();
        addTitleField.setPromptText("Enter title");
        addTitleField.setMaxWidth(500);
        addTitleField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        addDescriptionArea = new TextArea();
        addDescriptionArea.setPromptText("Enter description");
        addDescriptionArea.setPrefRowCount(4);
        addDescriptionArea.setMaxWidth(500);
        addDescriptionArea.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        addPlaceField = new TextField();
        addPlaceField.setPromptText("Enter place");
        addPlaceField.setMaxWidth(500);
        addPlaceField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        addDatePicker = new DatePicker();
        addDatePicker.setPromptText("Select date");
        addDatePicker.setMaxWidth(500);
        addDatePicker.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        addHourSpinner = new Spinner<>(new IntegerSpinnerValueFactory(1, 12, 9));
        addHourSpinner.setEditable(true);
        addHourSpinner.setPrefWidth(80);
        addHourSpinner.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        addMinuteSpinner = new Spinner<>(new IntegerSpinnerValueFactory(0, 59, 0));
        addMinuteSpinner.setEditable(true);
        addMinuteSpinner.setPrefWidth(80);
        addMinuteSpinner.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        addAmPmComboBox = new ComboBox<>(FXCollections.observableArrayList("AM", "PM"));
        addAmPmComboBox.setValue("AM");
        addAmPmComboBox.setPrefWidth(100);
        addAmPmComboBox.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        HBox timeBox = new HBox(10, new Label("Hour:"), addHourSpinner,
                new Label("Minute:"), addMinuteSpinner,
                new Label(""), addAmPmComboBox);
        timeBox.setAlignment(Pos.CENTER);

        addNewsButton = new Button("Add News");
        addNewsButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #1ABC9C, #16A085); " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-background-radius: 5px; " +
            "-fx-padding: 10px 20px;"
        );
        addNewsButton.setOnAction(e -> handleAddNews());

        card.getChildren().addAll(
                new Label("Title:"), addTitleField,
                new Label("Description:"), addDescriptionArea,
                new Label("Place:"), addPlaceField,
                new Label("Date:"), addDatePicker,
                new Label("Time:"), timeBox,
                addNewsButton
        );

        addPane.getChildren().add(card);
        return addPane;
    }

    // -------------------- Edit News Pane --------------------
    private Pane createEditNewsPane() {
        VBox editPane = new VBox(15);
        editPane.setPadding(new Insets(20));
        editPane.setAlignment(Pos.TOP_CENTER);
        editPane.setStyle("-fx-background-color: transparent;");

        // Create a card for the input fields
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: #72A0C1; " +
            "-fx-background-radius: 10px; " +
            "-fx-border-radius: 10px; " +
            "-fx-border-color: #5E8CA8; " +
            "-fx-border-width: 1px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 10, 0, 0, 0);"
        );
        card.setMaxWidth(600);

        // Initialize the hour spinner
        editHourSpinner = new Spinner<>(new IntegerSpinnerValueFactory(1, 12, 9));
        editHourSpinner.setEditable(true);
        editHourSpinner.setPrefWidth(80);
        editHourSpinner.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        // Initialize the minute spinner
        editMinuteSpinner = new Spinner<>(new IntegerSpinnerValueFactory(0, 59, 0));
        editMinuteSpinner.setEditable(true);
        editMinuteSpinner.setPrefWidth(80);
        editMinuteSpinner.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        // Initialize the AM/PM combo box
        editAmPmComboBox = new ComboBox<>(FXCollections.observableArrayList("AM", "PM"));
        editAmPmComboBox.setValue("AM");
        editAmPmComboBox.setPrefWidth(100);
        editAmPmComboBox.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        // Layout for time fields
        HBox timeBox = new HBox(10, new Label("Hour:"), editHourSpinner,
                new Label("Minute:"), editMinuteSpinner,
                new Label("AM/PM:"), editAmPmComboBox);
        timeBox.setAlignment(Pos.CENTER);

        // Add other components to the card
        editNewsComboBox = new ComboBox<>();
        editNewsComboBox.setPromptText("Select News (ID - Title)");
        editNewsComboBox.setMaxWidth(500);
        editNewsComboBox.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");
        refreshEditNewsComboBox();

        loadNewsButton = new Button("Load News");
        loadNewsButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #4178A0, #2C5A80); " +
            "-fx-text-fill: white; -fx-font-size: 14px; " +
            "-fx-background-radius: 5px; -fx-padding: 10px 20px;"
        );
        loadNewsButton.setOnAction(e -> loadNewsForEdit());

        editNewsIdField = new TextField();
        editNewsIdField.setPromptText("News ID");
        editNewsIdField.setMaxWidth(500);
        editNewsIdField.setEditable(false);
        editNewsIdField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        editTitleField = new TextField();
        editTitleField.setPromptText("Edit title");
        editTitleField.setMaxWidth(500);
        editTitleField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        editDescriptionArea = new TextArea();
        editDescriptionArea.setPromptText("Edit description");
        editDescriptionArea.setPrefRowCount(4);
        editDescriptionArea.setMaxWidth(500);
        editDescriptionArea.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        editPlaceField = new TextField();
        editPlaceField.setPromptText("Edit place");
        editPlaceField.setMaxWidth(500);
        editPlaceField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        editDatePicker = new DatePicker();
        editDatePicker.setPromptText("Select date");
        editDatePicker.setMaxWidth(500);
        editDatePicker.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        updateNewsButton = new Button("Update News");
        updateNewsButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #1F6F5C, #2A9070); " +
            "-fx-text-fill: white; -fx-font-size: 14px; " +
            "-fx-background-radius: 5px; -fx-padding: 10px 20px;"
        );
        updateNewsButton.setOnAction(e -> handleUpdateNews());

        deleteNewsButton = new Button("Delete News");
        deleteNewsButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #A03232, #8B2222); " +
            "-fx-text-fill: white; -fx-font-size: 14px; " +
            "-fx-background-radius: 5px; -fx-padding: 10px 20px;"
        );
        deleteNewsButton.setOnAction(e -> handleDeleteNews());

        HBox buttonBox = new HBox(15, updateNewsButton, deleteNewsButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Add all components to the card
        card.getChildren().addAll(
                new Label("Select News (ID - Title):"), editNewsComboBox,
                loadNewsButton,
                new Label("News ID:"), editNewsIdField,
                new Label("Title:"), editTitleField,
                new Label("Description:"), editDescriptionArea,
                new Label("Place:"), editPlaceField,
                new Label("Date:"), editDatePicker,
                new Label("Time:"), timeBox,
                buttonBox
        );

        // Wrap the card in a ScrollPane to ensure all fields are visible
        ScrollPane scrollPane = new ScrollPane(card);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        // Wrap the ScrollPane in a VBox to center it
        VBox centerBox = new VBox(scrollPane);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(20));
        centerBox.setMaxWidth(Double.MAX_VALUE);

        // Add the centerBox to the editPane
        editPane.getChildren().add(centerBox);
        return editPane;
    }

    // -------------------- Events Section --------------------
    private VBox createManageEventsPage() {
        VBox manageEventsPage = new VBox(20);
        manageEventsPage.setPadding(new Insets(20));
        manageEventsPage.setAlignment(Pos.TOP_CENTER);

        // Set background image
        Image backgroundImage = new Image(BACKGROUND_IMAGE_URL);
        BackgroundImage bgImage = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
        );
        manageEventsPage.setBackground(new Background(bgImage));

        // Header
        Label header = new Label("Manage Events");
        header.setFont(Font.font("Roboto", FontWeight.BOLD, 28));
        header.setStyle("-fx-text-fill: #2C3E50;");
        VBox.setMargin(header, new Insets(0, 0, 20, 0));

        // Create a card for the content
        VBox card = new VBox(20);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.9); " +
            "-fx-background-radius: 20px; " +
            "-fx-border-radius: 20px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 15, 0, 0, 0);"
        );
        card.setMaxWidth(1000);

        // TabPane for Add/Edit Events
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");

        Tab addEventTab = new Tab("Add Event", createAddEventPane());
        Tab editEventTab = new Tab("Edit/Delete Event", createEditEventPane());
        tabPane.getTabs().addAll(addEventTab, editEventTab);

        // Add a button to view events
        viewEventsButton = new Button("View Events");
        viewEventsButton.setStyle("-fx-background-color: linear-gradient(to bottom right, #72a0c1, #2980B9); -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 5px;");
        viewEventsButton.setOnAction(e -> updateView((BorderPane) manageEventsPage.getParent(), Section.VIEW_EVENTS));

        card.getChildren().addAll(header, tabPane, viewEventsButton);
        manageEventsPage.getChildren().add(card);

        return manageEventsPage;
    }

    // -------------------- Add Event Pane --------------------
    private Pane createAddEventPane() {
        VBox addPane = new VBox(15);
        addPane.setPadding(new Insets(20));
        addPane.setAlignment(Pos.TOP_CENTER);
        addPane.setStyle("-fx-background-color: transparent;");

        // Create a card for the input fields
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: #72A0C1; " +
            "-fx-background-radius: 10px; " +
            "-fx-border-radius: 10px; " +
            "-fx-border-color: #5E8CA8; " +
            "-fx-border-width: 1px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 10, 0, 0, 0);"
        );
        card.setMaxWidth(600);

        // Add components to the card
        addEventTitleField = new TextField();
        addEventTitleField.setPromptText("Enter event title");
        addEventTitleField.setMaxWidth(500);
        addEventTitleField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        addEventStateField = new TextField();
        addEventStateField.setPromptText("Enter state");
        addEventStateField.setMaxWidth(500);
        addEventStateField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        addEventDistrictField = new TextField();
        addEventDistrictField.setPromptText("Enter district");
        addEventDistrictField.setMaxWidth(500);
        addEventDistrictField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        addEventPlaceField = new TextField();
        addEventPlaceField.setPromptText("Enter place");
        addEventPlaceField.setMaxWidth(500);
        addEventPlaceField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        // Initialize the description field
        addEventDescriptionArea = new TextArea();
        addEventDescriptionArea.setPromptText("Enter event description");
        addEventDescriptionArea.setPrefRowCount(4);
        addEventDescriptionArea.setMaxWidth(500);
        addEventDescriptionArea.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        addEventDatePicker = new DatePicker();
        addEventDatePicker.setPromptText("Select event date");
        addEventDatePicker.setMaxWidth(500);
        addEventDatePicker.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        // Initialize the hour spinner
        addEventHourSpinner = new Spinner<>(new IntegerSpinnerValueFactory(1, 12, 9));
        addEventHourSpinner.setEditable(true);
        addEventHourSpinner.setPrefWidth(80);
        addEventHourSpinner.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        // Initialize the minute spinner
        addEventMinuteSpinner = new Spinner<>(new IntegerSpinnerValueFactory(0, 59, 0));
        addEventMinuteSpinner.setEditable(true);
        addEventMinuteSpinner.setPrefWidth(80);
        addEventMinuteSpinner.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        // Initialize the AM/PM combo box
        addEventAmPmComboBox = new ComboBox<>(FXCollections.observableArrayList("AM", "PM"));
        addEventAmPmComboBox.setValue("AM");
        addEventAmPmComboBox.setPrefWidth(100);
        addEventAmPmComboBox.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        // Layout for time fields
        HBox timeBox = new HBox(10, new Label("Hour:"), addEventHourSpinner,
                new Label("Minute:"), addEventMinuteSpinner,
                new Label("AM/PM:"), addEventAmPmComboBox);
        timeBox.setAlignment(Pos.CENTER);

        // Add Event button
        addEventButton = new Button("Add Event");
        addEventButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #1ABC9C, #16A085); " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-background-radius: 5px; " +
            "-fx-padding: 10px 20px;"
        );
        addEventButton.setOnAction(e -> handleAddEvent());

        // Add all components to the card
        card.getChildren().addAll(
                new Label("Event Title:"), addEventTitleField,
                new Label("State:"), addEventStateField,
                new Label("District:"), addEventDistrictField,
                new Label("Place:"), addEventPlaceField,
                new Label("Description:"), addEventDescriptionArea,
                new Label("Event Date:"), addEventDatePicker,
                new Label("Event Time:"), timeBox,
                addEventButton
        );

        // Wrap the card in a ScrollPane to ensure all fields are visible
        ScrollPane scrollPane = new ScrollPane(card);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        // Add the ScrollPane to the addPane
        addPane.getChildren().add(scrollPane);
        return addPane;
    }

    // -------------------- Edit Event Pane --------------------
    private Pane createEditEventPane() {
        VBox editPane = new VBox(15);
        editPane.setPadding(new Insets(20));
        editPane.setAlignment(Pos.TOP_CENTER);
        editPane.setStyle("-fx-background-color: transparent;");

        // Create a card for the input fields
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: #72A0C1; " +
            "-fx-background-radius: 10px; " +
            "-fx-border-radius: 10px; " +
            "-fx-border-color: #5E8CA8; " +
            "-fx-border-width: 1px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 10, 0, 0, 0);"
        );
        card.setMaxWidth(600);

        // Add components to the card
        editEventComboBox = new ComboBox<>();
        editEventComboBox.setPromptText("Select Event (by Title)");
        editEventComboBox.setMaxWidth(500);
        editEventComboBox.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");
        refreshEditEventComboBox();

        loadEventButton = new Button("Load Event");
        loadEventButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #4178A0, #2C5A80); " +
            "-fx-text-fill: white; -fx-font-size: 14px; " +
            "-fx-background-radius: 5px; -fx-padding: 10px 20px;"
        );
        loadEventButton.setOnAction(e -> loadEventForEdit());

        editEventIdField = new TextField();
        editEventIdField.setPromptText("Event ID");
        editEventIdField.setMaxWidth(500);
        editEventIdField.setEditable(false);
        editEventIdField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        editEventTitleField = new TextField();
        editEventTitleField.setPromptText("Edit event title");
        editEventTitleField.setMaxWidth(500);
        editEventTitleField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        editEventStateField = new TextField();
        editEventStateField.setPromptText("Edit state");
        editEventStateField.setMaxWidth(500);
        editEventStateField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        editEventDistrictField = new TextField();
        editEventDistrictField.setPromptText("Edit district");
        editEventDistrictField.setMaxWidth(500);
        editEventDistrictField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        editEventPlaceField = new TextField();
        editEventPlaceField.setPromptText("Edit place");
        editEventPlaceField.setMaxWidth(500);
        editEventPlaceField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        // Initialize the description field
        editEventDescriptionArea = new TextArea(); // Declare and initialize the description field
        editEventDescriptionArea.setPromptText("Edit event description");
        editEventDescriptionArea.setPrefRowCount(4);
        editEventDescriptionArea.setMaxWidth(500);
        editEventDescriptionArea.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        editEventDatePicker = new DatePicker();
        editEventDatePicker.setPromptText("Select event date");
        editEventDatePicker.setMaxWidth(500);
        editEventDatePicker.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        editEventHourSpinner = new Spinner<>(new IntegerSpinnerValueFactory(1, 12, 9));
        editEventHourSpinner.setEditable(true);
        editEventHourSpinner.setPrefWidth(80);
        editEventHourSpinner.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        editEventMinuteSpinner = new Spinner<>(new IntegerSpinnerValueFactory(0, 59, 0));
        editEventMinuteSpinner.setEditable(true);
        editEventMinuteSpinner.setPrefWidth(80);
        editEventMinuteSpinner.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        editEventAmPmComboBox = new ComboBox<>(FXCollections.observableArrayList("AM", "PM"));
        editEventAmPmComboBox.setValue("AM");
        editEventAmPmComboBox.setPrefWidth(80);
        editEventAmPmComboBox.setStyle("-fx-font-size: 14px; -fx-background-radius: 5px; -fx-padding: 8px;");

        HBox timeBox = new HBox(10, new Label("Hour:"), editEventHourSpinner,
                new Label("Minute:"), editEventMinuteSpinner,
                new Label(""), editEventAmPmComboBox);
        timeBox.setAlignment(Pos.CENTER);

        updateEventButton = new Button("Update Event");
        updateEventButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #1F6F5C, #2A9070); " +
            "-fx-text-fill: white; -fx-font-size: 14px; " +
            "-fx-background-radius: 5px; -fx-padding: 10px 20px;"
        );
        updateEventButton.setOnAction(e -> handleUpdateEvent());

        deleteEventButton = new Button("Delete Event");
        deleteEventButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #A03232, #8B2222); " +
            "-fx-text-fill: white; -fx-font-size: 14px; " +
            "-fx-background-radius: 5px; -fx-padding: 10px 20px;"
        );
        deleteEventButton.setOnAction(e -> handleDeleteEvent());

        HBox buttonBox = new HBox(15, updateEventButton, deleteEventButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Add all components to the card
        card.getChildren().addAll(
                new Label("Select Event (by Title):"), editEventComboBox,
                loadEventButton,
                new Label("Event ID:"), editEventIdField,
                new Label("Event Title:"), editEventTitleField,
                new Label("State:"), editEventStateField,
                new Label("District:"), editEventDistrictField,
                new Label("Place:"), editEventPlaceField,
                new Label("Description:"), editEventDescriptionArea, // Add description field here
                new Label("Event Date:"), editEventDatePicker,
                new Label("Event Time:"), timeBox,
                buttonBox
        );

        // Wrap the card in a ScrollPane to ensure all fields are visible
        ScrollPane scrollPane = new ScrollPane(card);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        // Wrap the ScrollPane in a VBox to center it
        VBox centerBox = new VBox(scrollPane);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(20));
        centerBox.setMaxWidth(Double.MAX_VALUE);

        // Add the centerBox to the editPane
        editPane.getChildren().add(centerBox);
        return editPane;
    }
    // -------------------- View News Page --------------------
    private VBox createViewNewsPage() {
        VBox viewNewsPage = new VBox(20);
        viewNewsPage.setPadding(new Insets(20));
        viewNewsPage.setAlignment(Pos.TOP_CENTER);

        // Set background image
        Image backgroundImage = new Image(BACKGROUND_IMAGE_URL);
        BackgroundImage bgImage = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
        );
        viewNewsPage.setBackground(new Background(bgImage));

        // Header
        Label header = new Label("View News");
        header.setFont(Font.font("Roboto", FontWeight.BOLD, 28));
        header.setStyle("-fx-text-fill: #2C3E50;");
        VBox.setMargin(header, new Insets(0, 0, 20, 0));

        // Create a card for the content
        VBox card = new VBox(20);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.9); " +
            "-fx-background-radius: 20px; " +
            "-fx-border-radius: 20px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 15, 0, 0, 0);"
        );
        card.setMaxWidth(1200); // Increase the max width of the card

        // TableView to display the news data
        TableView<News> newsTable = new TableView<>();
        newsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ID Column (centered and small width)
        TableColumn<News, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setStyle("-fx-alignment: CENTER;"); // Center-align the numbers within the ID column
        idColumn.setPrefWidth(50); // Set a small width for the ID column
        idColumn.setResizable(false); // Prevent the ID column from being resized

        // Title Column (centered)
        TableColumn<News, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setStyle("-fx-alignment: CENTER;"); // Center-align the values within the Title column

        // Description Column (with truncation and popup)
        TableColumn<News, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String description, boolean empty) {
                super.updateItem(description, empty);
                if (empty || description == null) {
                    setText(null);
                } else {
                    // Truncate long descriptions
                    setText(description.length() > 50 ? description.substring(0, 50) + "..." : description);
                    setOnMouseClicked(e -> {
                        if (!isEmpty()) {
                            showFullDescriptionPopup(description);
                        }
                    });
                }
            }
        });

        // Place Column (centered)
        TableColumn<News, String> placeColumn = new TableColumn<>("Place");
        placeColumn.setCellValueFactory(new PropertyValueFactory<>("place"));
        placeColumn.setStyle("-fx-alignment: CENTER;"); // Center-align the values within the Place column

        // Date Column (centered)
        TableColumn<News, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setStyle("-fx-alignment: CENTER;"); // Center-align the values within the Date column

        // Time Column (centered)
        TableColumn<News, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeColumn.setStyle("-fx-alignment: CENTER;"); // Center-align the values within the Time column

        // Add all columns to the TableView by default
        newsTable.getColumns().addAll(idColumn, titleColumn, descriptionColumn, placeColumn, dateColumn, timeColumn);

        // Fetch data from the database and set it in the TableView
        ObservableList<News> newsList = getNewsListFromDatabase();
        newsTable.setItems(newsList);

        // Wrap the TableView in a ScrollPane to make it horizontally scrollable
        ScrollPane scrollPane = new ScrollPane(newsTable);
        scrollPane.setFitToWidth(true); // Allow horizontal scrolling
        scrollPane.setFitToHeight(true); // Allow vertical scrolling
        scrollPane.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        // Set preferred size for the TableView
        newsTable.setPrefWidth(1100); // Increase the width of the TableView
        newsTable.setPrefHeight(600); // Increase the height of the TableView

        // Add components to the card
        card.getChildren().addAll(header, scrollPane);
        viewNewsPage.getChildren().add(card);

        return viewNewsPage;
    }
   
    // -------------------- View Events Page --------------------
    private VBox createViewEventsPage() {
        VBox viewEventsPage = new VBox(20);
        viewEventsPage.setPadding(new Insets(20));
        viewEventsPage.setAlignment(Pos.TOP_CENTER);

        // Set background image
        Image backgroundImage = new Image(BACKGROUND_IMAGE_URL);
        BackgroundImage bgImage = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
        );
        viewEventsPage.setBackground(new Background(bgImage));

        // Header
        Label header = new Label("View Events");
        header.setFont(Font.font("Roboto", FontWeight.BOLD, 28));
        header.setStyle("-fx-text-fill: #2C3E50;");
        VBox.setMargin(header, new Insets(0, 0, 20, 0));

        // Create a card for the content
        VBox card = new VBox(20);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.9); " +
            "-fx-background-radius: 20px; " +
            "-fx-border-radius: 20px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.3), 15, 0, 0, 0);"
        );
        card.setMaxWidth(1200); // Increase the max width of the card

        // Add checkboxes for selecting columns
        CheckBox idCheckBox = new CheckBox("ID");
        CheckBox titleCheckBox = new CheckBox("Title");
        CheckBox stateCheckBox = new CheckBox("State");
        CheckBox districtCheckBox = new CheckBox("District");
        CheckBox placeCheckBox = new CheckBox("Place");
        CheckBox descriptionCheckBox = new CheckBox("Description");
        CheckBox dateCheckBox = new CheckBox("Date");
        CheckBox timeCheckBox = new CheckBox("Time");

        // Set default selections
        idCheckBox.setSelected(true);
        titleCheckBox.setSelected(true);
        stateCheckBox.setSelected(true);
        districtCheckBox.setSelected(true);
        placeCheckBox.setSelected(true);
        descriptionCheckBox.setSelected(true);
        dateCheckBox.setSelected(true);
        timeCheckBox.setSelected(true);

        // Layout for checkboxes
        HBox checkboxBox = new HBox(10, idCheckBox, titleCheckBox, stateCheckBox, districtCheckBox, placeCheckBox, descriptionCheckBox, dateCheckBox, timeCheckBox);
        checkboxBox.setAlignment(Pos.CENTER);

        // TableView to display the events data
        TableView<Event> eventsTable = new TableView<>();
        eventsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ID Column (centered and small width)
        TableColumn<Event, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setStyle("-fx-alignment: CENTER;"); // Center-align the numbers within the ID column
        idColumn.setPrefWidth(50); // Set a small width for the ID column
        idColumn.setResizable(false); // Prevent the ID column from being resized

        // Title Column (centered)
        TableColumn<Event, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setStyle("-fx-alignment: CENTER;"); // Center-align the values within the Title column

        // State Column (centered)
        TableColumn<Event, String> stateColumn = new TableColumn<>("State");
        stateColumn.setCellValueFactory(new PropertyValueFactory<>("state"));
        stateColumn.setStyle("-fx-alignment: CENTER;"); // Center-align the values within the State column

        // District Column (centered)
        TableColumn<Event, String> districtColumn = new TableColumn<>("District");
        districtColumn.setCellValueFactory(new PropertyValueFactory<>("district"));
        districtColumn.setStyle("-fx-alignment: CENTER;"); // Center-align the values within the District column

        // Place Column (centered)
        TableColumn<Event, String> placeColumn = new TableColumn<>("Place");
        placeColumn.setCellValueFactory(new PropertyValueFactory<>("place"));
        placeColumn.setStyle("-fx-alignment: CENTER;"); // Center-align the values within the Place column

        // Description Column (with truncation and popup)
        TableColumn<Event, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String description, boolean empty) {
                super.updateItem(description, empty);
                if (empty || description == null) {
                    setText(null);
                } else {
                    // Truncate long descriptions
                    setText(description.length() > 50 ? description.substring(0, 50) + "..." : description);
                    setOnMouseClicked(e -> {
                        if (!isEmpty()) {
                            showFullDescriptionPopup(description);
                        }
                    });
                }
            }
        });

        // Date Column (centered)
        TableColumn<Event, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setStyle("-fx-alignment: CENTER;"); // Center-align the values within the Date column

        // Time Column (centered)
        TableColumn<Event, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeColumn.setStyle("-fx-alignment: CENTER;"); // Center-align the values within the Time column

        // Add all columns to the TableView by default
        eventsTable.getColumns().addAll(idColumn, titleColumn, stateColumn, districtColumn, placeColumn, descriptionColumn, dateColumn, timeColumn);

        // Handle checkbox selections
        idCheckBox.setOnAction(e -> toggleColumnVisibility(idColumn, idCheckBox.isSelected()));
        titleCheckBox.setOnAction(e -> toggleColumnVisibility(titleColumn, titleCheckBox.isSelected()));
        stateCheckBox.setOnAction(e -> toggleColumnVisibility(stateColumn, stateCheckBox.isSelected()));
        districtCheckBox.setOnAction(e -> toggleColumnVisibility(districtColumn, districtCheckBox.isSelected()));
        placeCheckBox.setOnAction(e -> toggleColumnVisibility(placeColumn, placeCheckBox.isSelected()));
        descriptionCheckBox.setOnAction(e -> toggleColumnVisibility(descriptionColumn, descriptionCheckBox.isSelected()));
        dateCheckBox.setOnAction(e -> toggleColumnVisibility(dateColumn, dateCheckBox.isSelected()));
        timeCheckBox.setOnAction(e -> toggleColumnVisibility(timeColumn, timeCheckBox.isSelected()));

        // Fetch data from the database and set it in the TableView
        ObservableList<Event> eventsList = getEventsListFromDatabase();
        eventsTable.setItems(eventsList);

        // Wrap the TableView in a ScrollPane to make it horizontally scrollable
        ScrollPane scrollPane = new ScrollPane(eventsTable);
        scrollPane.setFitToWidth(true); // Allow horizontal scrolling
        scrollPane.setFitToHeight(true); // Allow vertical scrolling
        scrollPane.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        // Set preferred size for the TableView
        eventsTable.setPrefWidth(1100); // Increase the width of the TableView
        eventsTable.setPrefHeight(600); // Increase the height of the TableView

        // Add components to the card
        card.getChildren().addAll(header, checkboxBox, scrollPane);
        viewEventsPage.getChildren().add(card);

        return viewEventsPage;
    }
    private void toggleColumnVisibility(TableColumn<Event, ?> column, boolean isVisible) {
        column.setVisible(isVisible);
    }
    
   

  
    private void refreshVolunteersTable() {
        try {
            ObservableList<Volunteer> volunteers = getVolunteersListFromDatabase();
            if (volunteersTable == null) {
                System.err.println("Volunteers table is not initialized");
                return;
            }
            Platform.runLater(() -> {
                volunteersTable.setItems(volunteers);
                if (volunteers.isEmpty()) {
                    showAlert("Information", "No volunteers found in database");
                }
            });
        } catch (Exception e) {
            Platform.runLater(() -> 
                showAlert("Error", "Failed to refresh volunteers: " + e.getMessage()));
        }
    }
    // -------------------- Popup for Full Description --------------------
    private void showFullDescriptionPopup(String description) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Full Description");

        TextArea descriptionArea = new TextArea(description);
        descriptionArea.setWrapText(true);
        descriptionArea.setEditable(false);

        VBox layout = new VBox(10, descriptionArea);
        layout.setPadding(new Insets(10));

        Scene scene = new Scene(layout, 400, 300);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }

    // -------------------- Donations Section --------------------
   

    // -------------------- Volunteers Section --------------------
    

    // -------------------- Disasters Section --------------------
   

    // -------------------- News Event Handlers --------------------
    private void handleAddNews() {
        String title = addTitleField.getText().trim();
        String description = addDescriptionArea.getText().trim();
        String place = addPlaceField.getText().trim();
        LocalDate date = addDatePicker.getValue();
        int hour = addHourSpinner.getValue();
        int minute = addMinuteSpinner.getValue();
        String ampm = addAmPmComboBox.getValue();

        if (title.isEmpty() || description.isEmpty() || place.isEmpty() || date == null || ampm == null) {
            showAlert("Error", "All fields must be filled out!");
            return;
        }
        int hour24 = convertTo24Hour(hour, ampm);
        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String timeStr = String.format("%02d:%02d:00", hour24, minute);

        saveNewsToDatabase(title, description, place, dateStr, timeStr);
        showAlert("Success", "News added successfully!");
        clearAddFields();
        refreshEditNewsComboBox();
    }

    private void loadNewsForEdit() {
        String selectedItem = editNewsComboBox.getValue();
        if (selectedItem == null || selectedItem.trim().isEmpty()) {
            showAlert("Error", "Please select a news item to load.");
            return;
        }
        String[] parts = selectedItem.split(" - ", 2);
        if (parts.length < 2) {
            showAlert("Error", "Invalid selection format.");
            return;
        }
        String newsId = parts[0];
        String[] details = getNewsDetailsById(newsId);

        if (details != null && details.length >= 6) { // Ensure the details array has enough elements
            editNewsIdField.setText(details[3]); // news_id
            editTitleField.setText(details[0]); // title
            editDescriptionArea.setText(details[1]); // description
            editPlaceField.setText(details[2]); // place

            // Parse the date
            LocalDate date = LocalDate.parse(details[4], DateTimeFormatter.ISO_LOCAL_DATE);
            editDatePicker.setValue(date);

            // Parse the time
            LocalTime time = LocalTime.parse(details[5], DateTimeFormatter.ofPattern("HH:mm:ss"));
            int hour = time.getHour();
            int minute = time.getMinute();
            String ampm = (hour >= 12) ? "PM" : "AM";
            int hour12 = hour % 12;
            if (hour12 == 0) hour12 = 12; // Handle midnight (12 AM)

            editHourSpinner.getValueFactory().setValue(hour12);
            editMinuteSpinner.getValueFactory().setValue(minute);
            editAmPmComboBox.setValue(ampm);
        } else {
            showAlert("Error", "Failed to load news details or invalid data format.");
        }
    }

    private void handleUpdateNews() {
        String selectedItem = editNewsComboBox.getValue();
        if (selectedItem == null || selectedItem.trim().isEmpty()) {
            showAlert("Error", "Please load a news item to update.");
            return;
        }
        String[] parts = selectedItem.split(" - ", 2);
        if (parts.length < 2) {
            showAlert("Error", "Invalid selection format.");
            return;
        }
        String newsId = parts[0];
        String newTitle = editTitleField.getText().trim();
        String newDescription = editDescriptionArea.getText().trim();
        String newPlace = editPlaceField.getText().trim();
        LocalDate newDate = editDatePicker.getValue();
        int hour = editHourSpinner.getValue(); // Ensure editHourSpinner is initialized
        int minute = editMinuteSpinner.getValue();
        String ampm = editAmPmComboBox.getValue();

        if (newTitle.isEmpty() || newDescription.isEmpty() || newPlace.isEmpty() || newDate == null || ampm == null) {
            showAlert("Error", "All fields must be filled out for updating!");
            return;
        }
        int hour24 = convertTo24Hour(hour, ampm);
        String dateStr = newDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String timeStr = String.format("%02d:%02d:00", hour24, minute);

        updateNewsInDatabaseById(newsId, newTitle, newDescription, newPlace, dateStr, timeStr);
        showAlert("Success", "News updated successfully!");
        clearEditFields();
        refreshEditNewsComboBox();
    }

    private void handleDeleteNews() {
        String selectedItem = editNewsComboBox.getValue();
        if (selectedItem == null || selectedItem.trim().isEmpty()) {
            showAlert("Error", "Please select a news item to delete.");
            return;
        }
        String[] parts = selectedItem.split(" - ", 2);
        if (parts.length < 2) {
            showAlert("Error", "Invalid selection format.");
            return;
        }
        String newsId = parts[0];
        deleteNewsFromDatabaseById(newsId);
        showAlert("Success", "News deleted successfully!");
        clearEditFields();
        refreshEditNewsComboBox();
    }

    // -------------------- Events Event Handlers --------------------
    private void handleAddEvent() {
        String title = addEventTitleField.getText().trim();
        String state = addEventStateField.getText().trim();
        String district = addEventDistrictField.getText().trim();
        String place = addEventPlaceField.getText().trim();
        String description = addEventDescriptionArea.getText().trim(); // Get the description
        LocalDate date = addEventDatePicker.getValue();
        int hour = addEventHourSpinner.getValue();
        int minute = addEventMinuteSpinner.getValue();
        String ampm = addEventAmPmComboBox.getValue();

        // Validate input fields
        if (title.isEmpty() || state.isEmpty() || district.isEmpty() || place.isEmpty() || description.isEmpty() || date == null || ampm == null) {
            showAlert("Error", "All fields must be filled out!");
            return;
        }

        // Convert 12-hour time to 24-hour time
        int hour24 = convertTo24Hour(hour, ampm);
        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String timeStr = String.format("%02d:%02d:00", hour24, minute);

        // Save the event to the database
        saveEventToDatabase(title, state, district, place, description, dateStr, timeStr);

        // Show success message
        showAlert("Success", "Event added successfully!");

        // Clear input fields
        clearAddEventFields(); // Clear all fields, including time and description
    }
   
   

    private void setupVolunteersTableColumns() {
        volunteersTable.getColumns().clear();

        // ID Column
        TableColumn<Volunteer, Integer> idCol = new TableColumn<>("Volunteer ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("volunteerId"));
        idCol.setStyle("-fx-alignment: CENTER;");
        idCol.setPrefWidth(100);

        // Event ID Column
        TableColumn<Volunteer, String> eventIdCol = new TableColumn<>("Event ID");
        eventIdCol.setCellValueFactory(new PropertyValueFactory<>("eventId"));
        eventIdCol.setStyle("-fx-alignment: CENTER;");
        eventIdCol.setPrefWidth(100);

        // First Name Column
        TableColumn<Volunteer, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstname"));
        firstNameCol.setStyle("-fx-alignment: CENTER;");
        firstNameCol.setPrefWidth(150);

        // Last Name Column
        TableColumn<Volunteer, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastname"));
        lastNameCol.setStyle("-fx-alignment: CENTER;");
        lastNameCol.setPrefWidth(150);

        // Phone Column
        TableColumn<Volunteer, String> phoneCol = new TableColumn<>("Phone Number");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        phoneCol.setStyle("-fx-alignment: CENTER;");
        phoneCol.setPrefWidth(150);

        // Status Column
        TableColumn<Volunteer, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setStyle("-fx-alignment: CENTER;");
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if (status.equals("ADDED")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (status.equals("DELETED")) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: black;");
                    }
                }
            }
        });

        // Action Column
        TableColumn<Volunteer, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(200);
        actionCol.setStyle("-fx-alignment: CENTER;");
        
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button addButton = new Button("Add");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttons = new HBox(5, addButton, deleteButton);

            {
                buttons.setAlignment(Pos.CENTER);
                
                // Style the buttons
                addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
                
                // Add button action
                addButton.setOnAction(event -> {
                    Volunteer volunteer = getTableView().getItems().get(getIndex());
                    volunteer.setStatus("ADDED");
                    updateVolunteerStatusInDatabase(volunteer.getVolunteerId(), "ADDED");
                    getTableView().refresh();
                });
                
                // Delete button action
                deleteButton.setOnAction(event -> {
                    Volunteer volunteer = getTableView().getItems().get(getIndex());
                    volunteer.setStatus("DELETED");
                    deleteVolunteerFromDatabase(volunteer);
                    getTableView().refresh();
                });
            }
            private void updateVolunteerStatusInDatabase(int volunteerId, String status) {
                String sql = "UPDATE volunteers SET status = ? WHERE volunteer_id = ?";
                try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, status);
                    pstmt.setInt(2, volunteerId);
                    int rowsAffected = pstmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        System.out.println("Volunteer status updated successfully");
                    } else {
                        showAlert("Error", "No volunteer was updated.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Database Error", "Failed to update volunteer status: " + e.getMessage());
                }
            }
            private void deleteVolunteerFromDatabase(Volunteer volunteer) {
                String sql = "DELETE FROM volunteers WHERE volunteer_id = ?";
                try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, volunteer.getVolunteerId());
                    int rowsAffected = pstmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        showAlert("Success", "Volunteer deleted successfully!");
                    } else {
                        showAlert("Error", "No volunteer was deleted.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Database Error", "Failed to delete volunteer: " + e.getMessage());
                }
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Volunteer volunteer = getTableView().getItems().get(getIndex());
                    if (volunteer.getStatus().equals("PENDING")) {
                        setGraphic(buttons);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        volunteersTable.getColumns().addAll(idCol, eventIdCol, firstNameCol, lastNameCol, phoneCol, statusCol, actionCol);
        volunteersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    private void loadEventForEdit() {
        String selectedEvent = editEventComboBox.getValue();
        if (selectedEvent == null || selectedEvent.trim().isEmpty()) {
            showAlert("Error", "Please select an event to load.");
            return;
        }

        // Fetch event details from the database
        String[] details = getEventDetails(selectedEvent);
        if (details != null) {
            editEventIdField.setText(details[3]); // Event ID
            editEventTitleField.setText(selectedEvent); // Event Title
            editEventStateField.setText(details[4]); // State
            editEventDistrictField.setText(details[5]); // District
            editEventPlaceField.setText(details[2]); // Place
            editEventDescriptionArea.setText(details[6]); // Description

            // Parse the date
            LocalDate eventDate = LocalDate.parse(details[0], DateTimeFormatter.ISO_LOCAL_DATE);
            editEventDatePicker.setValue(eventDate);

            // Parse the time
            String[] timeParts = details[1].split(":");
            if (timeParts.length >= 2) {
                int hour24 = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                String ampm = (hour24 >= 12) ? "PM" : "AM";
                int hour12 = hour24 % 12;
                if (hour12 == 0) hour12 = 12; // Handle midnight (12 AM)
                editEventHourSpinner.getValueFactory().setValue(hour12);
                editEventMinuteSpinner.getValueFactory().setValue(minute);
                editEventAmPmComboBox.setValue(ampm);
            }
        } else {
            showAlert("Error", "Failed to load event details.");
        }
    }
    private void handleUpdateEvent() {
        String selectedEvent = editEventComboBox.getValue();
        if (selectedEvent == null || selectedEvent.trim().isEmpty()) {
            showAlert("Error", "Please load an event to update.");
            return;
        }
        String newTitle = editEventTitleField.getText().trim();
        String newState = editEventStateField.getText().trim();
        String newDistrict = editEventDistrictField.getText().trim();
        String newPlace = editEventPlaceField.getText().trim();
        String newDescription = editEventDescriptionArea.getText().trim(); // Get the updated description
        LocalDate newDate = editEventDatePicker.getValue();
        int hour = editEventHourSpinner.getValue();
        int minute = editEventMinuteSpinner.getValue();
        String ampm = editEventAmPmComboBox.getValue();

        if (newTitle.isEmpty() || newState.isEmpty() || newDistrict.isEmpty() || newPlace.isEmpty() || newDescription.isEmpty() || newDate == null || ampm == null) {
            showAlert("Error", "All fields must be filled out for updating!");
            return;
        }
        int hour24 = convertTo24Hour(hour, ampm);
        String dateStr = newDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String timeStr = String.format("%02d:%02d:00", hour24, minute);

        // Update the event in the database
        updateEventInDatabase(selectedEvent, newTitle, newState, newDistrict, newPlace, newDescription, dateStr, timeStr);
        showAlert("Success", "Event updated successfully!");
        clearEditEventFields();
        refreshEditEventComboBox();
    }

    private void handleDeleteEvent() {
        String selectedEvent = editEventComboBox.getValue();
        if (selectedEvent == null || selectedEvent.trim().isEmpty()) {
            showAlert("Error", "Please select an event to delete.");
            return;
        }
        deleteEventFromDatabase(selectedEvent);
        showAlert("Success", "Event deleted successfully!");
        clearEditEventFields();
        refreshEditEventComboBox();
    }

    // Helper method to convert 12-hour time to 24-hour time
    private int convertTo24Hour(int hour, String ampm) {
        if ("AM".equalsIgnoreCase(ampm)) {
            return (hour == 12) ? 0 : hour;
        } else {
            return (hour == 12) ? 12 : hour + 12;
        }
    }

    // -------------------- News Database Operations --------------------
    private void saveNewsToDatabase(String title, String description, String place, String date, String time) {
        String sql = "INSERT INTO news (title, description, place, date, time) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setString(3, place);
            pstmt.setString(4, date);
            pstmt.setString(5, time);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save news to the database.");
        }
    }

    private String[] getNewsDetailsById(String newsId) {
        String sql = "SELECT news_id, title, description, place, date, time FROM news WHERE news_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newsId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new String[]{
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("place"),
                    rs.getString("news_id"),
                    rs.getString("date"), // Ensure this is in yyyy-MM-dd format
                    rs.getString("time")  // Ensure this is in hh:mm a format (e.g., 09:00 AM)
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateNewsInDatabaseById(String newsId, String newTitle, String newDescription, String newPlace, String newDate, String newTime) {
        String sql = "UPDATE news SET title = ?, description = ?, place = ?, date = ?, time = ? WHERE news_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newTitle);
            pstmt.setString(2, newDescription);
            pstmt.setString(3, newPlace);
            pstmt.setString(4, newDate);
            pstmt.setString(5, newTime);
            pstmt.setString(6, newsId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update news in the database.");
        }
    }

    private void deleteNewsFromDatabaseById(String newsId) {
        String sql = "DELETE FROM news WHERE news_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newsId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to delete news from the database.");
        }
    }

    private ObservableList<News> getNewsListFromDatabase() {
        ObservableList<News> newsList = FXCollections.observableArrayList();
        String sql = "SELECT news_id, title, description, place, date, time FROM news";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                newsList.add(new News(
                        rs.getInt("news_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("place"),
                        rs.getString("date"),
                        rs.getString("time")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newsList;
    }

    private void refreshEditNewsComboBox() {
        ObservableList<String> newsList = getNewsListFromDatabase().stream()
                .map(news -> news.getId() + " - " + news.getTitle())
                .collect(FXCollections::observableArrayList, ObservableList::add, ObservableList::addAll);
        editNewsComboBox.setItems(newsList);
    }

    private void saveEventToDatabase(String title, String state, String district, String place, String description, String date, String time) {
        String sql = "INSERT INTO events (title, state, district, place, description, date, time) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, state);
            pstmt.setString(3, district);
            pstmt.setString(4, place);
            pstmt.setString(5, description); // Add description here
            pstmt.setString(6, date);
            pstmt.setString(7, time);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save event to the database.");
        }
    }
    
    private String[] getEventDetails(String title) {
        String sql = "SELECT event_id, date, time, place, state, district, description FROM events WHERE title = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new String[]{
                    rs.getString("date"),         // Index 0
                    rs.getString("time"),         // Index 1
                    rs.getString("place"),        // Index 2
                    rs.getString("event_id"),     // Index 3
                    rs.getString("state"),        // Index 4
                    rs.getString("district"),     // Index 5
                    rs.getString("description")   // Index 6
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    private void updateEventInDatabase(String originalTitle, String newTitle, String newState, String newDistrict, String newPlace, String newDescription, String newDate, String newTime) {
        String sql = "UPDATE events SET title = ?, state = ?, district = ?, place = ?, description = ?, date = ?, time = ? WHERE title = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newTitle);
            pstmt.setString(2, newState);
            pstmt.setString(3, newDistrict);
            pstmt.setString(4, newPlace);
            pstmt.setString(5, newDescription); // Include the description
            pstmt.setString(6, newDate);
            pstmt.setString(7, newTime);
            pstmt.setString(8, originalTitle);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update event in the database.");
        }
    }
    private void deleteEventFromDatabase(String title) {
        String sql = "DELETE FROM events WHERE title = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to delete event from the database.");
        }
    }

    private ObservableList<Event> getEventsListFromDatabase() {
        ObservableList<Event> eventsList = FXCollections.observableArrayList();
        String sql = "SELECT event_id, title, state, district, place, description, date, time FROM events";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                eventsList.add(new Event(
                        rs.getInt("event_id"),
                        rs.getString("title"),
                        rs.getString("state"),
                        rs.getString("district"),
                        rs.getString("place"),
                        rs.getString("description"),
                        rs.getString("date"),
                        rs.getString("time")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to fetch events from the database.");
        }

        return eventsList;
    }

    private void refreshEditEventComboBox() {
        ObservableList<String> eventList = getEventsListFromDatabase().stream()
                .map(Event::getTitle)
                .collect(FXCollections::observableArrayList, ObservableList::add, ObservableList::addAll);
        editEventComboBox.setItems(eventList);
    }

    // -------------------- Utility Methods --------------------
    private void clearAddFields() {
        addTitleField.clear();
        addDescriptionArea.clear();
        addPlaceField.clear();
        addDatePicker.setValue(null);
        addHourSpinner.getValueFactory().setValue(1);
        addMinuteSpinner.getValueFactory().setValue(0);
        addAmPmComboBox.setValue("AM");
    }

    private void clearEditFields() {
        editNewsIdField.clear();
        editTitleField.clear();
        editDescriptionArea.clear();
        editPlaceField.clear();
        editDatePicker.setValue(null);
        editHourSpinner.getValueFactory().setValue(1);
        editMinuteSpinner.getValueFactory().setValue(0);
        editAmPmComboBox.setValue("AM");
        editNewsComboBox.setValue(null);
    }

    private void clearAddEventFields() {
        addEventTitleField.clear(); // Clear the title field
        addEventStateField.clear(); // Clear the state field
        addEventDistrictField.clear(); // Clear the district field
        addEventPlaceField.clear(); // Clear the place field
        addEventDescriptionArea.clear(); // Clear the description area
        addEventDatePicker.setValue(null); // Clear the date picker

        // Reset the time fields to their default values
        addEventHourSpinner.getValueFactory().setValue(1); // Reset hour spinner to 1
        addEventMinuteSpinner.getValueFactory().setValue(0); // Reset minute spinner to 0
        addEventAmPmComboBox.setValue("AM"); // Reset AM/PM combo box to "AM"
    }

    private void clearEditEventFields() {
        editEventIdField.clear();
        editEventTitleField.clear();
        editEventStateField.clear();
        editEventDistrictField.clear();
        editEventPlaceField.clear();
        editEventDescriptionArea.clear(); // Clear the description area
        editEventDatePicker.setValue(null);
        editEventHourSpinner.getValueFactory().setValue(1);
        editEventMinuteSpinner.getValueFactory().setValue(0);
        editEventAmPmComboBox.setValue("AM");
        editEventComboBox.setValue(null);
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private VBox createAlertsSection() {
        VBox alertsSection = new VBox(10);
        alertsSection.setPadding(new Insets(20));
        Label alertsLabel = new Label("Alerts Management");
        alertsLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 24));
        alertsLabel.setStyle("-fx-text-fill: #2C3E50;");
        alertsSection.getChildren().add(alertsLabel);
        return alertsSection;
    }
   

    private VBox createVolunteersSection() {
        VBox volunteersSection = new VBox(20);
        volunteersSection.setPadding(new Insets(20));
        volunteersSection.setAlignment(Pos.TOP_CENTER);

        // Header
        Label header = new Label("Volunteer Management");
        header.setFont(Font.font("Roboto", FontWeight.BOLD, 28));
        header.setStyle("-fx-text-fill: #2C3E50;");

        // Initialize and setup the table
        volunteersTable = new TableView<>();
        setupVolunteersTableColumns();

        // Add components to the layout
        volunteersSection.getChildren().addAll(header, volunteersTable);
        VBox.setVgrow(volunteersTable, Priority.ALWAYS);

        // Load initial data
        refreshVolunteersTable();

        return volunteersSection;
    }

  

   
  
   
    
    private VBox createDonationsSection() {
        VBox donationsSection = new VBox(20);
        donationsSection.setPadding(new Insets(20));
        donationsSection.setAlignment(Pos.TOP_CENTER);

        // Set background image
        Image backgroundImage = new Image(BACKGROUND_IMAGE_URL);
        BackgroundImage bgImage = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
        );
        donationsSection.setBackground(new Background(bgImage));

        // Header
        Label header = new Label("Donation Management");
        header.setFont(Font.font("Roboto", FontWeight.BOLD, 28));
        header.setStyle("-fx-text-fill: #FFFFFF;");

        // Create tab pane for donations and allocations
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Donations Tab
        Tab donationsTab = new Tab("Donation Requests", createDonationsTable());
        // Allocations Tab
        Tab allocationsTab = new Tab("Allocation Requests", createAllocationsTable());

        tabPane.getTabs().addAll(donationsTab, allocationsTab);

        donationsSection.getChildren().addAll(header, tabPane);
        return donationsSection;
    }
    private Pane createDonationsTable() {
        VBox donationsPane = new VBox(10);
        donationsPane.setPadding(new Insets(10));

        donationsTable = new TableView<>(); // Initialize the donations table
        setupDonationsTableColumns(donationsTable);

        ObservableList<Donation> donations = getDonationsFromDatabase();
        donationsTable.setItems(donations);

        donationsPane.getChildren().add(donationsTable);
        return donationsPane;
    }
    private ObservableList<Donation> getDonationsFromDatabase() {
        ObservableList<Donation> donations = FXCollections.observableArrayList();
        String sql = "SELECT id, user_id, amount, payment_method, status, admin_response " +
                     "FROM donations WHERE transaction_type = 'DONATION'";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                donations.add(new Donation(
                    rs.getInt("id"),
                    rs.getString("user_id"),
                    rs.getDouble("amount"),
                    rs.getString("payment_method"),
                    rs.getString("status"),
                    rs.getString("admin_response")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to fetch donations: " + e.getMessage());
        }
        return donations;
    }
    private ObservableList<Allocation> getAllocationsFromDatabase() {
        ObservableList<Allocation> allocations = FXCollections.observableArrayList();
        String sql = "SELECT id, user_id, amount, description, status, admin_response " +
                     "FROM donations WHERE transaction_type = 'REQUEST'";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                allocations.add(new Allocation(
                    rs.getInt("id"),
                    rs.getString("user_id"),
                    rs.getDouble("amount"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getString("admin_response")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to fetch allocations: " + e.getMessage());
        }
        return allocations;
    }
   
    private Pane createAllocationsTable() {
        VBox allocationsPane = new VBox(10);
        allocationsPane.setPadding(new Insets(10));

        allocationsTable = new TableView<>(); // Initialize the allocations table
        setupAllocationsTableColumns(allocationsTable);

        ObservableList<Allocation> allocations = getAllocationsFromDatabase();
        allocationsTable.setItems(allocations);

        allocationsPane.getChildren().add(allocationsTable);
        return allocationsPane;
    }
   
    // -------------------- Model Classes --------------------
    public static class News {
        private final int id;
        private final String title;
        private final String description;
        private final String place;
        private final String date;
        private final String time;

        public News(int id, String title, String description, String place, String date, String time) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.place = place;
            this.date = date;
            this.time = time;
        }

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getPlace() {
            return place;
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }
    }

    public static class Event {
        private final int id;
        private final String title;
        private final String state;
        private final String district;
        private final String place;
        private final String description;
        private final String date;
        private final String time;

        public Event(int id, String title, String state, String district, String place, String description, String date, String time) {
            this.id = id;
            this.title = title;
            this.state = state;
            this.district = district;
            this.place = place;
            this.description = description;
            this.date = date;
            this.time = time;
        }

        // Getters for all attributes
        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getState() {
            return state;
        }

        public String getDistrict() {
            return district;
        }

        public String getPlace() {
            return place;
        }

        public String getDescription() {
            return description;
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }
    }
    public static class Volunteer {
        private final SimpleIntegerProperty volunteerId;
        private final SimpleStringProperty firstname;
        private final SimpleStringProperty lastname;
        private final SimpleStringProperty userId;
        private final SimpleStringProperty eventId;
        private final SimpleStringProperty phoneNumber;
        private final SimpleStringProperty status;

        public Volunteer(int volunteerId, String firstname, String lastname, 
                        String userId, String eventId, String phoneNumber) {
            this.volunteerId = new SimpleIntegerProperty(volunteerId);
            this.firstname = new SimpleStringProperty(firstname);
            this.lastname = new SimpleStringProperty(lastname);
            this.userId = new SimpleStringProperty(userId);
            this.eventId = new SimpleStringProperty(eventId);
            this.phoneNumber = new SimpleStringProperty(phoneNumber);
            this.status = new SimpleStringProperty("PENDING"); // Default status
        }

        // Getters
        public int getVolunteerId() { return volunteerId.get(); }
        public String getFirstname() { return firstname.get(); }
        public String getLastname() { return lastname.get(); }
        public String getUserId() { return userId.get(); }
        public String getEventId() { return eventId.get(); }
        public String getPhoneNumber() { return phoneNumber.get(); }
        public String getStatus() { return status.get(); }

        // Setters
        public void setStatus(String status) { this.status.set(status); }

        // Property getters
        public SimpleIntegerProperty volunteerIdProperty() { return volunteerId; }
        public SimpleStringProperty firstnameProperty() { return firstname; }
        public SimpleStringProperty lastnameProperty() { return lastname; }
        public SimpleStringProperty userIdProperty() { return userId; }
        public SimpleStringProperty eventIdProperty() { return eventId; }
        public SimpleStringProperty phoneNumberProperty() { return phoneNumber; }
        public SimpleStringProperty statusProperty() { return status; }
    }
        
    private ObservableList<Volunteer> getVolunteersListFromDatabase() {
        ObservableList<Volunteer> volunteersList = FXCollections.observableArrayList();
        String sql = "SELECT volunteer_id, firstname, lastname, user_id, event_id, ph_no, status FROM volunteers";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Volunteer volunteer = new Volunteer(
                    rs.getInt("volunteer_id"),
                    rs.getString("firstname"),
                    rs.getString("lastname"),
                    rs.getString("user_id"),
                    rs.getString("event_id"),
                    rs.getString("ph_no")
                );
                // Set the status from database or default to "PENDING"
                String status = rs.getString("status");
                volunteer.setStatus(status != null ? status : "PENDING");
                volunteersList.add(volunteer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load volunteers: " + e.getMessage());
        }

        return volunteersList;
    }
        
        
        
        
        
        
      
       
        public static class Donation {
            private final SimpleIntegerProperty id;
            private final SimpleStringProperty userId;
            private final SimpleDoubleProperty amount;
            private final SimpleStringProperty paymentMethod;
            private final SimpleStringProperty status;
            private final SimpleStringProperty adminResponse;

            // Constructor with all fields
            public Donation(int id, String userId, double amount, String paymentMethod, 
                           String status, String adminResponse) {
                this.id = new SimpleIntegerProperty(id);
                this.userId = new SimpleStringProperty(userId);
                this.amount = new SimpleDoubleProperty(amount);
                this.paymentMethod = new SimpleStringProperty(paymentMethod);
                this.status = new SimpleStringProperty(status);
                this.adminResponse = new SimpleStringProperty(adminResponse);
            }

            // Getters
            public int getId() { return id.get(); }
            public String getUserId() { return userId.get(); }
            public double getAmount() { return amount.get(); }
            public String getPaymentMethod() { return paymentMethod.get(); }
            public String getStatus() { return status.get(); }
            public String getAdminResponse() { return adminResponse.get(); }

            // Setters
            public void setStatus(String status) { this.status.set(status); }
            public void setAdminResponse(String response) { this.adminResponse.set(response); }

            // Property getters
            public SimpleIntegerProperty idProperty() { return id; }
            public SimpleStringProperty userIdProperty() { return userId; }
            public SimpleDoubleProperty amountProperty() { return amount; }
            public SimpleStringProperty paymentMethodProperty() { return paymentMethod; }
            public SimpleStringProperty statusProperty() { return status; }
            public SimpleStringProperty adminResponseProperty() { return adminResponse; }
        }
        public static class Allocation {
            private final SimpleIntegerProperty id;
            private final SimpleStringProperty userId;
            private final SimpleDoubleProperty amount;
            private final SimpleStringProperty description;
            private final SimpleStringProperty status;
            private final SimpleStringProperty adminResponse;

            // Constructor with all fields
            public Allocation(int id, String userId, double amount, String description,
                             String status, String adminResponse) {
                this.id = new SimpleIntegerProperty(id);
                this.userId = new SimpleStringProperty(userId);
                this.amount = new SimpleDoubleProperty(amount);
                this.description = new SimpleStringProperty(description);
                this.status = new SimpleStringProperty(status);
                this.adminResponse = new SimpleStringProperty(adminResponse);
            }

            // Getters
            public int getId() { return id.get(); }
            public String getUserId() { return userId.get(); }
            public double getAmount() { return amount.get(); }
            public String getDescription() { return description.get(); }
            public String getStatus() { return status.get(); }
            public String getAdminResponse() { return adminResponse.get(); }

            // Setters
            public void setStatus(String status) { this.status.set(status); }
            public void setAdminResponse(String response) { this.adminResponse.set(response); }

            // Property getters
            public SimpleIntegerProperty idProperty() { return id; }
            public SimpleStringProperty userIdProperty() { return userId; }
            public SimpleDoubleProperty amountProperty() { return amount; }
            public SimpleStringProperty descriptionProperty() { return description; }
            public SimpleStringProperty statusProperty() { return status; }
            public SimpleStringProperty adminResponseProperty() { return adminResponse; }
        }
       
        private void setupDonationsTableColumns(TableView<Donation> table) {
            table.getColumns().clear();

            // ID Column
            TableColumn<Donation, Integer> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            idCol.setStyle("-fx-alignment: CENTER;");
            idCol.setPrefWidth(80);

            // User ID Column
            TableColumn<Donation, String> userIdCol = new TableColumn<>("User ID");
            userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
            userIdCol.setStyle("-fx-alignment: CENTER;");
            userIdCol.setPrefWidth(100);

            // Amount Column
            TableColumn<Donation, Double> amountCol = new TableColumn<>("Amount");
            amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
            amountCol.setStyle("-fx-alignment: CENTER;");
            amountCol.setPrefWidth(100);
            amountCol.setCellFactory(tc -> new TableCell<>() {
                @Override
                protected void updateItem(Double amount, boolean empty) {
                    super.updateItem(amount, empty);
                    setText(empty || amount == null ? null : String.format("₹%,.2f", amount));
                }
            });

            // Payment Method Column
            TableColumn<Donation, String> methodCol = new TableColumn<>("Payment Method");
            methodCol.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
            methodCol.setStyle("-fx-alignment: CENTER;");
            methodCol.setPrefWidth(150);

            // Status/Action Column
            TableColumn<Donation, String> actionCol = new TableColumn<>("Status/Actions");
            actionCol.setCellValueFactory(cellData -> {
                String status = cellData.getValue().getStatus();
                if (status.equals("PENDING")) {
                    return new SimpleStringProperty("");
                } else {
                    return new SimpleStringProperty(status);
                }
            });
            actionCol.setCellFactory(column -> new TableCell<>() {
                private final Button acceptBtn = new Button("Accept");
                private final Button rejectBtn = new Button("Reject");
                private final HBox buttons = new HBox(10, acceptBtn, rejectBtn);
                private final Label statusLabel = new Label();

                {
                    buttons.setAlignment(Pos.CENTER);
                    acceptBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                    rejectBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
                    
                    statusLabel.setStyle("-fx-font-weight: bold;");
                    statusLabel.setAlignment(Pos.CENTER);
                    statusLabel.setMaxWidth(Double.MAX_VALUE);
                    
                    acceptBtn.setOnAction(event -> {
                        Donation donation = getTableView().getItems().get(getIndex());
                        handleAcceptDonation(donation);
                    });
                    
                    rejectBtn.setOnAction(event -> {
                        Donation donation = getTableView().getItems().get(getIndex());
                        handleRejectDonation(donation);
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        Donation donation = getTableView().getItems().get(getIndex());
                        if (donation.getStatus().equals("PENDING")) {
                            setGraphic(buttons);
                            statusLabel.setText("");
                        } else {
                            statusLabel.setText(donation.getStatus());
                            if (donation.getStatus().equals("ACCEPTED")) {
                                statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            } else {
                                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            }
                            setGraphic(statusLabel);
                        }
                    }
                }
            });
            actionCol.setPrefWidth(200);

            table.getColumns().addAll(idCol, userIdCol, amountCol, methodCol, actionCol);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }

        private void handleAcceptDonation(Donation donation) {
            String sql = "UPDATE donations SET status = 'ACCEPTED', admin_response = 'Donation accepted' " +
                         "WHERE id = ? AND transaction_type = 'DONATION'";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, donation.getId());
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    Platform.runLater(() -> {
                        donation.setStatus("ACCEPTED");
                        donationsTable.refresh();
                        showAlert("Success", "Donation ID " + donation.getId() + " has been accepted!");
                    });
                } else {
                    showAlert("Error", "No donation was updated.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Database Error", "Failed to accept donation: " + e.getMessage());
            }
        }

        private void handleRejectDonation(Donation donation) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Reject Donation");
            dialog.setHeaderText("Rejecting Donation ID: " + donation.getId());
            dialog.setContentText("Reason for rejection:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                String reason = "Rejected: " + result.get().trim();
                String sql = "UPDATE donations SET status = 'REJECTED', admin_response = ? " +
                             "WHERE id = ? AND transaction_type = 'DONATION'";
                
                try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    
                    pstmt.setString(1, reason);
                    pstmt.setInt(2, donation.getId());
                    int rowsAffected = pstmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        Platform.runLater(() -> {
                            donation.setStatus("REJECTED");
                            donationsTable.refresh();
                            showAlert("Success", "Donation ID " + donation.getId() + " has been rejected!");
                        });
                    } else {
                        showAlert("Error", "No donation was updated.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Database Error", "Failed to reject donation: " + e.getMessage());
                }
            }
        }


        private void handleApproveAllocation(Allocation allocation) {
            String sql = "UPDATE donations SET status = 'APPROVED', admin_response = 'Request approved' " +
                         "WHERE id = ? AND transaction_type = 'REQUEST'";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, allocation.getId());
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    Platform.runLater(() -> {
                        allocation.setStatus("APPROVED");
                        allocationsTable.refresh();
                        showAlert("Success", "Allocation ID " + allocation.getId() + " has been approved!");
                    });
                } else {
                    showAlert("Error", "No allocation was updated.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Database Error", "Failed to approve allocation: " + e.getMessage());
            }
        }

        private void handleDenyAllocation(Allocation allocation) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Deny Allocation");
            dialog.setHeaderText("Denying Allocation ID: " + allocation.getId());
            dialog.setContentText("Reason for denial:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                String reason = "Denied: " + result.get().trim();
                String sql = "UPDATE donations SET status = 'DENIED', admin_response = ? " +
                             "WHERE id = ? AND transaction_type = 'REQUEST'";
                
                try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    
                    pstmt.setString(1, reason);
                    pstmt.setInt(2, allocation.getId());
                    int rowsAffected = pstmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        Platform.runLater(() -> {
                            allocation.setStatus("DENIED");
                            allocationsTable.refresh();
                            showAlert("Success", "Allocation ID " + allocation.getId() + " has been denied!");
                        });
                    } else {
                        showAlert("Error", "No allocation was updated.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Database Error", "Failed to deny allocation: " + e.getMessage());
                }
            }
        
        }
        private void setupAllocationsTableColumns(TableView<Allocation> table) {
            table.getColumns().clear();

            // ID Column
            TableColumn<Allocation, Integer> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            idCol.setStyle("-fx-alignment: CENTER;");
            idCol.setPrefWidth(80);

            // User ID Column
            TableColumn<Allocation, String> userIdCol = new TableColumn<>("User ID");
            userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
            userIdCol.setStyle("-fx-alignment: CENTER;");
            userIdCol.setPrefWidth(100);

            // Amount Column
            TableColumn<Allocation, Double> amountCol = new TableColumn<>("Amount");
            amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
            amountCol.setStyle("-fx-alignment: CENTER;");
            amountCol.setPrefWidth(150);
            amountCol.setCellFactory(tc -> new TableCell<>() {
                @Override
                protected void updateItem(Double amount, boolean empty) {
                    super.updateItem(amount, empty);
                    setText(empty || amount == null ? null : String.format("₹%,.2f", amount));
                }
            });

            // Purpose Column with text wrapping and tooltip
            TableColumn<Allocation, String> purposeCol = new TableColumn<>("Purpose");
            purposeCol.setCellValueFactory(new PropertyValueFactory<>("description"));
            purposeCol.setPrefWidth(250);
            purposeCol.setCellFactory(tc -> new TableCell<>() {
                private final Text text = new Text();
                private final StackPane pane = new StackPane(text);
                private Tooltip tooltip = null;
                
                {
                    text.wrappingWidthProperty().bind(purposeCol.widthProperty().subtract(15));
                    pane.setPadding(new Insets(5));
                    pane.setStyle("-fx-background-color: transparent;");
                    
                    // Show tooltip on hover
                    pane.setOnMouseEntered(e -> {
                        if (text.getText() != null && !text.getText().isEmpty()) {
                            tooltip = new Tooltip(text.getText());
                            tooltip.setStyle("-fx-font-size: 12px; -fx-max-width: 400px; -fx-wrap-text: true;");
                            Tooltip.install(pane, tooltip);
                        }
                    });
                    
                    // Remove tooltip when mouse exits
                    pane.setOnMouseExited(e -> {
                        if (tooltip != null) {
                            Tooltip.uninstall(pane, tooltip);
                            tooltip = null;
                        }
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        if (tooltip != null) {
                            Tooltip.uninstall(pane, tooltip);
                            tooltip = null;
                        }
                    } else {
                        text.setText(item);
                        setGraphic(pane);
                    }
                }
            });

            // Status/Action Column
            TableColumn<Allocation, String> actionCol = new TableColumn<>("Status/Actions");
            actionCol.setCellValueFactory(cellData -> {
                String status = cellData.getValue().getStatus();
                if (status.equals("PENDING")) {
                    return new SimpleStringProperty("");
                } else {
                    return new SimpleStringProperty(status);
                }
            });
            actionCol.setCellFactory(column -> new TableCell<>() {
                private final Button approveBtn = new Button("Approve");
                private final Button denyBtn = new Button("Deny");
                private final HBox buttons = new HBox(10, approveBtn, denyBtn);
                private final Label statusLabel = new Label();

                {
                    buttons.setAlignment(Pos.CENTER);
                    approveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                    denyBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
                    
                    statusLabel.setStyle("-fx-font-weight: bold;");
                    statusLabel.setAlignment(Pos.CENTER);
                    statusLabel.setMaxWidth(Double.MAX_VALUE);
                    statusLabel.setPadding(new Insets(5));
                    
                    approveBtn.setOnAction(event -> {
                        Allocation allocation = getTableView().getItems().get(getIndex());
                        handleApproveAllocation(allocation);
                    });
                    
                    denyBtn.setOnAction(event -> {
                        Allocation allocation = getTableView().getItems().get(getIndex());
                        handleDenyAllocation(allocation);
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        Allocation allocation = getTableView().getItems().get(getIndex());
                        if (allocation.getStatus().equals("PENDING")) {
                            setGraphic(buttons);
                            statusLabel.setText("");
                        } else {
                            statusLabel.setText(allocation.getStatus());
                            if (allocation.getStatus().equals("APPROVED")) {
                                statusLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: #4CAF50; -fx-padding: 5px;");
                            } else {
                                statusLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: #F44336; -fx-padding: 5px;");
                            }
                            setGraphic(statusLabel);
                        }
                    }
                }
            });
            actionCol.setPrefWidth(200);

            table.getColumns().addAll(idCol, userIdCol, amountCol, purposeCol, actionCol);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }}
