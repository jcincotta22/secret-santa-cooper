package christmas;

import christmas.controllers.UserController;
import christmas.errros.ResponseError;
import christmas.services.UserService;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import java.net.UnknownHostException;

import static christmas.utils.JsonUtil.toJson;
import static spark.Spark.*;

public class Bootstrap {
    final private static Logger logger = LogManager.getLogger(Bootstrap.class.getName());

    public static void main(String[] args) throws UnknownHostException {
        port(8080);
        new UserController(new UserService());

        after((req, res) -> {
            res.type("application/json");
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
