package ui.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.File;

public class FxTheme {

    private FxTheme() {
    }

    public static BorderPane page(String title, String subtitle, Node content) {
        BorderPane page = new BorderPane();
        page.getStyleClass().add("page");
        page.setTop(pageHeader(title, subtitle));
        page.setCenter(content);
        BorderPane.setMargin(content, new Insets(0, 0, 0, 0));
        return page;
    }

    public static VBox pageHeader(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("page-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("page-subtitle");

        VBox box = new VBox(4, titleLabel, subtitleLabel);
        box.getStyleClass().add("page-header");
        return box;
    }

    public static VBox card(String title, Node content) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");

        VBox card = new VBox(12, titleLabel, content);
        card.getStyleClass().add("card");
        VBox.setVgrow(content, Priority.ALWAYS);
        return card;
    }

    public static VBox card(Node content) {
        VBox card = new VBox(12, content);
        card.getStyleClass().add("card");
        VBox.setVgrow(content, Priority.ALWAYS);
        return card;
    }

    public static BorderPane ledgerPage(String title, String subtitle, Node content) {
        BorderPane page = new BorderPane();
        page.getStyleClass().add("ledger-page");
        page.setTop(ledgerStrip(title, subtitle));
        page.setCenter(content);
        return page;
    }

    public static VBox ledgerStrip(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("ledger-strip-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("ledger-strip-subtitle");

        VBox strip = new VBox(2, titleLabel, subtitleLabel);
        strip.getStyleClass().add("ledger-strip");
        return strip;
    }

    public static BorderPane ledgerWorkspace(Node mainSurface, Node inspector) {
        BorderPane workspace = new BorderPane();
        workspace.getStyleClass().add("ledger-workspace");
        workspace.setCenter(mainSurface);
        workspace.setRight(inspector);
        BorderPane.setMargin(inspector, new Insets(0, 0, 0, 10));
        return workspace;
    }

    public static VBox ledgerInspector(String title, Node content) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("ledger-inspector-title");

        VBox inspector = new VBox(9, titleLabel, content);
        inspector.getStyleClass().add("ledger-inspector");
        VBox.setVgrow(content, Priority.ALWAYS);
        return inspector;
    }

    public static HBox ledgerCommandBar(Node... controls) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("ledger-command-bar");
        row.getChildren().addAll(controls);
        return row;
    }

    public static VBox ledgerSurface(String title, Node commandBar, Node table) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("ledger-surface-title");

        VBox surface = new VBox(8, titleLabel, commandBar, table);
        surface.getStyleClass().add("ledger-surface");
        VBox.setVgrow(table, Priority.ALWAYS);
        return surface;
    }

    public static void styleWorkbench(BorderPane pane) {
        pane.getStyleClass().add("workbench");
    }

    public static void styleFormCard(VBox card) {
        card.getStyleClass().add("workbench-form-card");
        card.setMinWidth(300);
        card.setPrefWidth(330);
    }

    public static void styleTableCard(VBox card) {
        card.getStyleClass().add("workbench-table-card");
    }

    public static VBox statCard(String title, String value, String subtitle) {
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("summary-number");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("muted-label");
        subtitleLabel.setWrapText(true);

        VBox box = new VBox(4, titleLabel, valueLabel, subtitleLabel);
        box.getStyleClass().add("card");
        box.setMinWidth(180);
        box.setMinHeight(116);
        return box;
    }

    public static HBox actionRow(Button... buttons) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_RIGHT);
        row.getChildren().addAll(buttons);
        return row;
    }

    public static HBox toolbar(Node... controls) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("toolbar-card");
        row.getChildren().addAll(controls);
        return row;
    }

    public static Node logo(double height) {
        File logoFile = new File("Safad_Logo.png");
        if (!logoFile.exists()) {
            Label fallback = new Label("SAFAD");
            fallback.getStyleClass().add("logo-fallback");
            return fallback;
        }

        ImageView imageView = new ImageView(new Image(logoFile.toURI().toString()));
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        return imageView;
    }

    public static Button primaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("primary-button");
        return button;
    }

    public static Button secondaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("secondary-button");
        return button;
    }

    public static Button dangerButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("danger-button");
        return button;
    }

    public static TextField textField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        return field;
    }

    public static void styleComboBox(ComboBox<?> comboBox) {
        comboBox.getStyleClass().add("clean-selector");
    }

    public static void styleTable(TableView<?> table) {
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        FxTableUtil.bindColumnMode(table);
    }

    public static void showInfo(String message) {
        showAlert(Alert.AlertType.INFORMATION, "SAFAD", message);
    }

    public static void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "SAFAD", message);
    }

    public static boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("SAFAD");
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(buttonType -> buttonType.getButtonData().isDefaultButton()).isPresent();
    }

    public static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static Insets padding() {
        return new Insets(20);
    }
}
