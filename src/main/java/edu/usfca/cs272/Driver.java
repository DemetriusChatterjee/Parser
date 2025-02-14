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
        // Handle directory case
        if (Files.isDirectory(inputPath)) {
            // Find all .txt and .text files recursively
            var finder = Files.walk(inputPath)
                .filter(path -> Files.isRegularFile(path))
                .filter(path -> {
                    String name = path.toString().toLowerCase();
                    return name.endsWith(".txt") || name.endsWith(".text");
                });
                
            // Process each file and sum the counts
            int totalCount = 0;
            try (var files = finder) {
                for (Path file : (Iterable<Path>) files::iterator) {
                    String content = Files.readString(file);
                    String[] words = FileStemmer.parse(content);
                    totalCount += words.length;
                }
            }
            
            // Write total counts if output path is provided
            if (outputPath != null) {
                Map<String, Integer> counts = Map.of("counts", totalCount);
                JsonWriter.writeObject(counts, outputPath);
            }
        }
        // Handle single file case
        else {
            String fileContents = Files.readString(inputPath);
            String[] words = FileStemmer.parse(fileContents);
            
            // Write counts if output path is provided
            if (outputPath != null) {
                Map<String, Integer> counts = Map.of("counts", words.length);
                JsonWriter.writeObject(counts, outputPath);
            }
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
        
        // Use default path if -counts flag is present but no path provided
        Path outputPath = parser.hasFlag("counts") ? 
            parser.getPath("counts", Path.of("counts.json")) : 
            null;

        try {
            if (inputPath == null) {
                System.err.println("No input path provided with -text flag");
                return;
            }
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
