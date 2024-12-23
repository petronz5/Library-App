package devatron.com.controller;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import devatron.com.App;
import devatron.com.database.DatabaseConnection;
import devatron.com.model.User;
import java.io.IOException;

import org.bson.Document;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    @FXML
    private TextField registerUsernameField;
    @FXML
    private PasswordField registerPasswordField;

    public void onLoginClick() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        User user = authenticate(username, password);
        
        if (user == null) {
            // Controlla se l'username esiste
            if (usernameExists(username)) {
                errorLabel.setText("Password non corretta.");
            } else {
                errorLabel.setText("Username non esistente.");
            }
            errorLabel.setVisible(true);
        } else {
            // Autenticazione riuscita, mostra la schermata dei libri
            try {
                App.showBookScreen(user);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean usernameExists(String username) {
        MongoDatabase database = DatabaseConnection.getDatabase();
        MongoCollection<Document> users = database.getCollection("users");
        Document userDoc = users.find(new Document("username", username)).first();
        return userDoc != null;
    }

    public void onRegisterClick() {
        String username = registerUsernameField.getText();
        String password = registerPasswordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Tutti i campi devono essere compilati.");
            return;
        }

        // Invia la richiesta di registrazione
        sendRegistrationRequest(username, password);

        // Pulisci i campi di registrazione
        registerUsernameField.clear();
        registerPasswordField.clear();
    }

    private void sendRegistrationRequest(String username, String password) {
        MongoDatabase database = DatabaseConnection.getDatabase();
        MongoCollection<Document> requests = database.getCollection("registration_requests");

        Document request = new Document("username", username)
                .append("password", password) // Non dovresti mai salvare la password in chiaro
                .append("status", "pending"); // Stato della richiesta

        requests.insertOne(request);
        errorLabel.setText("Richiesta di registrazione inviata. Attendi l'approvazione.");
    }

    public User authenticate(String username, String password) {
        MongoDatabase database = DatabaseConnection.getDatabase();
        MongoCollection<Document> users = database.getCollection("users");
    
        Document userDoc = users.find(new Document("username", username)).first();
    
        if (userDoc != null) {
            String storedPassword = userDoc.getString("password"); // Assicurati di avere la password memorizzata
            String role = userDoc.getString("role");
            
            // Controlla se la password fornita corrisponde a quella memorizzata
            if (storedPassword.equals(password)) {
                return new User(username, password, role);
            }
        }
        return null; // Restituisce null se l'autenticazione fallisce
    }
}