
package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.Javalin;
import service.ClearService;
import service.GameService;
import service.UserService;

import java.util.List;

public class Server {

    private final Javalin javalin;

    private final UserDAO userDAO = new MemoryUserDAO();
    private final AuthDAO authDAO = new MemoryAuthDAO();
    private final GameDAO gameDAO = new MemoryGameDAO();

    public Server() {

        System.out.println("DEBUG: MY SERVER CONSTRUCTOR IS RUNNING");

        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(new io.javalin.json.JavalinGson());
        });

        Gson gson = new Gson();

        ClearService clearService =
                new ClearService(userDAO, authDAO, gameDAO);

        UserService userService =
                new UserService(userDAO, authDAO);

        GameService gameService =
                new GameService(gameDAO, authDAO);

        // 1. CLEAR DATABASE
        javalin.delete("/db", ctx -> {

            System.out.println("DEBUG: DELETE /db WAS CALLED");

            try {
                clearService.clear();

                System.out.println("DEBUG: Database cleared successfully");

                ctx.status(200)
                        .contentType("application/json")
                        .result("{}");

            } catch (Exception e) {

                System.out.println("DEBUG: Clear failed: " + e.getMessage());
                e.printStackTrace();

                ctx.status(500).json(
                        new ErrorResult("Error: " + e.getMessage())
                );
            }
        });

        // 2. REGISTER USER
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
                        new AuthResult(auth.username(), auth.authToken())
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

        // 3. LOGIN USER
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
                        new AuthResult(auth.username(), auth.authToken())
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

        // 4. LOGOUT USER
        javalin.delete("/session", ctx -> {
            try {
                userService.logout(ctx.header("authorization"));

                ctx.status(200).result("{}");

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

        // 5. LIST GAMES
        javalin.get("/game", ctx -> {
            try {
                var games = gameService.listGames(
                        ctx.header("authorization")
                );

                List<GameSummary> summaries = games.stream()
                        .map(game -> new GameSummary(
                                game.gameID(),
                                game.whiteUsername(),
                                game.blackUsername(),
                                game.gameName()
                        ))
                        .toList();

                ctx.status(200).json(
                        new GamesResult(summaries)
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

        // 6. CREATE GAME
        javalin.post("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");

                CreateGameRequest request =
                        gson.fromJson(ctx.body(), CreateGameRequest.class);

                if (request == null) {
                    throw new IllegalArgumentException("bad request");
                }

                int gameID = gameService.createGame(
                        authToken,
                        request.gameName()
                );

                ctx.status(200).json(
                        new CreateGameResult(gameID)
                );

            } catch (SecurityException e) {
                ctx.status(401).json(
                        new ErrorResult("Error: unauthorized")
                );

            } catch (IllegalArgumentException e) {
                ctx.status(400).json(
                        new ErrorResult("Error: bad request")
                );

            } catch (Exception e) {
                ctx.status(500).json(
                        new ErrorResult("Error: " + e.getMessage())
                );
            }
        });

        // 7. JOIN GAME
        javalin.put("/game", ctx -> {
            try {
                String authToken = ctx.header("authorization");

                JoinGameRequest request =
                        gson.fromJson(ctx.body(), JoinGameRequest.class);

                if (request == null || request.gameID() == null) {
                    throw new IllegalArgumentException("bad request");
                }

                gameService.joinGame(
                        authToken,
                        request.playerColor(),
                        request.gameID()
                );

                ctx.status(200).result("{}");

            } catch (SecurityException e) {
                ctx.status(401).json(
                        new ErrorResult("Error: unauthorized")
                );

            } catch (IllegalStateException e) {
                ctx.status(403).json(
                        new ErrorResult("Error: already taken")
                );

            } catch (IllegalArgumentException e) {
                ctx.status(400).json(
                        new ErrorResult("Error: bad request")
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

    private record LoginRequest(
            String username,
            String password
    ) {}

    private record AuthResult(
            String username,
            String authToken
    ) {}

    private record GameSummary(
            int gameID,
            String whiteUsername,
            String blackUsername,
            String gameName
    ) {}

    private record GamesResult(
            List<GameSummary> games
    ) {}

    private record CreateGameRequest(
            String gameName
    ) {}

    private record CreateGameResult(
            int gameID
    ) {}

    private record JoinGameRequest(
            String playerColor,
            Integer gameID
    ) {}

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
