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
	private final Map<String, TreeMap<String, TreeSet<Integer>>> index; // TODO TreeMap for the first type
	
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
		
		/* TODO 
		var locations = index.get(stem);
		
		if (locations == null) {
			locations = new TreeMap<>();
			index.put(stem, locations);
		}
		
		var positions = locations.get(location);
		
		...
		*/
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
			for (int i = 0; i < stems.size(); i++) { // TODO for-each + a counter variable
				add(stems.get(i), location, i + 1);
			}
		}
	}
	
	/*
	 * TODO Start think about other generally useful methods and view methods
	 * that don't break encapsulation
	 * 
	 * toString
	 * 
	 * has or contains methods
	 * num or size methods
	 * get or view methods
	 * 
	 * Keep in mind the homework/lecture examples are fewer levels of nesting
	 * so you need more methods for this class
	 * 
	 * FileIndex, PrefixMap, WordGroup
	 */
	
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
}