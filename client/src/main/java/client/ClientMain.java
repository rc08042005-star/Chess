
package client;

import java.util.Scanner;

public class ClientMain {

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);

                if (port < 1 || port > 65535) {
                    System.out.println(
                            "Invalid port. Using default port 8080.");
                    port = DEFAULT_PORT;
                }

            } catch (NumberFormatException e) {
                System.out.println(
                        "Invalid port. Using default port 8080.");
            }
        }

        ServerFacade serverFacade = new ServerFacade(port);

        try (Scanner scanner = new Scanner(System.in)) {

            while (scanner.hasNextLine()) {

                PreloginUI preloginUI =
                        new PreloginUI(serverFacade, scanner);

                preloginUI.run();

                if (!preloginUI.isRunning()) {
                    break;
                }

                String authToken = preloginUI.getAuthToken();

                if (authToken != null) {
                    PostloginUI postloginUI =
                            new PostloginUI(
                                    serverFacade,
                                    scanner,
                                    authToken);

                    postloginUI.run();
                }
            }
        }
    }
}
