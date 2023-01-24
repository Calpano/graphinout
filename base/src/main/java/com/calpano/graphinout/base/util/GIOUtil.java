package com.calpano.graphinout.base.util;

import com.calpano.graphinout.base.GioGraphInOutConstants;
import com.calpano.graphinout.base.exception.GioException;
import com.calpano.graphinout.base.exception.GioExceptionMessage;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class GIOUtil {

    public static String makeElement(String name, LinkedHashMap<String, String> attributes) {
        final StringBuilder format = new StringBuilder("<").append(name);
        attributes.entrySet().stream().forEach(s -> format.append(String.format(" %s=\"%s\"", s.getKey(), s.getValue())));
        format.append("/>").append(GioGraphInOutConstants.NEW_LINE_SEPARATOR);
        return format.toString();
    }
    public static String makeElement(String name){
        return makeElement(name,new LinkedHashMap<>());
    }
    public static String makeStartElement(String name, LinkedHashMap<String, String> attributes) {
        final StringBuilder format = new StringBuilder("<").append(name);
        attributes.entrySet().stream().forEach(s -> format.append(String.format(" %s=\"%s\"", s.getKey(), s.getValue())));
        format.append(">").append(GioGraphInOutConstants.NEW_LINE_SEPARATOR);
        return format.toString();
    }
    public static String makeStartElement(String name){
        return  makeStartElement(name,new LinkedHashMap<>());
    }

    public static String makeEndElement(String name) {
        return "</" + name + ">"+GioGraphInOutConstants.NEW_LINE_SEPARATOR;
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
