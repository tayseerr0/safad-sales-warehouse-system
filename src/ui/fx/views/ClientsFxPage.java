package ui.fx.views;

import dao.ClientDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Client;
import ui.fx.FxTableUtil;
import ui.fx.FxTheme;

import java.time.LocalDate;

public class ClientsFxPage extends VBox {

    private final ClientDAO clientDAO = new ClientDAO();
    private final ObservableList<Client> clients = FXCollections.observableArrayList();

    private final TextField idField = FxTheme.textField("ID");
    private final TextField nameField = FxTheme.textField("Client name");
    private final TextField phoneField = FxTheme.textField("Phone");
    private final TextField emailField = FxTheme.textField("Email");
    private final DatePicker registrationDatePicker = new DatePicker(LocalDate.now());
    private final TextField cityField = FxTheme.textField("City");
    private final TextField addressField = FxTheme.textField("Address");
    private final ComboBox<String> typeComboBox = new ComboBox<>();
    private final TextField searchField = FxTheme.textField("Search clients");
    private final TableView<Client> table = new TableView<>();

    public ClientsFxPage() {
        FxTheme.styleComboBox(typeComboBox);
        typeComboBox.getStyleClass().add("compact-selector");
        getChildren().add(FxTheme.page("Clients", "Add, update, delete, search, and filter SAFAD clients.", createContent()));
        loadClients();
    }

    private BorderPane createContent() {
        idField.setEditable(false);
        typeComboBox.setItems(FXCollections.observableArrayList("Individual", "Company", "Reseller"));
        typeComboBox.getSelectionModel().selectFirst();

        configureTable();

        BorderPane content = new BorderPane();
        content.setTop(createToolbar());
        content.setLeft(FxTheme.card("Client Form", createForm()));
        content.setCenter(FxTheme.card("Client Table", table));
        BorderPane.setMargin(content.getLeft(), new javafx.geometry.Insets(0, 16, 0, 0));
        return content;
    }

    private HBox createToolbar() {
        Button searchButton = FxTheme.secondaryButton("Search");
        Button refreshButton = FxTheme.secondaryButton("Refresh");
        searchButton.setOnAction(e -> searchClients());
        refreshButton.setOnAction(e -> loadClients());

        HBox toolbar = FxTheme.toolbar(searchField, searchButton, refreshButton);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        return toolbar;
    }

    private GridPane createForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        addRow(form, 0, "ID", idField);
        addRow(form, 1, "Name", nameField);
        addRow(form, 2, "Phone", phoneField);
        addRow(form, 3, "Email", emailField);
        addRow(form, 4, "Registration", registrationDatePicker);
        addRow(form, 5, "City", cityField);
        addRow(form, 6, "Address", addressField);
        addRow(form, 7, "Type", typeComboBox);

        Button addButton = FxTheme.primaryButton("Add");
        Button updateButton = FxTheme.primaryButton("Update");
        Button deleteButton = FxTheme.dangerButton("Delete");
        Button clearButton = FxTheme.secondaryButton("Clear");

        addButton.setOnAction(e -> addClient());
        updateButton.setOnAction(e -> updateClient());
        deleteButton.setOnAction(e -> deleteClient());
        clearButton.setOnAction(e -> clearForm());

        form.add(FxTheme.actionRow(addButton, updateButton, deleteButton, clearButton), 0, 8, 2, 1);
        return form;
    }

    private void addRow(GridPane form, int row, String label, javafx.scene.Node field) {
        form.add(new javafx.scene.control.Label(label), 0, row);
        form.add(field, 1, row);
    }

    private void configureTable() {
        table.getColumns().add(FxTableUtil.column("ID", Client::getClientId, 70));
        table.getColumns().add(FxTableUtil.column("Name", Client::getClientName, 160));
        table.getColumns().add(FxTableUtil.column("Phone", Client::getPhone, 120));
        table.getColumns().add(FxTableUtil.column("Email", Client::getEmail, 190));
        table.getColumns().add(FxTableUtil.column("Registration", Client::getRegistrationDate, 120));
        table.getColumns().add(FxTableUtil.column("City", Client::getCity, 110));
        table.getColumns().add(FxTableUtil.column("Address", Client::getAddress, 180));
        table.getColumns().add(FxTableUtil.column("Type", Client::getClientType, 100));
        FxTableUtil.installSearch(table, clients, searchField);
        FxTheme.styleTable(table);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, client) -> {
            if (client != null) {
                fillForm(client);
            }
        });
    }

    private void loadClients() {
        clients.setAll(clientDAO.getAllClients());
    }

    private void searchClients() {
        String keyword = searchField.getText().trim();
        clients.setAll(keyword.isEmpty() ? clientDAO.getAllClients() : clientDAO.searchClients(keyword));
    }

    private void addClient() {
        Client client = readForm(false);
        if (client != null && clientDAO.addClient(client)) {
            FxTheme.showInfo("Client added successfully.");
            clearForm();
            loadClients();
        } else {
            FxTheme.showError("Failed to add client.");
        }
    }

    private void updateClient() {
        Client client = readForm(true);
        if (client != null && clientDAO.updateClient(client)) {
            FxTheme.showInfo("Client updated successfully.");
            clearForm();
            loadClients();
        } else {
            FxTheme.showError("Failed to update client.");
        }
    }

    private void deleteClient() {
        if (idField.getText().isBlank()) {
            FxTheme.showError("Select a client first.");
            return;
        }

        if (FxTheme.confirm("Delete selected client?") && clientDAO.deleteClient(Integer.parseInt(idField.getText()))) {
            FxTheme.showInfo("Client deleted successfully.");
            clearForm();
            loadClients();
        }
    }

    private Client readForm(boolean includeId) {
        if (nameField.getText().trim().isEmpty()) {
            FxTheme.showError("Client name is required.");
            return null;
        }

        Client client = new Client(
                nameField.getText().trim(),
                phoneField.getText().trim(),
                emailField.getText().trim(),
                registrationDatePicker.getValue(),
                cityField.getText().trim(),
                addressField.getText().trim(),
                typeComboBox.getValue()
        );

        if (includeId) {
            if (idField.getText().isBlank()) {
                FxTheme.showError("Select a client first.");
                return null;
            }
            client.setClientId(Integer.parseInt(idField.getText()));
        }

        return client;
    }

    private void fillForm(Client client) {
        idField.setText(String.valueOf(client.getClientId()));
        nameField.setText(client.getClientName());
        phoneField.setText(client.getPhone());
        emailField.setText(client.getEmail());
        registrationDatePicker.setValue(client.getRegistrationDate());
        cityField.setText(client.getCity());
        addressField.setText(client.getAddress());
        typeComboBox.setValue(client.getClientType());
    }

    private void clearForm() {
        idField.clear();
        nameField.clear();
        phoneField.clear();
        emailField.clear();
        registrationDatePicker.setValue(LocalDate.now());
        cityField.clear();
        addressField.clear();
        typeComboBox.getSelectionModel().selectFirst();
        table.getSelectionModel().clearSelection();
    }
}
