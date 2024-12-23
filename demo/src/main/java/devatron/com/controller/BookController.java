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

        // Add listeners
        backButton.setOnAction(event -> backButtonAction());
        addBookButton.setOnAction(event -> addBook());
        authorFilterField.textProperty().addListener((observable, oldValue, newValue) -> {
            List<Book> filteredBooks = books.stream()
                    .filter(book -> book.getAuthor().toLowerCase().contains(newValue.toLowerCase()))
                    .collect(Collectors.toList());
            bookTable.setItems(FXCollections.observableArrayList(filteredBooks));
        });

        adminButton.setOnAction(event -> onAdminAreaButtonClick());
        loanManagementButton.setOnAction(event -> onLoanManagementButtonClick());

        // Add double-click listener for bookTable
        bookTable.setRowFactory(tv -> {
            TableRow<Book> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Book selectedBook = row.getItem();
                    openLoanManagementForBook(selectedBook);
                }
            });
            row.setOnKeyPressed(event -> {
                if (!row.isEmpty() && event.getCode().toString().equals("ENTER")) {
                    Book selectedBook = row.getItem();
                    openLoanManagementForBook(selectedBook);
                }
            });
            return row;
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
