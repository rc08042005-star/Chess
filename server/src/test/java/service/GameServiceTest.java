
package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    private GameService gameService;
    private UserService userService;
    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        var userDAO = new MemoryUserDAO();
        var authDAO = new MemoryAuthDAO();
        var gameDAO = new MemoryGameDAO();

        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);

        authToken = userService.register(
                "rahul",
                "password123",
                "rahul@example.com"
        ).authToken();
    }

    @Test
    void listGamesEmpty() throws Exception {
        var games = gameService.listGames(authToken);

        assertNotNull(games);
        assertTrue(games.isEmpty());
    }

    @Test
    void listGamesUnauthorized() throws Exception {
        assertThrows(SecurityException.class, () ->
                gameService.listGames("invalid-token")
        );
    }

    @Test
    void createGameSuccess() throws Exception {
        int gameID = gameService.createGame(
                authToken,
                "First Game"
        );

        assertTrue(gameID > 0);
        assertEquals(1, gameService.listGames(authToken).size());
    }

    @Test
    void createGameUnauthorized() throws Exception {
        assertThrows(SecurityException.class, () ->
                gameService.createGame(
                        "invalid-token",
                        "First Game"
                )
        );
    }

    @Test
    void createGameMissingName() throws Exception {
        assertThrows(IllegalArgumentException.class, () ->
                gameService.createGame(authToken, null)
        );
    }

    @Test
    void joinWhiteSuccess() throws Exception {
        int gameID = gameService.createGame(
                authToken,
                "First Game"
        );

        gameService.joinGame(authToken, "WHITE", gameID);

        var game = gameService.listGames(authToken)
                .iterator()
                .next();

        assertEquals("rahul", game.whiteUsername());
    }

    @Test
    void joinBlackSuccess() throws Exception {
        int gameID = gameService.createGame(
                authToken,
                "First Game"
        );

        gameService.joinGame(authToken, "BLACK", gameID);

        var game = gameService.listGames(authToken)
                .iterator()
                .next();

        assertEquals("rahul", game.blackUsername());
    }

    @Test
    void joinOccupiedColor() throws Exception {
        int gameID = gameService.createGame(
                authToken,
                "First Game"
        );

        gameService.joinGame(authToken, "WHITE", gameID);

        String secondToken = userService.register(
                "joseph",
                "password456",
                "joseph@example.com"
        ).authToken();

        assertThrows(IllegalStateException.class, () ->
                gameService.joinGame(
                        secondToken,
                        "WHITE",
                        gameID
                )
        );
    }

    @Test
    void joinInvalidGame() throws Exception {
        assertThrows(IllegalArgumentException.class, () ->
                gameService.joinGame(
                        authToken,
                        "WHITE",
                        99999
                )
        );
    }

    @Test
    void joinInvalidToken() throws Exception {
        int gameID = gameService.createGame(
                authToken,
                "First Game"
        );

        assertThrows(SecurityException.class, () ->
                gameService.joinGame(
                        "invalid-token",
                        "WHITE",
                        gameID
                )
        );
    }

    @Test
    void joinInvalidColor() throws Exception {
        int gameID = gameService.createGame(
                authToken,
                "First Game"
        );

        assertThrows(IllegalArgumentException.class, () ->
                gameService.joinGame(
                        authToken,
                        "GREEN",
                        gameID
                )
        );
    }
}
