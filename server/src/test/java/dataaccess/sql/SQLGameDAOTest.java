
package dataaccess.sql;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class SQLGameDAOTest {

    private GameDAO gameDAO;

    @BeforeEach
    public void setup() throws Exception {
        SQLDatabase.initialize();
        gameDAO = new SQLGameDAO();
        gameDAO.clear();
    }

    @Test
    public void createGameSuccess() throws Exception {
        GameData game = new GameData(
                0, null, null, "Test Game", new ChessGame()
        );

        int gameID = gameDAO.createGame(game);

        assertTrue(gameID > 0);

        GameData saved = gameDAO.getGame(gameID);

        assertNotNull(saved);
        assertEquals(gameID, saved.gameID());
        assertEquals("Test Game", saved.gameName());
    }

    @Test
    public void getMissingGameReturnsNull() throws Exception {
        assertNull(gameDAO.getGame(-1));
    }

    @Test
    public void listGamesSuccess() throws Exception {
        gameDAO.createGame(new GameData(
                0, null, null, "Game One", new ChessGame()
        ));

        gameDAO.createGame(new GameData(
                0, null, null, "Game Two", new ChessGame()
        ));

        Collection<GameData> games = gameDAO.listGames();

        assertEquals(2, games.size());
    }

    @Test
    public void updateGameSuccess() throws Exception {
        int gameID = gameDAO.createGame(new GameData(
                0, null, null, "Original Game", new ChessGame()
        ));

        GameData updated = new GameData(
                gameID,
                "player1",
                "player2",
                "Updated Game",
                new ChessGame()
        );

        gameDAO.updateGame(updated);

        GameData saved = gameDAO.getGame(gameID);

        assertNotNull(saved);
        assertEquals("player1", saved.whiteUsername());
        assertEquals("player2", saved.blackUsername());
        assertEquals("Updated Game", saved.gameName());
    }

    @Test
    public void updateMissingGameFails() throws Exception {
        GameData missing = new GameData(
                -1, null, null, "Missing Game", new ChessGame()
        );

        assertThrows(
                DataAccessException.class,
                () -> gameDAO.updateGame(missing)
        );
    }

    @Test
    public void clearGamesSuccess() throws Exception {
        gameDAO.createGame(new GameData(
                0, null, null, "Game One", new ChessGame()
        ));

        gameDAO.clear();

        assertTrue(gameDAO.listGames().isEmpty());
    }

    @Test
    public void chessGameStatePersists() throws Exception {
        ChessGame chessGame = new ChessGame();
        chessGame.getBoard().resetBoard();

        int gameID = gameDAO.createGame(new GameData(
                0, null, null, "Persistence Test", chessGame
        ));

        GameData saved = gameDAO.getGame(gameID);

        assertNotNull(saved);
        assertNotNull(saved.game());
        assertNotNull(saved.game().getBoard());

        // Verify a starting pawn survives JSON serialization.
        assertNotNull(saved.game().getBoard().getPiece(
                new chess.ChessPosition(2, 1)
        ));
    }
}
