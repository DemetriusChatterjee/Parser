package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * A utility class for processing search queries. This class provides methods for cleaning,
 * parsing, and stemming query words from both individual lines and files. It ensures all
 * processed queries are returned as sorted lists of unique stems.
 *
 * @author Demetrius Chatterjee
 * @version Spring 2025
 */
public final class QueryProcessor {
	/** Prevent instantiation of utility class. */
	private QueryProcessor() {}
	
	/**
	 * Processes a single line of query text into a sorted list of unique stems.
	 * The processing includes:
	 * 1. Cleaning and parsing the text into words
	 * 2. Stemming each word
	 * 3. Removing duplicates
	 * 4. Sorting the stems alphabetically
	 *
	 * @param line the line of query text to process
	 * @return a sorted list of unique stems
	 */
	public static List<String> processLine(final String line) { // TODO These are essentially add methods for your map of results
		// Use TreeSet to handle both duplicate removal and sorting
		final TreeSet<String> stems = FileStemmer.uniqueStems(line);
		return new ArrayList<>(stems);
		
		// TODO This can store the results into the map
	}
	
	/**
	 * Reads a query file and processes each line into a list of queries,
	 * where each query is a sorted list of unique stems. Empty or blank
	 * lines in the input file are skipped.
	 *
	 * @param path the path to the query file to process
	 * @return a list of processed queries, where each query is a sorted list of unique stems
	 * @throws IOException if unable to read or process the query file
	 */
	public static List<List<String>> processQueryFile(final Path path) throws IOException {
		final List<List<String>> queries = new ArrayList<>();
		
		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.isBlank()) {
					queries.add(processLine(line));
				}
			}
		}
		
		return queries;
	}
}