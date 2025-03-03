package edu.usfca.cs272;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
	 * Adds a word stem and its position to the index for a specific file location. g
	 *
	 * @param stem the word stem to add
	 * @param location the file path where the stem was found
	 * @param position the position of the stem in the file (1-based)
	 */
	public void add(String stem, String location, int position) {
		var locations = index.get(stem);
		
		if (locations == null) {
			locations = new TreeMap<>();
			index.put(stem, locations);
		}
		
		var positions = locations.get(location);
		
		if (positions == null) {
			positions = new TreeSet<>();
			locations.put(location, positions);
		}
		
		positions.add(position);
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
			int position = 1;
			for (String stem : stems) {
				add(stem, location, position++);
			}
		}
	}
	
	/**
	 * Returns whether the index contains the given stem.
	 *
	 * @param stem the stem to look for
	 * @return true if the stem is in the index
	 */
	public boolean containsStem(String stem) {
		return index.containsKey(stem);
	}
	
	// TODO containsCount(String location) --> test your counts map instead
	
	/**
	 * Returns whether the index contains the given stem and location.
	 *
	 * @param stem the stem to look for
	 * @param location the location to look for
	 * @return true if the location is found for the stem
	 */
	public boolean containsLocation(String stem, String location) {
		// TODO stem being access twice... do the get first always
		return index.containsKey(stem) && index.get(stem).containsKey(location);
	}
	
	/**
	 * Returns whether the index contains the given position for a stem and location.
	 *
	 * @param stem the stem to look for
	 * @param location the location to look for
	 * @param position the position to look for
	 * @return true if the position is found
	 */
	public boolean containsPosition(String stem, String location, int position) {
		if (!containsLocation(stem, location)) {
			return false;
		}
		return index.get(stem).get(location).contains(position);
	}
	
	// TODO numCounts() 
	
	/**
	 * Returns the number of unique stems in the index.
	 *
	 * @return the number of stems
	 */
	public int numStems() {
		return index.size();
	}
	
	/**
	 * Returns the number of locations for a given stem.
	 *
	 * @param stem the stem to look up
	 * @return the number of locations or 0 if stem not found
	 */
	public int numLocations(String stem) {
		return index.containsKey(stem) ? index.get(stem).size() : 0;
	}
	
	/**
	 * Returns the number of positions for a stem in a location.
	 *
	 * @param stem the stem to look up
	 * @param location the location to look up
	 * @return the number of positions or 0 if not found
	 */
	public int numPositions(String stem, String location) {
		if (!containsLocation(stem, location)) {
			return 0;
		}
		return index.get(stem).get(location).size();
	}
	
	/**
	 * Returns an unmodifiable view of the stems in the index.
	 *
	 * @return set of stems
	 */
	public Set<String> getStems() {
		return Collections.unmodifiableSet(index.keySet());
	}
	
	/**
	 * Returns an unmodifiable view of the locations for a stem.
	 *
	 * @param stem the stem to look up
	 * @return set of locations or empty set if stem not found
	 */
	public Set<String> getLocations(String stem) {
		if (!index.containsKey(stem)) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(index.get(stem).keySet());
	}
	
	/**
	 * Returns an unmodifiable view of the positions for a stem and location.
	 *
	 * @param stem the stem to look up
	 * @param location the location to look up
	 * @return set of positions or empty set if not found
	 */
	public Set<Integer> getPositions(String stem, String location) {
		if (!containsLocation(stem, location)) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(index.get(stem).get(location));
	}
	
	@Override
	public String toString() {
		return index.toString();
	}
	
	/* TODO 
	public void toJson(Path path) throws IOException {
		call your JsonWriter method here instead
	}
	
	This will change Driver and JsonWriter
	*/
	
	/**
	 * Gets the inverted index data structure.
	 *
	 * @return the inverted index
	 */
	public Map<String, TreeMap<String, TreeSet<Integer>>> getIndex() { // TODO Remove
		return index;
	}
	
	/**
	 * Gets the word counts data structure.
	 *
	 * @return the word counts
	 */
	public TreeMap<String, Integer> getCounts() { // TODO Breaking encapsulation, fix
		return counts;
	}
	
	/**
	 * Clears both the inverted index and word counts.
	 */
	public void clear() {
		index.clear();
		counts.clear();
	}

	// TODO Remove project 2 stuff, make sure you can explain why you make design choices like nesting here
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