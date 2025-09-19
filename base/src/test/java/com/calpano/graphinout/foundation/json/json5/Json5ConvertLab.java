package com.calpano.graphinout.foundation.json.json5;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Json5ConvertLab {

    public static void main(String[] args) throws URISyntaxException {
        try {
            String resourceName = "example.connected.json5";
            ClassLoader classLoader = Json5ConvertLab.class.getClassLoader();
            URL resourceUrl = classLoader.getResource(resourceName);
            Path inputPath = Paths.get(resourceUrl.toURI());
            String json5Content = Files.readString(inputPath, StandardCharsets.UTF_8);

            // strip JSON5 specifics
            String jsonContent = Json5Preprocessor.toJson(json5Content);

            // dump result
            Path outputPath = Paths.get("./reader-cj/src/test/resources/example.connected.json");
            Files.writeString(outputPath, jsonContent, StandardCharsets.UTF_8);

            System.out.println("Successfully converted JSON5 to JSON:");
            System.out.println("Input: " + inputPath.toAbsolutePath());
            System.out.println("Output: " + outputPath.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("Error during conversion: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
