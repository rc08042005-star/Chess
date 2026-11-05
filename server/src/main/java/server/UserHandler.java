
package server;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.UserService;

public class UserHandler {

    private final UserService userService;
    private final Gson gson = new Gson();

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public void register(Context ctx) {
        try {
            ApiModels.RegisterRequest request =
                    gson.fromJson(ctx.body(), ApiModels.RegisterRequest.class);

            if (request == null) {
                throw new IllegalArgumentException("bad request");
            }

            var auth = userService.register(
                    request.username(),
                    request.password(),
                    request.email()
            );

            ctx.status(200).json(
                    new ApiModels.AuthResult(
                            auth.username(),
                            auth.authToken()
                    )
            );

        } catch (IllegalArgumentException e) {
            sendError(ctx, 400, "bad request");

        } catch (IllegalStateException e) {
            sendError(ctx, 403, "already taken");

        } catch (Exception e) {
            sendError(ctx, 500, e.getMessage());
        }
    }

    public void login(Context ctx) {
        try {
            ApiModels.LoginRequest request =
                    gson.fromJson(ctx.body(), ApiModels.LoginRequest.class);

            if (request == null) {
                throw new IllegalArgumentException("bad request");
            }

            var auth = userService.login(
                    request.username(),
                    request.password()
            );

            ctx.status(200).json(
                    new ApiModels.AuthResult(
                            auth.username(),
                            auth.authToken()
                    )
            );

        } catch (IllegalArgumentException e) {
            sendError(ctx, 400, "bad request");

        } catch (SecurityException e) {
            sendError(ctx, 401, "unauthorized");

        } catch (Exception e) {
            sendError(ctx, 500, e.getMessage());
        }
    }

    public void logout(Context ctx) {
        try {
            userService.logout(ctx.header("authorization"));
            ctx.status(200).result("{}");

        } catch (SecurityException e) {
            sendError(ctx, 401, "unauthorized");

        } catch (Exception e) {
            sendError(ctx, 500, e.getMessage());
        }
    }

    private void sendError(Context ctx, int status, String message) {
        ctx.status(status).json(
                new ApiModels.ErrorResult("Error: " + message)
        );
    }
}
