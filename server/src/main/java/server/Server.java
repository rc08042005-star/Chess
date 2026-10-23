
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

        Gson gson = new Gson();

        ClearService clearService =
                new ClearService(userDAO, authDAO, gameDAO);

        UserService userService =
                new UserService(userDAO, authDAO);

        // Endpoint 1: Clear database
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

        // Endpoint 2: Register user
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
                        new AuthResult(
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

        // Endpoint 3: Login user
        javalin.post("/session", ctx -> {
            try {
                LoginRequest request =
                        gson.fromJson(ctx.body(), LoginRequest.class);

                if (request == null) {
                    throw new IllegalArgumentException("bad request");
                }

                var auth = userService.login(
                        request.username(),
                        request.password()
                );

                ctx.status(200).json(
                        new AuthResult(
                                auth.username(),
                                auth.authToken()
                        )
                );

            } catch (IllegalArgumentException e) {
                ctx.status(400).json(
                        new ErrorResult("Error: bad request")
                );

            } catch (SecurityException e) {
                ctx.status(401).json(
                        new ErrorResult("Error: unauthorized")
                );

            } catch (Exception e) {
                ctx.status(500).json(
                        new ErrorResult("Error: " + e.getMessage())
                );
            }
        });

        // Endpoint 4: Logout user
        javalin.delete("/session", ctx -> {
            try {
                String authToken = ctx.header("authorization");

                userService.logout(authToken);

                ctx.status(200);
                ctx.result("{}");

            } catch (SecurityException e) {
                ctx.status(401).json(
                        new ErrorResult("Error: unauthorized")
                );

            } catch (Exception e) {
                ctx.status(500).json(
                        new ErrorResult("Error: " + e.getMessage())
                );
            }
        });
    }

    // Request for registering a user
    private record RegisterRequest(
            String username,
            String password,
            String email
    ) {}

    // Request for logging in
    private record LoginRequest(
            String username,
            String password
    ) {}

    // Successful registration or login response
    private record AuthResult(
            String username,
            String authToken
    ) {}

    // Error response
    private record ErrorResult(
            String message
    ) {}

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
