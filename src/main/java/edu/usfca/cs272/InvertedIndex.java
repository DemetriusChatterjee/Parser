package edu.usfca.cs272;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.ArrayList;
import java.io.IOException;
import java.nio.file.Path;

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
	 * Returns the number of locations that have word counts.
	 *
	 * @return the number of locations with counts
	 */
	public int numCounts() {
		return counts.size();
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
		var locations = index.get(stem);
		if (locations == null) {
			return Collections.emptySet();
		}
		var positions = locations.get(location);
		return positions == null ? Collections.emptySet() : Collections.unmodifiableSet(positions);
	}
	
	@Override
	public String toString() {
		return index.toString();
	}
	
	/**
	 * Writes the inverted index to a JSON file.
	 *
	 * @param path the path to write the JSON file to
	 * @throws IOException if an IO error occurs
	 */
	public void toJson(Path path) throws IOException {
		JsonWriter.writeIndexObject(index, path);
	}
	
	/**
	 * Gets an unmodifiable view of the word counts data structure.
	 *
	 * @return an unmodifiable view of the word counts
	 */
	public Map<String, Integer> getCounts() {
		return Collections.unmodifiableMap(counts);
	}

	/**
	 * Clears both the inverted index and word counts.
	 */
	public void clear() {
		index.clear();
		counts.clear();
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
	 * Performs a search on the inverted index for a line of query words.
	 * 
	 * @param queries the set of query words to search for
	 * @param usePartialSearch whether to use partial search
	 * @return a list of sorted search results
	 */
	public List<SearchResult> search(Set<String> queries, boolean usePartialSearch) {
		return usePartialSearch ? partialSearch(queries) : exactSearch(queries);
	}
	
	/**
	 * Performs an exact search on the inverted index for a line of query words.
	 * For each location found, creates a SearchResult with metadata for ranking.
	 * 
	 * @param queries the set of query words to search for
	 * @return a list of sorted search results
	 */
	public List<SearchResult> exactSearch(Set<String> queries) {
		// Create a map to store search results (location -> SearchResult)
		HashMap<String, SearchResult> matches = new HashMap<>();
		
		// For each stem in the query
		for (String query : queries) {
			// Skip if stem not in index
			if (!index.containsKey(query)) {
				continue;
			}
			
			TreeMap<String, TreeSet<Integer>> locations = index.get(query);
			// For each location where this stem appears
			for (var entry : locations.entrySet()) {
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
		
		// Create a map to store search results (location -> SearchResult)
		HashMap<String, SearchResult> matches = new HashMap<>();
		
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
			}
		}
		List<SearchResult> results = new ArrayList<>(matches.values());
		// Sort results by score, count, and location
		results.sort(null); // Uses natural ordering defined by compareTo
		return results;
	}
}