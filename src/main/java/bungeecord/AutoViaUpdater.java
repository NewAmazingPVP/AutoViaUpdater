package bungeecord;

import common.ViaBackwards;
import common.ViaRewind;
import common.ViaRewindLegacySupport;
import common.ViaVersion;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

public final class AutoViaUpdater extends Plugin {

    private Configuration config;
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
        saveDefaultConfig();
        loadConfiguration();
        isViaVersionEnabled = config.getBoolean("ViaVersion.enabled");
        isViaVersionDev = config.getBoolean("ViaVersion.dev");
        isViaBackwardsEnabled = config.getBoolean("ViaBackwards.enabled");
        isViaBackwardsDev = config.getBoolean("ViaBackwards.dev");
        isViaRewindEnabled = config.getBoolean("ViaRewind.enabled");
        isViaRewindDev = config.getBoolean("ViaRewind.dev");
        isViaRewindLegacyEnabled = config.getBoolean("ViaRewind-Legacy.enabled");

        updateChecker();
    }

    public void updateChecker() {
        long interval = config.getInt("Check-Interval");
        long updateInterval = interval*60;

        getProxy().getScheduler().schedule(this, () -> {
            getProxy().getScheduler().runAsync(this, () -> {
                try {
                    if (isViaVersionEnabled && !isViaVersionDev) {
                        m_viaVersion.updateViaVersion("bungeecord", null);
                    } else if (isViaVersionEnabled && isViaVersionDev) {
                        m_viaVersion.updateViaVersionDev("bungeecord");
                    }
                    if (isViaBackwardsEnabled && !isViaBackwardsDev) {
                        m_viaBackwards.updateViaBackwards("bungeecord", null);
                    } else if (isViaBackwardsEnabled && isViaBackwardsDev) {
                        m_viaBackwards.updateViaBackwardsDev("bungeecord", null);
                    }
                    if (isViaRewindEnabled && !isViaRewindDev) {
                        m_viaRewind.updateViaRewind("bungeecord", null);
                    } else if (isViaRewindEnabled && isViaRewindDev) {
                        m_viaRewind.updateViaRewindDev("bungeecord", null);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
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

}
