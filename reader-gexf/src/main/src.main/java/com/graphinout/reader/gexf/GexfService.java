package com.graphinout.reader.gexf;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;

import java.util.Arrays;
import java.util.List;

public class GexfService implements GioService {

    @Override
    public String id() {
        return "reader-gexf";
    }

    @Override
    public List<GioReader> readers() {
        return Arrays.asList(new GexfReader());
    }

}
