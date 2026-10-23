
package server;

import io.javalin.Javalin;
import dataaccess.*;
import service.ClearService;

public class Server {

    private final Javalin javalin;

    private final UserDAO userDAO = new MemoryUserDAO();
    private final AuthDAO authDAO = new MemoryAuthDAO();
    private final GameDAO gameDAO = new MemoryGameDAO();

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        ClearService clearService =
                new ClearService(userDAO, authDAO, gameDAO);

        javalin.delete("/db", ctx -> {
            clearService.clear();
            ctx.status(200);
            ctx.result("{}");
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
