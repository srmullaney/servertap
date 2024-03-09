package io.servertap.api.v1.websockets;

import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import io.servertap.ServerTapMain;
import io.servertap.api.v1.ApiV1Initializer;
import io.servertap.api.v1.websockets.models.Socket;
import io.servertap.api.v1.websockets.models.SocketID;
import io.servertap.utils.ConsoleListener;
import io.servertap.utils.pluginwrappers.EconomyWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class WebSocketHandler {
    private final Map<SocketID, Socket> sockets;
    private final ServerEventHandler serverEventHandler;
    private final ClientMessageHandler messageHandler;
    public WebSocketHandler(ServerTapMain main, Logger log, ApiV1Initializer api, EconomyWrapper economy, ConsoleListener consoleListener) {
        this.sockets = new HashMap<>();
        this.serverEventHandler = new ServerEventHandler(main, log, api, economy, sockets);
        this.messageHandler = new ClientMessageHandler(main, log, api, serverEventHandler, sockets);
    }
    public void getHandler(WsConfig ws) {
        ws.onConnect((ctx) -> {
            ctx.enableAutomaticPings(10, TimeUnit.SECONDS);
            sockets.put(new SocketID(ctx.getSessionId()), new Socket(ctx));
        });

        ws.onMessage(messageHandler::handle);

        ws.onClose(this::onDisconnect);

        ws.onError(this::onDisconnect);
    }
    private void onDisconnect(WsContext ctx) {
        SocketID id = new SocketID(ctx.getSessionId());
        sockets.remove(id);
        serverEventHandler.purgeSocket(id);
    }
}