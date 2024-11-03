package spigot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

import static common.BuildYml.updateBuildNumber;
import static common.UpdateVias.updateVia;
import static common.BuildYml.createYamlFile;

public final class AutoViaUpdater extends JavaPlugin {

    private FileConfiguration config;
    public boolean isViaVersionEnabled;
    public boolean isViaVersionDev;
    public boolean isViaVersionJava8;
    public boolean isViaBackwardsEnabled;
    public boolean isViaBackwardsDev;
    public boolean isViaBackwardsJava8;
    public boolean isViaRewindEnabled;
    public boolean isViaRewindDev;
    public boolean isViaRewindJava8;
    public boolean isViaRewindLegacyEnabled;
    public boolean isViaRewindLegacyDev;
    public boolean isViaRewindLegacyJava8;

    @Override
    public void onEnable() {
        new Metrics(this, 18603);
        loadConfiguration();
        createYamlFile(getDataFolder().getAbsolutePath(), false);

        isViaVersionEnabled = getConfig().getBoolean("ViaVersion.enabled");
        isViaVersionDev = getConfig().getBoolean("ViaVersion.dev");
        isViaVersionJava8 = getConfig().getBoolean("ViaVersion.java8build");

        isViaBackwardsEnabled = getConfig().getBoolean("ViaBackwards.enabled");
        isViaBackwardsDev = getConfig().getBoolean("ViaBackwards.dev");
        isViaBackwardsJava8 = getConfig().getBoolean("ViaBackwards.java8build");

        isViaRewindEnabled = getConfig().getBoolean("ViaRewind.enabled");
        isViaRewindDev = getConfig().getBoolean("ViaRewind.dev");
        isViaRewindJava8 = getConfig().getBoolean("ViaRewind.java8build");

        isViaRewindLegacyEnabled = getConfig().getBoolean("ViaRewind-Legacy.enabled");
        isViaRewindLegacyDev = getConfig().getBoolean("ViaRewind-Legacy.dev");

        updateChecker();
        getCommand("updatevias").setExecutor(new UpdateCommand());
    }

    public void updateChecker() {
        long interval = config.getInt("Check-Interval");
        long delay = config.getInt("Delay");
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::checkUpdateVias, delay * 20L, 20L * 60L * interval);
    }

    public void checkUpdateVias() {
        try {
            if(Bukkit.getPluginManager().getPlugin("ViaVersion") == null){
                updateBuildNumber("ViaVersion", -1);
            }
            if(Bukkit.getPluginManager().getPlugin("ViaBackwards") == null){
                updateBuildNumber("ViaBackwards", -1);
            }
            if(Bukkit.getPluginManager().getPlugin("ViaRewind") == null){
                updateBuildNumber("ViaRewind", -1);
            }
            if(Bukkit.getPluginManager().getPlugin("ViaRewind-Legacy-Support") == null){
                updateBuildNumber("ViaRewind%20Legacy%20Support", -1);
            }
            if (isViaVersionEnabled) {
                updateAndRestart("ViaVersion", isViaVersionDev, isViaVersionJava8);
            }
            if (isViaBackwardsEnabled) {
                updateAndRestart("ViaBackwards", isViaBackwardsDev, isViaBackwardsJava8);
            }
            if (isViaRewindEnabled) {
                updateAndRestart("ViaRewind", isViaRewindDev, isViaRewindJava8);
            }
            if (isViaRewindLegacyEnabled) {
                updateAndRestart("ViaRewind-Legacy", isViaRewindLegacyDev, isViaRewindLegacyJava8);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateAndRestart(String pluginName, boolean isDev, boolean isJava8) throws IOException {
        String pluginKey = isJava8 ? pluginName + "-Java8" : (isDev ? pluginName + "-Dev" : pluginName);
        if (updateVia(pluginKey, getDataFolder().getParent(), isDev, isJava8) && getConfig().getBoolean("AutoRestart")) {
            Bukkit.broadcastMessage(config.getString("AutoRestart-Message"));
            Bukkit.getScheduler().runTaskLater(this, Bukkit::shutdown, config.getLong("AutoRestart-Delay") * 20L);
        }
    }

    public void loadConfiguration() {
        saveDefaultConfig();
        config = getConfig();
        config.addDefault("ViaVersion.enabled", true);
        config.addDefault("ViaVersion.dev", false);
        config.addDefault("ViaVersion.java8build", false);
        config.addDefault("ViaBackwards.enabled", true);
        config.addDefault("ViaBackwards.dev", false);
        config.addDefault("ViaBackwards.java8build", false);
        config.addDefault("ViaRewind.enabled", true);
        config.addDefault("ViaRewind.dev", false);
        config.addDefault("ViaRewind.java8build", false);
        config.addDefault("ViaRewind-Legacy.enabled", true);
        config.addDefault("ViaRewind-Legacy.dev", false);
        config.addDefault("Check-Interval", 60);
        config.options().copyDefaults(true);
        saveConfig();
    }

    public class UpdateCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            checkUpdateVias();
            sender.sendMessage(ChatColor.AQUA + "Update checker for vias successful!");
            return true;
        }
    }
}
