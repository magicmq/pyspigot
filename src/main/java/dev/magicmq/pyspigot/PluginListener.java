package dev.magicmq.pyspigot;

import dev.magicmq.pyspigot.config.PluginConfig;
import dev.magicmq.pyspigot.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PluginListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("pyspigot.admin")) {
            Bukkit.getScheduler().runTaskLater(PySpigot.get(), () -> PySpigot.get().checkVersion((version) -> {
                StringUtils.Version thisVersion = new StringUtils.Version(PySpigot.get().getDescription().getVersion());
                StringUtils.Version latestVersion = new StringUtils.Version(version);
                if (thisVersion.compareTo(latestVersion) < 0) {
                    player.sendMessage(PluginConfig.getPrefix() + ChatColor.RED + "You're running an outdated version of PySpigot. The latest version is " + version + ". Download it here: " + ChatColor.RED + ChatColor.UNDERLINE + "https://www.spigotmc.org/resources/pyspigot.111006/");
                }
            }), 10L);
        }
    }
}
