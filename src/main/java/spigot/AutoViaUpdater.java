package spigot;

import common.ViaBackwards;
import common.ViaRewind;
import common.ViaRewindLegacySupport;
import common.ViaVersion;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static commonrework.UpdateVias.updateVia;
import static commonrework.YamlFileManager.createYamlFile;

public final class AutoViaUpdater extends JavaPlugin {

    private FileConfiguration config;
    private ViaVersion m_viaVersion;
    private ViaBackwards m_viaBackwards;
    private ViaRewind m_viaRewind;
    private ViaRewindLegacySupport m_viaRewindLegacySupport;
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
        m_viaVersion = new ViaVersion();
        m_viaBackwards = new ViaBackwards();
        m_viaRewind = new ViaRewind();
        m_viaRewindLegacySupport = new ViaRewindLegacySupport();
        loadConfiguration();
        createYamlFile(getDataFolder().getAbsolutePath());
        isViaVersionEnabled = getConfig().getBoolean("ViaVersion.enabled");
        isViaVersionDev = getConfig().getBoolean("ViaVersion.dev");
        isViaBackwardsEnabled = getConfig().getBoolean("ViaBackwards.enabled");
        isViaBackwardsDev = getConfig().getBoolean("ViaBackwards.dev");
        isViaRewindEnabled = getConfig().getBoolean("ViaRewind.enabled");
        isViaRewindDev = getConfig().getBoolean("ViaRewind.dev");
        isViaRewindLegacyEnabled = getConfig().getBoolean("ViaRewind-Legacy.enabled");
        updateChecker();
    }

    public void updateChecker() {
        config = getConfig();
        long interval = config.getInt("Check-Interval");
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
            @Override
            public void run() {
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
                    if (isViaRewindLegacyEnabled) {
                        updateVia("ViaRewind%20Legacy%20Support", getDataFolder().getParent(), false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0L, 20L * 60L * interval);
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
}
