package io.servertap.api.v1.websockets.models.events;

import io.servertap.utils.GsonSingleton;

public class ClientMessage {
    private String event;
    private String payload;

    public ClientMessage(String name, String payload) {
        this.event = name;
        this.payload = payload;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public <T> T getPayLoadAs(Class<T> clazz) {
        return GsonSingleton.getInstance().fromJson(payload, clazz);
    }
}
