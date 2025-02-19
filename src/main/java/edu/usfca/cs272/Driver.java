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
	/**
	 * The inverted index to store word locations
	 */
	private static final Map<String, TreeMap<String, TreeSet<Integer>>> index = new TreeMap<>();

	/**
	 * Processes the input file, building both word counts and inverted index.
	 * 
	 * @param inputPath the path to process text files from, can be a single file or directory
	 * @param outputPath the path to write word counts to, or null if no output needed
	 * @throws IOException if an IO error occurs while reading or writing files
	 * @return a TreeMap containing file paths and their word counts
	 */
	private static TreeMap<String, Integer> processFile(Path inputPath, Path outputPath) throws IOException {
		TreeMap<String, Integer> counts = new TreeMap<>();
		
		// Process single file or directory
		if (Files.isRegularFile(inputPath)) {
			processPath(inputPath, counts);
		}
		else if (Files.isDirectory(inputPath)) {
			Files.walk(inputPath)
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
		
		// Write results if output path provided
		if (outputPath != null) {
			JsonWriter.writeObject(counts, outputPath);
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
		for (int i = 0; i < stems.size(); i++) {
			String stem = stems.get(i);
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
		
		try {
			index.clear();
			Path inputPath = parser.getPath("-text");
			Path countsPath = parser.getPath("-counts", Path.of("counts.json"));
			Path indexPath = parser.getPath("-index", Path.of("index.json"));
			
			// Handle empty input case
			if (inputPath == null) {
				if (parser.hasFlag("-counts")) {
					JsonWriter.writeObject(new TreeMap<>(), countsPath);
				}
				if (parser.hasFlag("-index")) {
					JsonWriter.writeInvertedIndex(new TreeMap<>(), indexPath);
				}
				return;
			}
			
			// Process input and write outputs
			processFile(inputPath, parser.hasFlag("-counts") ? countsPath : null);
			if (parser.hasFlag("-index")) {
				JsonWriter.writeInvertedIndex(index, indexPath);
			}
			
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
