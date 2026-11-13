
package client;

import model.ApiModels;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;


public class ServerFacade {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;
    private final Gson gson;
    private final String baseUrl;

    public ServerFacade(int port) {
        this("http://localhost:" + port);
    }

    public ServerFacade(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;

        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public ApiModels.AuthResult register(
            String username, String password, String email)
            throws ServerFacadeException {

        var request = new ApiModels.RegisterRequest(
                username, password, email);

        return sendRequest(
                "POST", "/user", request, null,
                ApiModels.AuthResult.class);
    }

    public ApiModels.AuthResult login(
            String username, String password)
            throws ServerFacadeException {

        var request = new ApiModels.LoginRequest(
                username, password);

        return sendRequest(
                "POST", "/session", request, null,
                ApiModels.AuthResult.class);
    }

    public void logout(String authToken)
            throws ServerFacadeException {

        sendRequest(
                "DELETE", "/session", null, authToken,
                Void.class);
    }

    public int createGame(String authToken, String gameName)
            throws ServerFacadeException {

        var request = new ApiModels.CreateGameRequest(gameName);

        var result = sendRequest(
                "POST", "/game", request, authToken,
                ApiModels.CreateGameResult.class);

        return result.gameID();
    }

    public List<ApiModels.GameSummary> listGames(String authToken)
            throws ServerFacadeException {

        var result = sendRequest(
                "GET", "/game", null, authToken,
                ApiModels.GamesResult.class);

        return result.games();
    }

    public void joinGame(
            String authToken, String playerColor, int gameID)
            throws ServerFacadeException {

        var request = new ApiModels.JoinGameRequest(
                playerColor, gameID);

        sendRequest(
                "PUT", "/game", request, authToken,
                Void.class);
    }

    public void clear() throws ServerFacadeException {

        sendRequest(
                "DELETE", "/db", null, null,
                Void.class);
    }

    private <T> T sendRequest(
            String method,
            String path,
            Object requestBody,
            String authToken,
            Class<T> responseType)
            throws ServerFacadeException {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(TIMEOUT)
                .header("Accept", "application/json");

        if (authToken != null) {
            builder.header("authorization", authToken);
        }

        if (requestBody != null) {
            builder.header("Content-Type", "application/json");

            builder.method(
                    method,
                    HttpRequest.BodyPublishers.ofString(
                            gson.toJson(requestBody),
                            StandardCharsets.UTF_8));
        } else {
            builder.method(
                    method,
                    HttpRequest.BodyPublishers.noBody());
        }

        try {
            HttpResponse<String> response = httpClient.send(
                    builder.build(),
                    HttpResponse.BodyHandlers.ofString(
                            StandardCharsets.UTF_8));

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new ServerFacadeException(
                        errorMessage(response));
            }

            if (responseType == Void.class) {
                return null;
            }

            T result = gson.fromJson(
                    response.body(), responseType);

            if (result == null) {
                throw new ServerFacadeException(
                        "The server returned an empty response.");
            }

            return result;

        } catch (IOException e) {
            throw new ServerFacadeException(
                    "Could not connect to the chess server.", e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ServerFacadeException(
                    "The server request was interrupted.", e);

        } catch (JsonSyntaxException e) {
            throw new ServerFacadeException(
                    "The server returned an invalid response.", e);
        }
    }

    private String errorMessage(HttpResponse<String> response) {

        try {
            var error = gson.fromJson(
                    response.body(),
                    ApiModels.ErrorResult.class);

            if (error != null
                    && error.message() != null
                    && !error.message().isBlank()) {

                return error.message();
            }

        } catch (JsonSyntaxException ignored) {
            // Use a safe fallback if the response is invalid JSON.
        }

        return "The server could not complete the request.";
    }
}
