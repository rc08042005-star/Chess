
package client;

import chess.ChessBoard;
import chess.ChessGame;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class PostloginUI {

    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private final String authToken;
    private final ChessBoardRenderer boardRenderer;

    public PostloginUI(ServerFacade serverFacade,
                       Scanner scanner,
                       String authToken) {
        this.serverFacade = serverFacade;
        this.scanner = scanner;
        this.authToken = authToken;
        this.boardRenderer = new ChessBoardRenderer();
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

            String command = scanner.nextLine()
                    .trim()
                    .toLowerCase(Locale.ROOT);

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
                    case "play" -> playGame();
                    case "observe" -> observeGame();

                    default -> System.out.println(
                            "Unknown command. Type 'help' for options.");
                }
            } catch (ServerFacadeException e) {
                System.out.println(
                        "Unable to complete request: " + e.getMessage());
            }
        }
    }

    private void createGame() throws ServerFacadeException {
        System.out.print("Game name: ");

        if (!scanner.hasNextLine()) {
            return;
        }

        String gameName = scanner.nextLine().trim();

        if (gameName.isEmpty()) {
            System.out.println("Game name cannot be empty.");
            return;
        }

        serverFacade.createGame(authToken, gameName);

        System.out.println(
                "Game created successfully: " + gameName);
    }

    private void listGames() throws ServerFacadeException {
        List<ApiModels.GameSummary> games =
                serverFacade.listGames(authToken);

        if (games.isEmpty()) {
            System.out.println("No games available.");
            return;
        }

        displayGames(games);
    }

    private void playGame() throws ServerFacadeException {
        List<ApiModels.GameSummary> games =
                serverFacade.listGames(authToken);

        if (games.isEmpty()) {
            System.out.println(
                    "No games available. Create a game first.");
            return;
        }

        displayGames(games);

        int selection = selectGame(games.size());

        if (selection < 0) {
            return;
        }

        ApiModels.GameSummary game = games.get(selection);

        ChessGame.TeamColor perspective = selectColor(game);

        if (perspective == null) {
            return;
        }

        serverFacade.joinGame(
                authToken,
                perspective.name(),
                game.gameID());

        displayInitialBoard(perspective);

        System.out.println(
                "Joined game: " + game.gameName());
    }

    private void observeGame() throws ServerFacadeException {
        List<ApiModels.GameSummary> games =
                serverFacade.listGames(authToken);

        if (games.isEmpty()) {
            System.out.println(
                    "No games available to observe.");
            return;
        }

        displayGames(games);

        int selection = selectGame(games.size());

        if (selection < 0) {
            return;
        }

        ApiModels.GameSummary game = games.get(selection);

        displayInitialBoard(ChessGame.TeamColor.WHITE);

        System.out.println(
                "Observing game: " + game.gameName());
    }

    private ChessGame.TeamColor selectColor(
            ApiModels.GameSummary game) {

        while (true) {
            System.out.print(
                    "Choose a color (WHITE/BLACK or 'cancel'): ");

            if (!scanner.hasNextLine()) {
                return null;
            }

            String input = scanner.nextLine()
                    .trim()
                    .toUpperCase(Locale.ROOT);

            if (input.equals("CANCEL")) {
                System.out.println("Joining game cancelled.");
                return null;
            }

            if (!input.equals("WHITE") && !input.equals("BLACK")) {
                System.out.println(
                        "Invalid color. Enter WHITE or BLACK.");
                continue;
            }

            String currentPlayer = input.equals("WHITE")
                    ? game.whiteUsername()
                    : game.blackUsername();

            if (currentPlayer != null) {
                System.out.println(
                        "That color is already taken. "
                                + "Choose another color or type 'cancel'.");
                continue;
            }

            return ChessGame.TeamColor.valueOf(input);
        }
    }

    private void displayInitialBoard(
            ChessGame.TeamColor perspective) {

        ChessBoard board = new ChessBoard();
        board.resetBoard();

        boardRenderer.drawBoard(board, perspective);
    }

    private void displayGames(
            List<ApiModels.GameSummary> games) {

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

    private int selectGame(int gameCount) {
        while (true) {
            System.out.print(
                    "Select game number (or 'cancel'): ");

            if (!scanner.hasNextLine()) {
                return -1;
            }

            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("cancel")) {
                System.out.println(
                        "Game selection cancelled.");
                return -1;
            }

            try {
                int selection = Integer.parseInt(input);

                if (selection >= 1 && selection <= gameCount) {
                    return selection - 1;
                }

                System.out.println(
                        "Please choose a number between 1 and "
                                + gameCount + ".");

            } catch (NumberFormatException e) {
                System.out.println(
                        "Invalid input. Enter a number or 'cancel'.");
            }
        }
    }

    private String playerName(String username) {
        return username == null
                ? "Available"
                : username;
    }

    private void printHelp() {
        System.out.println("""
                Available commands:
                  help    - Show available commands
                  logout  - Log out
                  create  - Create a chess game
                  list    - List available games
                  play    - Join a chess game
                  observe - Observe a chess game
                """);
    }
}
