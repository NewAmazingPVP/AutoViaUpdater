package bungeecord;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.bukkit.command.CommandExecutor;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import static common.BuildYml.createYamlFile;
import static common.UpdateVias.updateVia;

public final class AutoViaUpdater extends Plugin {

    private Configuration config;
    public boolean isViaVersionEnabled;
    public boolean isViaVersionDev;
    public boolean isViaBackwardsEnabled;
    public boolean isViaBackwardsDev;
    public boolean isViaRewindEnabled;
    public boolean isViaRewindDev;
    public boolean isViaRewindLegacyEnabled;

    @Override
    public void onEnable() {
        new Metrics(this, 18605);
        saveDefaultConfig();
        loadConfiguration();
        createYamlFile(getDataFolder().getAbsolutePath(), true);
        isViaVersionEnabled = config.getBoolean("ViaVersion.enabled");
        isViaVersionDev = config.getBoolean("ViaVersion.dev");
        isViaBackwardsEnabled = config.getBoolean("ViaBackwards.enabled");
        isViaBackwardsDev = config.getBoolean("ViaBackwards.dev");
        isViaRewindEnabled = config.getBoolean("ViaRewind.enabled");
        isViaRewindDev = config.getBoolean("ViaRewind.dev");
        isViaRewindLegacyEnabled = config.getBoolean("ViaRewind-Legacy.enabled");
        updateChecker();
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new UpdateCommand());
    }

    public void updateChecker() {
        long interval = config.getInt("Check-Interval");
        long updateInterval = interval*60;

        getProxy().getScheduler().schedule(this, () -> {
            getProxy().getScheduler().runAsync(this, this::checkUpdateVias);
        }, 0L, updateInterval, TimeUnit.SECONDS);
    }

    private void saveDefaultConfig() {
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadConfiguration() {
        File file = new File(getDataFolder(), "config.yml");
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void checkUpdateVias(){
        try {
            if (isViaVersionEnabled && !isViaVersionDev) {
                updateVia("ViaVersion", getDataFolder().getParent(), false);
            } else if (isViaVersionEnabled && isViaVersionDev) {
                updateVia("ViaVersion-Dev", getDataFolder().getParent(), true);
            }
            if (isViaBackwardsEnabled && !isViaBackwardsDev) {
                updateVia("ViaBackwards", getDataFolder().getParent(), false);
            } else if (isViaBackwardsEnabled && isViaBackwardsDev) {
                updateVia("ViaBackwards-Dev", getDataFolder().getParent(), true);
            }
            if (isViaRewindEnabled && !isViaRewindDev) {
                updateVia("ViaRewind", getDataFolder().getParent(), false);
            } else if (isViaRewindEnabled && isViaRewindDev) {
                updateVia("ViaRewind-Dev", getDataFolder().getParent(), true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public class UpdateCommand extends Command {

        public UpdateCommand() {
            super("updatevias", "autoviaupdater.admin");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            checkUpdateVias();
            sender.sendMessage(ChatColor.AQUA + "Update checker for vias successful!");
        }
    }

}

