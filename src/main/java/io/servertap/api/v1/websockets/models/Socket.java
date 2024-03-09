package io.servertap.api.v1.websockets.models;

import io.javalin.websocket.WsConnectContext;
import io.servertap.api.v1.websockets.models.events.ServerMessage;

public class Socket {
    private final SocketID id;
    private final WsConnectContext ctx;
    public Socket(WsConnectContext ctx) {
        this.id = new SocketID(ctx.getSessionId());
        this.ctx = ctx;
    }

    public void send(String event, Object obj) {
        ctx.send(new ServerMessage(event, obj));
    }

    public SocketID getID() {
        return id;
    }
}
