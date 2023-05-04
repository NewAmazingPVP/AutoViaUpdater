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
        m_viaVersion = new ViaVersion();
        m_viaBackwards = new ViaBackwards();
        m_viaRewind = new ViaRewind();
        m_viaRewindLegacySupport = new ViaRewindLegacySupport();
        loadConfiguration();
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
        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                try {
                    if (isViaVersionEnabled && !isViaVersionDev) {
                        m_viaVersion.updateViaVersion("spigot");
                    } else if (isViaVersionEnabled && isViaVersionDev) {
                        m_viaVersion.updateViaVersionDev("spigot");
                    }
                    if (isViaBackwardsEnabled && !isViaBackwardsDev) {
                        m_viaBackwards.updateViaBackwards();
                    } else if (isViaBackwardsEnabled && isViaBackwardsDev) {
                        m_viaBackwards.updateViaBackwardsDev();
                    }
                    if (isViaRewindEnabled && !isViaRewindDev) {
                        m_viaRewind.updateViaRewind();
                    } else if (isViaRewindEnabled && isViaRewindDev) {
                        m_viaRewind.updateViaRewindDev();
                    }
                    if (isViaRewindLegacyEnabled) {
                        m_viaRewindLegacySupport.updateViaRewindLegacySupport();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 0L, 20L * 60L * interval);
    }

    public void loadConfiguration(){
        saveDefaultConfig();

        config = getConfig();
        config.addDefault("ViaVersion.enabled", true);
        config.set("ViaVersion.dev", false);
        config.set("ViaBackwards.enabled", true);
        config.set("ViaBackwards.dev", false);
        config.set("ViaRewind.enabled", true);
        config.set("ViaRewind.dev", false);
        config.set("ViaRewind-Legacy.enabled", true);
        config.set("Check-Interval", 60);

        config.options().copyDefaults(true);
        saveConfig();
    }
}
