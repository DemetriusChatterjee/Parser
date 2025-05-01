package edu.usfca.cs272;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * A utility class for processing search queries. This class provides methods for cleaning,
 * parsing, and stemming query words from both individual lines and files. It ensures all
 * processed queries are returned as sorted lists of unique stems.
 *
 * @author Demetrius Chatterjee
 * @version Spring 2025
 */
public class QueryProcessor {
	/** The map to store exact search results for each query. */
	private final TreeMap<String, List<InvertedIndex.SearchResult>> allResultsExact;
	
	/** The map to store partial search results for each query. */
	private final TreeMap<String, List<InvertedIndex.SearchResult>> allResultsPartial;

	/** The stemmer to use for stemming query words. */
	private final SnowballStemmer stemmer;

	/** The inverted index to store the results of the search to. */
	private final InvertedIndex index;

	/** Whether to use partial search. */
	private final boolean usePartialSearch;
	
	/**
	 * Constructor for QueryProcessor.
	 * 
	 * @param index the inverted index to use for searching
	 * @param usePartialSearch whether to use partial search
	 */
	public QueryProcessor(InvertedIndex index, boolean usePartialSearch) {
		this.allResultsExact = new TreeMap<>();
		this.allResultsPartial = new TreeMap<>();
		this.stemmer = new SnowballStemmer(ENGLISH);
		this.index = index;
		this.usePartialSearch = usePartialSearch;
	}
	
	/**
	 * Processes a single line of query text into a sorted list of unique stems.
	 *
	 * @param line the line of query text to process
	 * @return a sorted TreeSet of unique stems from the processed line
	 */
	public TreeSet<String> processLine(final String line) {
		// Use TreeSet to handle both duplicate removal and sorting
		return FileStemmer.uniqueStems(line, stemmer);
	}
	
	/**
	 * Processes a single query line, stems the words, searches the index, and stores the results.
	 *
	 * @param line the query line to process
	 * @return a list of search results from the inverted index
	**/
	public List<InvertedIndex.SearchResult> processQueryLine(String line) {
		// Process the line into stems
		TreeSet<String> stems = processLine(line);
		if (stems.isEmpty()) {
			return Collections.emptyList();
		}
		
		// Get the query string
		String queryString = getQueryString(stems);
		
		// Check if we already have results for this query
		if (getResults(usePartialSearch).containsKey(queryString)) {
			return getResults(usePartialSearch).get(queryString);
		}
		
		// Search the index
		List<InvertedIndex.SearchResult> results = index.search(stems, usePartialSearch);
		
		// Store the results
		getResults(usePartialSearch).put(queryString, results);
		
		return results;
	}

	/**
	 * Processes a query file and stores search results from the inverted index.
	 *
	 * @param path the path to the query file
	 * @throws IOException if unable to read or process the query file
	 */
	public void processQueryFile(Path path) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				processQueryLine(line);
			}
		}
	}
	
	/**
	 * Returns the query string from the given stems.
	 *
	 * @param stems the stems to use
	 * @return the query string
	 */
	public static String getQueryString(TreeSet<String> stems) {
		return String.join(" ", stems);
	}

	/**
	 * Writes the search results to a JSON file.
	 * 
	 * @param path the path to write the JSON file to
	 * @param usePartialResults whether to use partial search
	 * @throws IOException if an IO error occurs
	 */
	public void toJson(Path path, boolean usePartialResults) throws IOException {
		JsonWriter.writeSearchResults(getResults(usePartialResults), path);
	}
	
	/**
	 * Returns the results for the given usePartialResults.
	 * 
	 * @param usePartialResults whether to use partial search
	 * @return the results for the given usePartialResults
	 */
	private TreeMap<String, List<InvertedIndex.SearchResult>> getResults(boolean usePartialResults) {
		return usePartialResults ? allResultsPartial : allResultsExact;
	}

	/**
	 * Returns a string representation of the QueryProcessor object.
	 *
	 * @return a string representation of the QueryProcessor object
	 */
	@Override
	public String toString() {
		return String.format("QueryProcessor[exact=%d, partial=%d]", 
			allResultsExact.toString(), allResultsPartial.toString());
	}
	
	/**
	 * Returns an unmodifiable view of the search result keys.
	 * 
	 * @return an unmodifiable view of the search result keys
	 */
	public Set<String> getSearchResultKeys() {
		return Collections.unmodifiableSet(getResults(usePartialSearch).keySet());
	}

	/**
	 * Returns an unmodifiable view of the search results for a given query string.
	 * 
	 * @param queryString the query string to get results for
	 * @param usePartialSearch whether to use partial search results
	 * @return an unmodifiable view of the search results, or null if no results exist
	 */
	public List<InvertedIndex.SearchResult> getSearchResult(String queryString, boolean usePartialSearch) {
		List<InvertedIndex.SearchResult> results = getResults(usePartialSearch).get(queryString);
		return results != null ? Collections.unmodifiableList(results) : null;
	}
}