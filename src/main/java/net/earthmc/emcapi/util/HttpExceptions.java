package net.earthmc.emcapi.util;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.ServiceUnavailableResponse;

public class HttpExceptions {
    public static final BadRequestResponse NOT_A_JSON_OBJECT = new BadRequestResponse("Your query contains a value that is not a JSON object");
    public static final ServiceUnavailableResponse MISSING_PLUGIN = new ServiceUnavailableResponse("This endpoint is unavailable due to a missing plugin dependency.");
}
