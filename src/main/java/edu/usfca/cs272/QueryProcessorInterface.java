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
 * Interface defining common methods for query processing functionality.
 * Implemented by both QueryProcessor and ThreadSafeQueryProcessor.
 *
 * @author Demetrius Chatterjee
 * @version Spring 2025
 */
public interface QueryProcessorInterface {
    /**
     * Processes a single line of query text into a sorted list of unique stems.
     *
     * @param line the line of query text to process
     * @return a sorted TreeSet of unique stems from the processed line
     */
    default TreeSet<String> processLine(String line) {
        return FileStemmer.uniqueStems(line);
    }

    /**
     * Processes a single query line, stems the words, searches the index, and stores the results.
     *
     * @param line the query line to process
     * @return a list of search results from the inverted index
     */
    List<InvertedIndex.SearchResult> processQueryLine(String line);

    /**
     * Processes a query file and stores search results from the inverted index.
     *
     * @param path the path to the query file
     * @throws IOException if unable to read or process the query file
     */
    default void processQueryFile(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                processQueryLine(line);
            }
        }
    }

    /**
     * Returns the query string from the given stems.
     *
     * @param stems the stems to use
     * @return the query string
     */
    static String getQueryString(TreeSet<String> stems) {
        return String.join(" ", stems);
    }

    /**
     * Writes the search results to a JSON file.
     * 
     * @param path the path to write the JSON file to
     * @param usePartialResults whether to use partial search
     * @throws IOException if an IO error occurs
     */
    void toJson(Path path, boolean usePartialResults) throws IOException;

    /**
     * Returns an unmodifiable view of the search result keys.
     * 
     * @return an unmodifiable view of the search result keys
     */
    Set<String> getSearchResultKeys();

    /**
     * Returns an unmodifiable view of the search results for a given query string.
     * 
     * @param queryString the query string to get results for
     * @param usePartialSearch whether to use partial search results
     * @return an unmodifiable view of the search results, or null if no results exist
     */
    List<InvertedIndex.SearchResult> getSearchResult(String queryString, boolean usePartialSearch);
} 