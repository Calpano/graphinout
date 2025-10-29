package com.graphinout.reader.tripletext;

import com.graphinout.base.gio.GioService;
import com.graphinout.base.gio.GioReader;

import java.util.List;

public class TripleTextService implements GioService {

    @Override
    public String id() {
        return "reader-example";
    }

    @Override
    public List<GioReader> readers() {
        return List.of(new TripleTextReader());
    }

}
