package christmas;

import christmas.controllers.UserController;
import christmas.errros.ResponseError;
import christmas.mongo.ConnectToDB;
import christmas.services.UserService;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.bson.Document;

import java.net.UnknownHostException;
import java.util.UUID;

import static christmas.utils.JsonUtil.toJson;
import static spark.Spark.*;

public class Bootstrap {
    private static final Logger logger = LogManager.getLogger(Bootstrap.class.getName());

    public static void main(String[] args) throws UnknownHostException {
        UUID uuid = UUID.randomUUID();
        MongoCollection<Document> userCollection = ConnectToDB.getCollection("Users", uuid);
        IndexOptions options = new IndexOptions();
        options.background(true);
        userCollection.createIndex(Indexes.ascending("name"), options);

        port(8080);

        options("/*",
                (request, response) -> {

                    String accessControlRequestHeaders = request
                            .headers("Access-Control-Request-Headers");
                    if (accessControlRequestHeaders != null) {
                        response.header("Access-Control-Allow-Headers",
                                accessControlRequestHeaders);
                    }

                    String accessControlRequestMethod = request
                            .headers("Access-Control-Request-Method");
                    if (accessControlRequestMethod != null) {
                        response.header("Access-Control-Allow-Methods",
                                accessControlRequestMethod);
                    }

                    return "OK";
                });

        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));

        UserController.initUserEndpoints(new UserService());

        after((req, res) -> {
            res.type("application/json");
            logger.debug(req.headers());
            logger.debug(res.status());
        });

        exception(IllegalArgumentException.class, (e, req, res) -> {
            res.status(400);
            logger.error(new ResponseError(e).getMessage());
            res.body(toJson(new ResponseError(e).getMessage()));
        });

        exception(ResponseError.class, (e, req, res) -> {
            res.status(400);
            logger.error(new ResponseError(e).getMessage());
            res.body(toJson(new ResponseError(e).getMessage()));
        });

        get("/", (req, res) -> "Hello World");

    }
}
