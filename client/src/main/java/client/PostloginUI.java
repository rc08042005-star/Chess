
package client;

import java.util.List;
import java.util.Scanner;

public class PostloginUI {

    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private final String authToken;

    public PostloginUI(ServerFacade serverFacade,
                       Scanner scanner,
                       String authToken) {
        this.serverFacade = serverFacade;
        this.scanner = scanner;
        this.authToken = authToken;
    }

    public void run() {
        System.out.println("\nYou are now logged in!");
        printHelp();

        boolean loggedIn = true;

        while (loggedIn) {
            System.out.print("\n[LOGGED IN] >>> ");

            if (!scanner.hasNextLine()) {
                return;
            }

            String command = scanner.nextLine().trim().toLowerCase();

            try {
                switch (command) {
                    case "help" -> printHelp();
                    case "logout" -> {
                        serverFacade.logout(authToken);
                        System.out.println("Logged out successfully.");
                        loggedIn = false;
                    }
                    case "create" -> createGame();
                    case "list" -> listGames();
                    case "play", "observe" ->
                            System.out.println(
                                    "Board display will be added next.");
                    default -> System.out.println(
                            "Unknown command. Type 'help' for options.");
                }
            } catch (ServerFacadeException e) {
                System.out.println("Unable to complete request: "
                        + e.getMessage());
            }
        }
    }

    private void createGame() throws ServerFacadeException {
        System.out.print("Game name: ");
        String gameName = scanner.nextLine().trim();

        if (gameName.isEmpty()) {
            System.out.println("Game name cannot be empty.");
            return;
        }

        int gameID = serverFacade.createGame(authToken, gameName);
        System.out.println("Game created successfully: " + gameName);
    }

    private void listGames() throws ServerFacadeException {
        List<ApiModels.GameSummary> games =
                serverFacade.listGames(authToken);

        if (games.isEmpty()) {
            System.out.println("No games available.");
            return;
        }

        System.out.println("\nAvailable games:");

        for (int i = 0; i < games.size(); i++) {
            ApiModels.GameSummary game = games.get(i);

            System.out.printf(
                    "%d. %s | White: %s | Black: %s%n",
                    i + 1,
                    game.gameName(),
                    playerName(game.whiteUsername()),
                    playerName(game.blackUsername())
            );
        }
    }

    private String playerName(String username) {
        return username == null ? "Available" : username;
    }

    private void printHelp() {
        System.out.println("""
                Available commands:
                  help    - Show available commands
                  logout  - Log out
                  create  - Create a chess game
                  list    - List available games
                  play    - Join a chess game (coming next)
                  observe - Observe a chess game (coming next)
                """);
    }
}
