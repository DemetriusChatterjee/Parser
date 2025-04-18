package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import opennlp.tools.stemmer.snowball.SnowballStemmer;


/**
 * Builder class responsible for processing text files and building an inverted index.
 * See the README for details.
 * 
 * @author Demetrius Chatterjee
 * @version Spring 2025
 * @see InvertedIndex
 */
public final class InvertedIndexBuilder {
	/** 
	 * The inverted index data structure to populate with processed text.
	 * This is marked final to ensure thread-safety and prevent modification
	 * after construction.
	 */
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
	 * Tests whether a path is a text file by checking its extension.
	 *
	 * @param path the path to test
	 * @return true if the path has a .txt or .text extension
	 */
	private static boolean isTextFile(Path path) {
		String name = path.toString().toLowerCase();
		return name.endsWith(".txt") || name.endsWith(".text");
	}
	

	/**
	 * Builds the index from a single text file by processing its contents.
	 * Extracts stems from the file and adds them to the index with their
	 * associated file path.
	 *
	 * @param path the text file to process
	 * @param index the inverted index to build
	 * @throws IOException if an IO error occurs during file reading or processing
	 */
	private static void buildFile(Path path, InvertedIndex index) throws IOException {
		try (var reader = Files.newBufferedReader(path)) {
			String line;
			var stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
			int position = 1;
			while ((line = reader.readLine()) != null) {
				for (String word : new FileStemmer().parse(line)) {
					String stem = stemmer.stem(word).toString();
					index.add(stem, path.toString(), position);
					position++;
				}
			}
		}
	}
	
	/**
	 * Builds the index from a single text file by processing its contents.
	 * Extracts stems from the file and adds them to the index with their
	 * associated file path.
	 *
	 * @param path the text file to process
	 * @throws IOException if an IO error occurs during file reading or processing
	 */
	private void buildFile(Path path) throws IOException {
		buildFile(path, this.index);
	}

	/**
	 * Builds the index from a directory by recursively processing text files.
	 * Processes all files with .txt or .text extensions in the directory and
	 * its subdirectories, maintaining a consistent processing order using a TreeSet.
	 *
	 * @param directory the directory to process
	 * @throws IOException if an IO error occurs during directory traversal or file processing
	 */
	private void buildDirectory(Path directory) throws IOException { 
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
	public final void build(Path path) throws IOException {
		if (Files.isDirectory(path)) {
			buildDirectory(path);
		}
		else {
			buildFile(path);
		}
	}

	/**
	 * Writes the word counts to a JSON file.
	 *
	 * @param path the path to write the counts to
	 * @throws IOException if an IO error occurs
	 */
	public void writeCounts(Path path) throws IOException {
		JsonWriter.writeCountsObject(index.getCounts(), path);
	}

	/**
	 * Writes the inverted index to a JSON file.
	 *
	 * @param path the path to write the index to
	 * @throws IOException if an IO error occurs
	 */
	public void writeIndex(Path path) throws IOException {
		JsonWriter.writeIndexObject(index.getIndex(), path);
	}
}