package devatron.com.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Book {
    private final StringProperty title;
    private final StringProperty author;
    private final IntegerProperty quantity;
    private final StringProperty editor;

    public Book(String title, String author, int quantity, String editor) {
        this.title = new SimpleStringProperty(title);
        this.author = new SimpleStringProperty(author);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.editor = new SimpleStringProperty(editor);
    }

    // Title property
    public StringProperty titleProperty() {
        return title;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    // Author property
    public StringProperty authorProperty() {
        return author;
    }

    public String getAuthor() {
        return author.get();
    }

    public void setAuthor(String author) {
        this.author.set(author);
    }

    // Quantity property
    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    // Editor property
    public StringProperty editorProperty() {
        return editor;
    }

    public String getEditor() {
        return editor.get();
    }

    public void setEditor(String editor) {
        this.editor.set(editor);
    }
}
