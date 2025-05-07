package edu.usfca.cs272;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * A thread-safe implementation of InvertedIndexBuilder that uses a WorkQueue for
 * parallel processing of files and directories. This implementation parallelizes
 * both file processing and directory traversal for improved performance.
 * 
 * @author Demetrius Chatterjee
 * @version Spring 2025
 */
public class ThreadedInvertedIndexBuilder extends InvertedIndexBuilder {
    /**
     * The work queue for parallel processing.
     */
    private final WorkQueue queue;
    /**
     * The inverted index to build.
     */
    private final ThreadSafeInvertedIndex index;

    
    /**
     * Constructs a new ThreadSafeInvertedIndexBuilder with the given index and work queue.
     *
     * @param index the inverted index to build
     * @param queue the work queue for parallel processing
     */
    public ThreadedInvertedIndexBuilder(ThreadSafeInvertedIndex index, WorkQueue queue) {
        super(index);
        this.queue = queue;
        this.index = index;
    }

    /**
     * Builds the index from a file by processing its contents.
     *
     * @param path the input path to process
     * @throws IOException if an IO error occurs during file processing
     */
    @Override
    public void buildFile(Path path) throws IOException {
        List<String> stems = FileStemmer.listStems(path);
        index.addAll(stems, path.toString());
    }

    /**
     * Builds the index from a directory by recursively processing text files.
     *
     * @param directory the directory to process
     * @throws IOException if an IO error occurs during file processing
     */
    @Override
    public void buildDirectory(Path directory) throws IOException { 
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
			for (var path : stream) {
				if (Files.isDirectory(path)) {
					buildDirectory(path);
				}
				else if (super.isTextFile(path)) {
                    //queue.execute(new FileTask(path, index));
                    queue.execute(() -> {
                        try {
                            buildFile(path);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
				}
			}
		}
	}

    /**
     * Builds the index from a file or directory path by processing its contents.
     * If the path points to a directory, processes all text files in that directory
     * and its subdirectories recursively.
     *
     * @param path the input path to process
     * @throws IOException if an IO error occurs during file processing
     * @throws IllegalArgumentException if the path is null or does not exist
     */
    @Override
    public void build(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            buildDirectory(path);
        } else if (super.isTextFile(path)) {
            //queue.execute(new FileTask(path, index));
            queue.execute(() -> {
                try {
                    buildFile(path);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
        queue.finish();
    }

/**
 * A Runnable task that indexes a single file and merges the results into a thread-safe index.
 * This class is designed to be used with a WorkQueue for parallel file processing.
 *
 * @author Demetrius Chatterjee and CHATGPT how to avoid writng to the same index from multiple threads so use multiple indexes and then merge them
 * @version Spring 2025
 */
public class FileTask implements Runnable {
    /** The file to index */
    private final Path file;
    
    /** The thread-safe index */
    private final ThreadSafeInvertedIndex index;
    
    /** The local index for this task */
    private final InvertedIndex local;
    
    /** The builder for constructing the index */
    private final InvertedIndexBuilder builder;

    /**
     * Initializes the indexing task.
     *
     * @param file  the file to index
     * @param index the thread-safe index to populate
     */
    public FileTask(Path file, ThreadSafeInvertedIndex index) {
        this.file = file;
        this.index = index;
        this.local = new InvertedIndex();
        this.builder = new InvertedIndexBuilder(local);
    }

    /**
     * Processes the file and merges the results into the thread-safe index.
     * This method is called when the task is executed by the WorkQueue.
     */
    @Override
    public void run() {
        try {
            builder.buildFile(file);
            index.mergeIndex(local);
        } catch (IOException e) {
            System.err.println("Unable to process file: " + file);
            throw new UncheckedIOException(e);
        }
    }
} 
}