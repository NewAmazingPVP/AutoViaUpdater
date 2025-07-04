package common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static common.BuildYml.getDownloadedBuild;
import static common.BuildYml.updateBuildNumber;

public class UpdateVias {
    private static String directory;

    /**
     * Update a Via* plugin.
     *
     * @param viaName      configured plugin name (may include "-Dev")
     * @param dataDirectory plugins folder
     * @param isDev        true ⇒ want snapshot (“-SNAPSHOT”) builds
     * @param isJava8      true ⇒ use the Java-8 Jenkins job
     */
    public static boolean updateVia(String viaName, String dataDirectory, boolean isDev, boolean isJava8) throws IOException {
        //name = viaName.replace("ViaRewind%20Legacy%20Support-Dev", "ViaRewind%20Legacy%20Support%20DEV");
        directory = dataDirectory;

        String jobName = viaName.replace("-Dev", "");

        int latestBuild = getLatestBuild(jobName, isDev, isJava8);
        if (latestBuild == -1) {
            System.err.println("AutoViaUpdater: no matching build found for " + jobName);
            return false;
        }

        if (getDownloadedBuild(viaName) == -1) {
            downloadUpdate(jobName, latestBuild, viaName);
            updateBuildNumber(viaName, latestBuild);
            System.out.println(viaName + " was downloaded for the first time. " + "Please restart to let the plugin take effect.");
            return true;

        } else if (getDownloadedBuild(viaName) != latestBuild) {
            downloadUpdate(jobName, latestBuild, viaName);
            updateBuildNumber(viaName, latestBuild);
            return true;
        }

        return false;
    }

    private static int getLatestBuild(String jobName, boolean wantSnapshot, boolean java8) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        if (java8) {
            String url = "https://ci.viaversion.com/job/" + jobName + "/lastSuccessfulBuild/api/json";
            return mapper.readTree(new URL(url)).get("number").asInt();
        }

        String listUrl = "https://ci.viaversion.com/job/" + jobName + "/api/json?tree=builds[number]";
        ArrayNode builds = (ArrayNode) mapper.readTree(new URL(listUrl)).get("builds");
        if (builds == null) return -1;

        for (JsonNode b : builds) {
            int num = b.get("number").asInt();
            String file = getArtifactFileName(jobName, num);
            if (file == null) continue;
            boolean isSnap = file.contains("-SNAPSHOT");
            if (wantSnapshot == isSnap) return num;
        }
        return -1;
    }

    private static String getArtifactFileName(String jobName, int build) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String url = "https://ci.viaversion.com/job/" + jobName + "/" + build + "/api/json";
        ArrayNode artifacts = (ArrayNode) mapper.readTree(new URL(url)).get("artifacts");
        if (artifacts == null) return null;

        for (JsonNode art : artifacts) {
            String file = art.get("fileName").asText();
            if (!file.contains("sources")) return file;
        }
        return null;
    }

    private static void downloadUpdate(String jobName, int build, String localName) throws IOException {
        String rel = getLatestDownload(jobName, build);
        String url = "https://ci.viaversion.com/job/" + jobName + "/" + build + "/artifact/" + rel;

        boolean updateFolder = new File(directory, "update").exists();
        String outPath = directory + "/" + localName + ".jar";

        if (updateFolder) {
            for (File f : new File(directory).listFiles()) {
                if (f.isFile() &&
                        f.getName().toLowerCase().contains(
                                localName.toLowerCase()
                                        .replace("-dev", "")
                                        .replace("%20", "-")
                                        .replace("-java8", ""))) {
                    outPath = directory + "/update/" + localName + ".jar";
                    break;
                }
            }
        }

        try (InputStream in = new URL(url).openStream();
             FileOutputStream out = new FileOutputStream(outPath)) {

            byte[] buf = new byte[1024];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            System.out.println("New version of " + localName + " downloaded. Please restart the server.");

        } catch (IOException e) {
            System.out.println("Error downloading new version of " + localName + "\n" + e);
        }
    }

    private static String getLatestDownload(String jobName, int build) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String url = "https://ci.viaversion.com/job/" + jobName + "/" + build + "/api/json";
        ArrayNode artifacts = (ArrayNode) mapper.readTree(new URL(url)).get("artifacts");

        JsonNode selected = null;
        for (JsonNode art : artifacts) {
            String fileName = art.get("fileName").asText();
            if (!fileName.contains("sources")) {
                selected = art;
                break;
            }
        }
        if (selected == null && !artifacts.isEmpty()) selected = artifacts.get(0);
        return selected.get("relativePath").asText();
    }
}
