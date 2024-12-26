package devatron.com.controller;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import devatron.com.App;
import devatron.com.database.DatabaseConnection;
import devatron.com.model.Book;
import devatron.com.model.User;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BookController {
    @FXML
    private TableView<Book> bookTable;

    @FXML
    private TableColumn<Book, String> titleColumn, authorColumn, editorColumn, genreColumn, isbnColumn;

    @FXML
    private TableColumn<Book, Integer> quantityColumn, availableQuantityColumn, publishYearColumn;

    @FXML
    private TableColumn<Book, Double> priceColumn;

    @FXML
    private TextField titleField, authorField, editorField, genreField, isbnField;

    @FXML
    private TextField quantityField, availableQuantityField, publishYearField, priceField;

    @FXML
    private TextField titleFilterField, authorFilterField, editorFilterField, genreFilterField, priceFilterField;

    @FXML
    private Button backButton;

    @FXML
    private Button addBookButton;

    @FXML
    private Button adminButton;

    @FXML
    private Button loanManagementButton;

    private User currentUser;

    private ObservableList<Book> books = FXCollections.observableArrayList();

    private ContextMenu bookContextMenu;

    @FXML
    private VBox filterPane;

    @FXML
    private Button toggleFilterButton;

    private boolean isFilterPaneExpanded = false;

    @FXML
    private void toggleFilterPane() {
        if (isFilterPaneExpanded) {
            collapseFilterPane(); // Riduci la larghezza
        } else {
            expandFilterPane(); // Espandi la larghezza
        }
        isFilterPaneExpanded = !isFilterPaneExpanded;
    }

    private void expandFilterPane() {
        filterPane.setPrefWidth(250); // Imposta larghezza espansa
        toggleFilterButton.setText("◄");
        filterPane.applyCss();
        filterPane.layout();
    }

    private void collapseFilterPane() {
        filterPane.setPrefWidth(30); // Imposta larghezza ridotta
        toggleFilterButton.setText("►");
        filterPane.applyCss();
        filterPane.layout();
    }

    @FXML
    public void initialize() {
        System.out.println("Initializing BookController...");

        // Initialize TableView columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        editorColumn.setCellValueFactory(new PropertyValueFactory<>("editor"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        availableQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("availableQuantity"));
        publishYearColumn.setCellValueFactory(new PropertyValueFactory<>("publicationYear"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        collapseFilterPane();

        // Load books into the ObservableList
        books.setAll(getBooks());
        bookTable.setItems(books);

        configureTableColumns();
        createBookContextMenu();

        // Configura le azioni per il TableView
        bookTable.setRowFactory(tv -> {
            TableRow<Book> row = new TableRow<>();

            // Doppio clic con il tasto sinistro per aprire Gestione Prestiti
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY && !row.isEmpty()) {
                    Book selectedBook = row.getItem();
                    openLoanManagementForBook(selectedBook); // Schermata gestione prestiti
                } else if (event.getButton() == MouseButton.SECONDARY && !row.isEmpty()) {
                    // Clic destro per aprire il menu contestuale
                    Book selectedBook = row.getItem();
                    bookTable.getSelectionModel().select(selectedBook);
                    bookContextMenu.show(row, event.getScreenX(), event.getScreenY());
                } else {
                    // Nascondi il menu se clicchi fuori
                    bookContextMenu.hide();
                }
            });

            return row;
        });

        // Filtri
        titleFilterField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        authorFilterField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        editorFilterField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        genreFilterField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        priceFilterField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        // Eventi per i pulsanti
        backButton.setOnAction(event -> backButtonAction());
        addBookButton.setOnAction(event -> addBook());
        adminButton.setOnAction(event -> onAdminAreaButtonClick());
        loanManagementButton.setOnAction(event -> onLoanManagementButtonClick());

        // Validazioni
        quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                quantityField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        availableQuantityField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                availableQuantityField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d{0,2})?")) {
                priceField.setText(oldValue);
            }
        });

        publishYearField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                publishYearField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void createBookContextMenu() {
        bookContextMenu = new ContextMenu();

        MenuItem editMenuItem = new MenuItem("Modifica");
        editMenuItem.setOnAction(event -> {
            Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                editBookDetails(selectedBook);
            } else {
                System.out.println("No book selected for editing.");
            }
        });

        MenuItem deleteMenuItem = new MenuItem("Cancella");
        deleteMenuItem.setOnAction(event -> {
            Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                deleteBook(selectedBook);
            } else {
                System.out.println("No book selected for deletion.");
            }
        });

        bookContextMenu.getItems().addAll(editMenuItem, deleteMenuItem);
    }

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

    private void configureTableColumns() {
        bookTable.getColumns().clear();

        double totalWidth = bookTable.getWidth();

        TableColumn<Book, String> titleColumn = new TableColumn<>("Titolo Libro");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(0.2)); // 20% della tabella

        TableColumn<Book, String> authorColumn = new TableColumn<>("Autore");
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(0.15)); // 15% della tabella

        TableColumn<Book, String> editorColumn = new TableColumn<>("Editore");
        editorColumn.setCellValueFactory(new PropertyValueFactory<>("editor"));
        editorColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(0.1)); // 10% della tabella

        TableColumn<Book, String> isbnColumn = new TableColumn<>("ISBN");
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        isbnColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(0.1)); // 15% della tabella

        TableColumn<Book, Integer> quantityColumn = new TableColumn<>("Q. Totale");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(0.1)); // 5% della tabella

        TableColumn<Book, Integer> availableQuantityColumn = new TableColumn<>("Q. Disponibile");
        availableQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("availableQuantity"));
        availableQuantityColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(0.1)); // 5% della tabella

        TableColumn<Book, String> genreColumn = new TableColumn<>("Genere");
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        genreColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(0.1)); // 5% della tabella

        TableColumn<Book, Double> priceColumn = new TableColumn<>("Prezzo (€)");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(0.1)); // 10% della tabella

        TableColumn<Book, Integer> publishYearColumn = new TableColumn<>("Anno");
        publishYearColumn.setCellValueFactory(new PropertyValueFactory<>("publicationYear"));
        publishYearColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(0.05)); // 10% della tabella

        bookTable.getColumns().addAll(
                titleColumn,
                authorColumn,
                isbnColumn,
                quantityColumn,
                availableQuantityColumn,
                genreColumn,
                priceColumn,
                editorColumn,
                publishYearColumn);
    }

    private void applyFilters() {
        String titleFilter = titleFilterField.getText().toLowerCase();
        String authorFilter = authorFilterField.getText().toLowerCase();
        String editorFilter = editorFilterField.getText().toLowerCase();
        String genreFilter = genreFilterField.getText().toLowerCase();
        String priceFilter = priceFilterField.getText().toLowerCase();

        List<Book> filteredBooks = books.stream()
                .filter(book -> book.getTitle().toLowerCase().contains(titleFilter))
                .filter(book -> book.getAuthor().toLowerCase().contains(authorFilter))
                .filter(book -> book.getEditor().toLowerCase().contains(editorFilter))
                .filter(book -> book.getGenre().toLowerCase().contains(genreFilter))
                .filter(book -> book.getPrice() >= Double.parseDouble(priceFilter))
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
        TextField availableQuantityField = new TextField(String.valueOf(book.getAvailableQuantity()));
        TextField genreField = new TextField(book.getGenre());
        TextField priceField = new TextField(String.valueOf(book.getPrice()));
        TextField isbnField = new TextField(book.getIsbn());
        TextField publishYearField = new TextField(String.valueOf(book.getPublicationYear()));

        // Add inputs to the grid
        grid.add(new Label("Titolo:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Autore:"), 0, 1);
        grid.add(authorField, 1, 1);
        grid.add(new Label("Editore:"), 0, 2);
        grid.add(editorField, 1, 2);
        grid.add(new Label("Quantità:"), 0, 3);
        grid.add(quantityField, 1, 3);
        grid.add(new Label("Quantità Disponibile:"), 0, 4);
        grid.add(availableQuantityField, 1, 4);
        grid.add(new Label("Genere:"), 0, 5);
        grid.add(genreField, 1, 5);
        grid.add(new Label("Prezzo:"), 0, 6);
        grid.add(priceField, 1, 6);
        grid.add(new Label("ISBN:"), 0, 7);
        grid.add(isbnField, 1, 7);
        grid.add(new Label("Anno di pubblicazione:"), 0, 8);
        grid.add(publishYearField, 1, 8);

        // Add the grid to the dialog
        dialog.getDialogPane().setContent(grid);

        // Convert the result to a list when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return List.of(
                        titleField.getText(),
                        authorField.getText(),
                        editorField.getText(),
                        quantityField.getText(),
                        availableQuantityField.getText(),
                        genreField.getText(),
                        priceField.getText(),
                        isbnField.getText(),
                        publishYearField.getText()

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
                int newAvailableQuantity = Integer.parseInt(result.get(4));
                String newGenre = result.get(5);
                double newPrice = Double.parseDouble(result.get(6));
                String newIsbn = isbnField.getText();
                int newPublishYear = Integer.parseInt(result.get(8));

                // Update in the database
                MongoDatabase database = DatabaseConnection.getDatabase();
                MongoCollection<Document> booksCollection = database.getCollection("books");

                Document filter = new Document("title", book.getTitle());
                Document update = new Document("$set", new Document("title", newTitle)
                        .append("author", newAuthor)
                        .append("editor", newEditor)
                        .append("quantity", newQuantity)
                        .append("availableQuantity", newAvailableQuantity)
                        .append("genre", newGenre)
                        .append("price", newPrice)
                        .append("isbn", newIsbn)
                        .append("publishYear", newPublishYear));
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

    private void deleteBook(Book book) {
        try {
            System.out.println("Deleting book: " + book.getTitle());
            MongoDatabase database = DatabaseConnection.getDatabase();
            MongoCollection<Document> booksCollection = database.getCollection("books");
            booksCollection.deleteOne(new Document("title", book.getTitle()));
            books.setAll(getBooks());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error deleting book: " + e.getMessage());
        }
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
                int availableQuantity = doc.getInteger("availableQuantity", 0); // Default to 0 if null
                String genre = doc.getString("genre");
                String isbn = doc.getString("isbn");
                int publishYear = doc.getInteger("publishYear", 0); // Default to 0 if null
                double price = doc.containsKey("price") && doc.get("price") != null
                        ? ((Number) doc.get("price")).doubleValue()
                        : 0.0;

                bookList.add(
                        new Book(title, author, quantity, availableQuantity, editor, genre, price, publishYear, isbn));
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
        String genre = genreField.getText();
        String isbn = isbnField.getText();
        int quantity = Integer.parseInt(quantityField.getText());
        int availableQuantity = Integer.parseInt(availableQuantityField.getText());
        int publishYear = Integer.parseInt(publishYearField.getText());
        double price = Double.parseDouble(priceField.getText());

        if (title.isEmpty() || author.isEmpty() || editor.isEmpty() ||
                genre.isEmpty() || isbn.isEmpty() || quantityField.getText().isEmpty() ||
                availableQuantityField.getText().isEmpty() || publishYearField.getText().isEmpty() ||
                priceField.getText().isEmpty()) {
            showAlert("Errore", "Tutti i campi devono essere compilati.");
            return;
        }

        if (isbn.length() != 13) { // Controlla che l'ISBN abbia 13 caratteri
            showAlert("Errore", "Il codice ISBN deve essere lungo 13 caratteri.");
            return;
        }

        try {
            // Validazione e conversione della quantità
            quantity = Integer.parseInt(quantityField.getText());
            if (quantity < 0) {
                showAlert("Errore", "La quantità non può essere negativa.");
                return;
            }

            // Validazione e conversione della quantità disponibile
            availableQuantity = Integer.parseInt(availableQuantityField.getText());
            if (availableQuantity < 0 || availableQuantity > quantity) {
                showAlert("Errore", "La quantità disponibile deve essere compresa tra 0 e la quantità totale.");
                return;
            }

            // Validazione e conversione dell'anno di pubblicazione
            publishYear = Integer.parseInt(publishYearField.getText());
            if (publishYear < 1000 || publishYear > 9999) { // Limiti ragionevoli per un anno
                showAlert("Errore", "L'anno di pubblicazione deve essere un valore valido (ad esempio, 2024).");
                return;
            }

            // Validazione e conversione del prezzo
            price = Double.parseDouble(priceField.getText());
            if (price < 0) {
                showAlert("Errore", "Il prezzo non può essere negativo.");
                return;
            }

        } catch (NumberFormatException e) {
            showAlert("Errore",
                    "Quantità, quantità disponibile, anno di pubblicazione e prezzo devono essere valori numerici validi.");
            return;
        }

        try {
            MongoDatabase database = DatabaseConnection.getDatabase();
            MongoCollection<Document> booksCollection = database.getCollection("books");

            Document newBook = new Document("title", title)
                    .append("author", author)
                    .append("editor", editor)
                    .append("genre", genre)
                    .append("isbn", isbn)
                    .append("publishYear", publishYear)
                    .append("price", price)
                    .append("quantity", quantity)
                    .append("availableQuantity", availableQuantity);
            booksCollection.insertOne(newBook);

            titleField.clear();
            authorField.clear();
            editorField.clear();
            quantityField.clear();
            availableQuantityField.clear();
            genreField.clear();
            isbnField.clear();
            publishYearField.clear();
            priceField.clear();

            // Refresh the TableView
            books.setAll(getBooks());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error adding book: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
