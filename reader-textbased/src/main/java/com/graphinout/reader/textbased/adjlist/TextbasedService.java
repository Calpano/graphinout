package com.graphinout.reader.textbased.adjlist;

import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioReader;

import java.util.Arrays;
import java.util.List;

public class TextbasedService implements GioService {

    @Override
    public String id() {
        return "reader-adjlist";
    }

    @Override
    public List<GioReader> readers() {
        return Arrays.asList(new AdjListReader());
    }

}
