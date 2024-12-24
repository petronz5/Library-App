package devatron.com.model;

import javafx.beans.property.*;

public class Book {
    private final StringProperty title;
    private final StringProperty author;
    private final IntegerProperty quantity;
    private final IntegerProperty availableQuantity;
    private final StringProperty editor;
    private final StringProperty genre;
    private final DoubleProperty price;
    private final IntegerProperty publicationYear;
    private final StringProperty isbn;

    public Book(String title, String author, int quantity, int availableQuantity, String editor, String genre,
                double price, int publicationYear, String isbn) {
        this.title = new SimpleStringProperty(title);
        this.author = new SimpleStringProperty(author);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.availableQuantity = new SimpleIntegerProperty(availableQuantity);
        this.editor = new SimpleStringProperty(editor);
        this.genre = new SimpleStringProperty(genre);
        this.price = new SimpleDoubleProperty(price);
        this.publicationYear = new SimpleIntegerProperty(publicationYear);
        this.isbn = new SimpleStringProperty(isbn);
    }

    // Proprietà Title
    public StringProperty titleProperty() { return title; }
    public String getTitle() { return title.get(); }
    public void setTitle(String title) { this.title.set(title); }

    // Proprietà Author
    public StringProperty authorProperty() { return author; }
    public String getAuthor() { return author.get(); }
    public void setAuthor(String author) { this.author.set(author); }

    // Proprietà Quantity
    public IntegerProperty quantityProperty() { return quantity; }
    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }

    // Proprietà AvailableQuantity
    public IntegerProperty availableQuantityProperty() { return availableQuantity; }
    public int getAvailableQuantity() { return availableQuantity.get(); }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity.set(availableQuantity); }

    // Proprietà Editor
    public StringProperty editorProperty() { return editor; }
    public String getEditor() { return editor.get(); }
    public void setEditor(String editor) { this.editor.set(editor); }

    // Proprietà Genre
    public StringProperty genreProperty() { return genre; }
    public String getGenre() { return genre.get(); }
    public void setGenre(String genre) { this.genre.set(genre); }

    // Proprietà Price
    public DoubleProperty priceProperty() { return price; }
    public double getPrice() { return price.get(); }
    public void setPrice(double price) { this.price.set(price); }

    // Proprietà PublicationYear
    public IntegerProperty publicationYearProperty() { return publicationYear; }
    public int getPublicationYear() { return publicationYear.get(); }
    public void setPublicationYear(int publicationYear) { this.publicationYear.set(publicationYear); }

    // Proprietà ISBN
    public StringProperty isbnProperty() { return isbn; }
    public String getIsbn() { return isbn.get(); }
    public void setIsbn(String isbn) { this.isbn.set(isbn); }

}
