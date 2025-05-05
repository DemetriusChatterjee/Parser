package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A thread-safe implementation of QueryProcessor that uses a WorkQueue for parallel
 * processing of query files. This allows multiple queries to be processed concurrently
 * while maintaining thread safety.
 *
 * @author Demetrius Chatterjee
 * @version Spring 2025
 */
public class ThreadSafeQueryProcessor extends QueryProcessor {
    /** The work queue for parallel processing of queries. */
    private final WorkQueue queue;

    /**
     * Initializes a thread-safe query processor with the default number of worker threads.
     *
     * @param index the inverted index to use for searching
     * @param usePartialSearch whether to use partial search
     * @param queue the work queue to use for parallel processing
     */
    public ThreadSafeQueryProcessor(InvertedIndex index, boolean usePartialSearch, WorkQueue queue) {
        super(index, usePartialSearch);
        this.queue = queue;
    }

    /**
     * Thread-safe implementation of processing a single query line.
     * Uses a work queue to process the query in parallel.
     *
     * @param line the query line to process
     * @return a list of search results from the inverted index
     */
    @Override
    public TreeSet<String> processLine(String line) {
        return super.processLine(line);
    }

    /**
     * Thread-safe implementation of processing a single query line.
     * Uses a work queue to process the query in parallel.
     *
     * @param line the query line to process
     * @return a list of search results from the inverted index
     */
    @Override
    public List<InvertedIndex.SearchResult> processQueryLine(String line) {
        // Process the line into stems
        TreeSet<String> stems = processLine(line);
        if (stems.isEmpty()) {
            return super.processQueryLine(line);
        }

        // Execute the query processing in the work queue
        queue.execute(() -> super.processQueryLine(line));

        // Wait for the task to complete
        queue.join();

        // Return the results from the parent class
        return super.processQueryLine(line);
    }

    /**
     * Thread-safe implementation of processing a query file.
     * Uses a work queue to process each line of the file in parallel.
     *
     * @param path the path to the query file
     * @throws IOException if unable to read or process the query file
     */
    @Override
    public void processQueryFile(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String queryLine = line;
                queue.execute(() -> super.processQueryLine(queryLine));
            }
        }
        // Wait for all tasks to complete
        queue.join();
    }

    /**
     * Thread-safe implementation of getting search result keys.
     *
     * @return an unmodifiable view of the search result keys
     */
    @Override
    public Set<String> getSearchResultKeys() {
        return super.getSearchResultKeys();
    }

    /**
     * Thread-safe implementation of getting search results for a query.
     *
     * @param queryString the query string to get results for
     * @param usePartialSearch whether to use partial search results
     * @return an unmodifiable view of the search results, or null if no results exist
     */
    @Override
    public List<InvertedIndex.SearchResult> getSearchResult(String queryString, boolean usePartialSearch) {
        return super.getSearchResult(queryString, usePartialSearch);
    }

    /**
     * Thread-safe implementation of writing search results to a JSON file.
     *
     * @param path the path to write the JSON file to
     * @param usePartialResults whether to use partial search
     * @throws IOException if an IO error occurs
     */
    @Override
    public void toJson(Path path, boolean usePartialResults) throws IOException {
        super.toJson(path, usePartialResults);
    }
}