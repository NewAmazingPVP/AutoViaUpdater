package newamazingpvp.autoviaupdater;

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
        config = getConfig();

        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();

                FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);

                configuration.options().header("Enable/Disable plugins\nEnable dev for latest developer versions and disable dev for stable released versions");
                configuration.set("ViaVersion.enabled", true);
                configuration.set("ViaVersion.dev", false);
                configuration.set("ViaBackwards.enabled", true);
                configuration.set("ViaBackwards.dev", false);
                configuration.set("ViaRewind.enabled", true);
                configuration.set("ViaRewind.dev", false);
                configuration.set("ViaRewind-Legacy.enabled", true);
                configuration.set("Check-Interval", 30);

                configuration.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        isViaVersionEnabled = getConfig().getBoolean("ViaVersion.enabled");
        isViaVersionDev = getConfig().getBoolean("ViaVersion.dev");
        isViaBackwardsEnabled = getConfig().getBoolean("ViaBackwards.enabled");
        isViaBackwardsDev = getConfig().getBoolean("ViaBackwards.dev");
        isViaRewindEnabled = getConfig().getBoolean("ViaRewind.enabled");
        isViaRewindDev = getConfig().getBoolean("ViaRewind.dev");
        isViaRewindLegacyEnabled = getConfig().getBoolean("ViaRewind-Legacy.enabled");

        if (!config.contains("ViaVersion.enabled")) {
            config.set("ViaVersion.enabled", true);
        }
        if (!config.contains("ViaVersion.dev")) {
            config.set("ViaVersion.dev", false);
        }
        if (!config.contains("ViaBackwards.enabled")) {
            config.set("ViaBackwards.enabled", true);
        }
        if (!config.contains("ViaBackwards.dev")) {
            config.set("ViaBackwards.dev", false);
        }
        if (!config.contains("ViaRewind.enabled")) {
            config.set("ViaRewind.enabled", true);
        }
        if (!config.contains("ViaRewind.dev")) {
            config.set("ViaRewind.dev", false);
        }
        if (!config.contains("ViaRewind-Legacy.enabled")) {
            config.set("ViaRewind-Legacy.enabled", true);
        }
        if (!config.contains("Check-Interval")) {
            config.set("Check-Interval", 30);
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                        m_viaVersion.updateViaVersion();
                    } else if (isViaVersionEnabled && isViaVersionDev) {
                        m_viaVersion.updateViaVersionDev();
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

    public FileConfiguration getConfig() {
        try {
            File configFile = new File(getDataFolder(), "config.yml");
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(configFile);
            return configuration;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
