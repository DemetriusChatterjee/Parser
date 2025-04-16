package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Iterator;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using spaces.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author Demetrius Chatterjee
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
public class JsonWriter {
	
	/**
	 * Initializes a JsonWriter object. Private constructor to prevent instantiation
	 * of utility class.
	 */
	private JsonWriter() {
		// Do not instantiate.
	}
	
	/**
	 * Indents the writer by the specified number of times. Does nothing if the
	 * indentation level is 0 or less.
	 *
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(Writer writer, int indent) throws IOException {
		while (indent-- > 0) {
			writer.write("  ");
		}
	}

	/**
	 * Indents and then writes the String element.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write(element);
	}

	/**
	 * Indents and then writes the text element surrounded by {@code " "}
	 * quotation marks.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeQuote(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Writes a set value in JSON format.
	 *
	 * @param value the set value to write
	 * @param writer the writer to use
	 * @param indent the current indentation level
	 * @throws IOException if an IO error occurs
	 */
	private static void writeValue(TreeSet<Integer> value, Writer writer, int indent) throws IOException {
		if (value == null) {
			writer.write("null");
		} else {
			writeArray(value, writer, indent);
		}
	}

	/**
	 * Writes an integer value in JSON format.
	 *
	 * @param value the integer value to write
	 * @param writer the writer to use
	 * @param indent the current indentation level
	 * @throws IOException if an IO error occurs
	 */
	private static void writeValue(Integer value, Writer writer, int indent) throws IOException {
		if (value == null) {
			writer.write("null");
		} else {
			writer.write(value.toString());
		}
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndexObject(Map<String, TreeMap<String, TreeSet<Integer>>> elements, Writer writer, int indent) throws IOException {
		//TODO: Implement this method
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeCountsObject(Map<String, Integer> elements, Writer writer, int indent) throws IOException {
		writer.write('{');
		writer.write('\n');
		
		if (!elements.isEmpty()) {
			var iterator = elements.entrySet().iterator();
			
			// Write first key-value pair
			var entry = iterator.next();
			writeIndent(writer, indent + 1);
			writeQuote(entry.getKey(), writer, 0);
			writer.write(": ");
			writeValue(entry.getValue(), writer, indent + 1);
			
			// Write remaining pairs
			while (iterator.hasNext()) {
				writer.write(",\n");
				entry = iterator.next();
				writeIndent(writer, indent + 1);
				writeQuote(entry.getKey(), writer, 0);
				writer.write(": ");
				writeValue(entry.getValue(), writer, indent + 1);
			}
			
			writer.write('\n');
		}
		
		writeIndent(writer, indent);
		writer.write('}');
	}

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeArray(TreeSet<Integer> elements, Writer writer, int indent) throws IOException {
		writer.write('[');
		writer.write('\n');
		
		if (!elements.isEmpty()) {
			var iterator = elements.iterator();
			
			// Write first element (no preceding comma needed)
			writeIndent(writer, indent + 1);
			writer.write(iterator.next().toString());
			
			// Write remaining elements (preceded by commas)
			while (iterator.hasNext()) {
				writer.write(",\n");
				writeIndent(writer, indent + 1);
				writer.write(iterator.next().toString());
			}
			
			writer.write('\n');
		}
		
		writeIndent(writer, indent);
		writer.write(']');
	}

	/**
	 * Writes a Map data structure to a file in JSON format.
	 * 
	 * @param data the Map data to write
	 * @param path the file path to write to
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndexObject(Map<String, TreeMap<String, TreeSet<Integer>>> data, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeIndexObject(data, writer, 0);
		}
	}

	/**
	 * Writes a Map data structure to a file in JSON format.
	 * 
	 * @param data the Map data to write
	 * @param path the file path to write to
	 * @throws IOException if an IO error occurs
	 */
	public static void writeCountsObject(Map<String, Integer> data, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeCountsObject(data, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 */
	public static String writeIndexObject(Map<String, TreeMap<String, TreeSet<Integer>>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeIndexObject(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 */
	public static String writeCountsObject(Map<String, Integer> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeCountsObject(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Writes a single search result as a pretty JSON object.
	 * The JSON object will contain the count, score, and location of the search result.
	 *
	 * @param result the search result to write
	 * @param writer the writer to use
	 * @param indent the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	private static void writeSearchResult(
			InvertedIndex.SearchResult result,
			Writer writer, int indent) throws IOException {
		writer.write('{');
		writer.write('\n');
		
		// Write count
		writeIndent(writer, indent + 1);
		writer.write("\"count\": ");
		writer.write(String.valueOf(result.getCount()));
		writer.write(",\n");

		// Write score
		writeIndent(writer, indent + 1);
		writer.write("\"score\": ");
		writer.write(String.format("%.8f", result.getScore()));
		writer.write(",\n");
		
		// Write location
		writeIndent(writer, indent + 1);
		writer.write("\"where\": \"");
		writer.write(result.getWhere());
		writer.write("\"\n");
		
		writeIndent(writer, indent);
		writer.write('}');
	}
	
	/**
	 * Writes a list of search results as a pretty JSON array.
	 *
	 * @param results the list of search results to write
	 * @param writer the writer to use
	 * @param indent the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	private static void writeSearchResultArray(
			List<InvertedIndex.SearchResult> results,
			Writer writer, int indent) throws IOException {
		writer.write('[');
		writer.write('\n');
		
		if (!results.isEmpty()) {
			Iterator<InvertedIndex.SearchResult> iterator = results.iterator();
			
			// Write first result
			writeIndent(writer, indent + 1);
			writeSearchResult(iterator.next(), writer, indent + 1);
			
			// Write remaining results
			while (iterator.hasNext()) {
				writer.write(",\n");
				writeIndent(writer, indent + 1);
				writeSearchResult(iterator.next(), writer, indent + 1);
			}
			
			writer.write('\n');
		}
		
		writeIndent(writer, indent);
		writer.write(']');
	}
	
	/**
	 * Writes a collection of search results as a pretty JSON object.
	 * Each key is a query string and each value is an array of search results.
	 *
	 * @param results the map of query strings to search results
	 * @param writer the writer to use
	 * @param indent the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void writeSearchResults(
			Map<String, List<InvertedIndex.SearchResult>> results,
			Writer writer, int indent) throws IOException {
		writer.write('{');
		writer.write('\n');
		
		if (!results.isEmpty()) {
			Iterator<Map.Entry<String, List<InvertedIndex.SearchResult>>> iterator = results.entrySet().iterator();
			
			// Write first query and its results
			Map.Entry<String, List<InvertedIndex.SearchResult>> entry = iterator.next();
			writeIndent(writer, indent + 1);
			writeQuote(entry.getKey(), writer, 0);
			writer.write(": ");
			writeSearchResultArray(entry.getValue(), writer, indent + 1);
			
			// Write remaining queries and their results
			while (iterator.hasNext()) {
				writer.write(",\n");
				entry = iterator.next();
				writeIndent(writer, indent + 1);
				writeQuote(entry.getKey(), writer, 0);
				writer.write(": ");
				writeSearchResultArray(entry.getValue(), writer, indent + 1);
			}
			
			writer.write('\n');
		}
		
		writeIndent(writer, indent);
		writer.write('}');
	}
	
	/**
	 * Writes a map of search results as a pretty JSON object to file.
	 *
	 * @param results the map of query strings to search results
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 */
	public static void writeSearchResults(
			Map<String, List<InvertedIndex.SearchResult>> results,
			Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeSearchResults(results, writer, 0);
		}
	}
}
