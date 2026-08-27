package common;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

final class SafeFileReplace {

    interface ContentWriter {
        void write(Path temporaryFile) throws IOException;
    }

    interface AtomicMover {
        void move(Path source, Path target) throws IOException;
    }

    private static final AtomicMover DEFAULT_MOVER = (source, target) -> {
        try {
            Files.move(source, target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException unsupported) {
            throw new IOException("Atomic replacement is not supported for " + target
                    + "; the original file was preserved", unsupported);
        }
    };

    private SafeFileReplace() {
    }

    static void replace(Path target, ContentWriter writer) throws IOException {
        replace(target, writer, DEFAULT_MOVER);
    }

    static void replace(Path target, ContentWriter writer, AtomicMover mover) throws IOException {
        if (target == null) {
            throw new IllegalArgumentException("target");
        }
        if (writer == null) {
            throw new IllegalArgumentException("writer");
        }
        if (mover == null) {
            throw new IllegalArgumentException("mover");
        }

        Path absoluteTarget = target.toAbsolutePath().normalize();
        Path parent = absoluteTarget.getParent();
        if (parent == null) {
            throw new IOException("Target has no parent directory: " + target);
        }

        Files.createDirectories(parent);
        String prefix = absoluteTarget.getFileName().toString() + ".";
        if (prefix.length() < 3) {
            prefix = "update.";
        }

        Path temporaryFile = Files.createTempFile(parent, prefix, ".tmp");
        boolean replaced = false;
        try {
            writer.write(temporaryFile);

            if (!Files.isRegularFile(temporaryFile) || Files.size(temporaryFile) == 0L) {
                throw new IOException("Downloaded update is empty: " + temporaryFile.getFileName());
            }

            try (FileChannel channel = FileChannel.open(temporaryFile, StandardOpenOption.WRITE)) {
                channel.force(true);
            }

            mover.move(temporaryFile, absoluteTarget);
            replaced = true;
        } finally {
            if (!replaced) {
                Files.deleteIfExists(temporaryFile);
            }
        }
    }
}
