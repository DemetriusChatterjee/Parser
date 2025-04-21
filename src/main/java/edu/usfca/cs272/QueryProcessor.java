package edu.usfca.cs272;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

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
public final class QueryProcessor {
	/** The map to store search results for each query. */
	private final TreeMap<String, List<InvertedIndex.SearchResult>> allResults;
	
	// TODO Make this a member here: SnowballStemmer stemmer;
	
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
	private TreeSet<String> processLine(final String line) { // TODO Public
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
		// TODO Make public and combine with processSearchResults and move all of the per-line work into a new processQueryLine method
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
	
	/*
	 * TODO processQueryLine method:
	 * 
	 * stem
	 * join
	 * search
	 * store the results
	 * 
	 * Could make this return the list of results too
	 */
	
	/*
	 * TODO 
	 * processSearchResults(queries.txt, helloIndex, false)
	 * hello world --> hello.txt, world.txt
	 * apple --> fruit.txt
	 * 
	 * ---
	 * 
	 * Should we be able to change the query file?
	 * 
	 * processSearchResults(words.txt, helloIndex, false)
	 * apple --> fruit.txt
	 * ant cat --> animals.txt, words.txt
	 * 
	 * This is good, should be a parameter.
	 * 
	 * ----
	 * 
	 * What about the index?
	 * 
	 * processSearchResults(queries.txt, worldIndex, false)
	 * hello world --> world.html
	 * apple --> words.txt, food.txt, fruit.txt
	 * 
	 * We lose the original results from helloIndex!
	 * We also lose our ability to interpret the results
	 * 
	 * This is a problem! Make the index a member of the class that you pass and init
	 * in the constructor and can't change after that.
	 * 
	 * ----
	 * 
	 * processSearchResults(queries.txt, helloIndex, true)
	 * hello world --> hello.txt, world.txt. earth.txt
	 * apple --> fruit.txt, food.txt
	 * 
	 * The same problem! But not the same solution!
	 * 
	 * Choose 1 option:
	 * 
	 * 1) Is to do the same thing as before and make the partial search boolean
	 * a member of the class that can't change.
	 * 
	 * 2) Is to create 2 different maps, one partialResults and one exactResults.
	 * This is a little hard to multithread but can be made more efficient.
	 * 
	 * 3) Is to add a Boolean key to your existing map (this is a compromise option).
	 * 
	 *   a) Map<Boolean, TreeMap<String, List<InvertedIndex.SearchResult>>> allResults;
	 *   (this is an okay option)
	 *   
	 *   b) TreeMap<String, Map<Boolean, List<InvertedIndex.SearchResult>>> allResults;
	 *   (this I don't recommend)
	 */
	
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
			
			allResults.put(queryString, Collections.unmodifiableList(results));
		}
		
		/*
		 * TODO Create separate view/get methods instead (more reusable)
		 */
		return Collections.unmodifiableMap(allResults);
	}
	
	/**
	 * Returns the query string from the given stems.
	 *
	 * @param stems the stems to use
	 * @return the query string
	 */
	private String getQueryString(TreeSet<String> stems) { // TODO public
		return String.join(" ", stems);
	}
	
	// TODO toString
}