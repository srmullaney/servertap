package io.servertap.api.v1.websockets.models.events;

import com.google.gson.annotations.Expose;
import io.servertap.api.v1.models.Player;
import io.servertap.webhooks.models.events.WebhookEvent;

public class PlayerJoinEvent extends WebhookEvent {
    @Expose
    Player player;

    @Expose
    String joinMessage;

    public PlayerJoinEvent() {
        eventType = EventType.PlayerJoin;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getJoinMessage() {
        return joinMessage;
    }

    public void setJoinMessage(String joinMessage) {
        this.joinMessage = joinMessage;
    }
}
