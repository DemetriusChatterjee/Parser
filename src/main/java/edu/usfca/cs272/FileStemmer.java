package edu.usfca.cs272;

import java.text.Normalizer;
import java.util.Collection;
import java.util.TreeSet;
import java.util.regex.Pattern;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

/**
 * Utility class for parsing, cleaning, and stemming text and text files into
 * collections of processed words.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
public class FileStemmer {
	/** Regular expression that matches any whitespace. **/
	// Source: ChatGPT prompting with the idea of being more inclusive to all languages
	private final Pattern SPLIT_REGEX = Pattern.compile("(?U)\\p{Space}+");

	/** Regular expression that matches non-alphabetic characters. **/
	// Source: ChatGPT prompting with the idea of being more inclusive to all languages
	private final Pattern CLEAN_REGEX = Pattern.compile("(?U)[^\\p{Alpha}\\p{Space}]+");

	/**
	 * Cleans the text by removing any non-alphabetic characters (e.g. non-letters
	 * like digits, punctuation, symbols, and diacritical marks like the umlaut) and
	 * converting the remaining characters to lowercase.
	 *
	 * @param text the text to clean
	 * @return cleaned text
	 */
	private String clean(String text) {
		String cleaned = Normalizer.normalize(text, Normalizer.Form.NFD);
		cleaned = CLEAN_REGEX.matcher(cleaned).replaceAll("");
		return cleaned.toLowerCase();
	}

	/**
	 * Splits the supplied text by whitespaces.
	 *
	 * @param text the text to split
	 * @return an array of {@link String} objects
	 */
	private String[] split(String text) {
		return text.isBlank() ? new String[0] : SPLIT_REGEX.split(text.strip());
	}

	/**
	 * Parses the text into an array of clean words.
	 *
	 * @param text the text to clean and split
	 * @return an array of {@link String} objects
	 *
	 * @see #clean(String)
	 * @see #parse(String)
	 */
	public String[] parse(String text) {
		return split(clean(text));
	}

	/**
	 * Parses the line into cleaned and stemmed words and adds them to the provided
	 * collection.
	 *
	 * @param line the line of words to clean, split, and stem
	 * @param stems the collection to add stems
	 *
	 * @see #parse(String)
	 * @see Stemmer#stem(CharSequence)
	 * @see Collection#add(Object)
	 */
	private void addStems(String line, Collection<String> stems) {
		// Parse the line into words
		String[] words = parse(line);

		// Stem each word and add to collection
		for (String word : words) {
			// Get the stemmed version of the word
			String stemmed = stemmer.stem(word).toString();
			
			// Add to collection
			stems.add(stemmed);
		}
	}

	/**
	 * Parses the line into a set of unique, sorted, cleaned, and stemmed words.
	 *
	 * @param line the line of words to parse and stem
	 * @return a sorted set of unique cleaned and stemmed words
	 *
	 * @see #parse(String)
	 * @see Stemmer#stem(CharSequence)
	 * @see #addStems(String, TreeSet)
	 */
	public TreeSet<String> uniqueStems(String line) {
		// Create TreeSet to store unique stems in sorted order
		TreeSet<String> stems = new TreeSet<>();
		
		// Use addStems to populate the set
		addStems(line, stems);
		
		// Return the set
		return stems;
	}
	
	/** The stemmer to use across method calls */
	private final Stemmer stemmer;
	
	/**
	 * Initializes a new FileStemmer with an English Snowball stemmer.
	 */
	public FileStemmer() {
		this.stemmer = new SnowballStemmer(ENGLISH);
	}
}
