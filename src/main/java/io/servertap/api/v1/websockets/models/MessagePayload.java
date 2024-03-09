package io.servertap.api.v1.websockets.models;

public class MessagePayload {
    private String uuid;
    private String message;

    public MessagePayload(String message, String to) {
        this.message = message;
        this.uuid = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
