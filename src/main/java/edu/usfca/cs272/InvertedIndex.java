package edu.usfca.cs272;

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
	 * Performs an exact search on the inverted index for a line of query words.
	 * 
	 * @param line the line of query words to search for
	 * @return a map containing the search results, where each key is a file path and each value is a list of positions
	 */
	public Map<String, TreeSet<Integer>> exactSearch(String line) {
		// Create a map to store search results
		TreeMap<String, TreeSet<Integer>> results = new TreeMap<>();
		
		// Parse and stem the query line
		var stems = FileStemmer.listStems(line);
		if (stems.isEmpty()) {
			return results;
		}
		
		// Get the first stem's locations
		String firstStem = stems.get(0);
		if (!index.containsKey(firstStem)) {
			return results;
		}
		
		// Initialize results with first stem's locations
		for (var entry : index.get(firstStem).entrySet()) {
			String path = entry.getKey();
			TreeSet<Integer> positions = new TreeSet<>(entry.getValue());
			results.put(path, positions);
		}
		
		// For each remaining stem, keep only positions that form a consecutive sequence
		for (int i = 1; i < stems.size() && !results.isEmpty(); i++) {
			String stem = stems.get(i);
			if (!index.containsKey(stem)) {
				return new TreeMap<>();
			}
			
			// Remove paths that don't contain this stem
			results.keySet().removeIf(path -> !index.get(stem).containsKey(path));
			
			// For remaining paths, keep only valid consecutive positions
			for (var entry : results.entrySet()) {
				String path = entry.getKey();
				TreeSet<Integer> positions = entry.getValue();
				TreeSet<Integer> nextPositions = index.get(stem).get(path);
				
				// Keep only positions that are consecutive
				positions.removeIf(pos -> !nextPositions.contains(pos + i));
			}
			
			// Remove paths with no valid positions
			results.keySet().removeIf(path -> results.get(path).isEmpty());
		}
		
		return results;
	}
}