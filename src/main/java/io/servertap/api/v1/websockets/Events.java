package io.servertap.api.v1.websockets;

import java.util.Arrays;

public enum Events {
    // Client Events
    CHAT_BROADCAST("chat.broadcast"),
    CHAT_TELL("chat.tell"),
    ECON_DEBIT("economy.debit"),
    ECON_PAY("economy.pay"),
    PLACEHOLDER_REPLACE("placeholder.replace"),
    PLUGIN_DOWNLOAD("plugin.download"),
    SERVER_PING("server.ping"),
    SERVER_EXEC("server.exec"),
    SERVER_WHITELIST_ADD("server.whitelist.add"),
    SERVER_WHITELIST_REMOVE("server.whitelist.delete"),
    SERVER_WORLD_SAVE("server.world.save"),
    SERVER_WORLDS_SAVE("server.worlds.save"),
    SERVER_EVENTS_SUB("server.events.subscribe"),
    SERVER_EVENTS_UNSUB("server.events.unsubscribe"),
    LEGACY_EXEC("executeCommand"),

    // Server Events

    PLAYER_JOIN("player.join"),
    PLAYER_QUIT("player.quit"),
    PLAYER_KICKED("player.kicked"),
    SERVER_ONLINE_PLAYER_LIST_UPDATED("server.playerlist.online.updated"),
    SERVER_PLAYER_LIST_UPDATED("server.playerlist.all.updated"),
    SERVER_WORLD_DATA_UPDATED("server.worlds.updated"),
    SERVER_DATA_UPDATED("server.updated"),
    SERVER_WHITELIST_UPDATED("server.whitelist.updated"),
    SERVER_OPS_UPDATED("server.opslist.updated");

    private String text;

    private Events(String readableName) {
        this.text = readableName;
    }

    public String getText() {
        return this.text;
    }
    public static Events fromText(String text) {
        return Arrays.stream(values())
                .filter(event -> event.text.equalsIgnoreCase(text))
                .findFirst().get();
    }
}
