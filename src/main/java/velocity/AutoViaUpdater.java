package velocity;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static common.BuildYml.createYamlFile;
import static common.BuildYml.updateBuildNumber;
import static common.UpdateVias.updateVia;

@Plugin(id = "autoviaupdater", name = "AutoViaUpdater", version = "8.5", url = "https://www.spigotmc.org/resources/autoviaupdater.109331/", authors = "NewAmazingPVP",
        dependencies = {
                @Dependency(id = "viaversion", optional = true),
                @Dependency(id = "viabackwards", optional = true),
                @Dependency(id = "viarewind", optional = true)
        })
public final class AutoViaUpdater {

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
    private final Metrics.Factory metricsFactory;

    @Inject
    public AutoViaUpdater(ProxyServer proxy, @DataDirectory Path dataDirectory, Metrics.Factory metricsFactory) {
        this.proxy = proxy;
        this.dataDirectory = dataDirectory;
        config = loadConfig(dataDirectory);
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        createYamlFile(dataDirectory.toAbsolutePath().toString(), true);
        metricsFactory.make(this, 18604);
        isViaVersionEnabled = config.getBoolean("ViaVersion.enabled");
        isViaVersionDev = config.getBoolean("ViaVersion.dev");
        isViaBackwardsEnabled = config.getBoolean("ViaBackwards.enabled");
        isViaBackwardsDev = config.getBoolean("ViaBackwards.dev");
        isViaRewindEnabled = config.getBoolean("ViaRewind.enabled");
        isViaRewindDev = config.getBoolean("ViaRewind.dev");
        updateChecker();
        CommandManager commandManager = proxy.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("updatevias")
                .plugin(this)
                .build();

        SimpleCommand simpleCommand = new UpdateCommand();
        commandManager.register(commandMeta, simpleCommand);
    }

    public void updateChecker() {
        long interval = config.getLong("Check-Interval");
        long delay = config.getLong("Delay");
        proxy.getScheduler().buildTask(this, this::checkUpdateVias).repeat(Duration.ofMinutes(interval)).delay(Duration.ofSeconds(delay)).schedule();
    }

    public void checkUpdateVias(){
        try {
            if(proxy.getPluginManager().getPlugin("viaversion").orElse(null) == null){
                updateBuildNumber("ViaVersion", -1);
            }
            if(proxy.getPluginManager().getPlugin("viabackwards").orElse(null) == null){
                updateBuildNumber("ViaBackwards", -1);
            }
            if(proxy.getPluginManager().getPlugin("viarewind").orElse(null) == null){
                updateBuildNumber("ViaRewind", -1);
            }
            if (isViaVersionEnabled) {
                updateAndRestart("ViaVersion", isViaVersionDev);
            }
            if (isViaBackwardsEnabled) {
                updateAndRestart("ViaBackwards", isViaBackwardsDev);
            }
            if (isViaRewindEnabled) {
                updateAndRestart("ViaRewind", isViaRewindDev);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateAndRestart(String pluginName, boolean isDev) throws IOException {
        String pluginKey = isDev ? pluginName + "-Dev" : pluginName;
        if (updateVia(pluginKey, dataDirectory.getParent().toString(), isDev) && config.getBoolean("AutoRestart")) {
            proxy.sendMessage(Component.text(config.getString("AutoRestart-Message")).color(NamedTextColor.AQUA));
            proxy.getScheduler().buildTask(this, () -> proxy.shutdown()).delay(Duration.ofSeconds(config.getLong("AutoRestart-Delay"))).schedule();
        }
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

    public class UpdateCommand implements SimpleCommand {
        @Override
        public boolean hasPermission(final Invocation invocation) {
            return invocation.source().hasPermission("autoviaupdater.admin");
        }
        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            checkUpdateVias();
            source.sendMessage(Component.text("Update checker for vias successful!").color(NamedTextColor.AQUA));
        }
    }
}