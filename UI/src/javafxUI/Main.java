package javafxUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafxUI.controller.EmulatorController;
import javafxUI.service.ThemeManager;

import java.net.URL;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file
        URL fxmlLocation = getClass().getResource("/javafxUI/view/fxml/emulator.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Parent root = fxmlLoader.load();

        // Get the controller
        EmulatorController controller = fxmlLoader.getController();

        // Scene setup
        Scene scene = new Scene(root, 1200, 800);

        // Initialize theme manager with the scene
        ThemeManager themeManager = ThemeManager.getInstance();
        themeManager.setScene(scene);

        // Set the controller's scene reference
        controller.setScene(scene);

        // Apply initial dark theme (this will load the CSS)
        themeManager.setTheme(ThemeManager.Theme.DARK);

        primaryStage.setTitle("S-Emulator - Dark/Light Theme Support");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}