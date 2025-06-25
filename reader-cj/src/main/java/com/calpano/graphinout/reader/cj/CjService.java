package com.calpano.graphinout.reader.cj;

import com.calpano.graphinout.base.GioService;
import com.calpano.graphinout.base.gio.GioReader;

import java.util.Arrays;
import java.util.List;

public class CjService implements GioService {
    @Override
    public String id() {
        return "reader-cj";
    }

    @Override
    public List<GioReader> readers() {
        return Arrays.asList(new CjReader(), new Json5Reader());
    }
}
