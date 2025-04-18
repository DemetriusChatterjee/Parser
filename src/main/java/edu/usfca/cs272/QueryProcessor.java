package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
	/** The map to store search results for each query. */
	private final TreeMap<String, List<InvertedIndex.SearchResult>> allResults;
	
	/**
	 * Constructor for QueryProcessor.
	 */
	public QueryProcessor() {
		this.allResults = new TreeMap<>();
	}
	
	/**
	 * Processes a single line of query text into a sorted list of unique stems.
	 *
	 * @param line the line of query text to process
	 * @return a sorted list of unique stems
	 */
	private TreeSet<String> processLine(final String line) {
		// Use TreeSet to handle both duplicate removal and sorting
		FileStemmer stemmer = new FileStemmer();
		return stemmer.uniqueStems(line);
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
	private List<TreeSet<String>> processQueryFile(Path path) throws IOException {
		List<TreeSet<String>> queries = new ArrayList<>();
		
		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					TreeSet<String> stems = processLine(line);
					if (stems != null && !stems.isEmpty()) {
						queries.add(stems);
					}
				}
			}
		}
		
		return queries;
	}
	
	/**
	 * Processes a query file and returns search results from the inverted index.
	 *
	 * @param path the path to the query file
	 * @param index the inverted index to search against
	 * @param usePartialSearch whether to use partial search
	 * @return map of search results for each query
	 * @throws IOException if unable to read or process the query file
	 */
	public Map<String, List<InvertedIndex.SearchResult>> processSearchResults(
			Path path, InvertedIndex index, boolean usePartialSearch) throws IOException {
		List<TreeSet<String>> queries = processQueryFile(path);
		
		// Process each query and add to results
		for (TreeSet<String> query : queries) {
			String queryString = getQueryString(query);
			TreeSet<String> processedQuery = query;
			
			List<InvertedIndex.SearchResult> results;
			if (usePartialSearch) {
				results = index.partialSearch(processedQuery);
			} else {
				results = index.exactSearch(processedQuery);
			}
			
			searchResults.put(queryString, results);
		}
		
		return searchResults;
	}
	
	/**
	 * Gets the cleaned and sorted query string from a list of stems.
	 * 
	 * @param stems the list of query stems
	 * @return the query string with stems joined by spaces
	 */
	private String getQueryString(TreeSet<String> stems) {
		return String.join(" ", stems);
	}
}