package devatron.com.controller;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import devatron.com.App;
import devatron.com.database.DatabaseConnection;
import devatron.com.model.Loan;
import devatron.com.model.User;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import org.bson.Document;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LoanManagementController {

    private User currentUser; // Campo per memorizzare l'utente attualmente autenticato

    @FXML
    private TableView<Loan> loanTable;

    @FXML
    private TableColumn<Loan, String> bookTitleColumn;

    @FXML
    private TableColumn<Loan, String> usernameColumn;

    @FXML
    private TableColumn<Loan, String> firstNameColumn;

    @FXML
    private TableColumn<Loan, String> lastNameColumn;

    @FXML
    private TableColumn<Loan, String> emailColumn;

    @FXML
    private TableColumn<Loan, String> phoneNumberColumn;

    @FXML
    private TableColumn<Loan, LocalDate> loanDateColumn;

    @FXML
    private TableColumn<Loan, LocalDate> returnDateColumn;

    @FXML
    private Button backButton;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

/*************  ✨ Codeium Command ⭐  *************/
    /**
     * Configura la tabella dei prestiti con le colonne desiderate e carica i dati
     * presenti nel database. Configura inoltre il pulsante "Indietro" per tornare
     * alla schermata di login.
     */
/******  3d40e2ce-e1d3-42a0-acba-714414c16008  *******/
    public void initialize() {
        // Configura le colonne della tabella
        bookTitleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBookTitle()));
        usernameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        firstNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFirstName()));
        lastNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLastName()));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        phoneNumberColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhoneNumber()));
        loanDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getLoanDate()));
        returnDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getReturnDate()));

        // Carica i dati nella tabella
        loanTable.setItems(getLoans());

        // Configura il pulsante "Indietro"
        backButton.setOnAction(event -> handleBack());
    }

    private ObservableList<Loan> getLoans() {
        MongoDatabase database = DatabaseConnection.getDatabase();
        MongoCollection<Document> loans = database.getCollection("loans");

        List<Loan> loanList = new ArrayList<>();
        for (Document doc : loans.find()) {
            String bookTitle = doc.getString("bookTitle");
            String username = doc.getString("username");
            String firstName = doc.getString("firstName");
            String lastName = doc.getString("lastName");
            String email = doc.getString("email");
            String phoneNumber = doc.getString("phoneNumber");
            LocalDate loanDate;
            LocalDate returnDate = null;

            try {
                loanDate = LocalDate.parse(doc.getString("loanDate"), dateFormatter);
            } catch (Exception e) {
                loanDate = LocalDate.parse(doc.getString("loanDate"), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }

            if (doc.containsKey("returnDate")) {
                try {
                    returnDate = LocalDate.parse(doc.getString("returnDate"), dateFormatter);
                } catch (Exception e) {
                    returnDate = LocalDate.parse(doc.getString("returnDate"), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
            }

            loanList.add(new Loan(bookTitle, username, firstName, lastName, email, phoneNumber, loanDate, returnDate));
        }

        return FXCollections.observableArrayList(loanList);
    }

    public void setCurrentUser (User user) {
        this.currentUser  = user;
    }

    @FXML
    private void handleBack() {
        try {
            // Chiudi lo Stage corrente
            Stage currentStage = (Stage) backButton.getScene().getWindow();
            currentStage.close();
            App.showBookScreen(currentUser);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Errore durante la navigazione alla schermata di login: " + e.getMessage());
        }
    }
}