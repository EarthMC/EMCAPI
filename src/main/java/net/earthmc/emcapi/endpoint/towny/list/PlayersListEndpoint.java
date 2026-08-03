package net.earthmc.emcapi.endpoint.towny.list;

import com.google.gson.JsonArray;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;
import net.earthmc.emcapi.util.ContentTypes;
import net.earthmc.emcapi.object.endpoint.GetEndpoint;
import net.earthmc.emcapi.util.EndpointUtils;

import java.util.List;

@OpenApi(
    path = "/v4/players",
    methods = HttpMethod.GET,
    summary = "Get a list of all players and their UUIDs",
    responses = {
        @OpenApiResponse(
            status = "200",
            content = {
                @OpenApiContent(
                    from = ContentTypes.NameUUID[].class,
                    example = """
                        [{"name":"K1kimor","uuid":"686b1bfa-38de-4eb9-9628-43ed3b56b76e"},{"name":"Veyronity","uuid":"5b8274bf-b162-4336-85a0-48f9d5380a78"}]
                        """
                )
            }
        )
    }
)
public class PlayersListEndpoint extends GetEndpoint {

    @Override
    public JsonArray getJsonElement() {
        List<Resident> residents = EndpointUtils.filterActiveResidents(TownyAPI.getInstance().getResidents());
        return EndpointUtils.getResidentArray(residents);
    }
}
