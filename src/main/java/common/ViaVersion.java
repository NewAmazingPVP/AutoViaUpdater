package common;

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
    public void updateViaVersion() throws IOException {
        String latestVersionUrl;
        try {
            latestVersionUrl = "https://ci.viaversion.com/job/ViaVersion/lastSuccessfulBuild/artifact/build/libs/ViaVersion-" + getLatestViaVersion() + ".jar";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String outputFilePath = "plugins/ViaVersion.jar";

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

        getLogger().info("New version found. Downloading latest version of ViaVersion...");

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

        getLogger().info(ChatColor.BLUE + "Newer ViaVersion downloaded to " + outputFilePath + ChatColor.YELLOW + ". Please restart the server to take effect.");
    }

    public String getLatestViaVersion() throws IOException {
        String url = "https://ci.viaversion.com/job/ViaVersion/lastSuccessfulBuild/";
        Document doc = Jsoup.connect(url).get();
        Element artifactLink = doc.selectFirst("a[href$=.jar]");
        assert artifactLink != null;
        String href = artifactLink.attr("href");
        return href.substring(href.indexOf("ViaVersion-") + "ViaVersion-".length(), href.lastIndexOf(".jar"));
    }

    public void updateViaVersionDev() throws IOException, URISyntaxException {
        String latestVersionUrl;
        try {
            latestVersionUrl = "https://ci.viaversion.com/job/ViaVersion-dev/lastSuccessfulBuild/artifact/build/libs/ViaVersion-" + getLatestViaVersionDev() + ".jar";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String outputFilePath = "plugins/ViaVersion.jar";

        Plugin viaVersionDevPlugin = Bukkit.getPluginManager().getPlugin("ViaVersion");
        if (viaVersionDevPlugin != null) {
            String currentVersion = viaVersionDevPlugin.getDescription().getVersion();
            try {
                if (currentVersion.equals(getLatestViaVersionDev())) {
                    return;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        getLogger().info("Downloading latest version of ViaVersion-Dev...");

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

        getLogger().info(ChatColor.BLUE + "Newer ViaVersion-Dev downloaded to " + outputFilePath + ChatColor.YELLOW + ". Please restart the server to take effect.");
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
