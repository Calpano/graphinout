package com.graphinout.engine;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioWriter;

import java.util.Set;
import java.util.TreeSet;

public class SelfCheck {

    public static void main(String[] args) {
        GioEngineCore engine = new GioEngineCore();
        Set<String> readable = new TreeSet<>();
        Set<String> writable = new TreeSet<>();

        for (GioReader reader : engine.readers()) {
            readable.add(reader.fileFormat().id());
        }
        for (GioWriter writer : engine.writers()) {
            writable.add(writer.fileFormat().id());
        }

        for (String id : readable) {
            System.out.println("graphinout ..reads.. format:" + id);
        }
        for (String id : writable) {
            System.out.println("graphinout ..writes.. format:" + id);
        }
    }
}
