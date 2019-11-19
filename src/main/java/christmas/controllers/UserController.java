package christmas.controllers;

import christmas.errros.ResponseError;
import christmas.models.User;
import christmas.services.UserRequestBody;
import christmas.services.UserService;
import christmas.utils.JsonUtil;
import org.bson.Document;
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
    private static final Logger logger = LogManager.getLogger(UserController.class.getName());
    private final UserService userService;

    private UserController(final UserService userService) {
        this.userService = userService;
    }

    public static void initUserEndpoints(final UserService userService) {
        UserController userController = new UserController(userService);
        userController.initEndpoints();
        logger.info("User endpoints successfully initialized");
    }

    private void initEndpoints() {
        get("api/users/:id", new Route() {
            @Override
            public Object handle(Request request, Response response) throws ResponseError, UnknownHostException {
                logger.debug("Authorizing user");
                // process request
                String auth = request.headers("Authorization");
                String b64Credentials = auth.substring("Basic".length()).trim();
                String credentials = new String(Base64.getDecoder().decode(b64Credentials));
                try {
                    Document user = userService.getUserWithPassword(
                            Integer.parseInt(request.params(":id")),
                            credentials
                    );

                    if(user != null) {
                        logger.debug("found user, sending secret santa");
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
            logger.debug("create user with name " + userRequestBody.getName());
            return userService.createUser(
                    userRequestBody.getName(),
                    userRequestBody.getEmail(),
                    userRequestBody.getParentIds(),
                    userRequestBody.getUsername(),
                    userRequestBody.getPassword()
            );
        }, json());

        post("/selection", (req, res) -> {
            logger.debug("starting secret santa selection");
            userService.setSecretSanta();
            return "Selection Made";
        });

    }
}
