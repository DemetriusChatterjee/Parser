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
	 * Checks if a Map object has String keys and converts it to {@code Map<String, Object>}.
	 *
	 * @param value the object to check and convert
	 * @return the converted map if valid
	 * @throws IllegalArgumentException if the map has non-String keys or if value is not a Map
	 */
	private static Map<String, Object> asStringMap(Object value) {
		if (!(value instanceof Map<?, ?> map)) {
			throw new IllegalArgumentException("Value is not a Map");
		}
		
		Map<String, Object> result = new TreeMap<>();
		for (var entry : map.entrySet()) {
			if (!(entry.getKey() instanceof String)) {
				throw new IllegalArgumentException("Map values must have String keys");
			}
			result.put((String) entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * Writes a value in the appropriate JSON format.
	 *
	 * @param value the value to write
	 * @param writer the writer to use
	 * @param indent the current indentation level
	 * @throws IOException if an IO error occurs
	 */
	private static void writeValue(Object value, Writer writer, int indent) throws IOException {
		if (value instanceof String str) {
			writer.write('"');
			writer.write(str);
			writer.write('"');
		}
		else if (value instanceof Number) {
			writer.write(value.toString());
		}
		else if (value instanceof Map<?, ?>) {
			writeObject(asStringMap(value), writer, indent);
		}
		else if (value instanceof Collection<?>) {
			writeArray((Collection<?>) value, writer, indent);
		}
		else if (value == null) {
			writer.write("null");
		}
		else {
			// For any other type, write as quoted string using toString()
			writer.write('"');
			writer.write(value.toString());
			writer.write('"');
		}
	}
	
	/*
	 * TODO Avoid an instance of approach here... don't need to go too far in // TODO: for the whole file
	 * supporing all kinds of json writing
	 * 
	 * Go back to the homework approach.
	 */

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
	public static void writeObject(Map<String, ?> elements, Writer writer, int indent) throws IOException {
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
	public static void writeArray(Collection<?> elements, Writer writer, int indent) throws IOException {
		writer.write('[');
		writer.write('\n');
		
		if (!elements.isEmpty()) {
			var iterator = elements.iterator();
			
			// Write first element (no preceding comma needed)
			writeIndent(writer, indent + 1);
			writeValue(iterator.next(), writer, indent + 1);
			
			// Write remaining elements (preceded by commas)
			while (iterator.hasNext()) {
				writer.write(",\n");
				writeIndent(writer, indent + 1);
				writeValue(iterator.next(), writer, indent + 1);
			}
			
			writer.write('\n');
		}
		
		writeIndent(writer, indent);
		writer.write(']');
	}

	/**
	 * Generic method to write any JSON-compatible data structure to a file.
	 * 
	 * @param data the data to write
	 * @param path the file path to write to
	 * @throws IOException if an IO error occurs
	 */
	private static void writeJson(Object data, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			if (data instanceof Map<?, ?>) {
				writeObject(asStringMap(data), writer, 0);
			}
			else if (data instanceof Collection<?>) {
				writeArray((Collection<?>) data, writer, 0);
			}
		}
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 */
	public static void writeArray(Collection<Object> elements, Path path) throws IOException {
		writeJson(elements, path);
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 */
	public static String writeArray(Collection<Object> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArray(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 */
	public static void writeObject(Map<String, ?> elements, Path path) throws IOException {
		writeJson(elements, path);
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 */
	public static String writeObject(Map<String, ?> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObject(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}
	
	private static void writeSearchResult(
			InvertedIndex.SearchResult result,
			Writer writer, int indent) throws IOException {
		writer.write('{');
		writer.write('\n');
		
		// Write location
		writeIndent(writer, indent + 1);
		writer.write("\"location\": \"");
		writer.write(result.getLocation());
		writer.write("\",\n");
		
		// Write score
		writeIndent(writer, indent + 1);
		writer.write("\"score\": ");
		writer.write(String.valueOf(result.getScore()));
		writer.write("\n");
		
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
