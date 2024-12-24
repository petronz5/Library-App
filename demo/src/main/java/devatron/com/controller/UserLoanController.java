package devatron.com.controller;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import devatron.com.App;
import devatron.com.database.DatabaseConnection;
import devatron.com.model.Book;
import devatron.com.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import org.bson.Document;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UserLoanController {

    @FXML
    private TextField bookTitleField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private Button confirmLoanButton;
    @FXML
    private Button backButton;
    @FXML
    private Label errorLabel;

    private Book selectedBook; // Assicurati di impostare questo valore quando apri la schermata

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private User currentUser;

    @FXML
    public void initialize() {
        // Altre inizializzazioni...

        // Aggiungi un listener per il campo del numero di telefono
        phoneNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Controlla se il nuovo valore è composto solo da cifre
            if (!newValue.matches("\\d*")) {
                phoneNumberField.setText(newValue.replaceAll("[^\\d]", "")); // Rimuovi caratteri non numerici
            }

            // Controlla la lunghezza del numero di telefono
            if (newValue.length() > 10) {
                phoneNumberField.setText(newValue.substring(0, 10)); // Limita a 10 cifre
            }

            // Mostra un messaggio di errore se la lunghezza non è valida
            if (newValue.length() < 9) {
                errorLabel.setText("Il numero di telefono deve avere tra 9 e 10 cifre.");
                errorLabel.setVisible(true);
            } else {
                errorLabel.setVisible(false); // Nascondi il messaggio di errore se la lunghezza è valida
            }
        });
    }

    public void setBook(Book book) {
        this.selectedBook = book;
        bookTitleField.setText(selectedBook.getTitle());
    }

    @FXML
    private void confirmLoan() {
        String username = usernameField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String phoneNumber = phoneNumberField.getText();

        if (!validateUserInput()) {
            System.out.println("Dati mancanti.");
            return;
        }

        if (selectedBook == null) {
            System.out.println("Nessun libro selezionato.");
            return;
        }

        try {
            MongoDatabase database = DatabaseConnection.getDatabase();
            MongoCollection<Document> loans = database.getCollection("loans");
            MongoCollection<Document> booksCollection = database.getCollection("books");

            LocalDate loanDate = LocalDate.now();
            LocalDate returnDate = loanDate.plusDays(30); // Imposta la returnDate a 30 giorni dopo la loanDate

            // Aggiungi il prestito nella collezione "loans"
            Document newLoan = new Document("bookTitle", selectedBook.getTitle())
                    .append("username", currentUser.getUsername())
                    .append("firstName", firstName)
                    .append("lastName", lastName)
                    .append("email", email)
                    .append("phoneNumber", phoneNumber)
                    .append("loanDate", loanDate.format(dateFormatter))
                    .append("returnDate", returnDate.format(dateFormatter));

            loans.insertOne(newLoan);

            // Decrementa il campo "availableQuantity" nel libro
            Document filter = new Document("title", selectedBook.getTitle());
            Document update = new Document("$inc", new Document("availableQuantity", -1));
            booksCollection.updateOne(filter, update);

            System.out.println("Prestito confermato: " + selectedBook.getTitle() + " a " + username);
            handleBack(); 
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Errore durante la conferma del prestito: " + e.getMessage());
        }
    }

    

    private boolean validateUserInput() {
        boolean isValid =   !usernameField.getText().isEmpty() &&
                            !firstNameField.getText().isEmpty() &&
                            !lastNameField.getText().isEmpty() &&
                            !emailField.getText().isEmpty() &&
                            !phoneNumberField.getText().isEmpty();
        
        String phoneNumber = phoneNumberField.getText();
        if (phoneNumber.length() < 9 || phoneNumber.length() > 10) {
            errorLabel.setText("Il numero di telefono deve avere tra 9 e 10 cifre.");
            errorLabel.setVisible(true);
            isValid = false;
        } else {
            errorLabel.setVisible(false);
        }
        return isValid;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        usernameField.setText(user.getUsername());
    }

    @FXML
    private void handleBack() {
        try {
            Stage currentStage = (Stage) backButton.getScene().getWindow();
            currentStage.close();
            App.showBookScreen(currentUser);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}