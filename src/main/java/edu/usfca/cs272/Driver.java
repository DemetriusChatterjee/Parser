package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

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
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program. Supported flags include:
	 *             "-text" for input file/directory path
	 *             "-counts" for word counts output path
	 *             "-index" for inverted index output path
	 */
	public static void main(String[] args) {
		Instant start = Instant.now();
		ArgumentParser parser = new ArgumentParser(args);
		InvertedIndex index = new InvertedIndex();
		
		// Process input path if provided
		if (parser.hasFlag("-text")) {
			Path inputPath = parser.getPath("-text");
			if (inputPath != null) {
				try {
					InvertedIndexBuilder builder = new InvertedIndexBuilder(index);
					builder.build(inputPath);
				}
				catch (IOException e) {
					System.err.println("Unable to index the files at path: " + inputPath);
				}
			}
		}
		
		// Write output files if flags are provided
		try {
			if (parser.hasFlag("-counts")) {
				Path countsPath = parser.getPath("-counts", Path.of("counts.json"));
				JsonWriter.writeObject(index.getCounts(), countsPath);
			}
			
			if (parser.hasFlag("-index")) {
				Path indexPath = parser.getPath("-index", Path.of("index.json"));
				JsonWriter.writeObject(index.getIndex(), indexPath);
			}
			
			Duration elapsed = Duration.between(start, Instant.now());
			System.out.printf("Elapsed: %.3f seconds%n", elapsed.toMillis() / 1000.0);
		}
		catch (IOException e) {
			System.err.println("Unable to write to output files: " + e.getMessage());
		}
	}

	/** Prevent instantiating this class of static methods. */
	private Driver() {}
}
