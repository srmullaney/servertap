package io.servertap.api.v1.websockets.models.events;

import com.google.gson.annotations.Expose;
import io.servertap.api.v1.models.Player;
import io.servertap.webhooks.models.events.WebhookEvent;

public class PlayerQuitEvent extends WebhookEvent {
    @Expose
    Player player;

    @Expose
    String quitMessage;

    public PlayerQuitEvent() {
        eventType = EventType.PlayerQuit;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getQuitMessage() {
        return quitMessage;
    }

    public void setQuitMessage(String quitMessage) {
        this.quitMessage = quitMessage;
    }
}
