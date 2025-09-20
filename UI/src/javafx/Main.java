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
        primaryStage.setTitle("S-Emulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // The inner classes (Instruction, Variable, Statistic) should be moved to their own files,
    // possibly in a new 'model' package.
    public static class Instruction {
        // Add properties for your instruction model
    }

    public static class Variable {
        // Add properties for variables
    }

    public static class Statistic {
        // Add properties for statistics
    }
}
