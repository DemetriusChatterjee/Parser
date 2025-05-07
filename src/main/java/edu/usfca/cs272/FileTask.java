package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;

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
            processFile();
            mergeResults();
        } catch (IOException e) {
            handleError(e);
        }
    }
    
    /**
     * Processes the file and builds the local index.
     * 
     * @throws IOException if there is an error reading the file
     */
    private void processFile() throws IOException {
        builder.buildFile(file);
    }
    
    /**
     * Merges the local index into the thread-safe index.
     */
    private void mergeResults() {
        index.mergeIndex(local);
    }
    
    /**
     * Handles any errors that occur during file processing.
     * 
     * @param e the exception that occurred
     */
    private void handleError(IOException e) {
        System.err.println("Unable to process file: " + file);
        throw new RuntimeException("Failed to process path: " + file, e);
    }
} 