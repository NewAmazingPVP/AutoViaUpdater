package common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.velocitypowered.api.plugin.PluginContainer;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ProxyServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;


import static org.bukkit.Bukkit.getLogger;

public final class ViaVersion {

    String currentVersion;
    public void updateViaVersion(String Platform, com.velocitypowered.api.proxy.ProxyServer Proxy) throws IOException {
        String latestVersionUrl;
        try {
            latestVersionUrl = "https://ci.viaversion.com/job/ViaVersion/lastSuccessfulBuild/artifact/build/libs/" + getLatestViaVersion();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String outputFilePath = "plugins/ViaVersion.jar";

        if(Platform == "spigot") {
            Plugin viaVersionPlugin = Bukkit.getPluginManager().getPlugin("ViaVersion");
            if (viaVersionPlugin != null) {
                currentVersion = viaVersionPlugin.getDescription().getVersion();
                try {
                    if (currentVersion.equals(getLatestViaVersion())) {
                        return;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (Platform == "bungeecord") {
            net.md_5.bungee.api.plugin.Plugin viaVersionPlugin = ProxyServer.getInstance().getPluginManager().getPlugin("ViaVersion");
            if (viaVersionPlugin != null) {
                currentVersion = viaVersionPlugin.getDescription().getVersion();
                try {
                    if (currentVersion.equals(getLatestViaVersion())) {
                        return;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (Platform == "velocity"){
            Optional<PluginContainer> viaVersionPlugin = Proxy.getPluginManager().getPlugin("ViaVersion");
            if (viaVersionPlugin.isPresent()) {
                String currentVersion = String.valueOf(viaVersionPlugin.get().getDescription().getVersion());
                try {
                    if (currentVersion.equals(getLatestViaVersion())) {
                        return;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        System.out.println("New version found. Downloading the latest version of ViaVersion...");

        try (InputStream in = new URL(latestVersionUrl).openStream();
             FileOutputStream out = new FileOutputStream(outputFilePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            getLogger().severe(ChatColor.RED + "Failed to download ViaVersion: " + e.getMessage());
            return;
        }

        if(Platform == "spigot") {
            getLogger().info(ChatColor.BLUE + "Newer ViaVersion downloaded to " + outputFilePath + ChatColor.YELLOW + ". Please restart the server to take effect.");
        } else if (Platform == "bungeecord") {
            ProxyServer.getInstance().getLogger().info(net.md_5.bungee.api.ChatColor.BLUE+ "Newer ViaVersion downloaded to " + outputFilePath + net.md_5.bungee.api.ChatColor.YELLOW + ". Please restart the server to take effect.");
        } else {
            //Proxy.getConsoleCommandSource().sendMessage((Component.text("Newer ViaVersion downloaded to ", NamedTextColor.BLUE).append(Component.text(outputFilePath)).append(Component.text(". Please restart the server to take effect.", NamedTextColor.YELLOW))));
            System.out.println("Newer ViaVersion downloaded to " + outputFilePath + ". Please restart the server to take effect.");
        }
    }

    public String getLatestViaVersion() throws IOException {
        String jenkinsUrl = "https://ci.viaversion.com/job/ViaVersion/lastSuccessfulBuild/api/json";
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(new URL(jenkinsUrl));

        return node.get("artifacts").get(0).get("fileName").asText();
    }

    public void updateViaVersionDev(String Platform, com.velocitypowered.api.proxy.ProxyServer Proxy) throws IOException, URISyntaxException {
        String latestVersionUrl;
        try {
            latestVersionUrl = "https://ci.viaversion.com/job/ViaVersion-dev/lastSuccessfulBuild/artifact/build/libs/" + getLatestViaVersionDev();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String outputFilePath = "plugins/ViaVersion.jar";

        if(Platform == "spigot") {
            Plugin viaVersionPlugin = Bukkit.getPluginManager().getPlugin("ViaVersion");
            if (viaVersionPlugin != null) {
                currentVersion = viaVersionPlugin.getDescription().getVersion();
                try {
                    if (currentVersion.equals(getLatestViaVersion())) {
                        return;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (Platform == "bungeecord") {
            net.md_5.bungee.api.plugin.Plugin viaVersionPlugin = ProxyServer.getInstance().getPluginManager().getPlugin("ViaVersion");
            if (viaVersionPlugin != null) {
                currentVersion = viaVersionPlugin.getDescription().getVersion();
                try {
                    if (currentVersion.equals(getLatestViaVersion())) {
                        return;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            Optional<PluginContainer> viaVersionPlugin = Proxy.getPluginManager().getPlugin("ViaVersion");
            if (viaVersionPlugin.isPresent()) {
                String currentVersion = String.valueOf(viaVersionPlugin.get().getDescription().getVersion());
                try {
                    if (currentVersion.equals(getLatestViaVersionDev())) {
                        return;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        System.out.println("New version found. Downloading the latest version of ViaVersion-Dev...");

        try (InputStream in = new URL(latestVersionUrl).openStream();
             FileOutputStream out = new FileOutputStream(outputFilePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            getLogger().severe(ChatColor.RED + "Failed to download ViaVersion-Dev: " + e.getMessage());
            return;
        }

        if(Platform == "spigot") {
            getLogger().info(ChatColor.BLUE + "Newer ViaVersion-Dev downloaded to " + outputFilePath + ChatColor.YELLOW + ". Please restart the server to take effect.");
        } else if (Platform == "bungeecord") {
            ProxyServer.getInstance().getLogger().info(net.md_5.bungee.api.ChatColor.BLUE + "Newer ViaVersion-Dev downloaded to " + outputFilePath + net.md_5.bungee.api.ChatColor.YELLOW + ". Please restart the server to take effect.");
        } else {
            //Proxy.getConsoleCommandSource().sendMessage((Component.text("Newer ViaVersion-Dev downloaded to ", NamedTextColor.BLUE).append(Component.text(outputFilePath)).append(Component.text(". Please restart the server to take effect.", NamedTextColor.YELLOW))));
            System.out.println("Newer ViaVersion-Dev downloaded to " + outputFilePath + ". Please restart the server to take effect.");
        }
    }
    public String getLatestViaVersionDev() throws IOException {
        String jenkinsUrl = "https://ci.viaversion.com/job/ViaVersion-Dev/lastSuccessfulBuild/api/json";
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(new URL(jenkinsUrl));

        return node.get("artifacts").get(0).get("fileName").asText();
    }

}
