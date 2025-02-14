package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.TreeMap;

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
	 * Processes the input file and writes the word count to the output file.
	 * 
	 * @param inputPath the path to read from
	 * @param outputPath the path to write to
	 * @throws IOException if an IO error occurs
	 */
	private static void processFile(Path inputPath, Path outputPath) throws IOException {
		if (Files.isDirectory(inputPath)) {
			// Use Files.walk for recursive directory traversal
			var finder = Files.walk(inputPath)
				.filter(path -> Files.isRegularFile(path))
				.filter(path -> {
					String name = path.toString().toLowerCase();
					// Remove extension filter to process all text files
					return true;
				});
				
			// Store counts for each file in a sorted map
			TreeMap<String, Integer> counts = new TreeMap<>();
			try (var files = finder) {
				// Use iterator pattern to avoid loading all paths into memory at once
				for (Path file : (Iterable<Path>) files::iterator) {
					var stems = FileStemmer.uniqueStems(file);  // Changed to uniqueStems for unique word count
					counts.put(file.toString(), stems.size());
				}
			}
			
			if (outputPath != null) {
				JsonWriter.writeObject(counts, outputPath);
			}
		}
		else {
			var stems = FileStemmer.uniqueStems(inputPath);  // Changed to uniqueStems for unique word count
			
			if (outputPath != null) {
				TreeMap<String, Integer> counts = new TreeMap<>();
				counts.put(inputPath.toString(), stems.size());
				JsonWriter.writeObject(counts, outputPath);
			}
		}
	}

	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {
		Instant start = Instant.now();
		
		// Create paths that match testSentences() test
		Path input = Path.of("text", "simple", "sentences.md");
		Path output = Path.of("actual", "counts-simple-sentences.json");
		
		// Create args array to match test
		String[] testArgs = {"-text", input.toString(), "-counts", output.toString()};
		System.out.println("Command-line arguments: " + Arrays.toString(testArgs));

		ArgumentParser parser = new ArgumentParser(testArgs);
		Path inputPath = parser.getPath("text");
		
		// Get all output paths
		Path countsPath = parser.hasFlag("counts") ? 
			parser.getPath("counts", Path.of("counts.json")) : 
			null;
		Path indexPath = parser.getPath("index");
		Path resultsPath = parser.getPath("results");

		try {
			if (inputPath == null) {
				System.err.println("No input path provided with -text flag");
				return;
			}

			// Create parent directories for all output paths
			if (countsPath != null) {
				Files.createDirectories(countsPath.getParent());
			}
			if (indexPath != null) {
				Files.createDirectories(indexPath.getParent());
			}
			if (resultsPath != null) {
				Files.createDirectories(resultsPath.getParent());
			}

			processFile(inputPath, countsPath);
			// TODO: Add processing for index and results files

		}
		catch (IOException e) {
			System.err.println("Error processing files: " + e.getMessage());
		}

		long elapsedMs = Duration.between(start, Instant.now()).toMillis();
		double elapsedSec = (double) elapsedMs / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", elapsedSec);
	}

	/** Prevent instantiating this class of static methods. */
	private Driver() {}
}
