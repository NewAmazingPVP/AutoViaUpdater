package common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static common.BuildYml.getDownloadedBuild;
import static common.BuildYml.updateBuildNumber;

public class UpdateVias {
    private static String directory;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    interface DownloadAction {
        void download() throws IOException;
    }

    public static boolean updateVia(String viaName, String dataDirectory, boolean wantSnapshot, boolean isDev, boolean isJava8) throws IOException {
        directory = dataDirectory;

        String jobPath;
        String buildKey = viaName;
        if (isJava8) {
            jobPath = "job/" + viaName + "-Java8";
            buildKey = viaName + "-Java8";
        } else if (isDev) {
            if (viaName.equals("ViaRewind%20Legacy%20Support")) {
                jobPath = "view/ViaRewind/job/" + viaName + "%20DEV";
                buildKey = viaName + "%20DEV";
            } else {
                jobPath = "job/" + viaName + "-DEV";
                buildKey = viaName + "-Dev";
            }
        } else {
            jobPath = "job/" + viaName;
        }

        int latestBuild = getLatestBuild(jobPath, wantSnapshot);
        if (latestBuild == -1) {
            System.err.println("AutoViaUpdater: no matching build found for " + jobPath);
            return false;
        }

        String localFileName = buildKey.replace("%20", "-");

        int downloadedBuild = getDownloadedBuild(buildKey);
        if (downloadedBuild == -1) {
            downloadAndRecordBuild(buildKey, latestBuild,
                    () -> downloadUpdate(jobPath, latestBuild, localFileName));
            System.out.println(localFileName + " was downloaded for the first time. " + "Please restart to let the plugin take effect.");
            return true;

        } else if (downloadedBuild != latestBuild) {
            downloadAndRecordBuild(buildKey, latestBuild,
                    () -> downloadUpdate(jobPath, latestBuild, localFileName));
            return true;
        }

        return false;
    }

    static void downloadAndRecordBuild(String buildKey, int build, DownloadAction download) throws IOException {
        download.download();
        updateBuildNumber(buildKey, build);
    }

    private static int getLatestBuild(String jobPath, boolean wantSnapshot) throws IOException {
        String listUrl = "https://ci.viaversion.com/" + jobPath + "/api/json?tree=builds[number]";
        ArrayNode builds = (ArrayNode) readJson(listUrl).get("builds");
        if (builds == null) return -1;

        for (JsonNode b : builds) {
            int num = b.get("number").asInt();
            String file = getArtifactFileName(jobPath, num);
            if (file == null) continue;
            boolean isSnap = file.contains("-SNAPSHOT");
            if (wantSnapshot) return num;
            if (!isSnap) return num;
        }
        return -1;
    }

    private static String getArtifactFileName(String jobPath, int build) throws IOException {
        String url = "https://ci.viaversion.com/" + jobPath + "/" + build + "/api/json";
        ArrayNode artifacts = (ArrayNode) readJson(url).get("artifacts");
        if (artifacts == null) return null;

        for (JsonNode art : artifacts) {
            String file = art.get("fileName").asText();
            if (!file.contains("sources")) return file;
        }
        return null;
    }

    private static void downloadUpdate(String jobPath, int build, String localName) throws IOException {
        String rel = getLatestDownload(jobPath, build);
        String url = "https://ci.viaversion.com/" + jobPath + "/" + build + "/artifact/" + rel;

        boolean updateFolder = new File(directory, "update").exists();
        String outPath = directory + "/" + localName + ".jar";

        if (updateFolder) {
            File[] files = new File(directory).listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile() &&
                            f.getName().toLowerCase().contains(
                                    localName.toLowerCase()
                                            .replace("-dev", "")
                                            .replace("-java8", ""))) {
                        outPath = directory + "/update/" + localName + ".jar";
                        break;
                    }
                }
            }
        }

        downloadToTarget(new URL(url), Paths.get(outPath));
        System.out.println("New version of " + localName + " downloaded. Please restart the server.");
    }

    static void downloadToTarget(final URL downloadUrl, Path target) throws IOException {
        SafeFileReplace.replace(target, temporaryFile -> {
            URLConnection conn = downloadUrl.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);

            try (InputStream in = conn.getInputStream();
                 OutputStream out = Files.newOutputStream(temporaryFile)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }

            validateJar(temporaryFile);
        });
    }

    static void validateJar(Path candidate) throws IOException {
        try (JarFile jar = new JarFile(candidate.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            byte[] buffer = new byte[8192];
            boolean containsFile = false;

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }

                containsFile = true;
                try (InputStream in = jar.getInputStream(entry)) {
                    while (in.read(buffer) != -1) {
                        // Read every entry so corrupt compressed data is rejected.
                    }
                }
            }

            if (!containsFile) {
                throw new IOException("Downloaded update JAR contains no files: " + candidate.getFileName());
            }
        } catch (IOException invalidJar) {
            throw new IOException("Downloaded update is not a valid JAR: " + candidate.getFileName(), invalidJar);
        }
    }

    private static String getLatestDownload(String jobPath, int build) throws IOException {
        String url = "https://ci.viaversion.com/" + jobPath + "/" + build + "/api/json";
        ArrayNode artifacts = (ArrayNode) readJson(url).get("artifacts");

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

    private static JsonNode readJson(String url) throws IOException {
        URLConnection conn = new URL(url).openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);
        try (InputStream in = conn.getInputStream()) {
            return MAPPER.readTree(in);
        }
    }
}
