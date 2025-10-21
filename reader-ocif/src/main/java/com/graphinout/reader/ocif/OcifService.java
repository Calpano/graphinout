package com.graphinout.reader.ocif;

import com.graphinout.base.GioService;
import com.graphinout.base.gio.GioReader;

import java.util.Arrays;
import java.util.List;

public class OcifService implements GioService {

    @Override
    public String id() {
        return "reader-ocif";
    }

    @Override
    public List<GioReader> readers() {
        return Arrays.asList(new OcifReader());
    }
}
