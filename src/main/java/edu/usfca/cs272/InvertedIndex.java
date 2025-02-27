package edu.usfca.cs272;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Data structure to store an inverted index and word counts from text files. 
 * See the README for details.
 *
 * @author Demetrius Chatterjee
 * @version Spring 2025
 */
public class InvertedIndex {
	/** The inverted index to store word locations */
	private final Map<String, TreeMap<String, TreeSet<Integer>>> index;
	
	/** Map to store word counts per file */
	private final TreeMap<String, Integer> counts;
	
	/**
	 * Initializes the inverted index and counts data structures.
	 */
	public InvertedIndex() {
		this.index = new TreeMap<>();
		this.counts = new TreeMap<>();
	}
	
	/**
	 * Adds a word stem and its position to the index for a specific file location.
	 *
	 * @param stem the word stem to add
	 * @param location the file path where the stem was found
	 * @param position the position of the stem in the file (1-based)
	 */
	public void add(String stem, String location, int position) {
		index.putIfAbsent(stem, new TreeMap<>());
		index.get(stem).putIfAbsent(location, new TreeSet<>());
		index.get(stem).get(location).add(position);
	}
	
	/**
	 * Adds all stems from a list to the index for a specific file location.
	 *
	 * @param stems the list of word stems to add
	 * @param location the file path where the stems were found
	 */
	public void addAll(List<String> stems, String location) {
		if (!stems.isEmpty()) {
			counts.put(location, stems.size());
			for (int i = 0; i < stems.size(); i++) {
				add(stems.get(i), location, i + 1);
			}
		}
	}
	
	/**
	 * Gets the inverted index data structure.
	 *
	 * @return the inverted index
	 */
	public Map<String, TreeMap<String, TreeSet<Integer>>> getIndex() {
		return index;
	}
	
	/**
	 * Gets the word counts data structure.
	 *
	 * @return the word counts
	 */
	public TreeMap<String, Integer> getCounts() {
		return counts;
	}
	
	/**
	 * Clears both the inverted index and word counts.
	 */
	public void clear() {
		index.clear();
		counts.clear();
	}

	/**
	 * Represents a search result with the file path and the number of matches.
	 */
	public static class SearchResult {
		/** The path where matches were found */
		private final String location;
		
		/** The total number of matches found */
		private final int matches;
		
		/**
		 * Creates a new search result.
		 *
		 * @param location the file path where matches were found
		 * @param matches the total number of matches found
		 */
		public SearchResult(String location, int matches) {
			this.location = location;
			this.matches = matches;
		}
		
		/**
		 * Gets the file path where matches were found.
		 *
		 * @return the file path
		 */
		public String getLocation() {
			return location;
		}
		
		/**
		 * Gets the total number of matches found.
		 *
		 * @return the number of matches
		 */
		public int getMatches() {
			return matches;
		}
	}
	
	/**
	 * Performs an exact search on the inverted index for a line of query words.
	 * For each location found, sums up the total number of matches across all query words.
	 * 
	 * @param line the line of query words to search for
	 * @return a map containing the search results, where each key is a file path and each value is the total matches
	 */
	public Map<String, Integer> exactSearch(String line) {
		// Create a map to store search results (location -> total matches)
		TreeMap<String, Integer> results = new TreeMap<>();
		
		// Process the query line to get sorted unique stems
		var stems = QueryProcessor.processLine(line);
		if (stems.isEmpty()) {
			return results;
		}
		
		// For each stem in the query
		for (String stem : stems) {
			// Skip if stem not in index
			if (!index.containsKey(stem)) {
				continue;
			}
			
			// For each location where this stem appears
			for (var entry : index.get(stem).entrySet()) {
				String location = entry.getKey();
				int count = entry.getValue().size(); // Number of times this stem appears in this location
				
				// Add or update the total matches for this location
				results.merge(location, count, Integer::sum);
			}
		}
		
		return results;
	}
	
	/**
	 * Performs exact searches for multiple query lines and returns all results.
	 * 
	 * @param queries the list of query lines to search for
	 * @return a list of maps, where each map contains the search results for one query line
	 */
	public List<Map<String, Integer>> exactSearchAll(List<String> queries) {
		List<Map<String, Integer>> allResults = new ArrayList<>();
		for (String query : queries) {
			allResults.add(exactSearch(query));
		}
		return allResults;
	}
}