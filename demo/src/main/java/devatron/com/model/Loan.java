package devatron.com.model;

import java.time.LocalDate;

public class Loan {
    private final String bookTitle;
    private final String username;
    private final String firstName;  // Nome
    private final String lastName;   // Cognome
    private final String email;
    private final String phoneNumber;
    private final LocalDate loanDate;
    private final LocalDate returnDate;

    public Loan(String bookTitle, String username, String firstName, String lastName, String email, String phoneNumber, LocalDate loanDate, LocalDate returnDate) {
        this.bookTitle = bookTitle;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.loanDate = loanDate;
        this.returnDate = returnDate;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }
}
