package javafxUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafxUI.controller.EmulatorController;
import javafxUI.controller.ServerModeController;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create TabPane
        TabPane tabPane = new TabPane();

        // Local Mode Tab (existing functionality)
        Tab localTab = new Tab("Local Mode");
        localTab.setClosable(false);
        FXMLLoader localLoader = new FXMLLoader(getClass().getResource("/javafxUI/view/fxml/emulator.fxml"));
        localTab.setContent(localLoader.load());
        EmulatorController localController = localLoader.getController();

        // Server Mode Tab (new functionality)
        Tab serverTab = new Tab("Server Mode");
        serverTab.setClosable(false);
        FXMLLoader serverLoader = new FXMLLoader(getClass().getResource("/javafxUI/view/fxml/serverMode.fxml"));
        serverTab.setContent(serverLoader.load());
        ServerModeController serverController = serverLoader.getController();

        tabPane.getTabs().addAll(localTab, serverTab);

        // Create scene
        Scene scene = new Scene(tabPane, 1200, 800);

        // Apply theme to local controller
        localController.setScene(scene);

        primaryStage.setTitle("S-Emulator - Hybrid Mode");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Cleanup on close
        primaryStage.setOnCloseRequest(event -> {
            serverController.cleanup();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}