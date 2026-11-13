
package client;

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
                break;
            }

            String command = scanner.nextLine().trim().toLowerCase();

            try {
                handleCommand(command);
            } catch (ServerFacadeException e) {
                System.out.println("Unable to complete request: "
                        + e.getMessage());
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
        String password = prompt("Password");

        ApiModels.AuthResult result =
                serverFacade.login(username, password);

        authToken = result.authToken();
        System.out.println("Logged in as " + result.username());
    }

    private void register() throws ServerFacadeException {
        String username = prompt("Username");
        String password = prompt("Password");
        String email = prompt("Email");

        ApiModels.AuthResult result =
                serverFacade.register(username, password, email);

        authToken = result.authToken();
        System.out.println("Registered and logged in as "
                + result.username());
    }

    private String prompt(String label) {
        System.out.print(label + ": ");
        return scanner.nextLine().trim();
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
