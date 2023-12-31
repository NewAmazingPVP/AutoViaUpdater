package commonrework;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class YamlFileManager {

    public static String file;

    public static void createYamlFile(String folder) {
        file = folder + "/donottouch.yml";
        Path filePath = Paths.get(file);

        if (!Files.exists(filePath)) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

            Yaml yaml = new Yaml(options);

            Map<String, Integer> initialData = Map.ofEntries(
                    Map.entry("ViaVersion", -1),
                    Map.entry("ViaVersion-Dev", -1),
                    Map.entry("ViaBackwards", -1),
                    Map.entry("ViaBackwards-Dev", -1),
                    Map.entry("ViaRewind", -1),
                    Map.entry("ViaRewind-Dev", -1),
                    Map.entry("ViaRewind%20Legacy%20Support", -1)
            );


            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                yaml.dump(initialData, writer);
                System.out.println("YAML file created successfully.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("YAML file already exists. Skipping creation.");
        }
    }

    public static void updateBuildNumber(String key, int newBuildNumber) {
        try {
            Path filePath = Paths.get(file);
            Map<String, Integer> data = readYamlFile(filePath);

            if (data.containsKey(key)) {
                data.put(key, newBuildNumber);
                writeYamlFile(filePath, data);
                System.out.println(key + " build number updated to " + newBuildNumber);
            } else {
                System.out.println(key + " not found in the YAML file.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getDownloadedBuild(String key) {
        try {
            Path filePath = Paths.get(file);
            Map<String, Integer> data = readYamlFile(filePath);

            if (data.containsKey(key)) {
                return data.get(key);
            } else {
                System.out.println(key + " not found in the YAML file.");
                return -1;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static Map<String, Integer> readYamlFile(Path filePath) throws IOException {
        Yaml yaml = new Yaml();
        try {
            Object obj = yaml.load(Files.newBufferedReader(filePath));
            if (obj instanceof Map) {
                Map<String, Integer> result = (Map<String, Integer>) obj;
                return result;
            } else {
                throw new RuntimeException("Invalid YAML file format. Expected a Map.");
            }
        } catch (IOException e) {
            throw new IOException("Error reading YAML file", e);
        }
    }

    private static void writeYamlFile(Path filePath, Map<String, Integer> data) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            yaml.dump(data, writer);
        }
    }
}
