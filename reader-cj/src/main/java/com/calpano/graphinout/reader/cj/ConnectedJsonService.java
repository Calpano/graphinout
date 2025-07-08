package com.calpano.graphinout.reader.cj;

import com.calpano.graphinout.base.GioService;
import com.calpano.graphinout.base.cj.CjWriter;
import com.calpano.graphinout.base.gio.GioReader;
import com.calpano.graphinout.foundation.json.JsonReader;

import java.util.Arrays;
import java.util.List;

public class ConnectedJsonService implements GioService {
    @Override
    public String id() {
        return "reader-cj";
    }

    @Override
    public List<GioReader> readers() {
        return Arrays.asList(new ConnectedJsonReader(), new ConnectedJson5Reader() );
    }
}
