package spigot;

import common.CronScheduler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static common.BuildYml.createYamlFile;
import static common.BuildYml.updateBuildNumber;
import static common.UpdateVias.updateVia;

public final class AutoViaUpdater extends JavaPlugin {

    private FileConfiguration config;
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
    public boolean isViaRewindLegacyEnabled;
    public boolean isViaRewindLegacyDev;
    public boolean isViaRewindLegacySnapshot;
    private ScheduledExecutorService executor;
    private ScheduledFuture<?> updateTask;
    private final AtomicBoolean isChecking = new AtomicBoolean(false);

    @Override
    public void onEnable() {

        new Metrics(this, 18603);
        loadConfiguration();
        createYamlFile(getDataFolder().getAbsolutePath(), false);
        reloadSettings();
        ThreadFactory tf = r -> {
            Thread t = new Thread(r, "AutoViaUpdater-Worker");
            t.setDaemon(true);
            return t;
        };
        executor = Executors.newSingleThreadScheduledExecutor(tf);
        updateChecker();
        getCommand("updatevias").setExecutor(new UpdateCommand());
    }

    public void updateChecker() {
        config = getConfig();
        String cronExpression = config.getString("Cron-Expression", "");
        long interval = config.getLong("Check-Interval");
        long delay = config.getLong("Delay");

        if (!cronExpression.isEmpty()) {
            CronScheduler scheduler = new CronScheduler(cronExpression);
            updateTask = executor.scheduleAtFixedRate(() -> scheduler.runIfDue(v -> checkUpdateVias()),
                    Math.max(0, delay), 1, TimeUnit.SECONDS);
        } else {
            updateTask = executor.scheduleAtFixedRate(this::checkUpdateVias,
                    Math.max(0, delay), Math.max(1, interval) * 60L, TimeUnit.SECONDS);
        }
    }

    private void reloadSettings() {
        reloadConfig();
        config = getConfig();
        isViaVersionEnabled = config.getBoolean("ViaVersion.enabled");
        isViaVersionDev = config.getBoolean("ViaVersion.dev", false);
        isViaVersionSnapshot = config.getBoolean("ViaVersion.snapshot", true);
        isViaVersionJava8 = config.getBoolean("ViaVersion.java8", false);
        isViaBackwardsEnabled = config.getBoolean("ViaBackwards.enabled");
        isViaBackwardsDev = config.getBoolean("ViaBackwards.dev", false);
        isViaBackwardsSnapshot = config.getBoolean("ViaBackwards.snapshot", true);
        isViaBackwardsJava8 = config.getBoolean("ViaBackwards.java8", false);
        isViaRewindEnabled = config.getBoolean("ViaRewind.enabled");
        isViaRewindDev = config.getBoolean("ViaRewind.dev", false);
        isViaRewindSnapshot = config.getBoolean("ViaRewind.snapshot", true);
        isViaRewindJava8 = config.getBoolean("ViaRewind.java8", false);
        isViaRewindLegacyEnabled = config.getBoolean("ViaRewind-Legacy.enabled");
        isViaRewindLegacyDev = config.getBoolean("ViaRewind-Legacy.dev", false);
        isViaRewindLegacySnapshot = config.getBoolean("ViaRewind-Legacy.snapshot", true);
    }

    public void checkUpdateVias() {
        if (!isChecking.compareAndSet(false, true)) return;
        try {
            reloadSettings();
            AtomicBoolean hasVV = new AtomicBoolean(false);
            AtomicBoolean hasVB = new AtomicBoolean(false);
            AtomicBoolean hasVR = new AtomicBoolean(false);
            AtomicBoolean hasVRL = new AtomicBoolean(false);
            SchedulerAdapter.runGlobalAndWait(this, () -> {
                hasVV.set(Bukkit.getPluginManager().getPlugin("ViaVersion") != null);
                hasVB.set(Bukkit.getPluginManager().getPlugin("ViaBackwards") != null);
                hasVR.set(Bukkit.getPluginManager().getPlugin("ViaRewind") != null);
                hasVRL.set(Bukkit.getPluginManager().getPlugin("ViaRewind-Legacy-Support") != null);
            }, 10, TimeUnit.SECONDS);
            if (!hasVV.get()) updateBuildNumber("ViaVersion", -1);
            if (!hasVB.get()) updateBuildNumber("ViaBackwards", -1);
            if (!hasVR.get()) updateBuildNumber("ViaRewind", -1);
            if (!hasVRL.get()) updateBuildNumber("ViaRewind%20Legacy%20Support", -1);
            boolean shouldRestart = false;
            if (isViaVersionEnabled && updatePlugin("ViaVersion", isViaVersionSnapshot, isViaVersionDev, isViaVersionJava8)) {
                shouldRestart = true;
            }
            if (isViaBackwardsEnabled && updatePlugin("ViaBackwards", isViaBackwardsSnapshot, isViaBackwardsDev, isViaBackwardsJava8)) {
                shouldRestart = true;
            }
            if (isViaRewindEnabled && updatePlugin("ViaRewind", isViaRewindSnapshot, isViaRewindDev, isViaRewindJava8)) {
                shouldRestart = true;
            }
            if (isViaRewindLegacyEnabled && updatePlugin("ViaRewind%20Legacy%20Support", isViaRewindLegacySnapshot, isViaRewindLegacyDev, false)) {
                shouldRestart = true;
            }
            if (shouldRestart && config.getBoolean("AutoRestart")) {
                String raw = config.getString("AutoRestart-Message");
                String msg = org.bukkit.ChatColor.translateAlternateColorCodes('&', raw == null ? "" : raw);
                SchedulerAdapter.runGlobal(this, () -> Bukkit.broadcastMessage(msg));
                SchedulerAdapter.runGlobalDelayed(this, Bukkit::shutdown, config.getLong("AutoRestart-Delay") * 20L);
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

    public void loadConfiguration() {
        saveDefaultConfig();

        config = getConfig();
        config.addDefault("ViaVersion.enabled", true);
        config.addDefault("ViaVersion.snapshot", true);
        config.addDefault("ViaVersion.dev", false);
        config.addDefault("ViaVersion.java8", false);
        config.addDefault("ViaBackwards.enabled", true);
        config.addDefault("ViaBackwards.snapshot", true);
        config.addDefault("ViaBackwards.dev", false);
        config.addDefault("ViaBackwards.java8", false);
        config.addDefault("ViaRewind.enabled", true);
        config.addDefault("ViaRewind.snapshot", true);
        config.addDefault("ViaRewind.dev", false);
        config.addDefault("ViaRewind.java8", false);
        config.addDefault("ViaRewind-Legacy.enabled", true);
        config.addDefault("ViaRewind-Legacy.snapshot", true);
        config.addDefault("ViaRewind-Legacy.dev", false);
        config.addDefault("Check-Interval", 60);
        config.options().copyDefaults(true);
    }

    @Override
    public void onDisable() {
        if (updateTask != null) {
            updateTask.cancel(false);
            updateTask = null;
        }
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    public class UpdateCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            sender.sendMessage(ChatColor.YELLOW + "Checking for Via updates...");
            executor.execute(() -> {
                checkUpdateVias();
                SchedulerAdapter.runGlobal(AutoViaUpdater.this,
                        () -> sender.sendMessage(ChatColor.AQUA + "Update checker for vias completed!"));
            });
            return true;
        }
    }
}
