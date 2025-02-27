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
 * A utility class for processing search queries. This includes cleaning, parsing,
 * and stemming query words according to the project requirements.
 */
public class QueryProcessor {
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
	public static List<String> processLine(String line) {
		// Use TreeSet to handle both duplicate removal and sorting
		TreeSet<String> stems = FileStemmer.uniqueStems(line);
		return new ArrayList<>(stems);
	}
	
	/**
	 * Reads a query file and processes each line into a list of queries,
	 * where each query is a sorted list of unique stems.
	 *
	 * @param path the path to the query file
	 * @return a list of processed queries
	 * @throws IOException if an IO error occurs
	 */
	public static List<List<String>> processQueryFile(Path path) throws IOException {
		List<List<String>> queries = new ArrayList<>();
		
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