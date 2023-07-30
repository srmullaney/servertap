package io.servertap.webhooks;

import com.google.gson.Gson;
import io.servertap.ServerTapMain;
import io.servertap.api.v1.models.ItemStack;
import io.servertap.api.v1.models.Player;
import io.servertap.utils.pluginwrappers.EconomyWrapper;
import io.servertap.utils.GsonSingleton;
import io.servertap.webhooks.models.events.*;
import io.servertap.utils.NormalizeMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class WebhookEventListener implements Listener {
    private final Logger log;
    private final ServerTapMain main;
    private final EconomyWrapper economyWrapper;
    private List<Webhook> webhooks;

    public WebhookEventListener(ServerTapMain main, FileConfiguration bukkitConfig, Logger logger, EconomyWrapper economyWrapper) {
        this.main = main;
        this.log = logger;
        this.economyWrapper = economyWrapper;

        loadWebhooksFromConfig(bukkitConfig);
    }

    public void loadWebhooksFromConfig(FileConfiguration bukkitConfig) {
        webhooks = getWebhooksFromConfig(bukkitConfig);
    }

    private List<Webhook> getWebhooksFromConfig(FileConfiguration bukkitConfig) {
        final List<Webhook> configWebhooks = new ArrayList<>();

        ConfigurationSection webhookSection = bukkitConfig.getConfigurationSection("webhooks");
        if (webhookSection == null) {
            return configWebhooks;
        }

        Set<String> webhookNames = webhookSection.getKeys(false);

        for (String webhookName : webhookNames) {
            String configPath = "webhooks." + webhookName + ".";

            Webhook.getWebhookFromConfig(bukkitConfig, webhookName, configPath, log)
                    .ifPresent(configWebhooks::add);
        }
        return configWebhooks;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        PlayerChatWebhookEvent eventModel = new PlayerChatWebhookEvent();

        eventModel.setPlayer(Player.fromBukkitPlayer(event.getPlayer(), economyWrapper));
        eventModel.setMessage(NormalizeMessage.normalize(event.getMessage(), main));
        eventModel.setPlayerName(event.getPlayer().getDisplayName());

        broadcastEvent(eventModel, WebhookEvent.EventType.PlayerChat);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        PlayerDeathWebhookEvent eventModel = new PlayerDeathWebhookEvent();

        Player player = Player.fromBukkitPlayer(event.getEntity(), economyWrapper);
        List<ItemStack> drops = new ArrayList<>();
        event.getDrops().forEach(itemStack -> drops.add(fromBukkitItemStack(itemStack)));

        eventModel.setPlayer(player);
        eventModel.setDrops(drops);
        eventModel.setDeathMessage(NormalizeMessage.normalize(event.getDeathMessage(), main));

        broadcastEvent(eventModel, WebhookEvent.EventType.PlayerDeath);
    }

    private ItemStack fromBukkitItemStack(org.bukkit.inventory.ItemStack itemStack) {
        ItemStack i = new ItemStack();
        i.setId("minecraft:" + itemStack.getType().toString().toLowerCase());
        i.setCount(itemStack.getAmount());
        i.setSlot(-1);
        return i;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerJoinWebhookEvent eventModel = new PlayerJoinWebhookEvent();

        Player player = Player.fromBukkitPlayer(event.getPlayer(), economyWrapper);

        eventModel.setPlayer(player);
        eventModel.setJoinMessage(NormalizeMessage.normalize(event.getJoinMessage(), main));

        broadcastEvent(eventModel, WebhookEvent.EventType.PlayerJoin);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerQuitWebhookEvent eventModel = new PlayerQuitWebhookEvent();

        Player player = Player.fromBukkitPlayer(event.getPlayer(), economyWrapper);

        eventModel.setPlayer(player);
        eventModel.setQuitMessage(NormalizeMessage.normalize(event.getQuitMessage(), main));

        broadcastEvent(eventModel, WebhookEvent.EventType.PlayerQuit);
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        PlayerKickWebhookEvent eventModel = new PlayerKickWebhookEvent();

        Player player = Player.fromBukkitPlayer(event.getPlayer(), economyWrapper);

        eventModel.setPlayer(player);
        eventModel.setReason(NormalizeMessage.normalize(event.getReason(), main));

        broadcastEvent(eventModel, WebhookEvent.EventType.PlayerKick);
    }

    private void broadcastEvent(WebhookEvent eventModel, WebhookEvent.EventType eventType) {
        for (Webhook webhook : webhooks) {
            List<WebhookEvent.EventType> registeredEvents = webhook.getRegisteredEvents();

            if (!registeredEvents.contains(eventType)) {
                continue;
            }

            Bukkit.getScheduler().runTaskAsynchronously(main, () -> sendHttpRequest(eventModel, webhook));
        }
    }

    private static void sendHttpRequest(WebhookEvent eventModel, Webhook webhook) {
        try {
            Gson gson = GsonSingleton.getInstance();
            String jsonContent = gson.toJson(eventModel);
            byte[] output = jsonContent.getBytes(StandardCharsets.UTF_8);

            URL url = new URL(webhook.getListenerUrl());
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setFixedLengthStreamingMode(output.length);
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            http.connect();
            try (OutputStream os = http.getOutputStream()) {
                os.write(output);
            }
        } catch (MalformedURLException ignored) {
            //This branch should never be reached, since all listeners are validated in the constructor
        } catch (IOException ignored) {}
    }
}
