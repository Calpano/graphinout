package com.calpano.graphinout.reader.tgf;

import com.calpano.graphinout.base.AbstractReaderTest;
import com.calpano.graphinout.base.reader.GioReader;

class TgfReaderTest2 extends AbstractReaderTest {

    @Override
    protected boolean canRead(String resourcePath) {
        return resourcePath.endsWith(".tgf");
    }

    @Override
    protected GioReader createReader() {
        return new TgfReader();
    }

}