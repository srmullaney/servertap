package io.servertap.api.v1.websockets;

import io.servertap.ServerTapMain;
import io.servertap.api.v1.ApiV1Initializer;
import io.servertap.api.v1.websockets.models.Socket;
import io.servertap.api.v1.websockets.models.SocketID;
import io.servertap.utils.pluginwrappers.EconomyWrapper;
import org.bukkit.Bukkit;
import org.w3c.dom.events.Event;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ServerEventHandler {
    private final ServerTapMain main;
    private final Map<SocketID, Socket> sockets;
    private final Map<Events, Set<SocketID>> eventsSubsMap;
    public ServerEventHandler(ServerTapMain main, Logger log, ApiV1Initializer api, EconomyWrapper economy, Map<SocketID, Socket> sockets) {
        this.main = main;
        this.sockets = sockets;
        this.eventsSubsMap = configureSubscribableEvents();
        new ServerEventListener(main, log, api, economy, this);
    }

    public void handle(Events event, Object eventObj) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            eventsSubsMap.get(event).forEach((socketID -> sockets.get(socketID).send(event.getText(), eventObj)));
        });
    }

    public void purgeSocket(SocketID id) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> eventsSubsMap.forEach((ignore, sockets) -> sockets.remove(id)));
    }

    public boolean subscribeTo(String event, SocketID id) {
        return eventsSubsMap.get(Events.fromText(event)).add(id);
    }

    public boolean unsubscribeFrom(String event, SocketID id) {
        return eventsSubsMap.get(Events.fromText(event)).remove(id);
    }

    private Map<Events, Set<SocketID>> configureSubscribableEvents() {
        List<String> events = main.getConfig().getStringList("websocket.serverEvents");
        return Arrays.stream(Events.values())
                .filter((event) -> events.contains(event.getText()))
                .distinct()
                .collect(Collectors.toMap(e -> e, e -> new HashSet<>()));
    }
}
