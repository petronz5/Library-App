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

    private Book selectedBook; // Assicurati di impostare questo valore quando apri la schermata

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private User currentUser;

/*************  ✨ Codeium Command ⭐  *************/
/**
 * Sets the selected book and updates the book title field.
 *
 * @param book the Book object to be set as selected
 */

/******  f3849d69-acdd-4235-aad8-226c0fc0dcd1  *******/
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

            LocalDate loanDate = LocalDate.now();
            LocalDate returnDate = loanDate.plusDays(30); // Imposta la returnDate a 30 giorni dopo la loanDate

            Document newLoan = new Document("bookTitle", selectedBook.getTitle())
                    .append("username", currentUser.getUsername())
                    .append("firstName", firstName)
                    .append("lastName", lastName)
                    .append("email", email)
                    .append("phoneNumber", phoneNumber)
                    .append("loanDate", loanDate.format(dateFormatter))
                    .append("returnDate", returnDate.format(dateFormatter));

            loans.insertOne(newLoan);
            System.out.println("Prestito confermato: " + selectedBook.getTitle() + " a " + username);
            handleBack(); 
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Errore durante la conferma del prestito: " + e.getMessage());
        }
    }

    private boolean validateUserInput() {
        return !usernameField.getText().isEmpty() &&
               !firstNameField.getText().isEmpty() &&
               !lastNameField.getText().isEmpty() &&
               !emailField.getText().isEmpty() &&
               !phoneNumberField.getText().isEmpty();
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