package newamazingpvp.autoviaupdater;

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

public final class ViaRewind {

    public void updateViaRewind() throws IOException {
        String latestVersionUrl;
        try {
            latestVersionUrl = "https://ci.viaversion.com/job/ViaRewind/lastSuccessfulBuild/artifact/all/target/ViaRewind-" + getLatestViaRewind() + ".jar";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String outputFilePath = "plugins/ViaRewind.jar";

        Plugin viaRewindPlugin = Bukkit.getPluginManager().getPlugin("ViaRewind");
        if (viaRewindPlugin != null) {
            String currentVersion = viaRewindPlugin.getDescription().getVersion();
            try {
                if (currentVersion.equals(getLatestViaRewind())) {
                    return;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        getLogger().info("New version found. Downloading latest version of ViaRewind...");

        try (InputStream in = new URL(latestVersionUrl).openStream();
             FileOutputStream out = new FileOutputStream(outputFilePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            getLogger().severe(ChatColor.RED + "Failed to download ViaRewind: " + e.getMessage());
            return;
        }

        getLogger().info(ChatColor.BLUE + "Newer ViaRewind downloaded to " + outputFilePath + ChatColor.YELLOW + ". Please restart the server to take effect.");
    }

    public String getLatestViaRewind() throws IOException {
        String url = "https://ci.viaversion.com/job/ViaRewind/lastSuccessfulBuild/";
        Document doc = Jsoup.connect(url).get();
        Element artifactLink = doc.selectFirst("a[href$=.jar]");
        assert artifactLink != null;
        String href = artifactLink.attr("href");
        return href.substring(href.indexOf("ViaRewind-") + "ViaRewind-".length(), href.lastIndexOf(".jar"));
    }

    public void updateViaRewindDev() throws IOException, URISyntaxException {
        String latestVersionUrl;
        try {
            latestVersionUrl = "https://ci.viaversion.com/job/ViaRewind-dev/lastSuccessfulBuild/artifact/all/target/ViaRewind-" + getLatestViaRewindDev() + ".jar";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String outputFilePath = "plugins/ViaRewind.jar";

        Plugin viaRewindDevPlugin = Bukkit.getPluginManager().getPlugin("ViaRewind");
        if (viaRewindDevPlugin != null) {
            String currentVersion = viaRewindDevPlugin.getDescription().getVersion();
            try {
                if (currentVersion.equals(getLatestViaRewindDev())) {
                    return;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        getLogger().info("New version found. Downloading latest version of ViaRewind-Dev...");

        try (InputStream in = new URL(latestVersionUrl).openStream();
             FileOutputStream out = new FileOutputStream(outputFilePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            getLogger().severe(ChatColor.RED + "Failed to download ViaRewind-Dev: " + e.getMessage());
            return;
        }

        getLogger().info(ChatColor.BLUE + "Newer ViaRewind-Dev downloaded to " + outputFilePath + ChatColor.YELLOW + ". Please restart the server to take effect.");
    }
    public String getLatestViaRewindDev() throws IOException {
        String url = "https://ci.viaversion.com/job/ViaRewind-Dev/lastSuccessfulBuild/";
        Document doc = Jsoup.connect(url).get();
        Element artifactLink = doc.selectFirst("a[href$=.jar]");
        assert artifactLink != null;
        String href = artifactLink.attr("href");
        return href.substring(href.indexOf("ViaRewind-") + "ViaRewind-".length(), href.lastIndexOf(".jar"));
    }
}
