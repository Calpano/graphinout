package com.calpano.graphinout.reader.tgf;

import com.calpano.graphinout.base.GioService;
import com.calpano.graphinout.base.gio.GioReader;

import java.util.Arrays;
import java.util.List;

public class TgfService implements GioService {

    @Override
    public String id() {
        return "reader-tgf";
    }

    @Override
    public List<GioReader> readers() {
        return Arrays.asList(new TgfReader());
    }

}
