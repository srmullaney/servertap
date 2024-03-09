package io.servertap.mojang.api;

import com.google.gson.Gson;
import io.servertap.mojang.api.models.PlayerInfo;
import io.servertap.utils.GsonSingleton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.stream.Collectors;

public class MojangApiService {
    private static final String getUuidResource = "https://api.mojang.com/users/profiles/minecraft/%s";
    private static final String getNameResource = "https://api.mojang.com/user/profile/%s";

    public static Optional<PlayerInfo> getPlayerInfoByUUIDOrName(String UUID, String name) throws IOException {
        if(name != null && !name.isEmpty()) return getPlayerInfoFromName(name);
        if(UUID != null && !UUID.isEmpty()) return getPlayerInfoFromUUID(UUID);
        return Optional.empty();
    }

    private static Optional<PlayerInfo> getPlayerInfo(String username, String url) throws IOException {
        Gson gson = GsonSingleton.getInstance();
        ApiResponse apiResponse = getApiResponse(String.format(url, username));
        PlayerInfo player = gson.fromJson(apiResponse.getContent(), PlayerInfo.class);
        return player == null ? Optional.empty() : Optional.of(player);
    }

    public static Optional<PlayerInfo> getPlayerInfoFromName(String username) throws IOException {
        return getPlayerInfo(username, getUuidResource);
    }

    public static Optional<PlayerInfo> getPlayerInfoFromUUID(String uuid) throws IOException {
        return getPlayerInfo(uuid, getNameResource);
    }

    private static ApiResponse getApiResponse(String resource) throws IOException {
        try {
            String responseContent;

            URL url = new URL(resource);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("GET");
            http.setDoInput(true);
            http.connect();

            if (http.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) return new ApiResponse("", http.getResponseCode());

            try (InputStream is = http.getInputStream()) {
                responseContent = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
            }

            return new ApiResponse(responseContent, http.getResponseCode());
        } catch (MalformedURLException ignored) {
            throw new IllegalArgumentException("The given resource string is not a valid URL.");
        }
    }

    private static <T> ApiResponse getApiResponse(String resource, T requestData) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    private static class ApiResponse {
        private String content;
        private int httpStatus;

        public ApiResponse(String content, int httpStatus) {
            setContent(content);
            setHttpStatus(httpStatus);
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getHttpStatus() {
            return httpStatus;
        }

        public void setHttpStatus(int httpStatus) {
            this.httpStatus = httpStatus;
        }
    }
}
