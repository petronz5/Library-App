package devatron.com.controller;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import devatron.com.database.DatabaseConnection;
import org.bson.Document;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;

public class AdminController {
    @FXML
    private ListView<String> registrationRequestsList;

    @FXML
    private Button approveButton;

    @FXML
    private Button rejectButton;

    @FXML
    private ListView<String> usersListView;

    @FXML
    private Button deleteUser;

    private ContextMenu userContextMenu;

    public void initialize() {
        loadRegistrationRequests();
        loadUsers();

        // Eventi per i pulsanti Approva e Rifiuta
        approveButton.setOnAction(event -> approveRequest());
        rejectButton.setOnAction(event -> rejectRequest());

        // Crea il menu contestuale
        createUserContextMenu();

        // Aggiungi il listener per il tasto destro sulla ListView degli utenti
        usersListView.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleUserListContextMenu);
    }

    private void createUserContextMenu() {
        userContextMenu = new ContextMenu();

        MenuItem promoteItem = new MenuItem("Promuovi a Admin");
        promoteItem.setOnAction(event -> promoteUser());

        MenuItem deleteItem = new MenuItem("Elimina Utente");
        deleteItem.setOnAction(event -> deleteUser());

        userContextMenu.getItems().addAll(promoteItem, deleteItem);
    }

    private void handleUserListContextMenu(MouseEvent event) {
        if (event.isSecondaryButtonDown() || event.isControlDown()) { // Tasto destro o Control
            String selectedUser = usersListView.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                userContextMenu.show(usersListView, event.getScreenX(), event.getScreenY());
            }
        } else {
            userContextMenu.hide(); // Nascondi il menu quando si clicca altrove
        }
    }

    private void promoteUser() {
        String selectedUser = usersListView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            MongoDatabase database = DatabaseConnection.getDatabase();
            MongoCollection<Document> users = database.getCollection("users");

            Document userDoc = users.find(new Document("username", selectedUser)).first();
            if (userDoc != null) {
                users.updateOne(userDoc, new Document("$set", new Document("role", "admin")));
                System.out.println("Utente " + selectedUser + " promosso a Admin.");
            }
        }
    }

    private void deleteUser() {
        String selectedUser = usersListView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            MongoDatabase database = DatabaseConnection.getDatabase();
            MongoCollection<Document> users = database.getCollection("users");

            users.deleteOne(new Document("username", selectedUser));
            usersListView.getItems().remove(selectedUser);
            System.out.println("Utente " + selectedUser + " eliminato.");
        }
    }

    private void loadRegistrationRequests() {
        MongoDatabase database = DatabaseConnection.getDatabase();
        MongoCollection<Document> requests = database.getCollection("registration_requests");

        List<Document> requestDocs = requests.find().into(new ArrayList<>());
        for (Document doc : requestDocs) {
            registrationRequestsList.getItems().add(doc.getString("username"));
        }
    }

    private void loadUsers() {
        MongoDatabase database = DatabaseConnection.getDatabase();
        MongoCollection<Document> users = database.getCollection("users");

        List<Document> userDocs = users.find().into(new ArrayList<>());
        for (Document doc : userDocs) {
            usersListView.getItems().add(doc.getString("username"));
        }
    }

    private void approveRequest() {
        String selectedUser = registrationRequestsList.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            MongoDatabase database = DatabaseConnection.getDatabase();
            MongoCollection<Document> users = database.getCollection("users");
            MongoCollection<Document> requests = database.getCollection("registration_requests");

            Document requestDoc = requests.find(new Document("username", selectedUser)).first();
            if (requestDoc != null) {
                Document newUser = new Document("username", requestDoc.getString("username"))
                        .append("password", requestDoc.getString("password"))
                        .append("role", "customer");

                users.insertOne(newUser);
                requests.deleteOne(requestDoc);
                registrationRequestsList.getItems().remove(selectedUser);
            }
        }
    }

    private void rejectRequest() {
        String selectedUser = registrationRequestsList.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            MongoDatabase database = DatabaseConnection.getDatabase();
            MongoCollection<Document> requests = database.getCollection("registration_requests");

            Document requestDoc = requests.find(new Document("username", selectedUser)).first();
            if (requestDoc != null) {
                requests.deleteOne(requestDoc);
                registrationRequestsList.getItems().remove(selectedUser);
            }
        }
    }
}
