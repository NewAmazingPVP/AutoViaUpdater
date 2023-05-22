package common;

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


import static org.bukkit.Bukkit.getLogger;

public final class ViaVersion {

    String currentVersion;
    public void updateViaVersion(String Platform, com.velocitypowered.api.proxy.ProxyServer Proxy) throws IOException {
        String latestVersionUrl;
        try {
            latestVersionUrl = "https://ci.viaversion.com/job/ViaVersion/lastSuccessfulBuild/artifact/build/libs/ViaVersion-" + getLatestViaVersion() + ".jar";
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
            if (viaViaVersionPlugin.isPresent()) {
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
            Proxy.getConsoleCommandSource().sendMessage((Component.text("Newer ViaVersion downloaded to ", NamedTextColor.BLUE).append(Component.text(outputFilePath)).append(Component.text(". Please restart the server to take effect.", NamedTextColor.YELLOW))));
        }
    }

    public String getLatestViaVersion() throws IOException {
        String url = "https://ci.viaversion.com/job/ViaVersion/lastSuccessfulBuild/";
        Document doc = Jsoup.connect(url).get();
        Element artifactLink = doc.selectFirst("a[href$=.jar]");
        assert artifactLink != null;
        String href = artifactLink.attr("href");
        return href.substring(href.indexOf("ViaVersion-") + "ViaVersion-".length(), href.lastIndexOf(".jar"));
    }

    public void updateViaVersionDev(String Platform, com.velocitypowered.api.proxy.ProxyServer Proxy) throws IOException, URISyntaxException {
        String latestVersionUrl;
        try {
            latestVersionUrl = "https://ci.viaversion.com/job/ViaVersion-dev/lastSuccessfulBuild/artifact/build/libs/ViaVersion-" + getLatestViaVersionDev() + ".jar";
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
        } else {
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
        }

        if(Platform == "spigot") {
            getLogger().info("Downloading latest version of ViaVersion-Dev...");
        } else {
            ProxyServer.getInstance().getLogger().info("Downloading latest version of ViaVersion-Dev...");
        }

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
        } else {
            ProxyServer.getInstance().getLogger().info(net.md_5.bungee.api.ChatColor.BLUE + "Newer ViaVersion-Dev downloaded to " + outputFilePath + net.md_5.bungee.api.ChatColor.YELLOW + ". Please restart the server to take effect.");
        }
    }
    public String getLatestViaVersionDev() throws IOException {
        String url = "https://ci.viaversion.com/job/ViaVersion-Dev/lastSuccessfulBuild/";
        Document doc = Jsoup.connect(url).get();
        Element artifactLink = doc.selectFirst("a[href$=.jar]");
        assert artifactLink != null;
        String href = artifactLink.attr("href");
        return href.substring(href.indexOf("ViaVersion-") + "ViaVersion-".length(), href.lastIndexOf(".jar"));
    }
}
