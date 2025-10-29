package com.graphinout.reader.dot;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioService;

import java.util.Arrays;
import java.util.List;

public class DotService implements GioService {

    @Override
    public String id() {
        return "reader-dot";
    }

    @Override
    public List<GioReader> readers() {
        return Arrays.asList(new DotReader());
    }

}
