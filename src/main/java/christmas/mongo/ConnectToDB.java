package christmas.mongo;

import christmas.models.User;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.mongodb.client.model.Filters.eq;

public class ConnectToDB {
    private static final Logger logger = LogManager.getLogger(ConnectToDB.class.getName());
    private static final String dbName = "secret_santa";
    private static ConcurrentHashMap<UUID, MongoClient> connectionMap = new ConcurrentHashMap<>();

    public static MongoClient connect() throws UnknownHostException {
        UUID uuid = UUID.randomUUID();
        MongoClient conn = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        connectionMap.put(uuid, conn);
        return conn;
    }

    private static synchronized void connect(UUID uuid) throws UnknownHostException {
        MongoClient conn = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        connectionMap.put(uuid, conn);
    }

    public static DBObject toDBObject(User user) {
        return new BasicDBObject("name", user.getName())
                .append("email", user.getEmail())
                .append("_id", user.getId())
                .append("parentIds", user.getParentIds())
                .append("username", user.getUsername())
                .append("password", user.getPassword());
    }

    private static DB getDB(String dbName) throws UnknownHostException {
        return ConnectToDB.connect().getDB(dbName);
    }

    private static synchronized MongoDatabase getDB(UUID uuid) throws UnknownHostException {
        ConnectToDB.connect(uuid);
        return connectionMap.get(uuid).getDatabase(dbName);
    }

    public static DBCollection getCollection(String name) throws UnknownHostException {
        return getDB(dbName).getCollection(name);
    }

    public static MongoCollection<Document> getCollection(String name, UUID uuid) throws UnknownHostException {
        return getDB(uuid).getCollection(name);
    }


    public static BasicDBObject findOne(String field, String value, DBCollection collection) {
        return (BasicDBObject) collection.find(new BasicDBObject(field, value)).next();
    }

    public static BasicDBObject findOne(String field, int value, DBCollection collection) {
        return (BasicDBObject) collection.find(new BasicDBObject(field, value)).next();
    }

    public static Document findOne(BasicDBObject query, MongoCollection<Document> collection) {
        return collection.find(query).first();
    }

    public static Document findOne(String field, Integer value, MongoCollection<Document> collection) {
        return collection.find(eq(field, value)).first();
    }

    public static synchronized void closeMongoConnection(UUID uuid) {
        if(connectionMap.containsKey(uuid)) {
            connectionMap.get(uuid).close();
            connectionMap.remove(uuid);
            logger.info("mongo connection " + uuid + " closed");
        }
    }

    public static synchronized void forceCloseAllConnections() {
        for(Map.Entry<UUID, MongoClient> key : connectionMap.entrySet()) {
            connectionMap.get(key.getKey()).close();
            connectionMap.remove(key.getKey());
            logger.info("mongo connection " + key.getKey() + " closed");
        }
    }
}
