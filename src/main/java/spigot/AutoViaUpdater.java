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
    public boolean isViaBackwardsEnabled;
    public boolean isViaBackwardsDev;
    public boolean isViaRewindEnabled;
    public boolean isViaRewindDev;
    public boolean isViaRewindLegacyEnabled;

    @Override
    public void onEnable() {

        new Metrics(this, 18603);
        loadConfiguration();
        createYamlFile(getDataFolder().getAbsolutePath(), false);
        isViaVersionEnabled = getConfig().getBoolean("ViaVersion.enabled");
        isViaVersionDev = getConfig().getBoolean("ViaVersion.dev");
        isViaBackwardsEnabled = getConfig().getBoolean("ViaBackwards.enabled");
        isViaBackwardsDev = getConfig().getBoolean("ViaBackwards.dev");
        isViaRewindEnabled = getConfig().getBoolean("ViaRewind.enabled");
        isViaRewindDev = getConfig().getBoolean("ViaRewind.dev");
        isViaRewindLegacyEnabled = getConfig().getBoolean("ViaRewind-Legacy.enabled");
        updateChecker();
        getCommand("updatevias").setExecutor(new UpdateCommand());
    }

    public void updateChecker() {
        config = getConfig();
        long interval = config.getInt("Check-Interval");
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                checkUpdateVias();
            }
        }, 1200L, 20L * 60L * interval);
    }

    public void checkUpdateVias(){
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
            if (isViaRewindLegacyEnabled) {
                updateVia("ViaRewind%20Legacy%20Support", getDataFolder().getParent(), false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadConfiguration(){
        saveDefaultConfig();

        config = getConfig();
        config.addDefault("ViaVersion.enabled", true);
        config.addDefault("ViaVersion.dev", false);
        config.addDefault("ViaBackwards.enabled", true);
        config.addDefault("ViaBackwards.dev", false);
        config.addDefault("ViaRewind.enabled", true);
        config.addDefault("ViaRewind.dev", false);
        config.addDefault("ViaRewind-Legacy.enabled", true);
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
