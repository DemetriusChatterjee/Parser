package edu.usfca.cs272;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

import java.util.Collections;

/**
 * A utility class for processing search queries. This class provides methods for cleaning,
 * parsing, and stemming query words from both individual lines and files. It ensures all
 * processed queries are returned as sorted lists of unique stems.
 *
 * @author Demetrius Chatterjee
 * @version Spring 2025
 */
public final class QueryProcessor { // TODO Why make the class final?
	/** The map to store exact search results for each query. */
	private final TreeMap<String, List<InvertedIndex.SearchResult>> allResultsExact;
	
	/** The map to store partial search results for each query. */
	private final TreeMap<String, List<InvertedIndex.SearchResult>> allResultsPartial;

	/** The stemmer to use for stemming query words. */
	private final SnowballStemmer stemmer;

	/** The inverted index to store the results of the search to. */
	private final InvertedIndex index;
	
	/**
	 * Constructor for QueryProcessor.
	 * 
	 * @param index the inverted index to use for searching
	 */
	public QueryProcessor(InvertedIndex index) {
		this.allResultsExact = new TreeMap<>();
		this.allResultsPartial = new TreeMap<>();
		this.stemmer = new SnowballStemmer(ENGLISH);
		this.index = index;
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
	
	// TODO private Map<...> getAllResults(boolean ...) { return exact or partial }
	
	/**
	 * Processes a single query line, stems the words, searches the index, and stores the results.
	 *
	 * @param line the query line to process
	 * @param usePartialSearch whether to use partial search
	 * @return a list of search results from the inverted index
	**/
	public List<InvertedIndex.SearchResult> processQueryLine(String line, boolean usePartialSearch) {
		// Process the line into stems
		TreeSet<String> stems = processLine(line);
		if (stems.isEmpty()) {
			return Collections.emptyList();
		}
		
		// Get the query string
		String queryString = getQueryString(stems);
		
		/*
		 * TODO
		 * 
		 * var allResults = getAllResults(...);
		 * 
		 * if the queryString is already in the allResults map, skip searching...
		 */
		
		
		// Search the index
		List<InvertedIndex.SearchResult> results;
		if (usePartialSearch) { // TODO results = index.search(stems, usePartialSearch)
			results = index.partialSearch(stems);
		} else {
			results = index.exactSearch(stems);
		}
		
		// TODO allResults.put(...)
		
		// Store the results
		if (usePartialSearch) {
			allResultsPartial.put(queryString, results);
		} else {
			allResultsExact.put(queryString, results);
		}
		
		return results;
	}

	/**
	 * Processes a query file and stores search results from the inverted index.
	 *
	 * @param path the path to the query file
	 * @param usePartialSearch whether to use partial search
	 * @throws IOException if unable to read or process the query file
	 */
	public void processSearchResults(Path path, boolean usePartialSearch) throws IOException { // TODO processQueryFile
		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim(); // TODO Remove
				if (!line.isEmpty()) { // TODO Remove
					processQueryLine(line, usePartialSearch);
				}
			}
		}
	}
	
	/**
	 * Returns the query string from the given stems.
	 *
	 * @param stems the stems to use
	 * @return the query string
	 */
	public String getQueryString(TreeSet<String> stems) { // TODO Make static
		return String.join(" ", stems);
	}
	
	/* TODO Pass in the boolean for the get methods to match your process methods...
	public ???? getResults(boolean usePartialResults) {
		
	}
	
	The get methods below break encapsulation
	*/
	
	/**
	 * Returns the exact search results.
	 *
	 * @return the exact search results
	 */
	public Map<String, List<InvertedIndex.SearchResult>> getExactResults() {
		return Collections.unmodifiableMap(allResultsExact);
	}
	
	/**
	 * Returns the partial search results.
	 *	
	 * @return the partial search results
	 */
	public Map<String, List<InvertedIndex.SearchResult>> getPartialResults() {
		return Collections.unmodifiableMap(allResultsPartial);
	}
	
	// TODO toString
}