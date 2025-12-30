package spl.lae;
import java.io.IOException;

import parser.*;

public class Main {
    public static void main(String[] args) throws IOException {
      // TODO: main
        if (args.length != 3) {
            System.err.println("Usage: not aligned with the requset - <number_of_threads> <input_file> <output_file>");
            System.exit(1);
        }

        int numThreads;
        String inputFilePath = args[1];
        String outputFilePath = args[2];
        try {
            numThreads = Integer.parseInt(args[0]);
            if (numThreads <= 0) {
                System.err.println("Error: Number of threads must be positive.");
                System.exit(1);
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid number of threads. Must be an integer.");
            System.exit(1);
            return;
        }

        LinearAlgebraEngine engine = new LinearAlgebraEngine(numThreads);
        InputParser parser = new InputParser();
        ComputationNode computationRoot;
        
        try {
            computationRoot = parser.parse(inputFilePath);
        } catch (Exception e) {
            try {
                OutputWriter.write("Error parsing input file: " + e.getMessage(), outputFilePath);
            } catch (IOException ioException) {
                System.err.println("Error writing error message to output file: " + ioException.getMessage());
            }
            System.err.println("Error parsing input file: " + e.getMessage());
            System.exit(1);
            return;
        }

        ComputationNode result;
        try {
            result = engine.run(computationRoot);
        } catch (Exception e) {
            try {
                OutputWriter.write("Illegal operation: " + e.getMessage(), outputFilePath);
            } catch (IOException ioException) {
                System.err.println("Error writing error message to output file: " + ioException.getMessage());
            }
            System.err.println("Error during computation: " + e.getMessage());
            System.exit(1);
            return;
        }

        try {
            double[][] resultMatrix = result.getMatrix();
            OutputWriter.write(resultMatrix, outputFilePath);
        } catch (Exception e) {
            System.err.println("Error writing result to output file: " + e.getMessage());
            try {
                OutputWriter.write("Error writing result: " + e.getMessage(), outputFilePath);
            } catch (IOException ioException) {
                System.err.println("Error writing error message to output file: " + ioException.getMessage());
            }
            System.exit(1);
            return;
        }

        System.out.println("Computation completed successfully.");
        System.out.println("\n--- Worker Report ---");
        System.out.println(engine.getWorkerReport());
    }
}