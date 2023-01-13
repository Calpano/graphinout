package com.calpano.graphinout.util;

import com.calpano.graphinout.exception.GioException;
import com.calpano.graphinout.exception.GioExceptionMessage;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.stream.Stream;

public class GIOUtil {


    public static String makeStartElement(String name, Map<String, String> attributes) {
        final StringBuilder format = new StringBuilder("<").append(name);
        attributes.entrySet().stream().forEach(s -> format.append(String.format(" %s=\"%s\" ", s.getKey(), s.getValue())));
        format.append(">");
        return format.toString();
    }

    public static String makeEndElement(String name) {
        return "</" + name + ">";
    }

    public static void copyFile(Path source, Path target) throws GioException {

        try (FileWriter fileWriter = new FileWriter(target.toFile(),true)) {

            Files.lines(source).forEach(s -> {
                try {
                    fileWriter.append(s.toString());
                } catch (IOException e) {
                    throw new RuntimeException(new GioException(GioExceptionMessage.temporary_exemption));
                }
            });

            fileWriter.flush();
        } catch (IOException e) {
            throw new GioException(GioExceptionMessage.temporary_exemption);
        }
    }
}
