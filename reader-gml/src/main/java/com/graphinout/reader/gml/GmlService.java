package com.graphinout.reader.gml;

import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioReader;
import com.graphinout.reader.gml.GmlReader;

import java.util.Arrays;
import java.util.List;

public class GmlService implements GioService {

    @Override
    public String id() {
        return "reader-gml";
    }

    @Override
    public List<GioReader> readers() {
        return Arrays.asList(new GmlReader());
    }

}
