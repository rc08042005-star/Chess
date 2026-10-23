
package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {

    @Test
    void clearEmptyDatabase() throws Exception {
        var userDAO = new MemoryUserDAO();
        var authDAO = new MemoryAuthDAO();
        var gameDAO = new MemoryGameDAO();

        var clearService = new ClearService(userDAO, authDAO, gameDAO);

        assertDoesNotThrow(clearService::clear);
    }

    @Test
    void clearRemovesRegisteredUsers() throws Exception {
        var userDAO = new MemoryUserDAO();
        var authDAO = new MemoryAuthDAO();
        var gameDAO = new MemoryGameDAO();

        var userService = new UserService(userDAO, authDAO);
        var clearService = new ClearService(userDAO, authDAO, gameDAO);

        userService.register(
                "rahul",
                "password123",
                "rahul@example.com"
        );

        clearService.clear();

        assertDoesNotThrow(() ->
                userService.register(
                        "rahul",
                        "password123",
                        "rahul@example.com"
                )
        );
    }

    @Test
    void clearRemovesGames() throws Exception {
        var userDAO = new MemoryUserDAO();
        var authDAO = new MemoryAuthDAO();
        var gameDAO = new MemoryGameDAO();

        var userService = new UserService(userDAO, authDAO);
        var gameService = new GameService(gameDAO, authDAO);
        var clearService = new ClearService(userDAO, authDAO, gameDAO);

        String token = userService.register(
                "rahul",
                "password123",
                "rahul@example.com"
        ).authToken();

        gameService.createGame(token, "Chess Match");

        assertEquals(1, gameService.listGames(token).size());

        clearService.clear();

        String newToken = userService.register(
                "rahul",
                "password123",
                "rahul@example.com"
        ).authToken();

        assertTrue(gameService.listGames(newToken).isEmpty());
    }
}
