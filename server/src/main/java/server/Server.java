
package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.Javalin;
import service.ClearService;
import service.UserService;

public class Server {

    private final Javalin javalin;

    private final UserDAO userDAO = new MemoryUserDAO();
    private final AuthDAO authDAO = new MemoryAuthDAO();
    private final GameDAO gameDAO = new MemoryGameDAO();

    public Server() {
        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(new io.javalin.json.JavalinGson());
        });

        // Clear database
        ClearService clearService =
                new ClearService(userDAO, authDAO, gameDAO);

        javalin.delete("/db", ctx -> {
            try {
                clearService.clear();
                ctx.status(200);
                ctx.result("{}");
            } catch (Exception e) {
                ctx.status(500).json(
                        new ErrorResult("Error: " + e.getMessage())
                );
            }
        });

        // Register a new user
        UserService userService = new UserService(userDAO, authDAO);
        Gson gson = new Gson();

        javalin.post("/user", ctx -> {
            try {
                RegisterRequest request =
                        gson.fromJson(ctx.body(), RegisterRequest.class);

                if (request == null) {
                    throw new IllegalArgumentException("bad request");
                }

                var auth = userService.register(
                        request.username(),
                        request.password(),
                        request.email()
                );

                ctx.status(200).json(
                        new RegisterResult(
                                auth.username(),
                                auth.authToken()
                        )
                );

            } catch (IllegalArgumentException e) {
                ctx.status(400).json(
                        new ErrorResult("Error: bad request")
                );

            } catch (IllegalStateException e) {
                ctx.status(403).json(
                        new ErrorResult("Error: already taken")
                );

            } catch (Exception e) {
                ctx.status(500).json(
                        new ErrorResult("Error: " + e.getMessage())
                );
            }
        });
    }

    private record RegisterRequest(
            String username,
            String password,
            String email
    ) {}

    private record RegisterResult(
            String username,
            String authToken
    ) {}

    private record ErrorResult(String message) {}

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
