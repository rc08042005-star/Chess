
package server;

import java.util.List;

public final class ApiModels {

    private ApiModels() {
    }

    public record RegisterRequest(
            String username,
            String password,
            String email
    ) {}

    public record LoginRequest(
            String username,
            String password
    ) {}

    public record AuthResult(
            String username,
            String authToken
    ) {}

    public record GameSummary(
            int gameID,
            String whiteUsername,
            String blackUsername,
            String gameName
    ) {}

    public record GamesResult(
            List<GameSummary> games
    ) {}

    public record CreateGameRequest(
            String gameName
    ) {}

    public record CreateGameResult(
            int gameID
    ) {}

    public record JoinGameRequest(
            String playerColor,
            Integer gameID
    ) {}

    public record ErrorResult(
            String message
    ) {}
}
