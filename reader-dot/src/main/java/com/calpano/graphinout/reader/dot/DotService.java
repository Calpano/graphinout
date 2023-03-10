package com.calpano.graphinout.reader.dot;

import com.calpano.graphinout.base.GioService;
import com.calpano.graphinout.base.reader.GioReader;

import java.util.Arrays;
import java.util.List;

public class DotService implements GioService {
    @Override
    public String id() {
        return "reader-dot";
    }

    @Override
    public List<GioReader> readers() {
        return Arrays.asList(new DotTextReader());
    }
}
