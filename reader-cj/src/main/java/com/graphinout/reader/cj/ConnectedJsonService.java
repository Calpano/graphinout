package com.graphinout.reader.cj;

import com.graphinout.base.GioService;
import com.graphinout.base.GioReader;

import java.util.Arrays;
import java.util.List;

public class ConnectedJsonService implements GioService {

    @Override
    public String id() {
        return "reader-cj";
    }

    @Override
    public List<GioReader> readers() {
        return Arrays.asList(new ConnectedJsonReader(), new ConnectedJson5Reader());
    }

}
