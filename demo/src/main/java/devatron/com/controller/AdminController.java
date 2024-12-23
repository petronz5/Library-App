package devatron.com.controller;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import devatron.com.database.DatabaseConnection;
import org.bson.Document;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;

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
    private ListView<String> usersListView; // Aggiungi questa riga
    @FXML
    private Button deleteUser; // Aggiungi questa riga

    public void initialize() {
        loadRegistrationRequests();
        loadUsers();
        
        approveButton.setOnAction(event -> approveRequest());
        rejectButton.setOnAction(event -> rejectRequest());
        deleteUser.setOnAction(event -> deleteUser());
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
        String selectedUser  = registrationRequestsList.getSelectionModel().getSelectedItem();
        if (selectedUser  != null) {
            MongoDatabase database = DatabaseConnection.getDatabase();
            MongoCollection<Document> users = database.getCollection("users");
            MongoCollection<Document> requests = database.getCollection("registration_requests");

            // Trova la richiesta di registrazione
            Document requestDoc = requests.find(new Document("username", selectedUser )).first();
            if (requestDoc != null) {
                // Crea un nuovo documento per l'utente
                Document newUser  = new Document("username", requestDoc.getString("username"))
                        .append("password", requestDoc.getString("password")) // Non dovresti mai salvare la password in chiaro
                        .append("role", "customer"); // Imposta il ruolo dell'utente

                // Aggiungi l'utente al database
                users.insertOne(newUser );

                // Rimuovi la richiesta di registrazione
                requests.deleteOne(requestDoc);

                // Ricarica la lista delle richieste
                registrationRequestsList.getItems().remove(selectedUser );
            }
        }
    }

    private void rejectRequest() {
        String selectedUser  = registrationRequestsList.getSelectionModel().getSelectedItem();
        if (selectedUser  != null) {
            MongoDatabase database = DatabaseConnection.getDatabase();
            MongoCollection<Document> requests = database.getCollection("registration_requests");

            // Trova la richiesta di registrazione
            Document requestDoc = requests.find(new Document("username", selectedUser )).first();
            if (requestDoc != null) {
                // Rimuovi la richiesta di registrazione
                requests.deleteOne(requestDoc);
                registrationRequestsList.getItems().remove(selectedUser );
            }
        }
    }

    private void deleteUser() {
        String selectedUser  = usersListView.getSelectionModel().getSelectedItem();
        if (selectedUser  != null) {
            MongoDatabase database = DatabaseConnection.getDatabase();
            MongoCollection<Document> users = database.getCollection("users");
            users.deleteOne(new Document("username", selectedUser));
            usersListView.getItems().remove(selectedUser);
        }
    }
}