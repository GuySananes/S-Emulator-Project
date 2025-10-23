package javafxUI.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafxUI.service.v2.ApiClient;
import javafxUI.service.v2.PullService;

public class ServerModeController {

    @FXML private TextField usernameField;
    @FXML private Button loginButton;
    @FXML private Label statusLabel;
    @FXML private Label creditsLabel;

    @FXML private TableView<ApiClient.UserRow> usersTable;
    @FXML private TableColumn<ApiClient.UserRow, String> nameColumn;
    @FXML private TableColumn<ApiClient.UserRow, Integer> mainCountColumn;
    @FXML private TableColumn<ApiClient.UserRow, Integer> funcCountColumn;
    @FXML private TableColumn<ApiClient.UserRow, Integer> creditsColumn;
    @FXML private TableColumn<ApiClient.UserRow, Integer> usedColumn;
    @FXML private TableColumn<ApiClient.UserRow, Integer> runsColumn;

    private final ApiClient apiClient = new ApiClient();
    private final PullService pullService = new PullService();
    private final ObservableList<ApiClient.UserRow> users = FXCollections.observableArrayList();

    private ApiClient.Session currentSession;

    @FXML
    public void initialize() {
        setupUsersTable();
        pullService.setUsersTarget(users);
        statusLabel.setText("Not logged in");
        creditsLabel.setText("Credits: -");
    }

    private void setupUsersTable() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        mainCountColumn.setCellValueFactory(new PropertyValueFactory<>("mainCount"));
        funcCountColumn.setCellValueFactory(new PropertyValueFactory<>("funcCount"));
        creditsColumn.setCellValueFactory(new PropertyValueFactory<>("credits"));
        usedColumn.setCellValueFactory(new PropertyValueFactory<>("used"));
        runsColumn.setCellValueFactory(new PropertyValueFactory<>("runs"));

        usersTable.setItems(users);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showError("Username required", "Please enter a username");
            return;
        }

        loginButton.setDisable(true);
        statusLabel.setText("Logging in...");

        new Thread(() -> {
            try {
                currentSession = apiClient.login(username);

                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("Logged in as: " + currentSession.user);
                    creditsLabel.setText("Credits: " + currentSession.credits);
                    usernameField.setDisable(true);
                    loginButton.setText("Logged In");

                    // Start polling
                    pullService.start();
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Login Failed", e.getMessage());
                    statusLabel.setText("Login failed");
                    loginButton.setDisable(false);
                });
            }
        }).start();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void cleanup() {
        pullService.stop();
    }
}