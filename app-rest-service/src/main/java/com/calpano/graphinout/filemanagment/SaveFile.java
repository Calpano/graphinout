package com.calpano.graphinout.filemanagment;

import com.calpano.graphinout.base.input.InputSource;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

@FunctionalInterface
public interface SaveFile<T> {

    String saveFile(@Nonnull IOResource<T> inputSource, @Nullable String inputId) throws IOException;

}
