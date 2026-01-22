package bungeecord;

import common.CronScheduler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import static common.BuildYml.createYamlFile;
import static common.BuildYml.updateBuildNumber;
import static common.UpdateVias.updateVia;

public final class AutoViaUpdater extends Plugin {

    private Configuration config;
    private final java.util.concurrent.atomic.AtomicBoolean isChecking = new java.util.concurrent.atomic.AtomicBoolean(false);
    public boolean isViaVersionEnabled;
    public boolean isViaVersionDev;
    public boolean isViaVersionSnapshot;
    public boolean isViaVersionJava8;
    public boolean isViaBackwardsEnabled;
    public boolean isViaBackwardsDev;
    public boolean isViaBackwardsSnapshot;
    public boolean isViaBackwardsJava8;
    public boolean isViaRewindEnabled;
    public boolean isViaRewindDev;
    public boolean isViaRewindSnapshot;
    public boolean isViaRewindJava8;

    @Override
    public void onEnable() {
        new Metrics(this, 18605);
        saveDefaultConfig();
        loadConfiguration();
        createYamlFile(getDataFolder().getAbsolutePath(), true);
        reloadSettings();
        updateChecker();
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new UpdateCommand());
    }

    public void updateChecker() {
        String cronExpression = config.getString("Cron-Expression", "");
        long interval = config.getLong("Check-Interval");
        long delay = config.getLong("Delay");

        if (!cronExpression.isEmpty()) {
            CronScheduler scheduler = new CronScheduler(cronExpression);
            getProxy().getScheduler().schedule(this, () -> scheduler.runIfDue(v -> checkUpdateVias()), delay, 1, TimeUnit.SECONDS);
        } else {
            getProxy().getScheduler().schedule(this, this::checkUpdateVias, delay, interval * 60, TimeUnit.SECONDS);
        }
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

    private void reloadSettings() {
        loadConfiguration();
        isViaVersionEnabled = config.getBoolean("ViaVersion.enabled", true);
        isViaVersionSnapshot = config.getBoolean("ViaVersion.snapshot", true);
        isViaVersionDev = config.getBoolean("ViaVersion.dev", false);
        isViaVersionJava8 = config.getBoolean("ViaVersion.java8", false);
        isViaBackwardsEnabled = config.getBoolean("ViaBackwards.enabled", true);
        isViaBackwardsSnapshot = config.getBoolean("ViaBackwards.snapshot", true);
        isViaBackwardsDev = config.getBoolean("ViaBackwards.dev", false);
        isViaBackwardsJava8 = config.getBoolean("ViaBackwards.java8", false);
        isViaRewindEnabled = config.getBoolean("ViaRewind.enabled", true);
        isViaRewindSnapshot = config.getBoolean("ViaRewind.snapshot", true);
        isViaRewindDev = config.getBoolean("ViaRewind.dev", false);
        isViaRewindJava8 = config.getBoolean("ViaRewind.java8", false);
    }

    public void checkUpdateVias() {
        if (!isChecking.compareAndSet(false, true)) return;
        try {
            reloadSettings();
            if (getProxy().getPluginManager().getPlugin("ViaVersion") == null) {
                updateBuildNumber("ViaVersion", -1);
            }
            if (getProxy().getPluginManager().getPlugin("ViaBackwards") == null) {
                updateBuildNumber("ViaBackwards", -1);
            }
            if (getProxy().getPluginManager().getPlugin("ViaRewind") == null) {
                updateBuildNumber("ViaRewind", -1);
            }
            boolean shouldRestart = isViaVersionEnabled && updatePlugin("ViaVersion", isViaVersionSnapshot, isViaVersionDev, isViaVersionJava8);
            if (isViaBackwardsEnabled && updatePlugin("ViaBackwards", isViaBackwardsSnapshot, isViaBackwardsDev, isViaBackwardsJava8)) {
                shouldRestart = true;
            }
            if (isViaRewindEnabled && updatePlugin("ViaRewind", isViaRewindSnapshot, isViaRewindDev, isViaRewindJava8)) {
                shouldRestart = true;
            }
            if (shouldRestart && config.getBoolean("AutoRestart")) {
                String raw = config.getString("AutoRestart-Message");
                String colored = net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', raw == null ? "" : raw);
                getProxy().broadcast(colored);
                getProxy().getScheduler().schedule(this, () -> getProxy().stop(), config.getLong("AutoRestart-Delay"), TimeUnit.SECONDS);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            isChecking.set(false);
        }
    }

    private boolean updatePlugin(String pluginName, boolean isSnapshot, boolean isDev, boolean isJava8) throws IOException {
        return updateVia(pluginName, getDataFolder().getParent(), isSnapshot, isDev, isJava8);
    }

    public class UpdateCommand extends Command {

        public UpdateCommand() {
            super("updatevias", "autoviaupdater.admin");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            sender.sendMessage(ChatColor.YELLOW + "Checking for Via updates...");
            getProxy().getScheduler().runAsync(AutoViaUpdater.this, () -> {
                checkUpdateVias();
                sender.sendMessage(ChatColor.AQUA + "Update checker for vias completed!");
            });
        }
    }

}
