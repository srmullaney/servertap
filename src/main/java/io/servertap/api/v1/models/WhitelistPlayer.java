package io.servertap.api.v1.models;

import com.google.gson.annotations.Expose;
import org.bukkit.OfflinePlayer;

public class WhitelistPlayer {
    @Expose
    private String uuid = null;

    @Expose
    private String name = null;

    public WhitelistPlayer offlinePlayer(OfflinePlayer player) {
        this.uuid = player.getUniqueId().toString();
        this.name = player.getName();
        return this;
    }

    public WhitelistPlayer uuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    /**
     * The Player's UUID
     *
     * @return uuid
     **/
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public WhitelistPlayer name(String displayName) {
        this.name = displayName;
        return this;
    }

    /**
     * The Player's display name
     *
     * @return displayName
     **/
    public String getName() {
        return name;
    }

    public void setName(String displayName) {
        this.name = displayName;
    }

    public boolean equals(WhitelistPlayer whitelist) {
        return whitelist.getUuid().equals(this.uuid);
    }
}