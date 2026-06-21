import db.DBConnection;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import model.AppUser;
import ui.LoginPage;
import ui.MainLayout;
import ui.SessionManager;
import ui.Theme;

import java.net.URL;

public class Main extends Application {

    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        boolean connected = DBConnection.testConnection();

        showLogin();
        stage.setMinWidth(1080);
        stage.setMinHeight(680);
        stage.show();

        if (!connected) {
            Theme.showAlert(
                    Alert.AlertType.WARNING,
                    "Database Connection",
                    "Failed to connect to safad_db. Check MySQL server, database name, username, and password."
            );
        }
    }

    private void showLogin() {
        LoginPage loginPage = new LoginPage(this::showMainApp);
        Scene scene = createScene(loginPage, 1080, 680);
        stage.setTitle("SAFAD Login");
        stage.setScene(scene);
    }

    private void showMainApp(AppUser user) {
        SessionManager.login(user);
        MainLayout mainLayout = new MainLayout(this::showLogin);
        Scene scene = createScene(mainLayout.getRoot(), 1280, 760);
        stage.setTitle("SAFAD Sales and Warehouse Management System");
        stage.setScene(scene);
    }

    private Scene createScene(Parent root, int width, int height) {
        Scene scene = new Scene(root, width, height);

        URL css = Main.class.getResource("/ui/styles/safad.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        return scene;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
