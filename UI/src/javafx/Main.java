package javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file
        URL fxmlLocation = getClass().getResource("/javafx/fxml/emulator.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Parent root = fxmlLoader.load();

        // Scene setup
        Scene scene = new Scene(root, 1200, 800);

        // Load CSS stylesheet
        scene.getStylesheets().add(getClass().getResource("/javafx/css/style.css").toExternalForm());

        primaryStage.setTitle("S-Emulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
