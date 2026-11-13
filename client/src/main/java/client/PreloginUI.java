
package client;
import model.ApiModels;
import java.util.Locale;
import java.util.Scanner;

public class PreloginUI {

    private final ServerFacade serverFacade;
    private final Scanner scanner;

    private String authToken;
    private boolean running = true;

    public PreloginUI(ServerFacade serverFacade, Scanner scanner) {
        this.serverFacade = serverFacade;
        this.scanner = scanner;
    }

    public void run() {
        System.out.println("Welcome to Chess!");
        printHelp();

        while (running && authToken == null) {
            System.out.print("\n[LOGGED OUT] >>> ");

            if (!scanner.hasNextLine()) {
                running = false;
                return;
            }

            String command = scanner.nextLine()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            try {
                handleCommand(command);
            } catch (ServerFacadeException e) {
                System.out.println(
                        "Unable to complete request: " + e.getMessage());
            }
        }
    }

    private void handleCommand(String command)
            throws ServerFacadeException {

        switch (command) {
            case "help" -> printHelp();
            case "login" -> login();
            case "register" -> register();
            case "quit", "exit" -> quit();

            default -> System.out.println(
                    "Unknown command. Type 'help' for options.");
        }
    }

    private void login() throws ServerFacadeException {
        String username = prompt("Username");

        if (username == null) {
            return;
        }

        String password = prompt("Password");

        if (password == null) {
            return;
        }

        ApiModels.AuthResult result =
                serverFacade.login(username, password);

        authToken = result.authToken();

        System.out.println(
                "Logged in as " + result.username());
    }

    private void register() throws ServerFacadeException {
        String username = prompt("Username");

        if (username == null) {
            return;
        }

        String password = prompt("Password");

        if (password == null) {
            return;
        }

        String email = prompt("Email");

        if (email == null) {
            return;
        }

        ApiModels.AuthResult result =
                serverFacade.register(username, password, email);

        authToken = result.authToken();

        System.out.println(
                "Registered and logged in as " + result.username());
    }

    private String prompt(String label) {
        while (true) {
            System.out.print(label + ": ");

            if (!scanner.hasNextLine()) {
                running = false;
                return null;
            }

            String input = scanner.nextLine().trim();

            if (!input.isEmpty()) {
                return input;
            }

            System.out.println(
                    label + " cannot be empty. Please try again.");
        }
    }

    private void quit() {
        running = false;
        System.out.println("Goodbye!");
    }

    private void printHelp() {
        System.out.println("""
                Available commands:
                  help     - Show available commands
                  login    - Log into your account
                  register - Create a new account
                  quit     - Exit Chess
                """);
    }

    public String getAuthToken() {
        return authToken;
    }

    public boolean isRunning() {
        return running;
    }
}
