package com.transpiler.main;

import com.transpiler.utils.FileUtils;
import com.transpiler.utils.Language;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * CLI entry point for the Multi-Language Transpiler.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Options options = new Options();

        options.addRequiredOption("i", "input", true, "Path to the input source file");
        options.addRequiredOption("f", "from", true, "Source language (e.g., python, java)");
        options.addRequiredOption("t", "to", true, "Target language (e.g., java, cpp)");
        options.addOption("o", "output", true, "Path to the output file (optional)");

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);

            String inputPath = cmd.getOptionValue("input");
            String fromLangStr = cmd.getOptionValue("from");
            String toLangStr = cmd.getOptionValue("to");
            String outputPath = cmd.getOptionValue("output");

            Language fromLang = Language.validate(fromLangStr);
            Language toLang = Language.validate(toLangStr);

            logger.info("Reading input file: {}", inputPath);
            String sourceCode = FileUtils.readFile(inputPath);

            TranspilerEngine engine = new TranspilerEngine();
            String transpiledCode = engine.transpile(sourceCode, fromLang, toLang);

            if (outputPath != null) {
                logger.info("Writing output to: {}", outputPath);
                FileUtils.writeFile(outputPath, transpiledCode);
            } else {
                System.out.println("--- Transpiled Code ---");
                System.out.println(transpiledCode);
                System.out.println("-----------------------");
            }

        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments: " + e.getMessage());
            formatter.printHelp("transpiler", options);
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Configuration error: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("File error: " + e.getMessage());
            logger.error("I/O error during transpilation", e);
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            logger.error("An unexpected error occurred", e);
            System.exit(1);
        }
    }
}
