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
    private static String name;
    private static String directory;
    private static String branch;
    public static void updateVia(String viaName, String dataDirectory, boolean isDev) throws IOException {
        name = viaName;
        directory = dataDirectory;
        if(isDev){
            branch = "dev";
        } else {
            branch = "master";
        }
        if (getDownloadedBuild(viaName) == -1) {
            downloadUpdate(viaName);
            updateBuildNumber(viaName, getLatestBuild());
            System.out.println(viaName + " was downloaded for the first time. Please restart to let the plugin take in effect.");
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

        boolean doesUpdateFolderExist = new File(directory, File.separator + "update").exists();
        String outputFilePath = directory + "/" + s + ".jar";
        if (doesUpdateFolderExist){
            File directoryFile = new File(directory);
            File[] files = directoryFile.listFiles();
            if (files != null) {
                boolean containsVia = false;
                for (File file : files) {
                    if (file.getName().toLowerCase().contains(s.toLowerCase().replace("-dev", "").replace("%20", "-"))) {
                        containsVia = true;
                        break;
                    }
                    /*if (file.getName().toLowerCase().contains("viaversion")) {
                        containsVia = true;
                        break;
                    }*/
                }
                if (!containsVia) {
                    outputFilePath = directory + "/update/" + s + ".jar";
                }
            }
        }

        try (InputStream in = new URL(latestVersionUrl).openStream();
             FileOutputStream out = new FileOutputStream(outputFilePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            System.out.println("New version of " + s + " downloaded. Please restart the server");
        } catch (IOException e) {
            System.out.println("Error downloading new version of " + s + "\n" + e);
        }
    }

    public static String getLatestDownload(String s) throws IOException {
        String jenkinsUrl = "https://ci.viaversion.com/job/" + s + "/lastSuccessfulBuild/api/json";
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(new URL(jenkinsUrl));

        ArrayNode artifacts = (ArrayNode) node.get("artifacts");

        JsonNode selectedArtifact = null;
        for (JsonNode artifact : artifacts) {
            String fileName = artifact.get("fileName").asText();
            if (!fileName.contains("sources")) {
                selectedArtifact = artifact;
                break;
            }
        }

        if (selectedArtifact == null && !artifacts.isEmpty()) {
            selectedArtifact = artifacts.get(0);
        }

        return selectedArtifact.get("relativePath").asText();
    }

}
