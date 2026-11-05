
package server;

import io.javalin.http.Context;
import service.ClearService;

public class ClearHandler {

    private final ClearService clearService;

    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
    }

    public void clear(Context ctx) {
        try {
            clearService.clear();
            ctx.status(200)
                    .contentType("application/json")
                    .result("{}");

        } catch (Exception e) {
            ctx.status(500).json(
                    new ApiModels.ErrorResult("Error: " + e.getMessage())
            );
        }
    }
}
