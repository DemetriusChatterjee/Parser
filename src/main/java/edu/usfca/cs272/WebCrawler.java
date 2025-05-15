package edu.usfca.cs272;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.*;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import opennlp.tools.stemmer.snowball.SnowballStemmer;


/**
 * A multithreaded web crawler that processes URIs to build an inverted index.
 * Uses a work queue for parallel processing of web pages and their links.
 *
 * @author Demetrius Chatterjee
 * @version Spring 2025
 */
public class WebCrawler {
    /** The inverted index to build */
    private final ThreadSafeInvertedIndex index;
    
    /** The work queue for parallel processing */
    private final WorkQueue queue;
    
    /** Set of URIs that have been processed to avoid duplicates */
    private final Set<URI> processed;

    /**
     * Initializes the web crawler with the given index and work queue.
     *
     * @param index the inverted index to build
     * @param queue the work queue for parallel processing
     */
    public WebCrawler(ThreadSafeInvertedIndex index, WorkQueue queue) {
        this.index = index;
        this.queue = queue;
        this.processed = new HashSet<>();
    }

    /**
     * Starts crawling from the given seed URI.
     *
     * @param seed the starting URI
     * @throws IOException if an IO error occurs
     */
    public void crawl(URI seed) throws IOException {
        // Start with the seed URI
        queue.execute(() -> processUri(seed));
        queue.finish();
    }

    /**
     * Processes a single URI by fetching its HTML content, cleaning it,
     * adding it to the index, and finding new links to process.
     *
     * @param uri the URI to process
     */
    private void processUri(URI uri) {
        // Skip if already processed
        synchronized (processed) {
            if (processed.contains(uri)) {
                return;
            }
            processed.add(uri);
        }
        
        try {
            String html;
            // Handle both local files and web URLs
            if (uri.getScheme().equals("file")) {
                html = HtmlFetcher.fetch(uri, 0); // No redirects for local files
            } else {
                // Use HTML Fetcher to get content with redirects
                html = HtmlFetcher.fetch(uri, 3); // Allow up to 3 redirects
            }
            
            if (html != null) {
                // Create a local index for this page
                InvertedIndex local = new InvertedIndex();
                
                // Find all valid HTTP(S) links
                Set<URI> links = LinkFinder.uniqueUris(uri, html);
                
                // Clean the HTML content
                String cleaned = HtmlCleaner.stripHtml(html);
                String[] words = cleaned.split("\\s+");
                SnowballStemmer stemmer = new SnowballStemmer(ENGLISH);

                // Process the cleaned text: split into words and add to local index
                int position = 1;
                for (String word : words) {
                    if (!word.isEmpty()) {
                        String stem = stemmer.stem(word).toString();
                        local.add(stem, uri.toString(), position++);
                    }
                }
                
                // Merge local index into main index
                index.mergeIndex(local);

                // Process new links
                for (URI link : links) {
                    queue.execute(() -> processUri(link));
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing URI: " + uri);
        }
    }
} 