package io.servertap.api.v1.websockets;

import io.javalin.websocket.WsMessageContext;
import io.servertap.ServerTapMain;
import io.servertap.api.v1.ApiV1Initializer;
import io.servertap.api.v1.EconomyApi;
import io.servertap.api.v1.websockets.models.EconomyPayload;
import io.servertap.api.v1.websockets.models.MessagePayload;
import io.servertap.api.v1.websockets.models.Socket;
import io.servertap.api.v1.websockets.models.SocketID;
import io.servertap.api.v1.websockets.models.events.ClientMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ClientMessageHandler {
    private final ServerTapMain main;
    private final Logger log;
    private final ApiV1Initializer api;
    private final ServerEventHandler serverEventHandler;
    private final Map<SocketID, Socket> sockets;
    private final Map<Events, BiConsumer<Socket, ClientMessage>> events;
    public ClientMessageHandler(ServerTapMain main, Logger log, ApiV1Initializer api, ServerEventHandler serverEventHandler, Map<SocketID, Socket> sockets) {
        this.main = main;
        this.log = log;
        this.api = api;
        this.serverEventHandler = serverEventHandler;
        this.sockets = sockets;
        this.events = getEnabledEvents();
    }

    public void handle(WsMessageContext ctx) {
        Socket socket = sockets.get(new SocketID(ctx.getSessionId()));
        try {
            ClientMessage msg = ctx.messageAsClass(ClientMessage.class);

            if(msg.getEvent() == null) {
                socket.send("error", "Invalid JSON. Event key not found.");
                return;
            }
            if(msg.getPayload() == null) {
                socket.send("error", "Invalid JSON. Payload key not found.");
                return;
            }
            events.get(Events.fromText(msg.getEvent())).accept(socket, msg);
        } catch (NoSuchElementException e) {
            socket.send("error", "That event doesn't exist!");
        } catch (Exception e) {
            socket.send("error", "Invalid JSON payload. Please check your code. [Did you call JSON.stringify() on the obj you are sending?]");
        }
    }

    private Map<Events, BiConsumer<Socket, ClientMessage>> getEnabledEvents() {
        List<String> eventsConfig = main.getConfig().getStringList("websocket.clientEvents");
        return mapEvents()
                .entrySet()
                .stream()
                .filter(e -> eventsConfig.contains(e.getKey().getText()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<Events, BiConsumer<Socket, ClientMessage>> mapEvents() {
        Map<Events, BiConsumer<Socket, ClientMessage>> events = new HashMap<>();

        events.put(Events.CHAT_TELL, (socket, msg) -> {
            MessagePayload payload = msg.getPayLoadAs(MessagePayload.class);
            api.getServerApi().tell(payload.getUuid(), payload.getMessage());
        });

        events.put(Events.CHAT_BROADCAST, (socket, msg) -> {
            MessagePayload payload = msg.getPayLoadAs(MessagePayload.class);
            api.getServerApi().broadcastMsg(payload.getMessage());
        });

        events.put(Events.ECON_DEBIT, (socket, msg) -> {
            EconomyPayload payload = msg.getPayLoadAs(EconomyPayload.class);
            api.getEconomyApi().accountManager(payload.getUuid(), payload.getAmount(), EconomyApi.TransactionType.DEBIT);
        });

        events.put(Events.ECON_PAY, (socket, msg) -> {
            EconomyPayload payload = msg.getPayLoadAs(EconomyPayload.class);
            api.getEconomyApi().accountManager(payload.getUuid(), payload.getAmount(), EconomyApi.TransactionType.PAY);
        });

        events.put(Events.PLACEHOLDER_REPLACE, (socket, msg) -> {
            MessagePayload payload = msg.getPayLoadAs(MessagePayload.class);
            api.getPapiApi().replacePlaceholders(payload.getUuid(), payload.getMessage());
        });

        events.put(Events.PLUGIN_DOWNLOAD, (socket, msg) -> {

        });

        events.put(Events.SERVER_PING, (socket, msg) -> {
            socket.send(String.format("%s.response", Events.SERVER_PING.getText()), "pong");
        });

        events.put(Events.SERVER_EXEC, (socket, msg) -> {

        });

        events.put(Events.SERVER_WHITELIST_ADD, (socket, msg) -> {

        });

        events.put(Events.SERVER_WHITELIST_REMOVE, (socket, msg) -> {

        });

        events.put(Events.SERVER_WORLD_SAVE, (socket, msg) -> {

        });

        events.put(Events.SERVER_WORLDS_SAVE, (socket, msg) -> {

        });

        events.put(Events.SERVER_EVENTS_SUB, (socket, msg) -> {
            boolean status = serverEventHandler.subscribeTo(msg.getPayload(), socket.getID());
        });

        events.put(Events.SERVER_EVENTS_UNSUB, (socket, msg) -> {
            boolean status = serverEventHandler.unsubscribeFrom(msg.getPayload(), socket.getID());
        });

        events.put(Events.LEGACY_EXEC, (socket, msg) -> {
            String cmd = msg.getPayload().trim();
            log.info(cmd);
                if (!cmd.isEmpty()) {
                    if (cmd.startsWith("/")) { cmd = cmd.substring(1); }

                    final String command = cmd;
                    if (main != null) {
                        // Run the command on the main thread
                        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                            try {
                                CommandSender sender = Bukkit.getServer().getConsoleSender();
                                Bukkit.dispatchCommand(sender, command);
                            } catch (Exception e) {
                                // Just warn about the issue
                                log.warning("Couldn't execute command over websocket");
                            }
                        });
                    }
                }
        });
        return events;
    };
}
