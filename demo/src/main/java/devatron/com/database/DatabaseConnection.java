package devatron.com.database;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class DatabaseConnection {
    private static final String URI = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "library_db";
    private static MongoClient mongoClient = null;

    public static MongoDatabase getDatabase() {
    if (mongoClient == null) {
        ConnectionString connectionString = new ConnectionString(URI);
        mongoClient = MongoClients.create(connectionString);
    }
    return mongoClient.getDatabase(DATABASE_NAME);
}

}
