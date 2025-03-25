import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class EMainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EventsPage.fxml"));
            Parent root = loader.load();

            // Set up the scene with proper size
            Scene scene = new Scene(root, 1407, 706);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Events Page");
            primaryStage.setResizable(true); // Allow resizing
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
