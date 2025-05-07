package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * A thread-safe implementation of InvertedIndexBuilder that uses a WorkQueue for
 * parallel processing of files and directories. This implementation parallelizes
 * both file processing and directory traversal for improved performance.
 * 
 * @author Demetrius Chatterjee
 * @version Spring 2025
 */
public class ThreadSafeInvertedIndexBuilder{
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
    public ThreadSafeInvertedIndexBuilder(ThreadSafeInvertedIndex index, WorkQueue queue) {
        this.queue = queue;
        this.index = index;
    }

    /**
	 * Tests whether a path is a text file by checking its extension.
	 *
	 * @param path the path to test
	 * @return true if the path has a .txt or .text extension
	 */
	public static boolean isTextFile(Path path) {
		String name = path.toString().toLowerCase();
		return name.endsWith(".txt") || name.endsWith(".text");
	}

    /**
     * Builds the index from a file by processing its contents.
     *
     * @param path the input path to process
     * @throws IOException if an IO error occurs during file processing
     */
    public void buildFile(Path path) throws IOException {
        InvertedIndexBuilder builder = new InvertedIndexBuilder(index);
        builder.buildFile(path);
    }

    /**
     * Builds the index from a directory by recursively processing text files.
     *
     * @param directory the directory to process
     * @throws IOException if an IO error occurs during file processing
     */
    public void buildDirectory(Path directory) throws IOException { 
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
			for (var path : stream) {
				if (Files.isDirectory(path)) {
					buildDirectory(path);
				}
				else if (isTextFile(path)) {
					buildFile(path);
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
    public void build(Path path) throws IOException {
        queue.execute(() -> {
            try {
                if (Files.isDirectory(path)) {
                    buildDirectory(path);
                } else if (isTextFile(path)) {
                    buildFile(path);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to process path: " + path, e);
            }
        });
        queue.finish();
    }
}