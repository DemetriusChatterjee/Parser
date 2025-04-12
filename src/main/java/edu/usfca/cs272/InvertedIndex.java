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
		
		// Update counts map
		counts.put(location, counts.getOrDefault(location, 0) + 1);
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
	
	/**
	 * Returns whether the index contains a word count for the given location.
	 *
	 * @param location the location to look for
	 * @return true if the location has a word count
	 */
	public boolean containsCount(String location) {
		return counts.containsKey(location);
	}
	
	/**
	 * Returns whether the index contains the given stem and location.
	 *
	 * @param stem the stem to look for
	 * @param location the location to look for
	 * @return true if the location is found for the stem
	 */
	public boolean containsLocation(String stem, String location) {
		var locations = index.get(stem);
		return locations != null && locations.containsKey(location);
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
		var locations = index.get(stem);
		if (locations == null) {
			return false;
		}
		var positions = locations.get(location);
		return positions != null && positions.contains(position);
	}
	
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
		var locations = index.get(stem);
		return locations == null ? 0 : locations.size();
	}
	
	/**
	 * Returns the number of positions for a stem in a location.
	 *
	 * @param stem the stem to look up
	 * @param location the location to look up
	 * @return the number of positions or 0 if not found
	 */
	public int numPositions(String stem, String location) {
		var locations = index.get(stem);
		if (locations == null) {
			return 0;
		}
		var positions = locations.get(location);
		return positions == null ? 0 : positions.size();
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
		var locations = index.get(stem);
		if (locations == null) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(locations.keySet());
	}
	
	/**
	 * Returns an unmodifiable view of the positions for a stem and location.
	 *
	 * @param stem the stem to look up
	 * @param location the location to look up
	 * @return set of positions or empty set if not found
	 */
	public Set<Integer> getPositions(String stem, String location) {
		var locations = index.get(stem);
		if (locations == null) {
			return Collections.emptySet();
		}
		var positions = locations.get(location);
		return positions == null ? Collections.emptySet() : Collections.unmodifiableSet(positions);
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
	 * Clears both the inverted index and word counts.
	 */
	public void clear() {
		index.clear();
		counts.clear();
	}

	/**
	 * Represents a search result with metadata for ranking.
	 */
	// TODO Make this a non-static inner class
	public static class SearchResult implements Comparable<SearchResult> {
		/** The path where matches were found */
		private final String where;
		
		/** The total number of matches found */
		private final int count; // TODO Make non-final
		
		/** The score (count/totalWords) for ranking */
		private final double score; // TODO Make non-final
		
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
			// TODO this.score = (double) count / counts.get(where);
		}
		
		/*
		 * TODO Create a method that updates the count and when the 
		 * count changes, updates the score...
		 */
		
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
	
	/* TODO 
	public List<SearchResult> search(Set<String> queries, boolean partial) {
		return partial ? etc.
	}
	*/
	
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
		
		// Create a map to store search results (location -> total matches)
		TreeMap<String, Integer> matches = new TreeMap<>();
		// TODO TreeMap<String, SearchResult> matches = new TreeMap<>();
		
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
				
				// Add or update the total matches for this location
				int current = matches.getOrDefault(location, 0);
				matches.put(location, current + count);
				
				/*
				 * TODO 
				 * if a search result is there...
				 *     matches.get(location).addCount(...)
				 */
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
	
	// TODO Remove exactSearchAll
	/**
	 * Performs exact searches for multiple query lines and returns all results.
	 * 
	 * @param queries the list of query lines to search for
	 * @return a map where each key is a query string and each value is a list of sorted search results
	 */
	public Map<String, List<SearchResult>> exactSearchAll(List<String> queries) {
		TreeMap<String, List<SearchResult>> allResults = new TreeMap<>();
		List<Set<String>> processedQueries = QueryProcessor.processQueries(queries);
		for (Set<String> processedQuery : processedQueries) {
			allResults.put(String.join(" ", processedQuery), exactSearch(processedQuery));
		}
		return allResults;
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
	
	// TODO Remove partialSearchAll
	/**
	 * Performs partial searches for multiple query lines and returns all results.
	 * 
	 * @param queries the list of query lines to search for
	 * @return a map where each key is a query string and each value is a list of sorted search results
	 */
	public Map<String, List<SearchResult>> partialSearchAll(List<String> queries) {
		TreeMap<String, List<SearchResult>> allResults = new TreeMap<>();
		for (String query : queries) {
			Set<String> processedQuery = new TreeSet<>(QueryProcessor.processLine(query));
			allResults.put(String.join(" ", processedQuery), partialSearch(processedQuery));
		}
		return allResults;
	}
}