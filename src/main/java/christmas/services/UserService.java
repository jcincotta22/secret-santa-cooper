package christmas.services;

import christmas.errros.ResponseError;
import christmas.mongo.ConnectToDB;
import christmas.models.User;
import christmas.utils.JsonUtil;
import christmas.utils.ListUtils;
import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

public class UserService {
    final private static Logger logger = LoggerFactory.getLogger(UserService.class);
    private static List<User> cachedUserList;

    public UserService() throws UnknownHostException {
        cachedUserList = getAllUsers();
    }

    private static void updateCounter(int postSequence, DBCollection counter, BasicDBObject collectionToUpdate) {
        BasicDBObject updated = collectionToUpdate.append("sequence_value", postSequence);
        BasicDBObject originalCollection = ConnectToDB.findOne("_id", "users", counter);
        logger.debug("Counter updated to {}", postSequence);
        counter.update(originalCollection, updated);
    }

    public DBObject getUser(int id) throws UnknownHostException {
        MongoClient mongoClient = ConnectToDB.connect();
        DB database = mongoClient.getDB("secret_santa");
        DBCollection collection = database.getCollection("Users");
        DBObject query = new BasicDBObject("_id", id);
        logger.debug("Successfully retrieved user with id {}", id);

        return collection.find(query).one();
    }

    public DBObject getUserWithPassword(int id, String credentials) throws UnknownHostException, ResponseError {
        MongoClient mongoClient = ConnectToDB.connect();
        DB database = mongoClient.getDB("secret_santa");
        DBCollection collection = database.getCollection("Users");
        DBObject query = new BasicDBObject("_id", id);
        logger.debug("Successfully retrieved user with id {}", id);
        DBObject dbUser = collection.find(query).one();
        User user = JsonUtil.jsonToObject(JsonUtil.toJson(dbUser), User.class);
        if(!credentials.equals(user.getUsername() + ":" + user.getPassword())) {
            throw new ResponseError("You do not have permissions");
        }

        return dbUser;
    }

    public DBObject createUser(String name, String email, ArrayList<Integer> parentIds, String username, String password) throws UnknownHostException {
        DBCollection userCollection = ConnectToDB.getCollection("Users");
        DBCollection counter = ConnectToDB.getCollection("counters");
        BasicDBObject userSequence = ConnectToDB.findOne("_id", "users", counter);
        int id  = userSequence.getInt("sequence_value");
        User user = new User(id, name, email, parentIds, username, password);
        DBObject newUser = ConnectToDB.toDBObject(user);
        userCollection.insert(newUser);

        logger.debug("User created with name, {}, and email, {}", user.getName(), user.getEmail());
        updateCounter(id + 1, counter, userSequence);

        return newUser;
    }

    public List<UserResource> findAll() throws IOException {
        List<UserResource> userResourceList = new ArrayList<>();

        for(User user : getAllUsers()) {
            userResourceList.add(UserResource.from(user));
        }
        return userResourceList;
    }

    public Map<String, List<Integer>> getAllSecretSantaIds() throws UnknownHostException {
        List<User> users = getAllUsers();
        List<Integer> userIds = new ArrayList<>();
        List<Integer> secretSantaIds = new ArrayList<>();
        Map<String, List<Integer>> userIdMap = new HashMap<>();
        for (User user : users) {
            secretSantaIds.add(user.getSecretSanta());
            userIds.add(user.getId());
        }

        Collections.sort(userIds);
        Collections.sort(secretSantaIds);
        userIdMap.put("userIds", userIds);
        userIdMap.put("ssIds", secretSantaIds);

        return userIdMap;
    }



    private void updateAllCachedUsers(User user) throws UnknownHostException {
        if(cachedUserList.size() == 1 && cachedUserList.get(0).getId() == user.getId()) {
            resetCache();
            updateAllCachedUsers(cachedUserList.get(0));
        } else if(cachedUserList.size() != 0) {
            ListUtils listUtils = new ListUtils();
            ArrayList<Integer> parentIdsPlusOwnId = user.getParentIds();
            parentIdsPlusOwnId.add(user.getId());

            if(noAvailableIds(cachedUserList, parentIdsPlusOwnId)) {
                resetCache();
                updateAllCachedUsers(cachedUserList.get(0));
            } else {
                int id = listUtils.randomIndexNoRepeat(cachedUserList, parentIdsPlusOwnId);
                user.setSecretSanta(id);
                DBCollection usersCollection = ConnectToDB.getCollection("Users");
                BasicDBObject collectionToUpdate = ConnectToDB.findOne("_id", user.getId(), usersCollection);
                updateUserSecretSanta(user, usersCollection, collectionToUpdate, "secretSanta");
                User updatedUser = JsonUtil.jsonToObject(JsonUtil.toJson(getUser(id)), User.class);
                updateAllCachedUsers(updatedUser);
            }
        } else {
            Map<String, List<Integer>> allIds = getAllSecretSantaIds();
            if(checkForDups(allIds.get("ssIds"))) {
                resetCache();
                updateAllCachedUsers(cachedUserList.get(0));
            } else {
                resetCache();
            }
        }
    }

    private boolean checkForDups (List<Integer> intList) {
        Map<Integer, Integer> intMap = new HashMap<>();
        for (Integer el : intList) {
            if(intMap.containsKey(el)) {
                return true;
            } else {
                intMap.put(el, el);
            }
        }
        return false;
    }

    private boolean noAvailableIds(List<User> cachedUserList, List<Integer> parentIds ) {
        for (User user : cachedUserList) {
            if(!ListUtils.includes(user.getId(), parentIds)){
                return false;
            }
        }
        return true;
    }

    public void setSecretSanta() throws UnknownHostException {
        resetCache();
        List<User> userList = getAllUsers();
        User user = userList.get(0);
        updateAllCachedUsers(user);
    }

    private static List<User> getAllUsers() throws UnknownHostException {
        DBCollection userCollection = ConnectToDB.getCollection("Users");

        List<User> userList = new ArrayList<>();

        DBCursor dbCursor = userCollection.find();
        while (dbCursor.hasNext()) {
            User user = JsonUtil.jsonToObject(JsonUtil.toJson(dbCursor.next()), User.class);
            userList.add(user);
        }

        return userList;
    }

    private static void updateUserSecretSanta(User user, DBCollection userCollection, BasicDBObject collectionToUpdate, String field) {
        BasicDBObject updated = collectionToUpdate.append(field, user.getSecretSanta());
        BasicDBObject originalCollection = ConnectToDB.findOne("_id", user.getId(), userCollection);
        logger.debug("Secret Santa updated to {}", user.getSecretSanta());
        userCollection.update(originalCollection, updated);
    }

    public String sendSecretSantaName(DBObject secretSanta) {
        return JsonUtil.jsonToObject(JsonUtil.toJson(secretSanta), User.class).getName();
    }

    private static void resetCache() throws UnknownHostException {
        cachedUserList = getAllUsers();
    }
}
