package commonrework;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static commonrework.YamlFileManager.getDownloadedBuild;
import static commonrework.YamlFileManager.updateBuildNumber;


public class UpdateVias {
    private static String name;
    private static String fileF;
    private static String branch;
    public static void updateVia(String viaName, String fileE, boolean isDev) throws IOException {
        name = viaName;
        fileF = fileE;
        if(isDev){
            branch = "dev";
        } else {
            branch = "master";
        }
        if (getDownloadedBuild(viaName) == -1) {
            downloadUpdate(viaName);
            updateBuildNumber(viaName, getLatestBuild());
        } else if(getDownloadedBuild(viaName) != getLatestBuild()){
            downloadUpdate(viaName);
            updateBuildNumber(viaName, getLatestBuild());
        }
    }

    public static int getLatestBuild() throws IOException {
        String jenkinsUrl = "https://ci.viaversion.com/job/" + name + "/lastSuccessfulBuild/api/json";
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(new URL(jenkinsUrl));
         return node.get("actions")
                .get(3)
                .get("buildsByBranchName")
                .get("refs/remotes/origin/" + branch)
                .get("buildNumber")
                .asInt();
    }

    public static void downloadUpdate(String s) throws IOException {
        String latestVersionUrl = "https://ci.viaversion.com/job/" + s + "/lastSuccessfulBuild/artifact/" + getLatestDownload(s);
        String outputFilePath = fileF + "/" + s + ".jar";
        try (InputStream in = new URL(latestVersionUrl).openStream();
             FileOutputStream out = new FileOutputStream(outputFilePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException ignored) {
        }
    }

    public static String getLatestDownload(String s) throws IOException {
        String jenkinsUrl = "https://ci.viaversion.com/job/" + s + "/lastSuccessfulBuild/api/json";
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(new URL(jenkinsUrl));

        return node.get("artifacts").get(0).get("relativePath").asText();
    }
}
