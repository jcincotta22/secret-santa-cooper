package christmas.services;

import christmas.errros.ResponseError;
import christmas.mongo.ConnectToDB;
import christmas.models.User;
import christmas.utils.JsonUtil;
import christmas.utils.ListUtils;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.bson.Document;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;

public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class.getName());

    private static List<User> cachedUserList;

    public UserService() throws UnknownHostException {
        cachedUserList = getAllUsers();
    }

    private static void updateCounter(int postSequence, DBCollection counter, BasicDBObject collectionToUpdate) {
        try {
            BasicDBObject updated = collectionToUpdate.append("sequence_value", postSequence);
            BasicDBObject originalCollection = ConnectToDB.findOne("_id", "users", counter);
            logger.debug("Counter updated to " + postSequence);
            counter.update(originalCollection, updated);
        } finally {
            ConnectToDB.forceCloseAllConnections();
        }
    }

    public Document getUser(int id) throws UnknownHostException {
        UUID uuid = UUID.randomUUID();
        try {
            MongoCollection<Document> collection = ConnectToDB.getCollection("Users", uuid);
            return ConnectToDB.findOne(new BasicDBObject("_id", id), collection);
        } catch (Exception e) {
            logger.error("mongo error", e);
            throw e;
        } finally {
            ConnectToDB.closeMongoConnection(uuid);
        }
    }

    public Document getUserWithPassword(int id, String credentials) throws UnknownHostException, ResponseError {
        Document dbUser = getUser(id);
        logger.info("found user with id:" + id);
        User user = JsonUtil.jsonToObject(JsonUtil.toJson(dbUser), User.class);
        if(!credentials.equals(user.getUsername() + ":" + user.getPassword())) {
            throw new ResponseError("You do not have permissions");
        }

        return dbUser;
    }

    public DBObject createUser(String name, String email, List<Integer> parentIds, String username, String password) throws UnknownHostException {
        try {
            DBCollection userCollection = ConnectToDB.getCollection("Users");
            DBCollection counter = ConnectToDB.getCollection("counters");
            BasicDBObject userSequence = ConnectToDB.findOne("_id", "users", counter);
            int id = userSequence.getInt("sequence_value");
            User user = new User(id, name, email, parentIds, username, password);
            DBObject newUser = ConnectToDB.toDBObject(user);
            userCollection.insert(newUser);

            logger.debug("User created with name, {}, and email, {}");
            updateCounter(id + 1, counter, userSequence);

            return newUser;
        } catch (Exception e) {
            logger.error(e);
            ConnectToDB.forceCloseAllConnections();
            throw e;
        } finally {
            ConnectToDB.forceCloseAllConnections();
        }
    }

    public List<UserResource> findAll() throws IOException {
        List<UserResource> userResourceList = new ArrayList<>();

        for(User user : getAllUsers()) {
            userResourceList.add(UserResource.from(user));
        }
        logger.debug("Successfully Retrieved all users");

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
        logger.debug("Sorting all ids");
        Collections.sort(userIds);
        Collections.sort(secretSantaIds);
        userIdMap.put("userIds", userIds);
        userIdMap.put("ssIds", secretSantaIds);
        logger.debug("Successfully Sorted ids");
        return userIdMap;
    }



    private void updateAllCachedUsers(User user) throws UnknownHostException {
        UUID uuid = UUID.randomUUID();
        if(cachedUserList.size() == 1 && cachedUserList.get(0).getId() == user.getId()) {
            resetCache();
            logger.debug("Updating cached users");
            updateAllCachedUsers(cachedUserList.get(0));
        } else if(cachedUserList.size() != 0) {
            ListUtils listUtils = new ListUtils();
            List<Integer> parentIdsPlusOwnId = user.getParentIds();
            parentIdsPlusOwnId.add(user.getId());

            if(noAvailableIds(cachedUserList, parentIdsPlusOwnId)) {
                resetCache();
                logger.debug("Updating cached users");
                updateAllCachedUsers(cachedUserList.get(0));
            } else {
                int id = listUtils.randomIndexNoRepeat(cachedUserList, parentIdsPlusOwnId);
                user.setSecretSanta(id);
                try {
                    MongoCollection<Document> usersCollection = ConnectToDB.getCollection("Users", uuid);
                    Document dbUser = getUser(user.getId());

                    updateUserSecretSanta(user, usersCollection, dbUser, "secretSanta");
                    User updatedUser = JsonUtil.jsonToObject(JsonUtil.toJson(getUser(id)), User.class);
                    updateAllCachedUsers(updatedUser);
                } catch (Exception e) {
                    logger.error("mongo error", e);
                    throw e;
                } finally {
                    ConnectToDB.closeMongoConnection(uuid);
                }

                }
        } else {
            Map<String, List<Integer>> allIds = getAllSecretSantaIds();
            if(checkForDups(allIds.get("ssIds"))) {
                resetCache();
                logger.debug("Updating cached users");
                updateAllCachedUsers(cachedUserList.get(0));
            } else {
                logger.debug("Updating cached users, Successfully set secret santas");
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
        try {
            DBCollection userCollection = ConnectToDB.getCollection("Users");

            List<User> userList = new ArrayList<>();

            BasicDBObject basicDBObject = new BasicDBObject();
            basicDBObject.put("name", 1);
            DBCursor dbCursor = userCollection.find().sort(basicDBObject);
            while (dbCursor.hasNext()) {
                User user = JsonUtil.jsonToObject(JsonUtil.toJson(dbCursor.next()), User.class);
                userList.add(user);
            }

            return userList;

        } catch (Exception e) {
            logger.error(e);
            ConnectToDB.forceCloseAllConnections();
            throw e;
        } finally {
            ConnectToDB.forceCloseAllConnections();
        }
    }

    private void updateUserSecretSanta(User user, MongoCollection<Document> userCollection, Document collectionToUpdate, String field) throws UnknownHostException {
        Document updated = collectionToUpdate.append(field, user.getSecretSanta());
        logger.debug("Secret Santa updated to " + user.getSecretSanta());
        userCollection.updateOne(eq("_id", user.getId()), new Document("$set", updated));
    }

    public String sendSecretSantaName(Document secretSanta) {
        return JsonUtil.jsonToObject(JsonUtil.toJson(secretSanta), User.class).getName();
    }

    private static void resetCache() throws UnknownHostException {
        cachedUserList = getAllUsers();
    }
}
