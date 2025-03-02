package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Demetrius Chatterjee
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
public class Driver {
	private static final Logger LOGGER = Logger.getLogger(Driver.class.getName());

	/**
	 * Helper method to write JSON output to a file. Handles writing the data and any
	 * potential IO exceptions.
	 *
	 * @param data the map of data to write as JSON
	 * @param path the path to write the JSON file
	 * @param errorMessage the error message to use if writing fails
	 */

	private static void writeJsonOutput(Map<String, ?> data, Path path, String errorMessage) {
		try {
			JsonWriter.writeObject(data, path);
		}
		catch (IOException e) {
			LOGGER.warning(errorMessage + ": " + e.getMessage());
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
	 *             "-query" for query file path
	 *             "-results" for search results output path
	 */
	public static void main(final String[] args) {
		final Instant start = Instant.now();
		final ArgumentParser parser = new ArgumentParser(args);
		final InvertedIndex index = new InvertedIndex();
		
		// Process input path if provided
		if (parser.hasFlag("-text")) {
			Path inputPath = parser.getPath("-text");
			if (inputPath != null) {
				try {
					InvertedIndexBuilder builder = new InvertedIndexBuilder(index);
					builder.build(inputPath);
				}
				catch (IllegalArgumentException e) {
					System.err.println("Invalid path: " + inputPath);
				}
				catch (IOException e) {
					System.err.println("Unable to index the files at path: " + inputPath);
				}
			}
			else {
				System.err.println("No path provided for -text flag.");
			}
		}
		
		// Write output files if flags are provided
		// Write counts output if flag provided
		if (parser.hasFlag("-counts")) {
			Path countsPath = parser.getPath("-counts", Path.of("counts.json"));
			writeJsonOutput(index.getCounts(), countsPath, "Unable to write counts to file");
		}
		
		// Write index output if flag provided 
		if (parser.hasFlag("-index")) {
			Path indexPath = parser.getPath("-index", Path.of("index.json")); 
			writeJsonOutput(index.getIndex(), indexPath, "Unable to write index to file");
		}

		// Handle search results
		Map<String, List<InvertedIndex.SearchResult>> searchResults = new TreeMap<>();
		
		if (parser.hasFlag("-query")) {
			Path queryPath = parser.getPath("-query");
			if (queryPath != null) {
				try {
					// Process all queries from the file
					var queries = QueryProcessor.processQueryFile(queryPath);
					
					// Convert processed queries back to strings for searching
					List<String> queryStrings = new ArrayList<>();
					for (List<String> query : queries) {
						queryStrings.add(String.join(" ", query));
					}
					
					// Perform exact search for all queries
					searchResults = index.exactSearchAll(queryStrings);
				}
				catch (IOException e) {
					LOGGER.warning("Unable to process query file: " + e.getMessage());
				}
			}
		}
		
		// Write results if -results flag is provided (even if empty)
		if (parser.hasFlag("-results")) {
			try {
				Path resultsPath = parser.getPath("-results", Path.of("results.json"));
				JsonWriter.writeSearchResults(searchResults, resultsPath);
			}
			catch (IOException e) {
				System.err.println("Unable to write results to file");
			}
		}
		
		Duration elapsed = Duration.between(start, Instant.now());
		System.out.printf("Elapsed: %.3f seconds%n", elapsed.toMillis() / 1000.0);
	}

	/** Prevent instantiating this class of static methods. */
	private Driver() {}
}
