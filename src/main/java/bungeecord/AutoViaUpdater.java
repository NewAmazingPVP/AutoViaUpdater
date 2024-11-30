package bungeecord;

import common.CronScheduler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import static common.BuildYml.createYamlFile;
import static common.BuildYml.updateBuildNumber;
import static common.UpdateVias.updateVia;

public final class AutoViaUpdater extends Plugin {

    private Configuration config;
    public boolean isViaVersionEnabled;
    public boolean isViaVersionDev;
    public boolean isViaVersionJava8;
    public boolean isViaBackwardsEnabled;
    public boolean isViaBackwardsDev;
    public boolean isViaBackwardsJava8;
    public boolean isViaRewindEnabled;
    public boolean isViaRewindDev;
    public boolean isViaRewindJava8;

    @Override
    public void onEnable() {
        new Metrics(this, 18605);
        saveDefaultConfig();
        loadConfiguration();
        createYamlFile(getDataFolder().getAbsolutePath(), true);
        isViaVersionEnabled = config.getBoolean("ViaVersion.enabled");
        isViaVersionDev = config.getBoolean("ViaVersion.dev");
        isViaVersionJava8 = config.getBoolean("ViaVersion.java8");
        isViaBackwardsEnabled = config.getBoolean("ViaBackwards.enabled");
        isViaBackwardsDev = config.getBoolean("ViaBackwards.dev");
        isViaBackwardsJava8 = config.getBoolean("ViaBackwards.java8");
        isViaRewindEnabled = config.getBoolean("ViaRewind.enabled");
        isViaRewindDev = config.getBoolean("ViaRewind.dev");
        isViaRewindJava8 = config.getBoolean("ViaRewind.java8");
        updateChecker();
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new UpdateCommand());
    }

    public void updateChecker() {
        String cronExpression = config.getString("Cron-Expression", "");
        long interval = config.getLong("Check-Interval");
        long delay = config.getLong("Delay");

        if (!cronExpression.isEmpty()) {
            CronScheduler scheduler = new CronScheduler(cronExpression);
            getProxy().getScheduler().schedule(this, () -> scheduler.runIfDue(v -> checkUpdateVias()), delay, 1, TimeUnit.SECONDS);
        } else {
            getProxy().getScheduler().schedule(this, this::checkUpdateVias, delay, interval * 60, TimeUnit.SECONDS);
        }
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

    public void checkUpdateVias(){
        try {
            if(getProxy().getPluginManager().getPlugin("ViaVersion") == null){
                updateBuildNumber("ViaVersion", -1);
            }
            if(getProxy().getPluginManager().getPlugin("ViaBackwards") == null){
                updateBuildNumber("ViaBackwards", -1);
            }
            if(getProxy().getPluginManager().getPlugin("ViaRewind") == null){
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
        if (updateVia(pluginKey, getDataFolder().getParent(), isDev) && config.getBoolean("AutoRestart")) {
            getProxy().broadcast(config.getString("AutoRestart-Message"));
            getProxy().getScheduler().schedule(this, () -> getProxy().stop(), config.getLong("AutoRestart-Delay"), TimeUnit.SECONDS);
        }
    }
    public class UpdateCommand extends Command {

        public UpdateCommand() {
            super("updatevias", "autoviaupdater.admin");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            checkUpdateVias();
            sender.sendMessage(ChatColor.AQUA + "Update checker for vias successful!");
        }
    }

}

