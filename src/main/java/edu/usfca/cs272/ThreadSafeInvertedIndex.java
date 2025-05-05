package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A thread-safe implementation of InvertedIndex using MultiReaderLock for
 * concurrent access control. This allows multiple readers or a single writer
 * at any time, ensuring thread safety during index building and searching.
 *
 * @author Demetrius Chatterjee
 * @version Spring 2025
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {
    /** The lock manager to control read/write access. */
    private final MultiReaderLock lock;

    /** 
     * Initializes the thread-safe inverted index with a new MultiReaderLock
     * for managing concurrent access.
     */
    public ThreadSafeInvertedIndex() {
        super();
        this.lock = new MultiReaderLock();
    }

    /**
     * Thread-safe implementation of adding a word stem and its position to the index.
     * Uses write lock to ensure exclusive access during modification.
     *
     * @param stem the word stem to add
     * @param location the file path where the stem was found
     * @param position the position of the stem in the file (1-based)
     */
    @Override
    public void add(String stem, String location, int position) {
        lock.writeLock().lock();
        try {
            super.add(stem, location, position);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Thread-safe implementation of adding multiple stems from a list to the index.
     * Uses write lock to ensure exclusive access during modification.
     *
     * @param stems the list of word stems to add
     * @param location the file path where the stems were found
     */
    @Override
    public void addAll(List<String> stems, String location) {
        lock.writeLock().lock();
        try {
            super.addAll(stems, location);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Thread-safe implementation of clearing both the inverted index and word counts.
     * Uses write lock to ensure exclusive access during modification.
     */
    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            super.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Thread-safe implementation of checking if a stem exists in the index.
     * Uses read lock to allow concurrent reads.
     *
     * @param stem the stem to look for
     * @return true if the stem is in the index
     */
    @Override
    public boolean containsStem(String stem) {
        lock.readLock().lock();
        try {
            return super.containsStem(stem);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Thread-safe implementation of checking if a location has a word count.
     * Uses read lock to allow concurrent reads.
     *
     * @param location the location to look for
     * @return true if the location has a word count
     */
    @Override
    public boolean containsCount(String location) {
        lock.readLock().lock();
        try {
            return super.containsCount(location);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Thread-safe implementation of checking if a location exists for a stem.
     * Uses read lock to allow concurrent reads.
     *
     * @param stem the stem to look for
     * @param location the location to look for
     * @return true if the location is found for the stem
     */
    @Override
    public boolean containsLocation(String stem, String location) {
        lock.readLock().lock();
        try {
            return super.containsLocation(stem, location);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Thread-safe implementation of checking if a position exists for a stem and location.
     * Uses read lock to allow concurrent reads.
     *
     * @param stem the stem to look for
     * @param location the location to look for
     * @param position the position to look for
     * @return true if the position is found
     */
    @Override
    public boolean containsPosition(String stem, String location, int position) {
        lock.readLock().lock();
        try {
            return super.containsPosition(stem, location, position);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Thread-safe implementation of getting the number of locations with word counts.
     * Uses read lock to allow concurrent reads.
     *
     * @return the number of locations with counts
     */
    @Override
    public int numCounts() {
        lock.readLock().lock();
        try {
            return super.numCounts();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Thread-safe implementation of getting the number of unique stems in the index.
     * Uses read lock to allow concurrent reads.
     *
     * @return the number of stems
     */
    @Override
    public int numStems() {
        lock.readLock().lock();
        try {
            return super.numStems();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Thread-safe implementation of getting the number of locations for a stem.
     * Uses read lock to allow concurrent reads.
     *
     * @param stem the stem to look up
     * @return the number of locations or 0 if stem not found
     */
    @Override
    public int numLocations(String stem) {
        lock.readLock().lock();
        try {
            return super.numLocations(stem);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Thread-safe implementation of getting the number of positions for a stem and location.
     * Uses read lock to allow concurrent reads.
     *
     * @param stem the stem to look up
     * @param location the location to look up
     * @return the number of positions or 0 if not found
     */
    @Override
    public int numPositions(String stem, String location) {
        lock.readLock().lock();
        try {
            return super.numPositions(stem, location);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Thread-safe implementation of getting an unmodifiable view of the stems.
     * Uses read lock to allow concurrent reads.
     *
     * @return set of stems
     */
    @Override
    public Set<String> getStems() {
        lock.readLock().lock();
        try {
            return super.getStems();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Thread-safe implementation of getting an unmodifiable view of the locations for a stem.
     * Uses read lock to allow concurrent reads.
     *
     * @param stem the stem to look up
     * @return set of locations or empty set if stem not found
     */
    @Override
    public Set<String> getLocations(String stem) {
        lock.readLock().lock();
        try {
            return super.getLocations(stem);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Thread-safe implementation of getting an unmodifiable view of the positions for a stem and location.
     * Uses read lock to allow concurrent reads.
     *
     * @param stem the stem to look up
     * @param location the location to look up
     * @return set of positions or empty set if not found
     */
    @Override
    public Set<Integer> getPositions(String stem, String location) {
        lock.readLock().lock();
        try {
            return super.getPositions(stem, location);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Thread-safe implementation of getting an unmodifiable view of the word counts.
     * Uses read lock to allow concurrent reads.
     *
     * @return an unmodifiable view of the word counts
     */
    @Override
    public Map<String, Integer> getCounts() {
        lock.readLock().lock();
        try {
            return super.getCounts();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Thread-safe implementation of getting the word count for a location.
     * Uses read lock to allow concurrent reads.
     *
     * @param location the location to look up
     * @return the word count for the location, or 0 if not found
     */
    @Override
    public Integer getCount(String location) {
        lock.readLock().lock();
        try {
            return super.getCount(location);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Thread-safe implementation of searching the index.
     * Uses read lock to allow concurrent reads.
     *
     * @param queries the set of query words to search for
     * @param usePartialSearch whether to use partial search
     * @return a list of sorted search results
     */
    @Override
    public List<SearchResult> search(Set<String> queries, boolean usePartialSearch) {
        lock.readLock().lock();
        try {
            return super.search(queries, usePartialSearch);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Thread-safe implementation of performing an exact search.
     * Uses read lock to allow concurrent reads.
     *
     * @param queries the set of query words to search for
     * @return a list of sorted search results
     */
    @Override
    public List<SearchResult> exactSearch(Set<String> queries) {
        lock.readLock().lock();
        try {
            return super.exactSearch(queries);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Thread-safe implementation of performing a partial search.
     * Uses read lock to allow concurrent reads.
     *
     * @param queries the set of query words to search for
     * @return a list of sorted search results
     */
    @Override
    public List<SearchResult> partialSearch(Set<String> queries) {
        lock.readLock().lock();
        try {
            return super.partialSearch(queries);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Thread-safe implementation of getting the string representation of the index.
     * Uses read lock to allow concurrent reads.
     *
     * @return string representation of the index
     */
    @Override
    public String toString() {
        lock.readLock().lock();
        try {
            return super.toString();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Thread-safe implementation of writing the index to a JSON file.
     * Uses read lock to allow concurrent reads.
     *
     * @param path the path to write the JSON file to
     * @throws IOException if an IO error occurs
     */
    @Override
    public void toJson(Path path) throws IOException {
        lock.readLock().lock();
        try {
            super.toJson(path);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Thread-safe implementation of writing the word counts to a JSON file.
     * Uses read lock to allow concurrent reads.
     *
     * @param path the path to write the counts to
     * @throws IOException if an IO error occurs
     */
    @Override
    public void writeCounts(Path path) throws IOException {
        lock.readLock().lock();
        try {
            super.writeCounts(path);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Thread-safe implementation of writing the index to a JSON file.
     * Uses read lock to allow concurrent reads.
     *
     * @param path the path to write the index to
     * @throws IOException if an IO error occurs
     */
    @Override
    public void writeIndex(Path path) throws IOException {
        lock.readLock().lock();
        try {
            super.writeIndex(path);
        } finally {
            lock.readLock().unlock();
        }
    }
}