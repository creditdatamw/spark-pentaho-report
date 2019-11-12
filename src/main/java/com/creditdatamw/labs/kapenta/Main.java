package com.creditdatamw.labs.kapenta;

import com.creditdatamw.labs.kapenta.autogen.PentahoApiGenerator;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static com.creditdatamw.labs.kapenta.Server.kapenta;

/**
 * Run the application to get an auto-generated Server API for Pentaho Reports
 *
 * @author Zikani
 */
public class Main {

    /**
     * Main method - runs the server
     *
     * @param args
     * @throws Exception
     */
    public static void main(String... args) throws Exception {
        if (args.length < 1) {
            showHelpAndExit();
        }

        String arg = args[0];

        if (arg.equalsIgnoreCase("generate")) {
            String dir = args[1];
            String out = args[2];
            generateConfiguration(dir, out);
            System.exit(0);
            return;
        } else if (arg.equalsIgnoreCase("serve")) {
            runServer(args[1]);
            return;
        }

        showHelpAndExit();
    }

    private static void generateConfiguration(String directory, String configFile) {
        PentahoApiGenerator generator = new PentahoApiGenerator(Paths.get(directory), Paths.get(configFile));
        try {
            generator.generate();
        } catch (Exception e) {
            System.err.println("Failed to generate YAML configuration. Got: " + e.getMessage());
        }
    }
    /**
     * Runs the server given a path to a YAML configuration file
     *
     * @param yamlFile
     */
    private static void runServer(String yamlFile) {
        if (Objects.isNull(yamlFile)) {
            showHelpAndExit();
        }

        if (!Files.exists(Paths.get(yamlFile))) {
            showHelpAndExit();
        }

        // Run the spark pentaho report server
        kapenta(yamlFile);
    }
    /**
     * Show help and exit the application
     */
    public static void showHelpAndExit() {
        throw new RuntimeException("Spark Pentaho Reports Server requires path to yaml configuration file to run!");
        // System.exit(-1);
    }
}
