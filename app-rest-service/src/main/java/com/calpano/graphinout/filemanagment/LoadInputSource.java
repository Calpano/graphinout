package com.calpano.graphinout.filemanagment;

import com.calpano.graphinout.base.input.InputSource;

import javax.annotation.Nonnull;
import java.io.IOException;

@FunctionalInterface
public interface LoadInputSource {

    IOResource<InputSource> load(@Nonnull String inputId) throws IOException;
}
