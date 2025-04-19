package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map;

/**
 * A thread-safe implementation of the InvertedIndex class that allows concurrent
 * access to the index data structure. This class extends the base InvertedIndex
 * and adds synchronization mechanisms to ensure thread safety.
 *
 * @author Demetrius Chatterjee
 * @version Spring 2025
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {
    /** The lock used to synchronize access to the inverted index */
    private final MultiReaderLock lock;
    
    /** The number of worker threads to use for parallel processing */
    private final int threads;

    /**
     * Initializes a new thread-safe inverted index with the specified number of worker threads.
     *
     * @param threads the number of worker threads to use for parallel processing
     */
    public ThreadSafeInvertedIndex(int threads) {
        super();
        this.lock = new MultiReaderLock();
        this.threads = threads;
    }

    /**
     * Adds a word stem, its location, and position to the inverted index in a thread-safe manner.
     * Uses write lock to ensure exclusive access during modification.
     *
     * @param stem the word stem to add
     * @param location the location (file) where the word was found
     * @param position the position of the word in the file
     */
    @Override
    public void add(String stem, String location, int position) {
        try {
            lock.writeLock().lock();
            super.add(stem, location, position);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Performs an exact search on the inverted index in a thread-safe manner.
     * Uses read lock to allow concurrent reads.
     *
     * @param queries the set of query terms to search for
     * @return a list of search results matching the exact query terms
     */
    @Override
    public List<SearchResult> exactSearch(Set<String> queries) {
        try {
            lock.readLock().lock();
            return super.exactSearch(queries);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Performs a partial search on the inverted index in a thread-safe manner.
     * Uses read lock to allow concurrent reads.
     *
     * @param queries the set of query terms to search for
     * @return a list of search results matching the partial query terms
     */
    @Override
    public List<SearchResult> partialSearch(Set<String> queries) {
        try {
            lock.readLock().lock();
            return super.partialSearch(queries);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Processes search results from a query file in a thread-safe manner using multiple worker threads.
     * Each query is processed concurrently, and results are collected in a synchronized manner.
     *
     * @param path the path to the query file
     * @param usePartialSearch if true, use partial search; otherwise use exact search
     * @return an unmodifiable map of query strings to their corresponding search results
     * @throws IOException if there is an error reading the query file
     */
    public Map<String, List<SearchResult>> processThreadSafeSearchResults(
            Path path, boolean usePartialSearch) throws IOException {
        // Initialize query processor and work queue
        QueryProcessor queryProcessor = new QueryProcessor();
        WorkQueue queue = new WorkQueue(threads);
        
        // Process the query file to get individual queries
        List<TreeSet<String>> queries = queryProcessor.processQueryFile(path);
        
        // Process each query concurrently
        for (TreeSet<String> query : queries) {
            final String queryString = queryProcessor.getQueryString(query);
            
            queue.execute(() -> {
                List<SearchResult> results;
                // Choose search type based on parameter
                if (usePartialSearch) {
                    results = partialSearch(query);
                } else {
                    results = exactSearch(query);
                }
                
                // Add results through QueryProcessor's synchronized method
                queryProcessor.addResults(queryString, results);
            });
        }
        
        // Wait for all queries to complete
        queue.join();
        return queryProcessor.getAllResults();
    }
}