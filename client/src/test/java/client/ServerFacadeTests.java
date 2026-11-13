
package client;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Server;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void startServer() {
        server = new Server();
        int port = server.run(0);
        facade = new ServerFacade(port);
    }

    @AfterAll
    public static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @BeforeEach
    public void clearDatabase() throws Exception {
        facade.clear();
    }

    @Test
    public void registerSuccess() throws Exception {
        ApiModels.AuthResult result = facade.register(
                "player1", "password123", "player1@example.com");

        assertNotNull(result);
        assertEquals("player1", result.username());
        assertNotNull(result.authToken());
        assertFalse(result.authToken().isBlank());
    }

    @Test
    public void registerDuplicateUsernameFails() throws Exception {
        facade.register(
                "player1", "password123", "player1@example.com");

        ServerFacadeException exception = assertThrows(
                ServerFacadeException.class,
                () -> facade.register(
                        "player1",
                        "anotherPassword",
                        "another@example.com"));

        assertNotNull(exception.getMessage());
    }

    @Test
    public void loginSuccess() throws Exception {
        facade.register(
                "player1", "password123", "player1@example.com");

        ApiModels.AuthResult result = facade.login(
                "player1", "password123");

        assertEquals("player1", result.username());
        assertNotNull(result.authToken());
        assertFalse(result.authToken().isBlank());
    }

    @Test
    public void loginWrongPasswordFails() throws Exception {
        facade.register(
                "player1", "password123", "player1@example.com");

        assertThrows(
                ServerFacadeException.class,
                () -> facade.login("player1", "wrongPassword"));
    }

    @Test
    public void logoutSuccess() throws Exception {
        ApiModels.AuthResult auth = facade.register(
                "player1", "password123", "player1@example.com");

        assertDoesNotThrow(
                () -> facade.logout(auth.authToken()));

        assertThrows(
                ServerFacadeException.class,
                () -> facade.listGames(auth.authToken()));
    }

    @Test
    public void logoutInvalidTokenFails() {
        assertThrows(
                ServerFacadeException.class,
                () -> facade.logout("invalidToken"));
    }

    @Test
    public void createGameSuccess() throws Exception {
        ApiModels.AuthResult auth = facade.register(
                "player1", "password123", "player1@example.com");

        int gameID = facade.createGame(
                auth.authToken(), "My Chess Game");

        assertTrue(gameID > 0);

        List<ApiModels.GameSummary> games =
                facade.listGames(auth.authToken());

        assertEquals(1, games.size());
        assertEquals("My Chess Game", games.get(0).gameName());
    }

    @Test
    public void createGameWithoutAuthorizationFails() {
        assertThrows(
                ServerFacadeException.class,
                () -> facade.createGame(
                        "invalidToken", "Unauthorized Game"));
    }

    @Test
    public void listGamesSuccess() throws Exception {
        ApiModels.AuthResult auth = facade.register(
                "player1", "password123", "player1@example.com");

        facade.createGame(auth.authToken(), "Game One");
        facade.createGame(auth.authToken(), "Game Two");

        List<ApiModels.GameSummary> games =
                facade.listGames(auth.authToken());

        assertEquals(2, games.size());
        assertTrue(games.stream()
                .anyMatch(game -> "Game One".equals(game.gameName())));
        assertTrue(games.stream()
                .anyMatch(game -> "Game Two".equals(game.gameName())));
    }

    @Test
    public void listGamesInvalidTokenFails() {
        assertThrows(
                ServerFacadeException.class,
                () -> facade.listGames("invalidToken"));
    }

    @Test
    public void joinGameSuccess() throws Exception {
        ApiModels.AuthResult auth = facade.register(
                "player1", "password123", "player1@example.com");

        int gameID = facade.createGame(
                auth.authToken(), "Join Test Game");

        assertDoesNotThrow(
                () -> facade.joinGame(
                        auth.authToken(), "WHITE", gameID));

        List<ApiModels.GameSummary> games =
                facade.listGames(auth.authToken());

        assertEquals(1, games.size());
        assertEquals("player1", games.get(0).whiteUsername());
    }

    @Test
    public void joinOccupiedColorFails() throws Exception {
        ApiModels.AuthResult firstPlayer = facade.register(
                "player1", "password123", "player1@example.com");

        ApiModels.AuthResult secondPlayer = facade.register(
                "player2", "password456", "player2@example.com");

        int gameID = facade.createGame(
                firstPlayer.authToken(), "Occupied Game");

        facade.joinGame(
                firstPlayer.authToken(), "WHITE", gameID);

        assertThrows(
                ServerFacadeException.class,
                () -> facade.joinGame(
                        secondPlayer.authToken(), "WHITE", gameID));
    }

    @Test
    public void clearDatabaseSuccess() throws Exception {
        ApiModels.AuthResult auth = facade.register(
                "player1", "password123", "player1@example.com");

        facade.createGame(auth.authToken(), "Temporary Game");

        assertDoesNotThrow(() -> facade.clear());

        assertThrows(
                ServerFacadeException.class,
                () -> facade.login("player1", "password123"));
    }

    @Test
    public void clearDatabaseTwiceSucceeds() {
        assertDoesNotThrow(() -> {
            facade.clear();
            facade.clear();
        });
    }

    @Test
    public void connectionFailureThrowsException() {
        ServerFacade unavailableServer =
                new ServerFacade("http://127.0.0.1:0");

        ServerFacadeException exception = assertThrows(
                ServerFacadeException.class,
                () -> unavailableServer.listGames("testToken"));

        assertEquals(
                "Could not connect to the chess server.",
                exception.getMessage());

        assertNotNull(exception.getCause());
    }

    @Test
    public void malformedServerResponseThrowsException() throws Exception {
        HttpServer fakeServer = createFakeServer(200, "{invalid json");

        try {
            ServerFacade testFacade =
                    new ServerFacade(fakeServer.getAddress().getPort());

            ServerFacadeException exception = assertThrows(
                    ServerFacadeException.class,
                    () -> testFacade.login("testuser", "password"));

            assertEquals(
                    "The server returned an invalid response.",
                    exception.getMessage());
        } finally {
            fakeServer.stop(0);
        }
    }

    @Test
    public void emptyServerResponseThrowsException() throws Exception {
        HttpServer fakeServer = createFakeServer(200, "null");

        try {
            ServerFacade testFacade =
                    new ServerFacade(fakeServer.getAddress().getPort());

            ServerFacadeException exception = assertThrows(
                    ServerFacadeException.class,
                    () -> testFacade.login("testuser", "password"));

            assertEquals(
                    "The server returned an empty response.",
                    exception.getMessage());
        } finally {
            fakeServer.stop(0);
        }
    }

    @Test
    public void malformedErrorResponseUsesSafeMessage() throws Exception {
        HttpServer fakeServer = createFakeServer(500, "{invalid json");

        try {
            ServerFacade testFacade =
                    new ServerFacade(fakeServer.getAddress().getPort());

            ServerFacadeException exception = assertThrows(
                    ServerFacadeException.class,
                    () -> testFacade.login("testuser", "password"));

            assertEquals(
                    "The server could not complete the request.",
                    exception.getMessage());
        } finally {
            fakeServer.stop(0);
        }
    }

    private HttpServer createFakeServer(int statusCode, String body)
            throws Exception {

        HttpServer fakeServer = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0);

        fakeServer.createContext("/session", exchange -> {
            byte[] response = body.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set(
                    "Content-Type", "application/json");

            exchange.sendResponseHeaders(statusCode, response.length);

            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });

        fakeServer.start();
        return fakeServer;
    }
}
