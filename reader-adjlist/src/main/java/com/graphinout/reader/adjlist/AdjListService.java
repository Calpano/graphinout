package com.graphinout.reader.adjlist;

import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioReader;

import java.util.Arrays;
import java.util.List;

public class AdjListService implements GioService {

    @Override
    public String id() {
        return "reader-adjlist";
    }

    @Override
    public List<GioReader> readers() {
        return Arrays.asList(new AdjListReader());
    }

}
