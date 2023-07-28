package com.calpano.graphinout.filemanagment;

import com.calpano.graphinout.base.input.InputSource;

import javax.annotation.Nonnull;
import java.io.IOException;

@FunctionalInterface
public interface LoadFile {

    IOResource<InputSource> loadFile(@Nonnull String inputId) throws IOException;
}
