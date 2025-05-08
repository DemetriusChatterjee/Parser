package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A thread-safe implementation of QueryProcessor that uses a WorkQueue for parallel
 * processing of query files. This allows multiple queries to be processed concurrently
 * while maintaining thread safety.
 *
 * @author Demetrius Chatterjee
 * @version Spring 2025
 */
public class ThreadSafeQueryProcessor {
    /** The map to store exact search results for each query. */
    private final TreeMap<String, List<InvertedIndex.SearchResult>> allResultsExact;
    
    /** The map to store partial search results for each query. */
    private final TreeMap<String, List<InvertedIndex.SearchResult>> allResultsPartial;

    /** The inverted index to store the results of the search to. */
    private final ThreadSafeInvertedIndex index;

    /** Whether to use partial search. */
    private final boolean usePartialSearch;

    /** The work queue for parallel processing of queries. */
    private final WorkQueue queue;

    /**
     * Initializes a thread-safe query processor with the default number of worker threads.
     *
     * @param index the inverted index to use for searching
     * @param usePartialSearch whether to use partial search
     * @param queue the work queue to use for parallel processing
     */
    public ThreadSafeQueryProcessor(ThreadSafeInvertedIndex index, boolean usePartialSearch, WorkQueue queue) {
        this.allResultsExact = new TreeMap<>();
        this.allResultsPartial = new TreeMap<>();
        this.index = index;
        this.usePartialSearch = usePartialSearch;
        this.queue = queue;
    }

    /**
     * Processes a single line of query text into a sorted list of unique stems.
     *
     * @param line the line of query text to process
     * @return a sorted TreeSet of unique stems from the processed line
     */
    public TreeSet<String> processLine(final String line) {
        return FileStemmer.uniqueStems(line);
    }

    /**
     * Thread-safe implementation of processing a single query line.
     * Uses a work queue to process the query in parallel.
     *
     * @param line the query line to process
     * @return a list of search results from the inverted index
     */
    public List<InvertedIndex.SearchResult> processQueryLine(String line) { // TODO Fix indentation
        // Process the line into stems
        TreeSet<String> stems = processLine(line);
        if (stems.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Get the query string
        String queryString = getQueryString(stems);
        
        // Get the current results
        var currentResults = getResults(usePartialSearch);
        synchronized (currentResults) {
            // Check if we already have results for this query
            if (currentResults.containsKey(queryString)) {
                return currentResults.get(queryString);
            }
        }   
            // Search the index
            List<InvertedIndex.SearchResult> results = index.search(stems, usePartialSearch);
        synchronized (currentResults) {
            // Store the results
            currentResults.put(queryString, results);
        }
            return results;
    }

    /**
     * Thread-safe implementation of processing a query file.
     * Uses a work queue to process each line of the file in parallel.
     *
     * @param path the path to the query file
     * @throws IOException if unable to read or process the query file
     */
    public void processQueryFile(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String queryLine = line;
                queue.execute(() -> processQueryLine(queryLine));
            }
        }
        // Wait for all tasks to complete
        queue.finish();
    }

    /**
     * Returns the query string from the given stems.
     *
     * @param stems the stems to use
     * @return the query string
     */
    public static String getQueryString(TreeSet<String> stems) {
        return String.join(" ", stems);
    }

    /**
     * Thread-safe implementation of writing search results to a JSON file.
     *
     * @param path the path to write the JSON file to
     * @param usePartialResults whether to use partial search
     * @throws IOException if an IO error occurs
     */
    public void toJson(Path path, boolean usePartialResults) throws IOException {
        var results = getResults(usePartialResults);
        synchronized (results) {
            JsonWriter.writeSearchResults(results, path);
        }
    }

    /**
     * Returns the results for the given usePartialResults.
     * 
     * @param usePartialResults whether to use partial search
     * @return the results for the given usePartialResults
     */
    private TreeMap<String, List<InvertedIndex.SearchResult>> getResults(boolean usePartialResults) {
        return usePartialResults ? allResultsPartial : allResultsExact;
    }

    /**
     * Returns a string representation of the QueryProcessor object.
     *
     * @return a string representation of the QueryProcessor object
     */
    @Override
    public String toString() {
        var results = getResults(usePartialSearch);
        synchronized (results) {
            return String.format("ThreadSafeQueryProcessor[exact=%d, partial=%d]", 
                allResultsExact.toString(), allResultsPartial.toString());
        }
    }

    /**
     * Returns an unmodifiable view of the search result keys.
     * 
     * @return an unmodifiable view of the search result keys
     */
    public Set<String> getSearchResultKeys() {
        var results = getResults(usePartialSearch);
        synchronized (results) {
            return Collections.unmodifiableSet(results.keySet());
        }
    }

    /**
     * Returns an unmodifiable view of the search results for a given query string.
     * 
     * @param queryString the query string to get results for
     * @param usePartialSearch whether to use partial search results
     * @return an unmodifiable view of the search results, or null if no results exist
     */
    public List<InvertedIndex.SearchResult> getSearchResult(String queryString, boolean usePartialSearch) {
        // Process the query string to ensure consistent format
        TreeSet<String> stems = processLine(queryString);
        String processedQuery = getQueryString(stems);
        
        var results = getResults(usePartialSearch);
        synchronized (results) {
            List<InvertedIndex.SearchResult> searchResults = results.get(processedQuery);
            return searchResults != null ? Collections.unmodifiableList(searchResults) : null;
        }
    }
}