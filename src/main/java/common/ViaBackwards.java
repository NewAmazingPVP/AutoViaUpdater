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

public final class ViaBackwards {

    public void updateViaBackwards() throws IOException {
        String latestVersionUrl;
        try {
            latestVersionUrl = "https://ci.viaversion.com/job/ViaBackwards/lastSuccessfulBuild/artifact/build/libs/ViaBackwards-" + getLatestViaBackwards() + ".jar";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String outputFilePath = "plugins/ViaBackwards.jar";

        Plugin viaBackwardsPlugin = Bukkit.getPluginManager().getPlugin("ViaBackwards");
        if (viaBackwardsPlugin != null) {
            String currentVersion = viaBackwardsPlugin.getDescription().getVersion();
            try {
                if (currentVersion.equals(getLatestViaBackwards())) {
                    return;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        getLogger().info("New version found. Downloading latest version of ViaBackwards...");

        try (InputStream in = new URL(latestVersionUrl).openStream();
             FileOutputStream out = new FileOutputStream(outputFilePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            getLogger().severe(ChatColor.RED + "Failed to download ViaBackwards: " + e.getMessage());
            return;
        }

        getLogger().info(ChatColor.BLUE + "Newer ViaBackwards downloaded to " + outputFilePath + ChatColor.YELLOW + ". Please restart the server to take effect.");
    }

    public String getLatestViaBackwards() throws IOException {
        String url = "https://ci.viaversion.com/job/ViaBackwards/lastSuccessfulBuild/";
        Document doc = Jsoup.connect(url).get();
        Element artifactLink = doc.selectFirst("a[href$=.jar]");
        assert artifactLink != null;
        String href = artifactLink.attr("href");
        return href.substring(href.indexOf("ViaBackwards-") + "ViaBackwards-".length(), href.lastIndexOf(".jar"));
    }

    public void updateViaBackwardsDev() throws IOException, URISyntaxException {
        String latestVersionUrl;
        try {
            latestVersionUrl = "https://ci.viaversion.com/job/ViaBackwards-dev/lastSuccessfulBuild/artifact/build/libs/ViaBackwards-" + getLatestViaBackwardsDev() + ".jar";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String outputFilePath = "plugins/ViaBackwards.jar";

        Plugin viaBackwardsDevPlugin = Bukkit.getPluginManager().getPlugin("ViaBackwards");
        if (viaBackwardsDevPlugin != null) {
            String currentVersion = viaBackwardsDevPlugin.getDescription().getVersion();
            try {
                if (currentVersion.equals(getLatestViaBackwardsDev())) {
                    return;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        getLogger().info("New version found. Downloading latest version of ViaBackwards-Dev...");

        try (InputStream in = new URL(latestVersionUrl).openStream();
             FileOutputStream out = new FileOutputStream(outputFilePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            getLogger().severe(ChatColor.RED + "Failed to download ViaBackwards-Dev: " + e.getMessage());
            return;
        }

        getLogger().info(ChatColor.BLUE + "Newer ViaBackwards-Dev downloaded to " + outputFilePath + ChatColor.YELLOW + ". Please restart the server to take effect.");
    }
    public String getLatestViaBackwardsDev() throws IOException {
        String url = "https://ci.viaversion.com/job/ViaBackwards-Dev/lastSuccessfulBuild/";
        Document doc = Jsoup.connect(url).get();
        Element artifactLink = doc.selectFirst("a[href$=.jar]");
        assert artifactLink != null;
        String href = artifactLink.attr("href");
        return href.substring(href.indexOf("ViaBackwards-") + "ViaBackwards-".length(), href.lastIndexOf(".jar"));
    }
}
