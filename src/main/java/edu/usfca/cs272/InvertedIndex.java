package edu.usfca.cs272;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
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
		JsonWriter.writeObject(index, path);
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
}