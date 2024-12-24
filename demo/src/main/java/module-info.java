module devatron.com {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.driver.core;
    requires org.mongodb.bson;
    requires javafx.base;

    opens devatron.com.controller to javafx.fxml;
    exports devatron.com;
    exports devatron.com.controller;
    opens devatron.com.model to javafx.base;
}