
package server;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.GameService;

import java.util.List;

public class GameHandler {

    private final GameService gameService;
    private final Gson gson = new Gson();

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void listGames(Context ctx) {
        try {
            var games = gameService.listGames(
                    ctx.header("authorization")
            );

            List<ApiModels.GameSummary> summaries = games.stream()
                    .map(game -> new ApiModels.GameSummary(
                            game.gameID(),
                            game.whiteUsername(),
                            game.blackUsername(),
                            game.gameName()
                    ))
                    .toList();

            ctx.status(200).json(
                    new ApiModels.GamesResult(summaries)
            );

        } catch (SecurityException e) {
            sendError(ctx, 401, "unauthorized");

        } catch (Exception e) {
            sendError(ctx, 500, e.getMessage());
        }
    }

    public void createGame(Context ctx) {
        try {
            String authToken = ctx.header("authorization");

            ApiModels.CreateGameRequest request =
                    gson.fromJson(
                            ctx.body(),
                            ApiModels.CreateGameRequest.class
                    );

            if (request == null) {
                throw new IllegalArgumentException("bad request");
            }

            int gameID = gameService.createGame(
                    authToken,
                    request.gameName()
            );

            ctx.status(200).json(
                    new ApiModels.CreateGameResult(gameID)
            );

        } catch (SecurityException e) {
            sendError(ctx, 401, "unauthorized");

        } catch (IllegalArgumentException e) {
            sendError(ctx, 400, "bad request");

        } catch (Exception e) {
            sendError(ctx, 500, e.getMessage());
        }
    }

    public void joinGame(Context ctx) {
        try {
            String authToken = ctx.header("authorization");

            ApiModels.JoinGameRequest request =
                    gson.fromJson(
                            ctx.body(),
                            ApiModels.JoinGameRequest.class
                    );

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
            sendError(ctx, 401, "unauthorized");

        } catch (IllegalStateException e) {
            sendError(ctx, 403, "already taken");

        } catch (IllegalArgumentException e) {
            sendError(ctx, 400, "bad request");

        } catch (Exception e) {
            sendError(ctx, 500, e.getMessage());
        }
    }

    private void sendError(Context ctx, int status, String message) {
        ctx.status(status).json(
                new ApiModels.ErrorResult("Error: " + message)
        );
    }
}
