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
import java.net.URL;

import static org.bukkit.Bukkit.getLogger;

public final class ViaRewindLegacySupport {

    public void updateViaRewindLegacySupport() throws IOException {
        String latestVersionUrl;
        try {
            latestVersionUrl = "https://ci.viaversion.com/job/ViaRewind%20Legacy%20Support/lastSuccessfulBuild/artifact/target/viarewind-legacy-support-" + getLatestViaRewindLegacySupport() + ".jar";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String outputFilePath = "plugins/viarewind-legacy-support.jar";

        Plugin viaRewindLegacyPlugin = Bukkit.getPluginManager().getPlugin("ViaRewind-Legacy-Support");
        if (viaRewindLegacyPlugin != null) {
            String currentVersion = viaRewindLegacyPlugin.getDescription().getVersion();
            try {
                if (currentVersion.equals(getLatestViaRewindLegacySupport())) {
                    return;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        getLogger().info("New version found. Downloading latest version of ViaRewindLegacy...");

        try (InputStream in = new URL(latestVersionUrl).openStream();
             FileOutputStream out = new FileOutputStream(outputFilePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            getLogger().severe(ChatColor.RED + "Failed to download ViaRewindLegacy: " + e.getMessage());
            return;
        }

        getLogger().info(ChatColor.BLUE + "Newer ViaRewindLegacy downloaded to " + outputFilePath + ChatColor.YELLOW + ". Please restart the server to take effect.");
    }

    public String getLatestViaRewindLegacySupport() throws IOException {
        String url = "https://ci.viaversion.com/view/ViaRewind/job/ViaRewind Legacy Support/lastSuccessfulBuild/";
        Document doc = Jsoup.connect(url).get();
        Element artifactLink = doc.selectFirst("a[href$=.jar]");
        assert artifactLink != null;
        String href = artifactLink.attr("href");
        int start = href.indexOf("viarewind-legacy-support-") + "viarewind-legacy-support-".length();
        int end = href.lastIndexOf(".jar");
        return href.substring(start, end);
    }

}