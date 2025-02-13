package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

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
	private static void processFile(Path inputPath, Path outputPath) throws IOException {
		String fileContents = Files.readString(inputPath);
		System.out.println("\nInput file contents:");
		System.out.println(fileContents);

		String[] words = FileStemmer.parse(fileContents);
		int wordCount = words.length;
		System.out.println("\nWord count: " + wordCount);

		JsonWriter.writeObject(Map.of("counts", wordCount), outputPath);
		System.out.println("\nWord count written to: " + outputPath);
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
		System.out.println("Command-line arguments: " + Arrays.toString(args));

		ArgumentParser parser = new ArgumentParser(args);
		Path inputPath = parser.getPath("-text");
		Path outputPath = parser.getPath("-counts");

		try {
			processFile(inputPath, outputPath);
		}
		catch (IOException e) {
			System.err.println("Error processing files: " + e.getMessage());
		}

		long elapsedMs = Duration.between(start, Instant.now()).toMillis();
		double elapsedSec = (double) elapsedMs / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", elapsedSec);
	}

	/** Prevent instantiating this class of static methods. */
	private Driver() {}
}
