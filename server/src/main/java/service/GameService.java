
package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;

import java.util.Collection;

public class GameService {

    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    // Endpoint 5: List all games
    public Collection<GameData> listGames(String authToken)
            throws DataAccessException {

        validateAuth(authToken);
        return gameDAO.listGames();
    }

    // Endpoint 6: Create a new game
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

    // Verify that the user is authenticated
    private void validateAuth(String authToken)
            throws DataAccessException {

        if (authToken == null || authToken.isBlank()
                || authDAO.getAuth(authToken) == null) {
            throw new SecurityException("unauthorized");
        }
    }
}
