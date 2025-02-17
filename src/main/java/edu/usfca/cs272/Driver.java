package edu.usfca.cs272;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.TreeMap;
/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Demetrius Chatterjee
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2025
 */
public class Driver {
	/**
	 * Processes the input file and writes the word count to the output file.
	 * 
	 * @param inputPath the path to read from
	 * @param outputPath the path to write to
	 * @throws IOException if an IO error occurs
	 */
	private static TreeMap<String, Integer> processFile(Path inputPath, Path outputPath) throws IOException {
		TreeMap<String, Integer> counts = new TreeMap<>();
		File startDir = new File(inputPath.toString());
		
		// If it's a single file and it's .md, process it directly
		if (startDir.isFile() && startDir.getName().toLowerCase().endsWith(".md")) {
			var stems = FileStemmer.listStems(startDir.toPath());
			if (stems.size() > 0) {
				counts.put(startDir.toString(), stems.size());
			}
			if (outputPath != null) {
				JsonWriter.writeObject(counts, outputPath);
			}
			return counts;
		}
		
		// Stack to keep track of directories to process
		java.util.ArrayDeque<File> stack = new java.util.ArrayDeque<>();
		stack.push(startDir);
		
		// Process all directories and files
		while (!stack.isEmpty()) {
			File current = stack.pop();
			
			if (current.isDirectory()) {
				File[] contents = current.listFiles();
				if (contents != null) {
					for (File file : contents) {
						stack.push(file);
					}
				}
			} else if (current.isFile()) {
				String fileName = current.getName().toLowerCase();
				if (fileName.endsWith(".txt") || fileName.endsWith(".text")) {
					var stems = FileStemmer.listStems(current.toPath());
					if (stems.size() > 0) {
						counts.put(current.toString(), stems.size());
					}
				}
			}
		}
		
		// Write results at the end
		if (outputPath != null) {
			JsonWriter.writeObject(counts, outputPath);
		}
		
		return counts;
	}

	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {
		Instant start = Instant.now();
		try {
			ArgumentParser parser = new ArgumentParser(args);
			parser.parse(args);
			Path inputPath = parser.getPath("-text");
			Path outputPath = null;
			if (parser.hasFlag("-counts")) {
				outputPath = parser.getPath("-counts", Path.of("counts.json"));
				// Create an empty TreeMap and write it if only -counts is provided
				if (inputPath == null) {
					JsonWriter.writeObject(new TreeMap<String, Integer>(), outputPath);
				}
			}
			
			// Only process if we have an input path
			if (inputPath != null) {
				processFile(inputPath, outputPath);
			}
			System.out.println(parser);
		} catch (IOException e) {
			System.err.println("Error processing files: " + e.getMessage());
			return;
		}

		long elapsedMs = Duration.between(start, Instant.now()).toMillis();
		double elapsedSec = (double) elapsedMs / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", elapsedSec);
	}

	/** Prevent instantiating this class of static methods. */
	private Driver() {}
}
