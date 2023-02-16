package com.calpano.graphinout.base.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class GIOUtil {

    private final static  String NEW_LINE_SEPARATOR =  System.lineSeparator();
    public static String makeElement(String name, Map<String, String> attributes) {
        final StringBuilder format = new StringBuilder("<").append(name);
        attributes.entrySet().stream().forEach(s -> format.append(String.format(" %s=\"%s\"", s.getKey(), s.getValue())));
        format.append("/>").append(NEW_LINE_SEPARATOR);
        return format.toString();
    }
    public static String makeElement(String name){
        return makeElement(name,new LinkedHashMap<>());
    }
    public static String makeStartElement(String name, Map<String, String> attributes) {
        final StringBuilder format = new StringBuilder("<").append(name);
        attributes.entrySet().stream().forEach(s -> format.append(String.format(" %s=\"%s\"", s.getKey(), s.getValue())));
        format.append(">").append(NEW_LINE_SEPARATOR);
        return format.toString();
    }
    public static String makeStartElement(String name){
        return  makeStartElement(name,new LinkedHashMap<>());
    }

    public static String makeEndElement(String name) {
        return "</" + name + ">"+NEW_LINE_SEPARATOR;
    }

}
