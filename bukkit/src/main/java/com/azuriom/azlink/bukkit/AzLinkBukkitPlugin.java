package com.azuriom.azlink.bukkit;

import com.azuriom.azlink.bukkit.command.BukkitCommandExecutor;
import com.azuriom.azlink.bukkit.command.BukkitCommandSender;
import com.azuriom.azlink.common.AzLinkPlatform;
import com.azuriom.azlink.common.AzLinkPlugin;
import com.azuriom.azlink.common.PlatformType;
import com.azuriom.azlink.common.command.CommandSender;
import com.azuriom.azlink.common.data.WorldData;
import com.azuriom.azlink.common.logger.JulLoggerAdapter;
import com.azuriom.azlink.common.logger.LoggerAdapter;
import com.azuriom.azlink.common.tasks.TpsTask;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public final class AzLinkBukkitPlugin extends JavaPlugin implements AzLinkPlatform {

    private final AzLinkPlugin plugin = new AzLinkPlugin(this);

    private final TpsTask tpsTask = new TpsTask();

    private LoggerAdapter logger;

    @Override
    public void onLoad() {
        logger = new JulLoggerAdapter(getLogger());
    }

    @Override
    public void onEnable() {
        try {
            Class.forName("com.google.gson.JsonObject");

            Class.forName("io.netty.channel.Channel");
        } catch (ClassNotFoundException e) {
            logger.error("Your server version is not compatible with this version of AzLink !");
            logger.error("Please download AzLink Legacy on https://azuriom.com/azlink");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        plugin.init();

        getCommand("azlink").setExecutor(new BukkitCommandExecutor(plugin));

        getServer().getScheduler().runTaskTimer(this, tpsTask, 0, 1);
    }

    @Override
    public void onDisable() {
        plugin.shutdown();
    }

    @Override
    public AzLinkPlugin getPlugin() {
        return plugin;
    }

    @Override
    public LoggerAdapter getLoggerAdapter() {
        return logger;
    }

    @Override
    public PlatformType getPlatformType() {
        return PlatformType.BUKKIT;
    }

    @Override
    public String getPlatformName() {
        return getServer().getName();
    }

    @Override
    public String getPlatformVersion() {
        return getServer().getVersion();
    }

    @Override
    public String getPluginVersion() {
        return getDescription().getVersion();
    }

    @Override
    public Path getDataDirectory() {
        return getDataFolder().toPath();
    }

    @Override
    public Optional<WorldData> getWorldData() {
        int loadedChunks = getServer().getWorlds().stream()
                .mapToInt(w -> w.getLoadedChunks().length)
                .sum();

        int entities = getServer().getWorlds().stream().mapToInt(w -> w.getEntities().size()).sum();

        return Optional.of(new WorldData(tpsTask.getTps(), loadedChunks, entities));
    }

    @Override
    public Stream<CommandSender> getOnlinePlayers() {
        return getServer().getOnlinePlayers().stream().map(BukkitCommandSender::new);
    }

    @Override
    public int getMaxPlayers() {
        return getServer().getMaxPlayers();
    }

    @Override
    public void dispatchConsoleCommand(String command) {
        getServer().dispatchCommand(getServer().getConsoleSender(), command);
    }

    @Override
    public void executeSync(Runnable runnable) {
        getServer().getScheduler().runTask(this, runnable);
    }

    @Override
    public void executeAsync(Runnable runnable) {
        getServer().getScheduler().runTaskAsynchronously(this, runnable);
    }
}
