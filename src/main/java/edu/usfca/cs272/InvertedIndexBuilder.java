package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Builder class responsible for processing text files and building an inverted index.
 * See the README for details.
 * 
 * @author Demetrius Chatterjee
 * @version Spring 2025
 */
public class InvertedIndexBuilder {
	/** The inverted index to build */
	private final InvertedIndex index;
	
	/**
	 * Initializes the builder with the inverted index to populate.
	 *
	 * @param index the inverted index to build
	 * @throws IllegalArgumentException if the index is null
	 */
	public InvertedIndexBuilder(InvertedIndex index) {
		if (index == null) {
			throw new IllegalArgumentException("Index cannot be null.");
		}
		this.index = index;
	}
	
	/**
	 * Processes a file or directory, adding its stems to the inverted index.
	 *
	 * @param inputPath the input path to process
	 * @throws IOException if an IO error occurs
	 */
	public void build(Path inputPath) throws IOException {
		if (inputPath == null || !Files.exists(inputPath)) {
			return;
		}
		
		if (Files.isRegularFile(inputPath)) {
			processFile(inputPath);
		}
		else if (Files.isDirectory(inputPath)) {
			// Reference: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Files.html#walk(java.nio.file.Path,java.nio.file.FileVisitOption...)
			Files.walk(inputPath)
				.filter(path -> path.toString().toLowerCase().endsWith(".txt") || 
							path.toString().toLowerCase().endsWith(".text"))
				.forEach(path -> {
					try {
						processFile(path);
					}
					catch (IOException e) {
						// Skip files with IO errors
					}
				});
		}
	}
	
	/**
	 * Processes a single text file, adding its stems to the inverted index.
	 *
	 * @param path the path to process
	 * @throws IOException if an IO error occurs
	 */
	private void processFile(Path path) throws IOException {
		if (path == null || !Files.isReadable(path)) {
			return;
		}
		
		var stems = FileStemmer.listStems(path);
		if (!stems.isEmpty()) {
			index.addAll(stems, path.toString());
		}
	}
}