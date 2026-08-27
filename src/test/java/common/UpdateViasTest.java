package common;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

public class UpdateViasTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void installsValidJar() throws Exception {
        Path directory = temporaryFolder.newFolder("valid").toPath();
        Path source = createJar(directory.resolve("source.jar"));
        Path target = directory.resolve("ViaVersion.jar");

        UpdateVias.downloadToTarget(source.toUri().toURL(), target);

        assertArrayEquals(Files.readAllBytes(source), Files.readAllBytes(target));
        assertNoTemporaryFiles(directory, target.getFileName().toString());
    }

    @Test
    public void replacesExistingTargetWithValidJar() throws Exception {
        Path directory = temporaryFolder.newFolder("replace").toPath();
        Path source = createJar(directory.resolve("source.jar"));
        Path target = directory.resolve("ViaVersion.jar");
        Files.write(target, bytes("original"));

        UpdateVias.downloadToTarget(source.toUri().toURL(), target);

        assertArrayEquals(Files.readAllBytes(source), Files.readAllBytes(target));
        assertNoTemporaryFiles(directory, target.getFileName().toString());
    }

    @Test
    public void rejectsInvalidJarAndPreservesExistingTarget() throws Exception {
        Path directory = temporaryFolder.newFolder("invalid").toPath();
        Path source = directory.resolve("error-response.html");
        Path target = directory.resolve("ViaVersion.jar");
        Files.write(source, bytes("<html>temporary Jenkins error</html>"));
        Files.write(target, bytes("original"));

        assertThrows(IOException.class,
                () -> UpdateVias.downloadToTarget(source.toUri().toURL(), target));

        assertArrayEquals(bytes("original"), Files.readAllBytes(target));
        assertNoTemporaryFiles(directory, target.getFileName().toString());
    }

    @Test
    public void rejectsEmptyPayloadAndPreservesExistingTarget() throws Exception {
        Path directory = temporaryFolder.newFolder("empty-payload").toPath();
        Path source = directory.resolve("empty.jar");
        Path target = directory.resolve("ViaVersion.jar");
        Files.write(source, new byte[0]);
        Files.write(target, bytes("original"));

        assertThrows(IOException.class,
                () -> UpdateVias.downloadToTarget(source.toUri().toURL(), target));

        assertArrayEquals(bytes("original"), Files.readAllBytes(target));
        assertNoTemporaryFiles(directory, target.getFileName().toString());
    }

    @Test
    public void connectionFailureDoesNotCreateTargetOrLeaveTemporaryFile() throws Exception {
        Path directory = temporaryFolder.newFolder("missing-source").toPath();
        Path missing = directory.resolve("missing.jar");
        Path target = directory.resolve("ViaVersion.jar");

        assertThrows(IOException.class,
                () -> UpdateVias.downloadToTarget(missing.toUri().toURL(), target));

        assertFalse(Files.exists(target));
        assertNoTemporaryFiles(directory, target.getFileName().toString());
    }

    @Test
    public void failedDownloadDoesNotPersistNewBuildNumber() throws Exception {
        Path directory = temporaryFolder.newFolder("build-number").toPath();
        Path versions = directory.resolve("versions.yml");
        Files.write(versions, bytes("ViaVersion: 41\n"));
        String originalBuildFile = BuildYml.file;

        try {
            BuildYml.file = versions.toString();

            assertThrows(IOException.class, () -> UpdateVias.downloadAndRecordBuild(
                    "ViaVersion", 42, () -> {
                        throw new IOException("simulated download failure");
                    }));
            assertEquals(41, BuildYml.getDownloadedBuild("ViaVersion"));

            UpdateVias.downloadAndRecordBuild("ViaVersion", 42, () -> {
                // Successful download; persistence is now allowed.
            });
            assertEquals(42, BuildYml.getDownloadedBuild("ViaVersion"));
        } finally {
            BuildYml.file = originalBuildFile;
        }
    }

    private static Path createJar(Path path) throws IOException {
        try (OutputStream file = Files.newOutputStream(path);
             JarOutputStream jar = new JarOutputStream(file)) {
            jar.putNextEntry(new JarEntry("plugin.yml"));
            jar.write(bytes("name: TestPlugin\nversion: 1.0\nmain: test.Plugin\n"));
            jar.closeEntry();
        }
        return path;
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private static void assertNoTemporaryFiles(Path directory, String targetName) throws IOException {
        int count = 0;
        try (DirectoryStream<Path> files = Files.newDirectoryStream(directory, targetName + ".*.tmp")) {
            for (Path ignored : files) {
                count++;
            }
        }
        assertEquals(0, count);
    }
}
