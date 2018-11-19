package christmas.mongo;

import christmas.models.User;
import com.mongodb.*;

import java.net.UnknownHostException;

public class ConnectToDB {

    public static MongoClient connect() throws UnknownHostException {
        return new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
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

    public static DBCollection getCollection(String name) throws UnknownHostException {
        return getDB("secret_santa").getCollection(name);
    }

    public static BasicDBObject findOne(String field, String value, DBCollection collection) {
        return (BasicDBObject) collection.find(new BasicDBObject(field, value)).next();
    }

    public static BasicDBObject findOne(String field, int value, DBCollection collection) {
        return (BasicDBObject) collection.find(new BasicDBObject(field, value)).next();
    }
}
