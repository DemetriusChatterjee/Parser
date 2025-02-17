package edu.usfca.cs272;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
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
	private static void processFile(Path inputPath, Path outputPath) throws IOException {
		
		try{
			File directory = new File(inputPath.toString());
			TreeMap<String, Integer> counts = new TreeMap<>();

			if (directory.isDirectory()) {
				File[] contents = directory.listFiles();
				if (contents != null) {
					for (File file : contents) {
						if (file.isDirectory()) {
							processFile(file.toPath(), null); // Recursively process subdirectories
						}
						else if (file.isFile()) {
							var stems = FileStemmer.uniqueStems(file.toPath());
							counts.put(file.getAbsolutePath(), stems.size());
						}
					}
				}
			}
			else {
				var stems = FileStemmer.uniqueStems(inputPath);
				counts.put(directory.getAbsolutePath(), stems.size());
			}
			
			if (outputPath != null) {
				JsonWriter.writeObject(counts, outputPath);
			}
		}catch (IOException e) {
			System.err.println("Error processing files: " + e.getMessage());
		}
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
		Path inputPath = parser.getPath("text");
		
		// Get all output paths
		Path countsPath = parser.hasFlag("counts") ? 
			parser.getPath("counts", Path.of("counts.json")) : 
			null;
		Path indexPath = parser.getPath("index");
		Path resultsPath = parser.getPath("results");

		try {
			if (inputPath == null) {
				System.err.println("No input path provided with -text flag");
				return;
			}

			// Create parent directories for all output paths
			if (countsPath != null) {
				Files.createDirectories(countsPath.getParent());
			}
			if (indexPath != null) {
				Files.createDirectories(indexPath.getParent());
			}
			if (resultsPath != null) {
				Files.createDirectories(resultsPath.getParent());
			}

			processFile(inputPath, countsPath);
			// TODO: Add processing for index and results files

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
