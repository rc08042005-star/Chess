
package client;

import java.util.Scanner;

public class ClientMain {

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println(
                        "Invalid port. Using default port 8080.");
            }
        }

        ServerFacade serverFacade = new ServerFacade(port);

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;

            while (running) {
                PreloginUI preloginUI =
                        new PreloginUI(serverFacade, scanner);

                preloginUI.run();

                if (!preloginUI.isRunning()) {
                    running = false;
                } else if (preloginUI.getAuthToken() != null) {
                    PostloginUI postloginUI =
                            new PostloginUI(
                                    serverFacade,
                                    scanner,
                                    preloginUI.getAuthToken()
                            );

                    postloginUI.run();

                    if (!scanner.hasNextLine()) {
                        running = false;
                    }
                }
            }
        }
    }
}
