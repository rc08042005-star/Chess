
package service;

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

    // Get all games for an authenticated user
    public Collection<GameData> listGames(String authToken)
            throws DataAccessException {

        if (authToken == null || authToken.isBlank()
                || authDAO.getAuth(authToken) == null) {
            throw new SecurityException("unauthorized");
        }

        return gameDAO.listGames();
    }
}
