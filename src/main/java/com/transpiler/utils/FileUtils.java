package com.transpiler.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for file operations.
 */
public class FileUtils {

    private FileUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Reads the content of a file.
     *
     * @param filePath Path to the file.
     * @return Content of the file as a string.
     * @throws IOException if an I/O error occurs.
     */
    public static String readFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readString(path);
    }

    /**
     * Writes content to a file.
     *
     * @param filePath Path to the file.
     * @param content  Content to write.
     * @throws IOException if an I/O error occurs.
     */
    public static void writeFile(String filePath, String content) throws IOException {
        Path path = Paths.get(filePath);
        Files.writeString(path, content);
    }

    /**
     * Extracts the language from a file extension.
     *
     * @param fileName The name of the file.
     * @return Optional containing the detected Language.
     */
    public static Language detectLanguage(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return switch (extension) {
            case "py" -> Language.PYTHON;
            case "java" -> Language.JAVA;
            case "c" -> Language.C;
            case "cpp", "cc", "h" -> Language.CPP;
            case "js" -> Language.JAVASCRIPT;
            case "ts" -> Language.TYPESCRIPT;
            case "rb" -> Language.RUBY;
            default -> throw new IllegalArgumentException("Unknown file extension: " + extension);
        };
    }

    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }
}
