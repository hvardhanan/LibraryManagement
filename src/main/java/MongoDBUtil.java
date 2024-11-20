import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDBUtil {
    private static final String URI = "mongodb://localhost:27017"; // MongoDB URI
    private static final String DATABASE_NAME = "LibraryDB"; // Database name
    private static MongoDatabase database;

    static {
        try {
            // Establish MongoDB connection using MongoClients.create
            MongoClient mongoClient = MongoClients.create(URI); // MongoClients.create is the preferred method in 5.x.x
            database = mongoClient.getDatabase(DATABASE_NAME); // Get the database
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1); // Exit if there's an error connecting to MongoDB
        }
    }

    public static MongoCollection<Document> getCollection(String collectionName) {
        // Return the collection specified by the collectionName
        return database.getCollection(collectionName);
    }
}
