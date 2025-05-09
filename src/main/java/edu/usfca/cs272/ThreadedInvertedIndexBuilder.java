package edu.usfca.cs272;

import java.io.IOException;
import java.io.UncheckedIOException;
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
        queue.execute(new FileTask(path));
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
        super.build(path);
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

        /**
         * Initializes the indexing task.
         *
         * @param file  the file to index
         */
        public FileTask(Path file) {
            this.file = file;
        }

        /**
         * Processes the file and merges the results into the thread-safe index.
         * This method is called when the task is executed by the WorkQueue.
         */
        @Override
        public void run() {
            try {
                InvertedIndex local = new InvertedIndex();
                List<String> stems = FileStemmer.listStems(file);
                local.addAll(stems, file.toString());
                index.mergeIndex(local);
            } catch (IOException e) {
                System.err.println("Unable to process file: " + file);
                throw new UncheckedIOException(e);
            }
        }
    } 
}