
package client;

import chess.ChessBoard;
import chess.ChessGame;

import java.util.List;
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
        String gameName = scanner.nextLine().trim();

        if (gameName.isEmpty()) {
            System.out.println("Game name cannot be empty.");
            return;
        }

        serverFacade.createGame(authToken, gameName);
        System.out.println("Game created successfully: " + gameName);
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
            System.out.println("No games available. Create a game first.");
            return;
        }

        displayGames(games);

        int selection = selectGame(games.size());
        if (selection < 0) {
            return;
        }

        ApiModels.GameSummary game = games.get(selection);

        System.out.print("Choose a color (WHITE/BLACK): ");
        String color = scanner.nextLine().trim().toUpperCase();

        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            System.out.println("Invalid color. Enter WHITE or BLACK.");
            return;
        }

        serverFacade.joinGame(authToken, color, game.gameID());

        ChessGame.TeamColor perspective =
                ChessGame.TeamColor.valueOf(color);

        ChessBoard board = new ChessBoard();
        board.resetBoard();

        boardRenderer.drawBoard(board, perspective);

        System.out.println("Joined game: " + game.gameName());
    }

    private void observeGame() throws ServerFacadeException {
        List<ApiModels.GameSummary> games =
                serverFacade.listGames(authToken);

        if (games.isEmpty()) {
            System.out.println("No games available to observe.");
            return;
        }

        displayGames(games);

        int selection = selectGame(games.size());
        if (selection < 0) {
            return;
        }

        ApiModels.GameSummary game = games.get(selection);

        ChessBoard board = new ChessBoard();
        board.resetBoard();

        boardRenderer.drawBoard(
                board, ChessGame.TeamColor.WHITE);

        System.out.println("Observing game: " + game.gameName());
    }

    private void displayGames(List<ApiModels.GameSummary> games) {
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
        System.out.print("Select game number: ");
        String input = scanner.nextLine().trim();

        try {
            int selection = Integer.parseInt(input);

            if (selection < 1 || selection > gameCount) {
                System.out.println(
                        "Please choose a listed game number.");
                return -1;
            }

            return selection - 1;

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
            return -1;
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
                  play    - Join a chess game
                  observe - Observe a chess game
                """);
    }
}
