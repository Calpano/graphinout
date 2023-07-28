package com.calpano.graphinout.filemanagment;

import com.calpano.graphinout.base.output.OutputSink;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

@FunctionalInterface
public interface SaveOutputSink {

    String save(@Nonnull IOResource<OutputSink> outputSink, @Nullable String inputId) throws IOException;

}
