
package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public class GameService {

    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    // Endpoint 5: List games
    public Collection<GameData> listGames(String authToken)
            throws DataAccessException {

        validateAuth(authToken);
        return gameDAO.listGames();
    }

    // Endpoint 6: Create game
    public int createGame(String authToken, String gameName)
            throws DataAccessException {

        validateAuth(authToken);

        if (gameName == null || gameName.isBlank()) {
            throw new IllegalArgumentException("bad request");
        }

        GameData game = new GameData(
                0,
                null,
                null,
                gameName,
                new ChessGame()
        );

        return gameDAO.createGame(game);
    }

    // Endpoint 7: Join game
    public void joinGame(String authToken, String playerColor, int gameID)
            throws DataAccessException {

        AuthData auth = validateAuth(authToken);

        if (playerColor == null ||
                (!playerColor.equals("WHITE") &&
                        !playerColor.equals("BLACK"))) {
            throw new IllegalArgumentException("bad request");
        }

        GameData game = gameDAO.getGame(gameID);

        if (game == null) {
            throw new IllegalArgumentException("bad request");
        }

        String username = auth.username();

        if (playerColor.equals("WHITE")) {
            if (game.whiteUsername() != null) {
                throw new IllegalStateException("already taken");
            }

            game = new GameData(
                    game.gameID(),
                    username,
                    game.blackUsername(),
                    game.gameName(),
                    game.game()
            );

        } else {
            if (game.blackUsername() != null) {
                throw new IllegalStateException("already taken");
            }

            game = new GameData(
                    game.gameID(),
                    game.whiteUsername(),
                    username,
                    game.gameName(),
                    game.game()
            );
        }

        gameDAO.updateGame(game);
    }

    // Validate user authentication
    private AuthData validateAuth(String authToken)
            throws DataAccessException {

        if (authToken == null || authToken.isBlank()) {
            throw new SecurityException("unauthorized");
        }

        AuthData auth = authDAO.getAuth(authToken);

        if (auth == null) {
            throw new SecurityException("unauthorized");
        }

        return auth;
    }
}
