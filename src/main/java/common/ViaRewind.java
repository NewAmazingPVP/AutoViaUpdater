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

public final class ViaRewind {

    public void updateViaRewind(String platform, com.velocitypowered.api.proxy.ProxyServer Proxy) throws IOException {
        String latestVersionUrl;
        try {
            latestVersionUrl = "https://ci.viaversion.com/job/ViaRewind/lastSuccessfulBuild/artifact/universal/build/libs/" + getLatestViaRewind();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String outputFilePath = "plugins/ViaRewind.jar";

        if (platform.equals("spigot")) {
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
        } else if (platform.equals("bungeecord")) {
            net.md_5.bungee.api.plugin.Plugin viaRewindPlugin = ProxyServer.getInstance().getPluginManager().getPlugin("ViaRewind");
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

            ProxyServer.getInstance().getLogger().info("New version found. Downloading latest version of ViaRewind...");

            try (InputStream in = new URL(latestVersionUrl).openStream();
                 FileOutputStream out = new FileOutputStream(outputFilePath)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                ProxyServer.getInstance().getLogger().severe(net.md_5.bungee.api.ChatColor.RED + "Failed to download ViaRewind: " + e.getMessage());
                return;
            }

            ProxyServer.getInstance().getLogger().info(net.md_5.bungee.api.ChatColor.BLUE + "Newer ViaRewind downloaded to " + outputFilePath + net.md_5.bungee.api.ChatColor.YELLOW + ". Please restart the server to take effect.");
        } else if (platform.equals("velocity")) {
            Optional<PluginContainer> viaRewindPlugin = Proxy.getPluginManager().getPlugin("ViaRewind");
            if (viaRewindPlugin.isPresent()) {
                String currentVersion = String.valueOf(viaRewindPlugin.get().getDescription().getVersion());
                try {
                    if (currentVersion.equals(getLatestViaRewind())) {
                        return;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            System.out.println("New version found. Downloading the latest version of ViaRewind...");

            try (InputStream in = new URL(latestVersionUrl).openStream();
                 FileOutputStream out = new FileOutputStream(outputFilePath)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                //Proxy.getConsoleCommandSource().sendMessage((Component.text("Failed to download ViaRewind: ", NamedTextColor.RED).append(Component.text(e.getMessage()))));
                System.out.println("Failed to downlaod Viarewind: " + e.getMessage());
                return;
            }
            //Proxy.getConsoleCommandSource().sendMessage((Component.text("Newer ViaRewind downloaded to ", NamedTextColor.BLUE).append(Component.text(outputFilePath)).append(Component.text(". Please restart the server to take effect.", NamedTextColor.YELLOW))));
            System.out.println("New ViaRewind downloaded to " + outputFilePath + ". Please restart the server to take effect.");
        }
    }


    public String getLatestViaRewind() throws IOException {
        String jenkinsUrl = "https://ci.viaversion.com/job/ViaRewind/lastSuccessfulBuild/api/json";
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(new URL(jenkinsUrl));

        return node.get("artifacts").get(0).get("fileName").asText();
    }

    public void updateViaRewindDev(String platform, com.velocitypowered.api.proxy.ProxyServer Proxy) throws IOException {
        String latestVersionUrl;
        try {
            latestVersionUrl = "https://ci.viaversion.com/job/ViaRewind-dev/lastSuccessfulBuild/artifact/all/target/" + getLatestViaRewind();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String outputFilePath = "plugins/ViaRewind.jar";

        if (platform.equals("spigot")) {
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
        } else if (platform.equals("bungeecord")) {
            net.md_5.bungee.api.plugin.Plugin viaRewindPlugin = ProxyServer.getInstance().getPluginManager().getPlugin("ViaRewind");
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

            ProxyServer.getInstance().getLogger().info("New version found. Downloading latest version of ViaRewind-Dev...");

            try (InputStream in = new URL(latestVersionUrl).openStream();
                 FileOutputStream out = new FileOutputStream(outputFilePath)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                ProxyServer.getInstance().getLogger().severe(net.md_5.bungee.api.ChatColor.RED + "Failed to download ViaRewind-Dev: " + e.getMessage());
                return;
            }

            ProxyServer.getInstance().getLogger().info(net.md_5.bungee.api.ChatColor.BLUE + "Newer ViaRewind-Dev downloaded to " + outputFilePath + net.md_5.bungee.api.ChatColor.YELLOW + ". Please restart the server to take effect.");
        } else if (platform.equals("velocity")) {
            Optional<PluginContainer> viaRewindPlugin = Proxy.getPluginManager().getPlugin("ViaRewind");
            if (viaRewindPlugin.isPresent()) {
                String currentVersion = String.valueOf(viaRewindPlugin.get().getDescription().getVersion());
                try {
                    if (currentVersion.equals(getLatestViaRewindDev())) {
                        return;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            System.out.println("New version found. Downloading the latest version of ViaRewind-Dev...");

            try (InputStream in = new URL(latestVersionUrl).openStream();
                 FileOutputStream out = new FileOutputStream(outputFilePath)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                //Proxy.getConsoleCommandSource().sendMessage((Component.text("Failed to download ViaRewind-Dev: ", NamedTextColor.RED).append(Component.text(e.getMessage()))));
                System.out.println("Failed to download ViaRewind-Dev: " + e.getMessage());
                return;
            }
            //Proxy.getConsoleCommandSource().sendMessage((Component.text("Newer ViaRewind-Dev downloaded to ", NamedTextColor.BLUE).append(Component.text(outputFilePath)).append(Component.text(". Please restart the server to take effect.", NamedTextColor.YELLOW))));
            System.out.println("Newer ViaRewind-Dev downloaded to " + outputFilePath + ". Please restart the server to take effect.");
        }
    }
    public String getLatestViaRewindDev() throws IOException {
        String jenkinsUrl = "https://ci.viaversion.com/job/ViaRewind-Dev/lastSuccessfulBuild/api/json";
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(new URL(jenkinsUrl));

        return node.get("artifacts").get(0).get("fileName").asText();
    }
}
