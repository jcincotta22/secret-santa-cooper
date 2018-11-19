package christmas;

import christmas.controllers.UserController;
import christmas.errros.ResponseError;
import christmas.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

import static christmas.utils.JsonUtil.toJson;
import static spark.Spark.*;

public class Bootstrap {
    final private static Logger logger = LoggerFactory.getLogger(Bootstrap.class);
    public static void main(String[] args) throws UnknownHostException {
        port(8080);
        new UserController(new UserService());

        after((req, res) -> {
            res.type("application/json");
        });

        exception(IllegalArgumentException.class, (e, req, res) -> {
            res.status(400);
            res.body(toJson(new ResponseError(e).getMessage()));
        });

        exception(ResponseError.class, (e, req, res) -> {
            res.status(400);
            res.body(toJson(new ResponseError(e).getMessage()));
        });

        get("/", (req, res) -> "Hello World");

  }
}
