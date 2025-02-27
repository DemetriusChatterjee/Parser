package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.DirectoryStream;
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
	
	/*
	 * TODO Either use "build" or "process" to name things, but not both
	 */
	
	/**
	 * Processes a file or directory, adding its stems to the inverted index.
	 *
	 * @param inputPath the input path to process
	 * @throws IOException if an IO error occurs
	 */
	public void build(Path inputPath) throws IOException {
		if (inputPath == null || !Files.exists(inputPath)) { // TODO Remove this block
			return;
		}
		
		/* TODO 
		if (Files.isDirectory(inputPath)) {
			processDirectory(inputPath);
		}
		else {
			processFile(inputPath);
		}
		*/ 
		
		if (Files.isRegularFile(inputPath)) {
			processFile(inputPath);
		}
		else if (Files.isDirectory(inputPath)) {
			processDirectory(inputPath);
		}
	}
	
	/**
	 * Processes a directory recursively, finding all text files.
	 *
	 * @param directory the directory to process
	 * @throws IOException if an IO error occurs
	 */
	private void processDirectory(Path directory) throws IOException { // TODO public
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
			for (Path path : stream) {
				if (Files.isRegularFile(path) && isTextFile(path)) {
					processFile(path);
				}
				else if (Files.isDirectory(path)) {
					processDirectory(path);
				}
			}
		}
	}
	
	/**
	 * Tests whether a path is a text file.
	 *
	 * @param path the path to test
	 * @return true if the path is a text file
	 */
	private boolean isTextFile(Path path) { // TODO public and static
		String name = path.toString().toLowerCase();
		return name.endsWith(".txt") || name.endsWith(".text");
	}
	
	/**
	 * Processes a single text file, adding its stems to the inverted index.
	 *
	 * @param path the path to process
	 * @throws IOException if an IO error occurs
	 */
	private void processFile(Path path) throws IOException { // TODO public
		if (path == null || !Files.isReadable(path)) { // TODO Remove
			return;
		}
		
		var stems = FileStemmer.listStems(path);
		if (!stems.isEmpty()) { // TODO Remove if statement
			index.addAll(stems, path.toString());
		}
	}
}