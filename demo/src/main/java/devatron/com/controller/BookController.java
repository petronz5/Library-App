package devatron.com.controller;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import devatron.com.App;
import devatron.com.database.DatabaseConnection;
import devatron.com.model.Book;
import devatron.com.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;

import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BookController {
    @FXML
    private TableView<Book> bookTable;

    @FXML
    private TableColumn<Book, String> titleColumn;

    @FXML
    private TableColumn<Book, String> authorColumn;

    @FXML
    private TableColumn<Book, String> editorColumn;

    @FXML
    private TableColumn<Book, Integer> quantityColumn;

    @FXML
    private Button backButton;

    @FXML
    private Button addBookButton;

    @FXML
    private TextField authorFilterField;

    @FXML
    private TextField titleFilterField;

    @FXML
    private TextField editorFilterField;

    @FXML
    private Button adminButton;

    @FXML
    private Button loanManagementButton;

    @FXML
    private TextField titleField;

    @FXML
    private TextField authorField;

    @FXML
    private TextField editorField;

    @FXML
    private TextField quantityField;

    private User currentUser;

    private ObservableList<Book> books = FXCollections.observableArrayList();

    public void setCurrentUser(User user) {
        this.currentUser = user;
        boolean isAdmin = "admin".equals(user.getRole());
        addBookButton.setVisible(isAdmin);
        adminButton.setVisible(isAdmin);
        System.out.println("Set current user: " + user.getUsername() + " (Role: " + user.getRole() + ")");
    }

    @FXML
    private void onAdminAreaButtonClick() {
        try {
            System.out.println("Navigating to admin area...");
            App.showAdminScreen();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error navigating to admin area: " + e.getMessage());
        }
    }

    @FXML
    private void onLoanManagementButtonClick() {
        try {
            System.out.println("Navigating to loan management...");
            App.showLoanManagementScreen();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error navigating to loan management: " + e.getMessage());
        }
    }

    @FXML
    private void backButtonAction() {
        try {
            System.out.println("Navigating back to login screen...");
            App.showLoginScreen();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error navigating to login screen: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        System.out.println("Initializing BookController...");

        // Initialize TableView columns
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        authorColumn.setCellValueFactory(cellData -> cellData.getValue().authorProperty());
        editorColumn.setCellValueFactory(cellData -> cellData.getValue().editorProperty());
        quantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());

        // Load books into the ObservableList
        books.setAll(getBooks());
        bookTable.setItems(books);

        // Add filtering functionality
        titleFilterField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        authorFilterField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        editorFilterField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        // Event listeners for buttons and actions
        backButton.setOnAction(event -> backButtonAction());
        addBookButton.setOnAction(event -> addBook());
        adminButton.setOnAction(event -> onAdminAreaButtonClick());
        loanManagementButton.setOnAction(event -> onLoanManagementButtonClick());

        // Aggiungi un listener per il campo della quantità
        quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) { // Controlla se il nuovo valore è composto solo da cifre
                quantityField.setText(newValue.replaceAll("[^\\d]", "")); // Rimuovi caratteri non numerici
            }
        });
    
        // Row double-click event
        bookTable.setRowFactory(tv -> {
            TableRow<Book> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Book selectedBook = row.getItem();
                    openLoanManagementForBook(selectedBook);
                }
            });
            return row;
        });

        // Handle key press event for the table
        bookTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.M) {
                Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
                if (selectedBook != null) {
                    editBookDetails(selectedBook);
                } else {
                    System.out.println("No book selected for editing.");
                }
            }
        });
    }

    private void applyFilters() {
        String titleFilter = titleFilterField.getText().toLowerCase();
        String authorFilter = authorFilterField.getText().toLowerCase();
        String editorFilter = editorFilterField.getText().toLowerCase();
    
        List<Book> filteredBooks = books.stream()
                .filter(book -> book.getTitle().toLowerCase().contains(titleFilter))
                .filter(book -> book.getAuthor().toLowerCase().contains(authorFilter))
                .filter(book -> book.getEditor().toLowerCase().contains(editorFilter))
                .collect(Collectors.toList());
    
        bookTable.setItems(FXCollections.observableArrayList(filteredBooks));
    }

    private void editBookDetails(Book book) {
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Modifica Libro");
        dialog.setHeaderText("Modifica i dettagli del libro selezionato");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Salva", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // Input fields
        TextField titleField = new TextField(book.getTitle());
        TextField authorField = new TextField(book.getAuthor());
        TextField editorField = new TextField(book.getEditor());
        TextField quantityField = new TextField(String.valueOf(book.getQuantity()));

        // Add inputs to the grid
        grid.add(new Label("Titolo:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Autore:"), 0, 1);
        grid.add(authorField, 1, 1);
        grid.add(new Label("Editore:"), 0, 2);
        grid.add(editorField, 1, 2);
        grid.add(new Label("Quantità:"), 0, 3);
        grid.add(quantityField, 1, 3);

        // Add the grid to the dialog
        dialog.getDialogPane().setContent(grid);

        // Convert the result to a list when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return List.of(
                    titleField.getText(),
                    authorField.getText(),
                    editorField.getText(),
                    quantityField.getText()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            try {
                String newTitle = result.get(0);
                String newAuthor = result.get(1);
                String newEditor = result.get(2);
                int newQuantity = Integer.parseInt(result.get(3));

                // Update in the database
                MongoDatabase database = DatabaseConnection.getDatabase();
                MongoCollection<Document> booksCollection = database.getCollection("books");

                Document filter = new Document("title", book.getTitle());
                Document update = new Document("$set", new Document("title", newTitle)
                        .append("author", newAuthor)
                        .append("editor", newEditor)
                        .append("quantity", newQuantity));
                booksCollection.updateOne(filter, update);

                // Refresh the table
                books.setAll(getBooks());
                System.out.println("Libro aggiornato: " + newTitle);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Errore durante l'aggiornamento del libro: " + e.getMessage());
            }
        });
    }


    private void openLoanManagementForBook(Book book) {
        try {
            System.out.println("Opening loan management for book: " + book.getTitle());
            App.showLoanManagementScreenWithBook(book, currentUser); // Passa currentUser
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error opening loan management: " + e.getMessage());
        }
    }
    

    private List<Book> getBooks() {
        try {
            System.out.println("Fetching books from the database...");
            MongoDatabase database = DatabaseConnection.getDatabase();
            MongoCollection<Document> booksCollection = database.getCollection("books");

            List<Book> bookList = new ArrayList<>();
            for (Document doc : booksCollection.find()) {
                String title = doc.getString("title");
                String author = doc.getString("author");
                String editor = doc.getString("editor");
                int quantity = doc.getInteger("quantity", 0); // Default to 0 if null
                System.out.println("Book found: Title='" + title + "', Author='" + author + "', Quantity=" + quantity);
                bookList.add(new Book(title, author, quantity, editor));
            }

            System.out.println("Total books retrieved: " + bookList.size());
            return bookList;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error fetching books from the database: " + e.getMessage());
            return new ArrayList<>(); // Return empty list on error
        }
    }

    private void addBook() {
        String title = titleField.getText();
        String author = authorField.getText();
        String editor = editorField.getText();
        int quantity;

        if (title.isEmpty() || author.isEmpty() || editor.isEmpty() || quantityField.getText().isEmpty()) {
            System.out.println("All fields must be filled.");
            return;
        }

        try {
            quantity = Integer.parseInt(quantityField.getText());
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity: " + quantityField.getText());
            return;
        }

        try {
            MongoDatabase database = DatabaseConnection.getDatabase();
            MongoCollection<Document> booksCollection = database.getCollection("books");

            Document newBook = new Document("title", title)
                    .append("author", author)
                    .append(editor, editor)
                    .append("quantity", quantity);
            booksCollection.insertOne(newBook);
            System.out.println("Book added: Title='" + title + "', Author='" + author + "', Quantity=" + quantity);

            titleField.clear();
            authorField.clear();
            editorField.clear();
            quantityField.clear();

            // Refresh the TableView
            books.setAll(getBooks());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error adding book: " + e.getMessage());
        }
    }
}
