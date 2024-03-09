package io.servertap.api.v1.websockets.models;

public class PlayerInfoPayload {
    private String uuid;
    private String displayName;

    public PlayerInfoPayload(String uuid, String displayName) {
        this.uuid = uuid;
        this.displayName = displayName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
