package velocity;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import common.ViaBackwards;
import common.ViaRewind;
import common.ViaVersion;

@Plugin(id = "autoviaupdater", name = "AutoViaUpdater", version = "2.0", url = "https://www.spigotmc.org/resources/autoviaupdater.109331/", authors = "NewAmazingPVP")
public final class AutoViaUpdater {

    private ViaVersion m_viaVersion;
    private ViaBackwards m_viaBackwards;
    private ViaRewind m_viaRewind;
    private Toml config;
    private ProxyServer proxy;
    private File myFile;
    private Path dataDirectory;
    public boolean isViaVersionEnabled;
    public boolean isViaVersionDev;
    public boolean isViaBackwardsEnabled;
    public boolean isViaBackwardsDev;
    public boolean isViaRewindEnabled;
    public boolean isViaRewindDev;

    @Inject
    public AutoViaUpdater(ProxyServer proxy, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.dataDirectory = dataDirectory;
        config = loadConfig(dataDirectory);
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        m_viaVersion = new ViaVersion();
        m_viaBackwards = new ViaBackwards();
        m_viaRewind = new ViaRewind();
        isViaVersionEnabled = config.getBoolean("ViaVersion.enabled");
        isViaVersionDev = config.getBoolean("ViaVersion.dev");
        isViaBackwardsEnabled = config.getBoolean("ViaBackwards.enabled");
        isViaBackwardsDev = config.getBoolean("ViaBackwards.dev");
        isViaRewindEnabled = config.getBoolean("ViaRewind.enabled");
        isViaRewindDev = config.getBoolean("ViaRewind.dev");
        updateChecker();
    }

    public void updateChecker() {
        long interval = config.getLong("Check-Interval");

        proxy.getScheduler().buildTask(this, () -> {
            try {
                if (isViaVersionEnabled && !isViaVersionDev) {
                    m_viaVersion.updateViaVersion("velocity");
                } else if (isViaVersionEnabled && isViaVersionDev) {
                    m_viaVersion.updateViaVersionDev("velocity");
                }
                if (isViaBackwardsEnabled && !isViaBackwardsDev) {
                    m_viaBackwards.updateViaBackwards("velocity", proxy);
                } else if (isViaBackwardsEnabled && isViaBackwardsDev) {
                    m_viaBackwards.updateViaBackwardsDev("velocity", proxy);
                }
                if (isViaRewindEnabled && !isViaRewindDev) {
                    m_viaRewind.updateViaRewind("velocity", proxy);
                } else if (isViaRewindEnabled && isViaRewindDev) {
                    m_viaRewind.updateViaRewindDev("velocity", proxy);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }).repeat(Duration.ofMinutes(interval)).schedule();
    }

    private Toml loadConfig(Path path) {
        File folder = path.toFile();
        File file = new File(folder, "config.toml");

        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (InputStream input = getClass().getResourceAsStream("/" + file.getName())) {
                if (input != null) {
                    Files.copy(input, file.toPath());
                } else {
                    file.createNewFile();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
                return null;
            }
        }
        return new Toml().read(file);
    }
}