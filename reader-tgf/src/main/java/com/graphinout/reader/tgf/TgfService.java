package com.graphinout.reader.tgf;

import com.graphinout.base.GioService;
import com.graphinout.base.GioReader;

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
