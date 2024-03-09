package io.servertap.api.v1.websockets.models.events;

public class ServerMessage {
    private String event;
    private Object payload;

    public ServerMessage(String name, Object payload) {
        this.event = name;
        this.payload = payload;
    }

    public String getEvent() {
        return event;
    }

    public Object getPayload() {
        return payload;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
