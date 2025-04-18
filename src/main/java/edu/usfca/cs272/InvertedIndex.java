package edu.usfca.cs272;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.ArrayList;

/**
 * Data structure to store an inverted index and word counts from text files. 
 * See the README for details.
 *
 * @author Demetrius Chatterjee
 * @version Spring 2025
 */
public class InvertedIndex {
	/** The inverted index to store word locations */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;
	
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
		TreeMap<String, TreeSet<Integer>> locations = index.get(stem);
		
		if (locations == null) {
			locations = new TreeMap<>();
			index.put(stem, locations);
		}
		
		TreeSet<Integer> positions = locations.get(location);
		
		if (positions == null) {
			positions = new TreeSet<>();
			locations.put(location, positions);
		}
		
		positions.add(position);
		
		// Update counts map
		counts.put(location, counts.getOrDefault(location, 0) + 1);
	}
	
	/**
	 * Gets an unmodifiable view of the word counts data structure.
	 *
	 * @return an unmodifiable view of the word counts
	 */
	public TreeMap<String, Integer> getCounts() {
		return new TreeMap<>(counts);
	}

	/**
	 * Gets an unmodifiable view of the inverted index data structure.
	 *
	 * @return an unmodifiable view of the inverted index
	 */
	public Map<String, TreeMap<String, TreeSet<Integer>>> getIndex() {
		return Collections.unmodifiableMap(index);
	}
	
	/**
	 * Gets the word count for a specific location.
	 *
	 * @param location the location to look up
	 * @return the word count for the location, or 0 if not found
	 */
	public Integer getCount(String location) {
		return counts.getOrDefault(location, 0);
	}
	
	/**
	 * Represents a search result with metadata for ranking.
	 */
	public class SearchResult implements Comparable<SearchResult> {
		/** The path where matches were found */
		private final String where;
		
		/** The total number of matches found */
		private int count;
		
		/** The score (count/totalWords) for ranking */
		private double score;
		
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
			this.score = (double) count / counts.get(where);
		}
		
		/**
		 * Updates the count and score for this search result.
		 * 
		 * @param count the new count value
		 */
		private void updateCount(int count) {
			if (this.count != count) {
				this.count = count;
				this.score = (double) count / counts.get(where);
			}
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
	 * Performs an exact search on the inverted index for a line of query words.
	 * For each location found, creates a SearchResult with metadata for ranking.
	 * 
	 * @param queries the set of query words to search for
	 * @return a list of sorted search results
	 */
	public List<SearchResult> exactSearch(Set<String> queries) {
		if (queries.isEmpty()) {
			return new ArrayList<>();
		}
		
		// Create a map to store search results (location -> SearchResult)
		TreeMap<String, SearchResult> matches = new TreeMap<>();
		
		// For each stem in the query
		for (String query : queries) {
			// Skip if stem not in index
			if (!index.containsKey(query)) {
				continue;
			}
			
			// For each location where this stem appears
			for (var entry : index.get(query).entrySet()) {
				String location = entry.getKey();
				int count = entry.getValue().size(); // Number of times this stem appears in this location
				
				// Get or create SearchResult for this location
				SearchResult result = matches.get(location);
				if (result == null) {
					int totalWords = counts.get(location);
					result = new SearchResult(location, count, totalWords);
					matches.put(location, result);
				} else {
					// Update existing result with additional matches
					result.updateCount(result.getCount() + count);
				}
			}
		}
		
		// Convert matches to sorted list
		List<SearchResult> results = new ArrayList<>(matches.values());
		results.sort(null); // Uses natural ordering defined by compareTo
		return results;
	}

	/**
	 * Performs a partial search on the inverted index for a line of query words.
	 * For each location found, creates a SearchResult with metadata for ranking.
	 * Partial search matches any word that starts with the query word.
	 * 
	 * @param queries the set of query words to search for
	 * @return a list of sorted search results
	 */
	public List<SearchResult> partialSearch(Set<String> queries) {
		if (queries.isEmpty()) {
			return new ArrayList<>();
		}
		
		// Create a map to store search results (location -> total matches)
		TreeMap<String, Integer> matches = new TreeMap<>();
		
		// For each stem in the query
		for (String query : queries) {
			// For each word in the index that starts with the stem
			for (var entry : index.entrySet()) {
				String word = entry.getKey();
				if (word.startsWith(query)) {
					// For each location where this word appears
					for (var locationEntry : entry.getValue().entrySet()) {
						String location = locationEntry.getKey();
						int count = locationEntry.getValue().size(); // Number of times this word appears in this location
						
						// Add or update the total matches for this location
						int current = matches.getOrDefault(location, 0);
						matches.put(location, current + count);
					}
				}
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
		return results;
	}
}