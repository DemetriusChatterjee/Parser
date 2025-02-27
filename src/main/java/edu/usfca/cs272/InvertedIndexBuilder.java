package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeSet;

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
	 * Builds the index from a file or directory path by processing its contents.
	 *
	 * @param path the input path to process
	 * @throws IOException if an IO error occurs
	 * @throws IllegalArgumentException if the path is null or does not exist
	 */
	public void build(Path path) throws IOException {
		if (path == null || !Files.exists(path)) {
			throw new IllegalArgumentException("Invalid path: " + path);
		}
		
		if (Files.isDirectory(path)) {
			buildDirectory(path);
		}
		else {
			buildFile(path);
		}
	}
	
	/**
	 * Builds the index from a directory by recursively processing text files.
	 *
	 * @param directory the directory to process
	 * @throws IOException if an IO error occurs
	 */
	public void buildDirectory(Path directory) throws IOException {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
			var paths = new TreeSet<Path>();
			stream.forEach(paths::add);
			
			for (var path : paths) {
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
	 * Tests whether a path is a text file by checking its extension.
	 *
	 * @param path the path to test
	 * @return true if the path has a .txt or .text extension
	 */
	public static boolean isTextFile(Path path) {
		var name = path.toString().toLowerCase();
		return name.endsWith(".txt") || name.endsWith(".text");
	}
	
	/**
	 * Builds the index from a single text file by processing its contents.
	 *
	 * @param path the text file to process
	 * @throws IOException if an IO error occurs
	 */
	public void buildFile(Path path) throws IOException {
		index.addAll(FileStemmer.listStems(path), path.toString());
	}
}