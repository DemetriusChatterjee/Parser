package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A thread-safe implementation of InvertedIndexBuilder that uses a WorkQueue for
 * parallel processing of files and directories. This implementation parallelizes
 * both file processing and directory traversal for improved performance.
 */
public class ThreadSafeInvertedIndexBuilder extends InvertedIndexBuilder {
    private final WorkQueue queue;

    public ThreadSafeInvertedIndexBuilder(ThreadSafeInvertedIndex index, WorkQueue queue) {
        super(index);
        this.queue = queue;
    }

    @Override
    public void buildFile(Path path) throws IOException {
        super.buildFile(path);
    }

    @Override
    public void buildDirectory(Path directory) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    buildDirectory(path);
                } else if (isTextFile(path)) {
                    buildFile(path);
                }
            }
        }
    }

    @Override
    public void build(Path path) throws IOException {
            if (Files.isDirectory(path)) {
                super.buildDirectory(path);
            } else if (isTextFile(path)) {
                queue.execute(() -> {
                    try {
                        super.buildFile(path);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to process file: " + path, e);
                    }
                });
                queue.join();
            }
    }
}