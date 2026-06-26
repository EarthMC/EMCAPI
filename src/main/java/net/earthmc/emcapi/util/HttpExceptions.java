package net.earthmc.emcapi.util;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.ServiceUnavailableResponse;
import io.javalin.http.UnauthorizedResponse;

public class HttpExceptions {
    public static final ServiceUnavailableResponse MISSING_PLUGIN = new ServiceUnavailableResponse("This endpoint is unavailable due to a missing plugin dependency.");
    public static final UnauthorizedResponse MISSING_API_KEY = new UnauthorizedResponse("Could not find an owner for this API key");
    public static final ForbiddenResponse FORBIDDEN = new ForbiddenResponse("This API key is not owned by the player queried");

    public static final BadRequestResponse NOT_A_JSON_OBJECT = new BadRequestResponse("Your query contains a value that is not a JSON object");
    public static final BadRequestResponse NOT_A_UUID = new BadRequestResponse("Your query contains an invalid UUID");
    public static final BadRequestResponse NOT_A_STRING = new BadRequestResponse("Your query contains a value that is not a string");
}
