package io.servertap.api.v1;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import io.servertap.Constants;
import io.servertap.utils.ValidationUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class PAPIApi {
    @OpenApi(
            path = "/v1/placeholders/replace",
            methods = {HttpMethod.POST},
            summary = "Process a string using PlaceholderAPI",
            description = "Process a string using PlaceholderAPI",
            tags = {"PlaceholderAPI"},
            headers = {
                    @OpenApiParam(name = "key")
            },
            requestBody = @OpenApiRequestBody(
                    required = true,
                    content = {
                            @OpenApiContent(
                                    mimeType = "application/x-www-form-urlencoded",
                                    properties = {
                                            @OpenApiContentProperty(name = "message", type = "string"),
                                            @OpenApiContentProperty(name = "uuid", type = "string")
                                    }
                            )
                    }
            ),
            responses = {
                    @OpenApiResponse(status = "200", content = @OpenApiContent(type = "application/json")),
                    @OpenApiResponse(status = "500", content = @OpenApiContent(type = "application/json"))
            }
    )
    public void replacePlaceholdersGet(Context ctx) {
        String passedUuid = ctx.formParam("uuid");
        String passedMessage = ctx.formParam("message");
        ctx.status(200).json(replacePlaceholders(passedUuid, passedMessage));
    }

    public String replacePlaceholders(String uuid, String msg) {
        OfflinePlayer player = null;
        if (uuid != null && !uuid.isEmpty()) {
            UUID playerUUID = ValidationUtils.safeUUID(uuid);
            if (playerUUID == null) {
                throw new BadRequestResponse(Constants.INVALID_UUID);
            }

            player = Bukkit.getOfflinePlayer(playerUUID);
        }

        if (msg == null || msg.isEmpty()) {
            throw new BadRequestResponse(Constants.PAPI_MESSAGE_MISSING);
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            msg = PlaceholderAPI.setPlaceholders(player, msg);
        }
        return msg;
    }
}
