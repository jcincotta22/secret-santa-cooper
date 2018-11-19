package christmas.controllers;

import christmas.errros.ResponseError;
import christmas.models.User;
import christmas.services.UserRequestBody;
import christmas.services.UserService;
import christmas.utils.JsonUtil;
import com.mongodb.DBObject;
import spark.Request;
import spark.Response;
import spark.Route;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import java.net.UnknownHostException;
import java.util.Base64;

import static christmas.utils.JsonUtil.json;
import static spark.Spark.*;

public class UserController {
    final private static Logger logger = LogManager.getLogger(UserController.class.getName());

    public UserController(final UserService userService) {

        get("api/users/:id", new Route() {
            @Override
            public Object handle(Request request, Response response) throws ResponseError, UnknownHostException {
                // process request
                String auth = request.headers("Authorization");
                String b64Credentials = auth.substring("Basic".length()).trim();
                String credentials = new String(Base64.getDecoder().decode(b64Credentials));
                try {
                    DBObject user = userService.getUserWithPassword(
                            Integer.parseInt(request.params(":id")),
                            credentials
                    );

                    if(user != null) {
                        User user1 = JsonUtil.jsonToObject(JsonUtil.toJson(user), User.class);
                        return userService.sendSecretSantaName(userService.getUser(user1.getSecretSanta()));
                    }
                    response.status(400);
                    logger.error(new ResponseError("No user with id '%s' found", request.params(":id")));
                    throw new ResponseError("No user with id '%s' found", request.params(":id"));

                } catch(ResponseError e) {
                    response.status(400);
                    logger.error(e.getMessage());
                    throw e;
                } catch(UnknownHostException e) {
                    response.status(500);
                    logger.error(e.getMessage());
                    throw e;
                }
            }
        }, json());

        get("/api/users", (req, res) -> userService.findAll(), json());

        get("/secret-ids", (req, res) -> userService.getAllSecretSantaIds(), json());

        post("/user", (req, res) -> {
            String body = req.body();
            UserRequestBody userRequestBody = JsonUtil.jsonToObject(body, UserRequestBody.class);
            return userService.createUser(
                    userRequestBody.getName(),
                    userRequestBody.getEmail(),
                    userRequestBody.getParentIds(),
                    userRequestBody.getUsername(),
                    userRequestBody.getPassword()
            );
        }, json());

        post("/selection", (req, res) -> {
            userService.setSecretSanta();
            return "Selection Made";
        });

    }
}