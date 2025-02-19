package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Demetrius Chatterjee
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
public class Driver {
	/*
	 * TODO
	 * Create 2 new classes:
	 * 
	 * 1) InvertedIndex data structure class that holds both the index and word
	 * count tree maps.
	 * 
	 * 2) "Builder" class, TextFileIndexer or InvertedIndexBuilder that handles
	 * the file IO, directory traversing, and stemming stuff. 
	 */
		
		
	// TODO Move this to data structure class, keep the final keyword but don't use the static keyword
	/**
	 * The inverted index to store word locations
	 */
	private static final Map<String, TreeMap<String, TreeSet<Integer>>> index = new TreeMap<>();

	/**
	 * Processes the input file, building both word counts and inverted index.
	 * 
	 * @param inputPath the path to process text files from, can be a single file or directory
	 * @param countsPath the path to write word counts to, or null if no output needed
	 * @param indexPath the path to write inverted index to, or null if no output needed
	 * @throws IOException if an IO error occurs while reading or writing files
	 * @return a TreeMap containing file paths and their word counts
	 */
	// TODO public static void processFile(Path inputPath, InvertedIndex index)
	private static TreeMap<String, Integer> processFile(Path inputPath, Path countsPath, Path indexPath) throws IOException {
		TreeMap<String, Integer> counts = new TreeMap<>(); // TODO Move into the data structure class
		
		// Process single file or directory
		if (Files.isRegularFile(inputPath)) {
			processPath(inputPath, counts);
		}
		else if (Files.isDirectory(inputPath)) {
			Files.walk(inputPath) // TODO Where did this come from? (Missing a citation)
				.filter(path -> path.toString().toLowerCase().endsWith(".txt") || 
							path.toString().toLowerCase().endsWith(".text"))
				.forEach(path -> {
					try {
						processPath(path, counts);
					}
					catch (IOException e) {
						// Skip files with IO errors
					}
				});
		}
		
		// TODO Move the logic below back into the main method
		// Write results if output paths provided
		if (countsPath != null) {
			JsonWriter.writeObject(counts, countsPath);
		}
		if (indexPath != null) {
			JsonWriter.writeObject(index, indexPath);  // Use modified JsonWriter method
		}
		
		return counts;
	}

	/**
	 * Processes a single file, adding its stems to the counts and index.
	 *
	 * @param path the file path to process
	 * @param counts the map to store word counts
	 * @throws IOException if an IO error occurs
	 */
	private static void processPath(Path path, TreeMap<String, Integer> counts) throws IOException {
		var stems = FileStemmer.listStems(path);
		if (!stems.isEmpty()) {
			counts.put(path.toString(), stems.size());
			addToIndex(stems, path.toString());
		}
	}

	/**
	 * Adds stems to the inverted index with their positions
	 * 
	 * @param stems the list of stemmed words to add
	 * @param location the file path where the stems were found
	 */
	private static void addToIndex(java.util.List<String> stems, String location) {
		// Keep track of position (starting at 1)
		for (int i = 0; i < stems.size(); i++) { // TODO This loop would be a great addAll method
			String stem = stems.get(i);
			
			// TODO This logic below would make a great InvertedIndex.add method
			index.putIfAbsent(stem, new TreeMap<>());
			index.get(stem).putIfAbsent(location, new TreeSet<>());
			index.get(stem).get(location).add(i + 1);  // Add position (1-based)
		}
	}

	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program. Supported flags include:
	 *             "-text" for input file/directory path
	 *             "-counts" for word counts output path
	 *             "-index" for inverted index output path
	 */
	public static void main(String[] args) {
		Instant start = Instant.now();
		ArgumentParser parser = new ArgumentParser(args);
		/* TODO 
		InvertedIndex index = new InvertedIndex();
		
		if (parser.hasFlag("-text")) {
			Path inputPath = parser.getPath("-text");
			
			try {
					trigger the building/indexing process
			}
			catch ( ) {
					Unable to index the files at path: ...
			}
		}
		*/
		
		try {
			index.clear();
			Path inputPath = parser.getPath("-text");
			
			// TODO Use parser.getPath("-counts", Path.of("counts.json"));
			Path countsPath = parser.hasFlag("-counts") ? parser.getPath("-counts", Path.of("counts.json")) : null;
			Path indexPath = parser.hasFlag("-index") ? parser.getPath("-index", Path.of("index.json")) : null;
			
			// Handle empty input case
			if (inputPath == null) {
				if (countsPath != null) {
					JsonWriter.writeObject(new TreeMap<>(), countsPath);
				}
				if (indexPath != null) {
					JsonWriter.writeObject(new TreeMap<>(), indexPath);  // Use modified JsonWriter method
				}
				return;
			}
			
			// Process input and write outputs
			processFile(inputPath, countsPath, indexPath);
			
			Duration elapsed = Duration.between(start, Instant.now());
			System.out.printf("Elapsed: %.3f seconds%n", elapsed.toMillis() / 1000.0);
		}
		catch (IOException e) {
			System.err.println("Unable to process files: " + e.getMessage());
		}
	}

	/** Prevent instantiating this class of static methods. */
	private Driver() {}
}
