package io.servertap.api.v1.models;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Objects;

public class Whitelist {
    @Expose
    private boolean enabled;
    @Expose
    private ArrayList<WhitelistPlayer> whitelistedPlayers;

    public Whitelist(ArrayList<WhitelistPlayer> whitelist, boolean enabled) {
        this.whitelistedPlayers = whitelist;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ArrayList<WhitelistPlayer> getWhitelistedPlayers() {
        return whitelistedPlayers;
    }

    public void setWhitelistedPlayers(ArrayList<WhitelistPlayer> whitelistedPlayers) {
        this.whitelistedPlayers = whitelistedPlayers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Whitelist whitelist1 = (Whitelist) o;
        return enabled == whitelist1.enabled && Objects.equals(whitelistedPlayers, whitelist1.whitelistedPlayers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, whitelistedPlayers);
    }
}
