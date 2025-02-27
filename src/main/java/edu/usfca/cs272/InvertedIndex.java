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
	 * Represents a search result with metadata for ranking.
	 */
	public static class SearchResult implements Comparable<SearchResult> {
		/** The path where matches were found */
		private final String where;
		
		/** The total number of matches found */
		private final int count;
		
		/** The score (count/totalWords) for ranking */
		private final double score;
		
		/**
		 * Creates a new search result.
		 *
		 * @param where the file path where matches were found
		 * @param count the total number of matches found
		 * @param totalWords the total number of words in the file
		 */
		public SearchResult(String where, int count, int totalWords) {
			this.where = where;
			this.count = count;
			this.score = (double) count / totalWords;
		}
		
		@Override
		public int compareTo(SearchResult other) {
			// First compare by score (descending)
			int comparison = Double.compare(other.score, this.score);
			if (comparison != 0) {
				return comparison;
			}
			
			// Then by count (descending)
			comparison = Integer.compare(other.count, this.count);
			if (comparison != 0) {
				return comparison;
			}
			
			// Finally by location (ascending, case-insensitive)
			return this.where.compareToIgnoreCase(other.where);
		}
		
		/**
		 * Gets the file path where matches were found.
		 *
		 * @return the file path
		 */
		public String getWhere() {
			return where;
		}
		
		/**
		 * Gets the total number of matches found.
		 *
		 * @return the number of matches
		 */
		public int getCount() {
			return count;
		}
		
		/**
		 * Gets the score used for ranking.
		 *
		 * @return the score
		 */
		public double getScore() {
			return score;
		}
	}
	
	/**
	 * Gets the cleaned and sorted query string from a list of stems.
	 * 
	 * @param stems the list of query stems
	 * @return the query string with stems joined by spaces
	 */
	private static String getQueryString(List<String> stems) {
		return String.join(" ", stems);
	}
	
	/**
	 * Performs an exact search on the inverted index for a line of query words.
	 * For each location found, creates a SearchResult with metadata for ranking.
	 * 
	 * @param line the line of query words to search for
	 * @return a map with the query string as key and a list of sorted search results as value
	 */
	public Map<String, List<SearchResult>> exactSearch(String line) {
		// Process the query line to get sorted unique stems
		var stems = QueryProcessor.processLine(line);
		if (stems.isEmpty()) {
			return new TreeMap<>();
		}
		
		// Create a map to store search results (location -> total matches)
		TreeMap<String, Integer> matches = new TreeMap<>();
		
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
				matches.merge(location, count, Integer::sum);
			}
		}
		
		// Convert matches to SearchResult objects with metadata
		List<SearchResult> results = new ArrayList<>();
		for (var entry : matches.entrySet()) {
			String location = entry.getKey();
			int matchCount = entry.getValue();
			int totalWords = counts.get(location);
			results.add(new SearchResult(location, matchCount, totalWords));
		}
		
		// Sort results by score, count, and location
		results.sort(null); // Uses natural ordering defined by compareTo
		
		// Create map with query string as key and sorted results as value
		TreeMap<String, List<SearchResult>> searchResults = new TreeMap<>();
		searchResults.put(getQueryString(stems), results);
		return searchResults;
	}
	
	/**
	 * Performs exact searches for multiple query lines and returns all results.
	 * 
	 * @param queries the list of query lines to search for
	 * @return a map where each key is a query string and each value is a list of sorted search results
	 */
	public Map<String, List<SearchResult>> exactSearchAll(List<String> queries) {
		TreeMap<String, List<SearchResult>> allResults = new TreeMap<>();
		for (String query : queries) {
			allResults.putAll(exactSearch(query));
		}
		return allResults;
	}
}