package io.servertap.api.v1.websockets;

import io.servertap.ServerTapMain;
import io.servertap.api.v1.ApiV1Initializer;
import io.servertap.api.v1.models.Player;
import io.servertap.api.v1.websockets.models.events.PlayerJoinEvent;
import io.servertap.api.v1.websockets.models.events.PlayerKickEvent;
import io.servertap.api.v1.websockets.models.events.PlayerQuitEvent;
import io.servertap.custom.events.BanListUpdatedAsyncEvent;
import io.servertap.custom.events.IpBanListUpdatedAsyncEvent;
import io.servertap.custom.events.OperatorListUpdatedAsyncEvent;
import io.servertap.custom.events.WhitelistUpdatedAsyncEvent;
import io.servertap.utils.FileWatcher;
import io.servertap.utils.NormalizeMessage;
import io.servertap.utils.pluginwrappers.EconomyWrapper;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ServerEventListener {
    private final ServerTapMain main;
    private final ApiV1Initializer api;
    private final EconomyWrapper economy;
    private final BukkitScheduler scheduler;
    private final FileConfiguration bukkitConfig;
    private final ServerEventHandler serverEventHandler;

    public ServerEventListener(ServerTapMain main, Logger log, ApiV1Initializer api, EconomyWrapper economy, ServerEventHandler serverEventHandler) {
        this.main = main;
        this.api = api;
        this.economy = economy;
        this.serverEventHandler = serverEventHandler;
        this.scheduler = Bukkit.getServer().getScheduler();
        this.bukkitConfig = main.getConfig();
        starFileWatcher(main, log);
        registerListeners();
    }

    private void starFileWatcher(ServerTapMain main, Logger log) {
        PluginManager pm = Bukkit.getPluginManager();
        FileWatcher fileWatcher = new FileWatcher(main, log);
        fileWatcher.watch("ops.json", ".", () -> pm.callEvent(new OperatorListUpdatedAsyncEvent()));
        fileWatcher.watch("whitelist.json", ".", () -> pm.callEvent(new WhitelistUpdatedAsyncEvent()));
        fileWatcher.watch("banned-players.json", ".", () -> pm.callEvent(new BanListUpdatedAsyncEvent()));
        fileWatcher.watch("banned-ips.json", ".", () -> pm.callEvent(new IpBanListUpdatedAsyncEvent()));
        fileWatcher.start();
    }

    private void registerListeners() {
        Map<Events, Supplier<Listener>> eventListeners = mapListeners();
        List<String> events = bukkitConfig.getStringList("websocket.serverEvents");

        events.stream()
                .filter((event) -> eventListeners.containsKey(Events.fromText(event)))
                .distinct()
                .forEach((event) -> registerListener(eventListeners.get(Events.fromText(event)).get()));
    }


    /**
     * Returns a Map Mops events to their respective listeners
     * Used to figure out if the user has enabled an event in the config and register it
     * @return ListenerMap
     */
    private Map<Events, Supplier<Listener>> mapListeners() {
        Map<Events, Supplier<Listener>> listenerMap = new HashMap<>();
        listenerMap.put(Events.PLAYER_JOIN, ServerEventListener.PlayerJoinListener::new);
        listenerMap.put(Events.PLAYER_QUIT, ServerEventListener.PlayerQuitListener::new);
        listenerMap.put(Events.PLAYER_KICKED, ServerEventListener.PlayerKickedListener::new);
        listenerMap.put(Events.SERVER_ONLINE_PLAYER_LIST_UPDATED, ServerEventListener.UpdateOnlinePlayersListListeners::new);
        listenerMap.put(Events.SERVER_PLAYER_LIST_UPDATED, ServerEventListener.UpdateAllPlayersListListeners::new);
        listenerMap.put(Events.SERVER_WORLD_DATA_UPDATED, ServerEventListener.UpdateWorldsDataListeners::new);
        listenerMap.put(Events.SERVER_DATA_UPDATED, ServerEventListener.UpdateServerDataListeners::new);
        listenerMap.put(Events.SERVER_WHITELIST_UPDATED, ServerEventListener.UpdateWhitelistListListeners::new);
        listenerMap.put(Events.SERVER_OPS_UPDATED, ServerEventListener.UpdateOperatorsListListeners::new);
        return listenerMap;
    }

    private void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, main);
    }

    // Event Handlers

    private void runOnNextTick(Runnable callback) {
        scheduler.runTask(main, callback);
    }

    private void onPlayerJoinHandler(org.bukkit.event.player.PlayerJoinEvent event) {
        PlayerJoinEvent eventModel = new PlayerJoinEvent();
        Player player = Player.fromBukkitPlayer(event.getPlayer(), economy);

        eventModel.setPlayer(player);
        eventModel.setJoinMessage(NormalizeMessage.normalize(event.getJoinMessage(), main));
        runOnNextTick(() -> serverEventHandler.handle(Events.PLAYER_JOIN, eventModel));
    }

    private void onPlayerQuitHandler(org.bukkit.event.player.PlayerQuitEvent event) {
        PlayerQuitEvent eventModel = new PlayerQuitEvent();
        Player player = Player.fromBukkitPlayer(event.getPlayer(), economy);

        eventModel.setPlayer(player);
        eventModel.setQuitMessage(NormalizeMessage.normalize(event.getQuitMessage(), main));
        runOnNextTick(() -> serverEventHandler.handle(Events.PLAYER_QUIT, eventModel));
    }

    private void onPlayerKickHandler(org.bukkit.event.player.PlayerKickEvent event) {
        PlayerKickEvent eventModel = new PlayerKickEvent();
        Player player = Player.fromBukkitPlayer(event.getPlayer(), economy);

        eventModel.setPlayer(player);
        eventModel.setReason(NormalizeMessage.normalize(event.getReason(), main));
        runOnNextTick(() -> serverEventHandler.handle(Events.PLAYER_KICKED, eventModel));
    }

    private void updateOnlinePlayersList(PlayerEvent event) {
        runOnNextTick(() -> serverEventHandler.handle(Events.SERVER_ONLINE_PLAYER_LIST_UPDATED, api.getPlayerApi().getOninePlayers()));
    }

    private void updateAllPlayersList() {
        runOnNextTick(() -> serverEventHandler.handle(Events.SERVER_PLAYER_LIST_UPDATED, api.getPlayerApi().getAllPlayers()));
    }

    private void updateWorldsInfo() {
        runOnNextTick(() -> serverEventHandler.handle(Events.SERVER_WORLD_DATA_UPDATED, api.getWorldApi().getWorlds()));
    }

    private void updateServerInfo() {
        runOnNextTick(() -> serverEventHandler.handle(Events.SERVER_DATA_UPDATED, api.getServerApi().getServer()));
    }

    private void updateWhitelistList() {
        runOnNextTick(() -> serverEventHandler.handle(Events.SERVER_WHITELIST_UPDATED, api.getServerApi().getWhitelist()));
    }

    private void updateOperatorsList() {
        runOnNextTick(() -> serverEventHandler.handle(Events.SERVER_OPS_UPDATED, api.getServerApi().getOpsList()));
    }

    // Bukkit Event Listeners

    private class PlayerJoinListener implements Listener {
        @EventHandler
        public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) { onPlayerJoinHandler(event); }
    }

    private class PlayerQuitListener implements Listener {
        @EventHandler
        public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) { onPlayerQuitHandler(event); }
    }

    private class PlayerKickedListener implements Listener {
        @EventHandler
        public void onPlayerKick(org.bukkit.event.player.PlayerKickEvent event) {
            onPlayerKickHandler(event);
        }
    }

    private class UpdateOnlinePlayersListListeners implements Listener {
        @EventHandler
        public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) { updateOnlinePlayersList(event); }
        @EventHandler
        public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) { updateOnlinePlayersList(event); }
        @EventHandler
        public void onPlayerDeath(PlayerDeathEvent event) { updateOnlinePlayersList(null); }
        @EventHandler
        public void onOperatorListUpdated(OperatorListUpdatedAsyncEvent event) { updateOnlinePlayersList(null); }
        @EventHandler
        public void onPlayerChangedWorld(PlayerChangedWorldEvent event) { updateOnlinePlayersList(event); }
        @EventHandler
        public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) { updateOnlinePlayersList(event); }
        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) { updateOnlinePlayersList(null); }
        @EventHandler
        public void onEntityRegainHealth(EntityRegainHealthEvent event) { updateOnlinePlayersList(null); }
    }

    private class UpdateAllPlayersListListeners implements Listener {
        @EventHandler
        public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) { updateAllPlayersList(); }
    }

    private class UpdateWorldsDataListeners implements Listener {
        @EventHandler
        public void onWeatherChange(WeatherChangeEvent event) { updateWorldsInfo(); }

        @EventHandler
        public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
            String command = event.getMessage();
            if(command.contains("/gamerule") && (command.contains("true") || command.contains("false")))
                updateWorldsInfo();
            if(command.contains("/difficulty") && command.length() > 11)
                updateWorldsInfo();
        }

        @EventHandler
        public void onServerCommand(ServerCommandEvent event) {
            String command = event.getCommand();
            if(command.contains("gamerule") && (command.contains("true") || command.contains("false")))
                updateWorldsInfo();
            if(command.contains("difficulty") && command.length() > 11)
                updateWorldsInfo();
        }
    }

    private class UpdateServerDataListeners implements Listener {
        @EventHandler
        public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) { updateServerInfo(); }
        @EventHandler
        public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) { updateServerInfo(); }
        @EventHandler
        public void onWhitelistUpdated(WhitelistUpdatedAsyncEvent event) { updateServerInfo(); }

        @EventHandler
        public void onBanListUpdated(BanListUpdatedAsyncEvent event) { updateServerInfo(); }

        @EventHandler
        public void onIpBanListUpdated(IpBanListUpdatedAsyncEvent event) { updateServerInfo(); }
    }

    private class UpdateWhitelistListListeners implements Listener {
        @EventHandler
        public void onWhitelistUpdated(WhitelistUpdatedAsyncEvent event) { updateWhitelistList(); }
    }

    private class UpdateOperatorsListListeners implements Listener {
        @EventHandler
        public void onOperatorListUpdated(OperatorListUpdatedAsyncEvent event) { updateOperatorsList(); }
    }
}
