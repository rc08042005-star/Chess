
package server;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;

import dataaccess.sql.SQLAuthDAO;
import dataaccess.sql.SQLDatabase;
import dataaccess.sql.SQLGameDAO;
import dataaccess.sql.SQLUserDAO;

import io.javalin.Javalin;
import io.javalin.json.JavalinGson;

import service.ClearService;
import service.GameService;
import service.UserService;

public class Server {

    private final Javalin javalin;

    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public Server() {
        try {
            // Create the database tables if they do not exist.
            SQLDatabase.initialize();

            // Use MySQL instead of in-memory storage.
            userDAO = new SQLUserDAO();
            authDAO = new SQLAuthDAO();
            gameDAO = new SQLGameDAO();

        } catch (DataAccessException e) {
            throw new RuntimeException(
                    "Failed to initialize Chess database", e);
        }

        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(new JavalinGson());
        });

        ClearService clearService =
                new ClearService(userDAO, authDAO, gameDAO);

        UserService userService =
                new UserService(userDAO, authDAO);

        GameService gameService =
                new GameService(gameDAO, authDAO);

        registerRoutes(clearService, userService, gameService);
    }

    private void registerRoutes(
            ClearService clearService,
            UserService userService,
            GameService gameService) {

        ClearHandler clearHandler =
                new ClearHandler(clearService);

        UserHandler userHandler =
                new UserHandler(userService);

        GameHandler gameHandler =
                new GameHandler(gameService);

        javalin.delete("/db", clearHandler::clear);

        javalin.post("/user", userHandler::register);
        javalin.post("/session", userHandler::login);
        javalin.delete("/session", userHandler::logout);

        javalin.get("/game", gameHandler::listGames);
        javalin.post("/game", gameHandler::createGame);
        javalin.put("/game", gameHandler::joinGame);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
