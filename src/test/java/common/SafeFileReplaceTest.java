package common;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

public class SafeFileReplaceTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void replacesExistingFileAtomically() throws Exception {
        Path directory = temporaryFolder.newFolder("success").toPath();
        Path target = directory.resolve("ViaVersion.jar");
        Files.write(target, bytes("old"));
        Path[] temporaryFile = new Path[1];

        SafeFileReplace.replace(target, candidate -> {
            temporaryFile[0] = candidate;
            assertEquals(directory, candidate.getParent());
            Files.write(candidate, bytes("new"));
        });

        assertArrayEquals(bytes("new"), Files.readAllBytes(target));
        assertFalse(Files.exists(temporaryFile[0]));
        assertNoTemporaryFiles(directory, target.getFileName().toString());
    }

    @Test
    public void preservesTargetAndCleansUpAfterPartialWriteFailure() throws Exception {
        Path directory = temporaryFolder.newFolder("partial").toPath();
        Path target = directory.resolve("ViaVersion.jar");
        Files.write(target, bytes("original"));

        assertThrows(IOException.class, () -> SafeFileReplace.replace(target, candidate -> {
            Files.write(candidate, bytes("partial"));
            throw new IOException("simulated connection failure");
        }));

        assertArrayEquals(bytes("original"), Files.readAllBytes(target));
        assertNoTemporaryFiles(directory, target.getFileName().toString());
    }

    @Test
    public void rejectsEmptyDownloadWithoutReplacingTarget() throws Exception {
        Path directory = temporaryFolder.newFolder("empty").toPath();
        Path target = directory.resolve("ViaVersion.jar");
        Files.write(target, bytes("original"));

        assertThrows(IOException.class,
                () -> SafeFileReplace.replace(target, candidate -> Files.write(candidate, new byte[0])));

        assertArrayEquals(bytes("original"), Files.readAllBytes(target));
        assertNoTemporaryFiles(directory, target.getFileName().toString());
    }

    @Test
    public void preservesTargetAndCleansUpAfterAtomicMoveFailure() throws Exception {
        Path directory = temporaryFolder.newFolder("move-failure").toPath();
        Path target = directory.resolve("ViaVersion.jar");
        Files.write(target, bytes("original"));

        assertThrows(IOException.class, () -> SafeFileReplace.replace(
                target,
                candidate -> Files.write(candidate, bytes("new")),
                (source, destination) -> {
                    throw new AtomicMoveNotSupportedException(
                            source.toString(), destination.toString(), "simulated unsupported move");
                }));

        assertArrayEquals(bytes("original"), Files.readAllBytes(target));
        assertNoTemporaryFiles(directory, target.getFileName().toString());
    }

    @Test
    public void createsUniqueTemporaryNames() throws Exception {
        Path directory = temporaryFolder.newFolder("unique").toPath();
        Path target = directory.resolve("ViaVersion.jar");
        List<Path> temporaryFiles = new ArrayList<>();

        SafeFileReplace.replace(target, candidate -> {
            temporaryFiles.add(candidate);
            Files.write(candidate, bytes("first"));
        });
        SafeFileReplace.replace(target, candidate -> {
            temporaryFiles.add(candidate);
            Files.write(candidate, bytes("second"));
        });

        assertEquals(2, temporaryFiles.size());
        assertNotEquals(temporaryFiles.get(0), temporaryFiles.get(1));
        assertArrayEquals(bytes("second"), Files.readAllBytes(target));
        assertNoTemporaryFiles(directory, target.getFileName().toString());
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
