package velocity;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import common.CronScheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static common.BuildYml.createYamlFile;
import static common.BuildYml.updateBuildNumber;
import static common.UpdateVias.updateVia;

@Plugin(id = "autoviaupdater", name = "AutoViaUpdater", version = "10.0.0", url = "https://www.spigotmc.org/resources/autoviaupdater.109331/", authors = "NewAmazingPVP",
        dependencies = {
                @Dependency(id = "viaversion", optional = true),
                @Dependency(id = "viabackwards", optional = true),
                @Dependency(id = "viarewind", optional = true)
        })
public final class AutoViaUpdater {

    private final Toml config;
    private final ProxyServer proxy;
    private File myFile;
    private final Path dataDirectory;
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

    private final Metrics.Factory metricsFactory;
    private final java.util.concurrent.atomic.AtomicBoolean isChecking = new java.util.concurrent.atomic.AtomicBoolean(false);

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
        isViaVersionEnabled = getTomlBoolean("ViaVersion", "enabled", true);
        isViaVersionSnapshot = getTomlBoolean("ViaVersion", "snapshot", true);
        isViaVersionDev = getTomlBoolean("ViaVersion", "dev", false);
        isViaVersionJava8 = getTomlBoolean("ViaVersion", "java8", false);
        isViaBackwardsEnabled = getTomlBoolean("ViaBackwards", "enabled", true);
        isViaBackwardsSnapshot = getTomlBoolean("ViaBackwards", "snapshot", true);
        isViaBackwardsDev = getTomlBoolean("ViaBackwards", "dev", false);
        isViaBackwardsJava8 = getTomlBoolean("ViaBackwards", "java8", false);
        isViaRewindEnabled = getTomlBoolean("ViaRewind", "enabled", true);
        isViaRewindSnapshot = getTomlBoolean("ViaRewind", "snapshot", true);
        isViaRewindDev = getTomlBoolean("ViaRewind", "dev", false);
        isViaRewindJava8 = getTomlBoolean("ViaRewind", "java8", false);
        updateChecker();
        CommandManager commandManager = proxy.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("updatevias")
                .plugin(this)
                .build();

        SimpleCommand simpleCommand = new UpdateCommand();
        commandManager.register(commandMeta, simpleCommand);
    }

    public void updateChecker() {
        String cronExpression = config.getString("Cron-Expression", "");
        long interval = config.getLong("Check-Interval");
        long delay = config.getLong("Delay");

        if (!cronExpression.isEmpty()) {
            CronScheduler scheduler = new CronScheduler(cronExpression);
            proxy.getScheduler()
                    .buildTask(this, () -> scheduler.runIfDue(v -> checkUpdateVias()))
                    .delay(Duration.ofSeconds(delay))
                    .repeat(Duration.ofSeconds(1))
                    .schedule();
        } else {
            proxy.getScheduler()
                    .buildTask(this, this::checkUpdateVias)
                    .delay(Duration.ofSeconds(delay))
                    .repeat(Duration.ofMinutes(interval))
                    .schedule();
        }
    }


    public void checkUpdateVias() {
        if (!isChecking.compareAndSet(false, true)) return;
        try {
            if (proxy.getPluginManager().getPlugin("viaversion").orElse(null) == null) {
                updateBuildNumber("ViaVersion", -1);
            }
            if (proxy.getPluginManager().getPlugin("viabackwards").orElse(null) == null) {
                updateBuildNumber("ViaBackwards", -1);
            }
            if (proxy.getPluginManager().getPlugin("viarewind").orElse(null) == null) {
                updateBuildNumber("ViaRewind", -1);
            }
            if (isViaVersionEnabled) {
                updateAndRestart("ViaVersion", isViaVersionSnapshot, isViaVersionDev, isViaVersionJava8);
            }
            if (isViaBackwardsEnabled) {
                updateAndRestart("ViaBackwards", isViaBackwardsSnapshot, isViaBackwardsDev, isViaBackwardsJava8);
            }
            if (isViaRewindEnabled) {
                updateAndRestart("ViaRewind", isViaRewindSnapshot, isViaRewindDev, isViaRewindJava8);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            isChecking.set(false);
        }
    }

    private void updateAndRestart(String pluginName, boolean isSnapshot, boolean isDev, boolean isJava8) throws IOException {
        String pluginKey = isDev ? pluginName + "-Dev" : (isJava8 ? pluginName + "-Java8" : pluginName);
        if (updateVia(pluginKey, dataDirectory.getParent().toString(), isSnapshot, isDev, isJava8) && config.getBoolean("AutoRestart")) {
            String raw = config.getString("AutoRestart-Message");
            Component msg = LegacyComponentSerializer.legacyAmpersand().deserialize(raw == null ? "" : raw);
            proxy.sendMessage(msg);
            proxy.getScheduler().buildTask(this, proxy::shutdown)
                    .delay(Duration.ofSeconds(config.getLong("AutoRestart-Delay")))
                    .schedule();
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

    private boolean getTomlBoolean(String table, String key, boolean def) {
        Toml t = config.getTable(table);
        if (t == null) return def;
        Boolean b = t.getBoolean(key);
        return b == null ? def : b;
    }

    public class UpdateCommand implements SimpleCommand {
        @Override
        public boolean hasPermission(final Invocation invocation) {
            return invocation.source().hasPermission("autoviaupdater.admin");
        }

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            source.sendMessage(Component.text("Checking for Via updates...").color(NamedTextColor.YELLOW));
            proxy.getScheduler().buildTask(AutoViaUpdater.this, () -> {
                checkUpdateVias();
                source.sendMessage(Component.text("Update checker for vias completed!").color(NamedTextColor.AQUA));
            }).schedule();
        }
    }
}
