package devatron.com;

import devatron.com.controller.BookController;
import devatron.com.controller.LoanManagementController;
import devatron.com.controller.LoginController;
import devatron.com.controller.UserLoanController;
import devatron.com.model.Book;
import devatron.com.model.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;


import java.io.IOException;

public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        // Mostra la schermata di login iniziale
        showLoginScreen();
    }

    public static void showLoginScreen() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/devatron/com/LoginView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void showBookScreen(User user) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/devatron/com/BookView.fxml"));
        Parent root = loader.load();
        BookController controller = loader.getController();
        controller.setCurrentUser (user); // Passa l'utente al controller
    
        // Usa primaryStage per impostare la nuova scena
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Book List");

        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void showLoanManagementScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/devatron/com/LoanManagementView.fxml"));
        Parent root = loader.load();
        Stage loanStage = new Stage(); // Crea un nuovo Stage
        loanStage.setScene(new Scene(root));
        loanStage.setTitle("Loan Management");
        loanStage.show();
    }

    public static void showAdminScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/devatron/com/AdminView.fxml"));
        Parent root = loader.load();
        Stage adminStage = new Stage(); // Crea un nuovo Stage
        adminStage.setScene(new Scene(root));
        adminStage.setTitle("Admin - Richieste di Registrazione");
        adminStage.show();
    }

    public static void showLoanManagementScreenWithBook(Book book, User user) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/devatron/com/users_make_loan.fxml"));
        Parent root = loader.load();

        UserLoanController controller = loader.getController(); // Cambia LoanManagementController in UserLoanController
        controller.setBook(book);
        controller.setCurrentUser (user); // Passa l'utente al controller

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Loan Management");
        stage.show();
    }


    public static void main(String[] args) {
        launch();
    }
}
